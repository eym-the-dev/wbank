package com.example.ui.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.WBankAccount
import com.example.domain.model.WBankUser
import com.example.data.entity.WBankLoanEntity
import com.example.domain.repository.TransferResult
import com.example.domain.repository.WBankRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.pow

sealed interface ApplicationsUiEvent {
    data class ShowSnackbar(val message: String) : ApplicationsUiEvent
    object LoanApproved : ApplicationsUiEvent
}

data class ApplicationsUiState(
    val user: WBankUser? = null,
    val account: WBankAccount? = null,
    val activeLoans: List<WBankLoanEntity> = emptyList(),
    val isApplying: Boolean = false,
    // Calculator States
    val selectedLoanType: String = "Consumer", // Consumer, Vehicle, Housing
    val calculatedMonthlyPayment: Double = 0.0,
    val calculatedTotalRepayment: Double = 0.0,
    val calculatedTotalInterest: Double = 0.0
)

class ApplicationsViewModel(private val repository: WBankRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplicationsUiState())
    val uiState: StateFlow<ApplicationsUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ApplicationsUiEvent>()
    val uiEvent: SharedFlow<ApplicationsUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Load and observe user details
            repository.getCurrentUserFlow().collect { user ->
                if (user != null) {
                    _uiState.value = _uiState.value.copy(user = user)
                    
                    // Observe active accounts for balance display
                    launch {
                        repository.getAccountByCustomerNumberFlow(user.customerNumber).collect { account ->
                            _uiState.value = _uiState.value.copy(account = account)
                        }
                    }

                    // Observe applied loans in DB
                    launch {
                        repository.getAppliedLoansFlow(user.customerNumber).collect { loans ->
                            _uiState.value = _uiState.value.copy(activeLoans = loans)
                        }
                    }
                }
            }
        }
    }

    // Amortization math
    fun calculateLoan(principal: Double, maturityMonths: Int, monthlyRatePercent: Double) {
        val r = monthlyRatePercent / 100.0
        val n = maturityMonths.toDouble()
        
        if (r == 0.0) {
            val monthly = principal / n
            _uiState.value = _uiState.value.copy(
                calculatedMonthlyPayment = monthly,
                calculatedTotalRepayment = principal,
                calculatedTotalInterest = 0.0
            )
            return
        }

        val numerator = r * (1.0 + r).pow(n)
        val denominator = (1.0 + r).pow(n) - 1.0
        val monthly = principal * (numerator / denominator)
        
        val totalRepayment = monthly * n
        val totalInterest = totalRepayment - principal

        _uiState.value = _uiState.value.copy(
            calculatedMonthlyPayment = monthly,
            calculatedTotalRepayment = totalRepayment,
            calculatedTotalInterest = totalInterest
        )
    }

    fun applyForLoan(principal: Double, interestRate: Double, maturityMonths: Int, monthlyPayment: Double) {
        val user = _uiState.value.user ?: return
        
        _uiState.value = _uiState.value.copy(isApplying = true)
        viewModelScope.launch {
            val result = repository.applyLoan(
                customerNumber = user.customerNumber,
                type = _uiState.value.selectedLoanType,
                amount = principal,
                rate = interestRate,
                months = maturityMonths,
                monthlyPay = monthlyPayment
            )
            _uiState.value = _uiState.value.copy(isApplying = false)
            
            when (result) {
                is TransferResult.Success -> {
                    _uiEvent.emit(ApplicationsUiEvent.LoanApproved)
                    _uiEvent.emit(ApplicationsUiEvent.ShowSnackbar("${_uiState.value.selectedLoanType} Loan approved and funds credited instantly!"))
                }
                is TransferResult.Error -> {
                    _uiEvent.emit(ApplicationsUiEvent.ShowSnackbar(result.message))
                }
            }
        }
    }

    fun selectLoanType(type: String) {
        _uiState.value = _uiState.value.copy(selectedLoanType = type)
    }
}
