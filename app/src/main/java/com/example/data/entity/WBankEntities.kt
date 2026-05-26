package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.domain.model.TransactionType
import com.example.domain.model.WBankAccount
import com.example.domain.model.WBankLedgerEntry
import com.example.domain.model.WBankUser

@Entity(tableName = "users")
data class WBankUserEntity(
    @PrimaryKey val customerNumber: String,
    val firstName: String,
    val lastName: String,
    val nationalId: String,
    val email: String,
    val passwordHash: String,
    val mainAccountIban: String,
    val isLoggedIn: Boolean = false
) {
    fun toDomain(): WBankUser {
        return WBankUser(
            customerNumber = customerNumber,
            firstName = firstName,
            lastName = lastName,
            nationalId = nationalId,
            email = email,
            passwordHash = passwordHash,
            mainAccountIban = mainAccountIban
        )
    }

    companion object {
        fun fromDomain(user: WBankUser, isLoggedIn: Boolean = false): WBankUserEntity {
            return WBankUserEntity(
                customerNumber = user.customerNumber,
                firstName = user.firstName,
                lastName = user.lastName,
                nationalId = user.nationalId,
                email = user.email,
                passwordHash = user.passwordHash,
                mainAccountIban = user.mainAccountIban,
                isLoggedIn = isLoggedIn
            )
        }
    }
}

@Entity(tableName = "accounts")
data class WBankAccountEntity(
    @PrimaryKey val iban: String,
    val customerNumber: String,
    val balance: Double,
    val currency: String
) {
    fun toDomain(): WBankAccount {
        return WBankAccount(
            iban = iban,
            customerNumber = customerNumber,
            balance = balance,
            currency = currency
        )
    }

    companion object {
        fun fromDomain(account: WBankAccount): WBankAccountEntity {
            return WBankAccountEntity(
                iban = account.iban,
                customerNumber = account.customerNumber,
                balance = account.balance,
                currency = account.currency
            )
        }
    }
}

@Entity(tableName = "ledger_entries")
data class WBankLedgerEntryEntity(
    @PrimaryKey val id: String,
    val senderIban: String,
    val receiverIban: String,
    val receiverName: String,
    val amount: Double,
    val type: String,
    val timestamp: Long,
    val description: String?
) {
    fun toDomain(): WBankLedgerEntry {
        val domainType = when (type) {
            "DEPOSIT" -> TransactionType.Deposit
            "WITHDRAWAL" -> TransactionType.Withdrawal
            "TRANSFER_OUT" -> TransactionType.TransferOut
            "TRANSFER_IN" -> TransactionType.TransferIn
            else -> TransactionType.Deposit
        }
        return WBankLedgerEntry(
            id = id,
            senderIban = senderIban,
            receiverIban = receiverIban,
            receiverName = receiverName,
            amount = amount,
            type = domainType,
            timestamp = timestamp,
            description = description
        )
    }

    companion object {
        fun fromDomain(entry: WBankLedgerEntry): WBankLedgerEntryEntity {
            val typeStr = when (entry.type) {
                is TransactionType.Deposit -> "DEPOSIT"
                is TransactionType.Withdrawal -> "WITHDRAWAL"
                is TransactionType.TransferOut -> "TRANSFER_OUT"
                is TransactionType.TransferIn -> "TRANSFER_IN"
            }
            return WBankLedgerEntryEntity(
                id = entry.id,
                senderIban = entry.senderIban,
                receiverIban = entry.receiverIban,
                receiverName = entry.receiverName,
                amount = entry.amount,
                type = typeStr,
                timestamp = entry.timestamp,
                description = entry.description
            )
        }
    }
}

class WBankTypeConverters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return when (type) {
            is TransactionType.Deposit -> "DEPOSIT"
            is TransactionType.Withdrawal -> "WITHDRAWAL"
            is TransactionType.TransferOut -> "TRANSFER_OUT"
            is TransactionType.TransferIn -> "TRANSFER_IN"
        }
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return when (value) {
            "DEPOSIT" -> TransactionType.Deposit
            "WITHDRAWAL" -> TransactionType.Withdrawal
            "TRANSFER_OUT" -> TransactionType.TransferOut
            "TRANSFER_IN" -> TransactionType.TransferIn
            else -> TransactionType.Deposit
        }
    }
}

@Entity(tableName = "investment_holdings")
data class WBankInvestmentHoldingEntity(
    @PrimaryKey val assetId: String,
    val assetName: String,
    val assetType: String,
    val amount: Double,
    val averageCost: Double,
    val currentPrice: Double,
    val dailyChangePercent: Double
)

@Entity(tableName = "applied_loans")
data class WBankLoanEntity(
    @PrimaryKey val id: String,
    val customerNumber: String,
    val type: String,
    val principalAmount: Double,
    val interestRate: Double,
    val maturityMonths: Int,
    val monthlyPayment: Double,
    val status: String,
    val timestamp: Long
)

