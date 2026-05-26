package com.example.ui.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.WBankAccount
import com.example.domain.repository.TransferResult
import com.example.domain.repository.WBankRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface TransferUiEvent {
    data class ShowSnackbar(val message: String) : TransferUiEvent
    data class NavigateToSuccess(
        val senderIban: String,
        val receiverIban: String,
        val receiverName: String,
        val amount: Double,
        val description: String,
        val timestamp: Long,
        val transactionId: String
    ) : TransferUiEvent
}

data class TransferUiState(
    val senderAccount: WBankAccount? = null,
    
    // Inputs
    val receiverIban: String = "",
    val receiverIbanError: String? = null,
    
    val receiverName: String = "",
    val receiverNameError: String? = null,
    
    val amount: String = "",
    val amountError: String? = null,
    
    val description: String = "",
    
    val isLoading: Boolean = false
)

class TransferViewModel(private val repository: WBankRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<TransferUiEvent>()
    val uiEvent: SharedFlow<TransferUiEvent> = _uiEvent.asSharedFlow()

    private val wbankPattern = Regex("^WBNK-TR\\d{2}-\\d{4}-\\d{4}-\\d{4}-\\d{4}-\\d{4}-\\d{2}$")

    init {
        loadSenderAccount()
    }

    private fun loadSenderAccount() {
        viewModelScope.launch {
            repository.getCurrentUserFlow().collect { user ->
                if (user != null) {
                    repository.getAccountByCustomerNumberFlow(user.customerNumber).collect { account ->
                        _uiState.value = _uiState.value.copy(senderAccount = account)
                    }
                }
            }
        }
    }

    fun onReceiverIbanChanged(value: String) {
        val error = when {
            value.isBlank() -> "IBAN is required."
            !value.matches(wbankPattern) -> "Must match: WBNK-TRXX-XXXX-XXXX-XXXX-XXXX-XX"
            _uiState.value.senderAccount?.iban == value -> "Cannot transfer to your own IBAN."
            else -> null
        }
        _uiState.value = _uiState.value.copy(receiverIban = value, receiverIbanError = error)
    }

    fun onReceiverNameChanged(value: String) {
        val error = if (value.isBlank()) "Receiver Name is required." else null
        _uiState.value = _uiState.value.copy(receiverName = value, receiverNameError = error)
    }

    fun onAmountChanged(value: String) {
        val parsed = value.toDoubleOrNull()
        val error = when {
            value.isBlank() -> "Amount is required."
            parsed == null || parsed <= 0 -> "Amount must be strictly greater than 0."
            else -> null
        }
        _uiState.value = _uiState.value.copy(amount = value, amountError = error)
    }

    fun onDescriptionChanged(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun executeTransfer() {
        val state = _uiState.value
        val senderAccount = state.senderAccount

        if (senderAccount == null) {
            viewModelScope.launch {
                _uiEvent.emit(TransferUiEvent.ShowSnackbar("Source bank account is unavailable."))
            }
            return
        }

        // Run validation triggers
        onReceiverIbanChanged(state.receiverIban)
        onReceiverNameChanged(state.receiverName)
        onAmountChanged(state.amount)

        val updatedState = _uiState.value
        if (updatedState.receiverIbanError != null || 
            updatedState.receiverNameError != null || 
            updatedState.amountError != null) {
            viewModelScope.launch {
                _uiEvent.emit(TransferUiEvent.ShowSnackbar("Check your inputs and try again."))
            }
            return
        }

        val amountDouble = state.amount.toDoubleOrNull() ?: 0.0

        // Business Rule Verification: If requestedAmount > availableBalance, EMIT UiEvent.ShowSnackbar with target warning
        if (amountDouble > senderAccount.balance) {
            viewModelScope.launch {
                _uiEvent.emit(TransferUiEvent.ShowSnackbar("Transactional Refused: Insufficient Funds in W Bank account."))
            }
            return
        }

        // Proceed to Atomic Transfer
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = repository.executeTransfer(
                senderIban = senderAccount.iban,
                receiverIban = state.receiverIban,
                receiverName = state.receiverName,
                amount = amountDouble,
                description = state.description.ifBlank { "P2P Money Transfer" }
            )
            _uiState.value = _uiState.value.copy(isLoading = false)

            when (result) {
                is TransferResult.Success -> {
                    val transactionId = UUID.randomUUID().toString()
                    val timestamp = System.currentTimeMillis()
                    _uiEvent.emit(
                        TransferUiEvent.NavigateToSuccess(
                            senderIban = senderAccount.iban,
                            receiverIban = state.receiverIban,
                            receiverName = state.receiverName,
                            amount = amountDouble,
                            description = state.description.ifBlank { "P2P Money Transfer" },
                            timestamp = timestamp,
                            transactionId = transactionId
                        )
                    )
                }
                is TransferResult.Error -> {
                    _uiEvent.emit(TransferUiEvent.ShowSnackbar(result.message))
                }
            }
        }
    }
}
