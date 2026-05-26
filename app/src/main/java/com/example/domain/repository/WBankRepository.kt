package com.example.domain.repository

import com.example.domain.model.WBankAccount
import com.example.domain.model.WBankLedgerEntry
import com.example.domain.model.WBankUser
import com.example.data.entity.WBankInvestmentHoldingEntity
import com.example.data.entity.WBankLoanEntity
import kotlinx.coroutines.flow.Flow

sealed interface TransferResult {
    object Success : TransferResult
    data class Error(val message: String) : TransferResult
}

sealed interface AuthResult {
    data class Success(val user: WBankUser) : AuthResult
    data class Error(val message: String) : AuthResult
}

interface WBankRepository {
    fun getCurrentUserFlow(): Flow<WBankUser?>
    fun getAccountByCustomerNumberFlow(customerNumber: String): Flow<WBankAccount?>
    fun getLedgerEntriesFlow(iban: String): Flow<List<WBankLedgerEntry>>
    
    // Investment Portfolio operations
    fun getAllHoldingsFlow(): Flow<List<WBankInvestmentHoldingEntity>>
    suspend fun buyAsset(assetId: String, assetName: String, assetType: String, amount: Double, pricePerUnit: Double): TransferResult
    suspend fun sellAsset(assetId: String, amountToSell: Double, pricePerUnit: Double): TransferResult
    suspend fun seedMockHoldings()

    // Loan / Applications operations
    fun getAppliedLoansFlow(customerNumber: String): Flow<List<WBankLoanEntity>>
    suspend fun applyLoan(customerNumber: String, type: String, amount: Double, rate: Double, months: Int, monthlyPay: Double): TransferResult

    // ATM Quick Operations & Quick Actions
    suspend fun executeAtmDeposit(iban: String, amount: Double, description: String = "ATM QR Cash Deposit"): TransferResult
    suspend fun executeAtmWithdrawal(iban: String, amount: Double, description: String = "ATM QR Cash Withdrawal"): TransferResult

    suspend fun login(email: String, passwordHash: String): AuthResult
    
    suspend fun signup(
        firstName: String,
        lastName: String,
        nationalId: String,
        email: String,
        passwordHash: String
    ): AuthResult
    
    suspend fun executeTransfer(
        senderIban: String,
        receiverIban: String,
        receiverName: String,
        amount: Double,
        description: String?
    ): TransferResult

    suspend fun getAccountByIbanDirect(iban: String): WBankAccount?
    suspend fun loadCurrentUser(): WBankUser?
    suspend fun logout()
    
    // Quick helper to seed demo data
    suspend fun seedMockReceiverAccounts()
}
