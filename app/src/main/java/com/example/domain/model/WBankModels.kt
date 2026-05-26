package com.example.domain.model

sealed interface TransactionType {
    object Deposit : TransactionType
    object Withdrawal : TransactionType
    object TransferOut : TransactionType
    object TransferIn : TransactionType
}

data class WBankUser(
    val customerNumber: String, // Unique 8-digit identifier
    val firstName: String,
    val lastName: String,
    val nationalId: String,     // Masked/Validated
    val email: String,
    val passwordHash: String,   // Simulated secure hash
    val mainAccountIban: String // Generated format: WBNK-TRXX-XXXX-XXXX-XXXX-XXXX-XX
)

data class WBankAccount(
    val iban: String,
    val customerNumber: String,
    var balance: Double = 10000.0, // Initial onboarding credit
    val currency: String = "USD"
)

data class WBankLedgerEntry(
    val id: String,
    val senderIban: String,
    val receiverIban: String,
    val receiverName: String,
    val amount: Double,
    val type: TransactionType,
    val timestamp: Long,
    val description: String?
)
