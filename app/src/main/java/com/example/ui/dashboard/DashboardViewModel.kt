package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.WBankAccount
import com.example.domain.model.WBankLedgerEntry
import com.example.domain.model.WBankUser
import com.example.domain.repository.WBankRepository
import com.example.domain.repository.TransferResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

sealed interface DashboardUiEvent {
    object NavigateToAuth : DashboardUiEvent
    data class ShowSnackbar(val message: String) : DashboardUiEvent
}

data class DashboardUiState(
    val user: WBankUser? = null,
    val account: WBankAccount? = null,
    val recentTransactions: List<WBankLedgerEntry> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(private val repository: WBankRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DashboardUiEvent>()
    val uiEvent: SharedFlow<DashboardUiEvent> = _uiEvent.asSharedFlow()

    private var dashboardJobs = mutableListOf<Job>()
    private var ledgerJob: Job? = null

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Listen reactor loop
            repository.getCurrentUserFlow().collect { user ->
                // Cancel existing sub-jobs when user context shifts
                dashboardJobs.forEach { it.cancel() }
                dashboardJobs.clear()
                ledgerJob?.cancel()
                ledgerJob = null

                if (user == null) {
                    _uiState.value = DashboardUiState(isLoading = false)
                    return@collect
                }
                
                // Track user details
                _uiState.value = _uiState.value.copy(user = user, isLoading = false)

                // Cleanly observe account changes
                val accountJob = launch {
                    repository.getAccountByCustomerNumberFlow(user.customerNumber).collect { account ->
                        if (account == null) {
                            _uiState.value = _uiState.value.copy(account = null, recentTransactions = emptyList())
                            return@collect
                        }

                        _uiState.value = _uiState.value.copy(account = account)

                        // Cleanly cancel previous recentTransactions subscription when account emits
                        ledgerJob?.cancel()
                        ledgerJob = launch {
                            repository.getLedgerEntriesFlow(account.iban).collect { entries ->
                                // Display last 3 transaction activities
                                val recent = entries.take(3)
                                _uiState.value = _uiState.value.copy(recentTransactions = recent)
                            }
                        }
                    }
                }
                dashboardJobs.add(accountJob)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiEvent.emit(DashboardUiEvent.NavigateToAuth)
        }
    }
    
    fun simulateCardRefresh() {
        viewModelScope.launch {
            _uiEvent.emit(DashboardUiEvent.ShowSnackbar("Securely refreshed your card details"))
        }
    }

    fun executeAtmDeposit(iban: String, amount: Double, message: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.executeAtmDeposit(iban, amount, message)
            _uiState.value = _uiState.value.copy(isLoading = false)
            when (result) {
                is TransferResult.Success -> {
                    _uiEvent.emit(DashboardUiEvent.ShowSnackbar("Instant ATM Deposit of \$" + String.format("%.2f", amount) + " completed!"))
                }
                is TransferResult.Error -> {
                    _uiEvent.emit(DashboardUiEvent.ShowSnackbar(result.message))
                }
            }
        }
    }

    fun executeAtmWithdrawal(iban: String, amount: Double, message: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.executeAtmWithdrawal(iban, amount, message)
            _uiState.value = _uiState.value.copy(isLoading = false)
            when (result) {
                is TransferResult.Success -> {
                    _uiEvent.emit(DashboardUiEvent.ShowSnackbar("Successful Withdrawal of \$" + String.format("%.2f", amount) + "!"))
                }
                is TransferResult.Error -> {
                    _uiEvent.emit(DashboardUiEvent.ShowSnackbar(result.message))
                }
            }
        }
    }

    fun executeBillPayment(iban: String, utilityName: String, subscriberId: String, amount: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val desc = "Bill Payment: $utilityName (Subscriber: $subscriberId)"
            val result = repository.executeAtmWithdrawal(iban, amount, desc)
            _uiState.value = _uiState.value.copy(isLoading = false)
            when (result) {
                is TransferResult.Success -> {
                    _uiEvent.emit(DashboardUiEvent.ShowSnackbar("Paid $utilityName bill of \$" + String.format("%.2f", amount) + " successfully!"))
                }
                is TransferResult.Error -> {
                    _uiEvent.emit(DashboardUiEvent.ShowSnackbar(result.message))
                }
            }
        }
    }

    fun executeGsmTopUp(iban: String, provider: String, phoneNumber: String, amount: Double, packageName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val desc = "GSM Top-up: $provider ($phoneNumber) - $packageName"
            val result = repository.executeAtmWithdrawal(iban, amount, desc)
            _uiState.value = _uiState.value.copy(isLoading = false)
            when (result) {
                is TransferResult.Success -> {
                    _uiEvent.emit(DashboardUiEvent.ShowSnackbar("Top-up of \$" + String.format("%.2f", amount) + " ($packageName) sent to $phoneNumber!"))
                }
                is TransferResult.Error -> {
                    _uiEvent.emit(DashboardUiEvent.ShowSnackbar(result.message))
                }
            }
        }
    }
}
