package com.example.ui.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.WBankAccount
import com.example.domain.model.WBankLedgerEntry
import com.example.domain.repository.WBankRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LedgerUiState(
    val account: WBankAccount? = null,
    val ledgerEntries: List<WBankLedgerEntry> = emptyList(),
    val filteredEntries: List<WBankLedgerEntry> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class LedgerViewModel(private val repository: WBankRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LedgerUiState(isLoading = true))
    val uiState: StateFlow<LedgerUiState> = _uiState.asStateFlow()

    private var ledgerJobs = mutableListOf<Job>()
    private var ledgerEntriesJob: Job? = null

    init {
        loadLedgerData()
    }

    private fun loadLedgerData() {
        viewModelScope.launch {
            repository.getCurrentUserFlow().collect { user ->
                // Cancel previous sub-collections when user changes
                ledgerJobs.forEach { it.cancel() }
                ledgerJobs.clear()
                ledgerEntriesJob?.cancel()
                ledgerEntriesJob = null

                if (user != null) {
                    val accountJob = launch {
                        repository.getAccountByCustomerNumberFlow(user.customerNumber).collect { account ->
                            if (account != null) {
                                _uiState.value = _uiState.value.copy(account = account)
                                
                                // Cancel previous entries flow subscription to avoid nested collection races
                                ledgerEntriesJob?.cancel()
                                ledgerEntriesJob = launch {
                                    repository.getLedgerEntriesFlow(account.iban).collect { entries ->
                                        _uiState.value = _uiState.value.copy(
                                            ledgerEntries = entries,
                                            isLoading = false,
                                            filteredEntries = filterEntries(entries, _uiState.value.searchQuery)
                                        )
                                    }
                                }
                            } else {
                                _uiState.value = _uiState.value.copy(isLoading = false)
                            }
                        }
                    }
                    ledgerJobs.add(accountJob)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        val filtered = filterEntries(_uiState.value.ledgerEntries, query)
        _uiState.value = _uiState.value.copy(searchQuery = query, filteredEntries = filtered)
    }

    private fun filterEntries(
        entries: List<WBankLedgerEntry>,
        query: String
    ): List<WBankLedgerEntry> {
        if (query.isBlank()) return entries
        return entries.filter {
            it.receiverName.contains(query, ignoreCase = true) ||
                    (it.description?.contains(query, ignoreCase = true) == true) ||
                    it.senderIban.contains(query, ignoreCase = true) ||
                    it.receiverIban.contains(query, ignoreCase = true)
        }
    }
}
