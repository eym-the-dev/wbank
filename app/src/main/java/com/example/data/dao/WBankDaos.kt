package com.example.data.dao

import androidx.room.*
import com.example.data.entity.WBankAccountEntity
import com.example.data.entity.WBankLedgerEntryEntity
import com.example.data.entity.WBankUserEntity
import com.example.data.entity.WBankInvestmentHoldingEntity
import com.example.data.entity.WBankLoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WBankUserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: WBankUserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): WBankUserEntity?

    @Query("SELECT * FROM users WHERE customerNumber = :customerNumber LIMIT 1")
    suspend fun getUserByCustomerNumber(customerNumber: String): WBankUserEntity?

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): WBankUserEntity?

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun clearAllUserSessions()

    @Update
    suspend fun updateUser(user: WBankUserEntity)
}

@Dao
interface WBankAccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: WBankAccountEntity)

    @Query("SELECT * FROM accounts WHERE iban = :iban LIMIT 1")
    suspend fun getAccountByIban(iban: String): WBankAccountEntity?

    @Query("SELECT * FROM accounts WHERE customerNumber = :customerNumber LIMIT 1")
    fun getAccountByCustomerNumberFlow(customerNumber: String): Flow<WBankAccountEntity?>

    @Query("SELECT * FROM accounts WHERE customerNumber = :customerNumber LIMIT 1")
    suspend fun getAccountByCustomerNumber(customerNumber: String): WBankAccountEntity?

    @Update
    suspend fun updateAccount(account: WBankAccountEntity)
}

@Dao
interface WBankLedgerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntry(entry: WBankLedgerEntryEntity)

    @Query("SELECT * FROM ledger_entries WHERE senderIban = :iban OR receiverIban = :iban ORDER BY timestamp DESC")
    fun getLedgerEntriesFlow(iban: String): Flow<List<WBankLedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries ORDER BY timestamp DESC")
    fun getAllLedgerEntriesFlow(): Flow<List<WBankLedgerEntryEntity>>
}

@Dao
interface WBankInvestmentHoldingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: WBankInvestmentHoldingEntity)

    @Query("SELECT * FROM investment_holdings")
    fun getAllHoldingsFlow(): Flow<List<WBankInvestmentHoldingEntity>>

    @Query("SELECT * FROM investment_holdings WHERE assetId = :assetId LIMIT 1")
    suspend fun getHoldingByAssetId(assetId: String): WBankInvestmentHoldingEntity?

    @Delete
    suspend fun deleteHolding(holding: WBankInvestmentHoldingEntity)

    @Query("DELETE FROM investment_holdings")
    suspend fun clearAllHoldings()
}

@Dao
interface WBankLoanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: WBankLoanEntity)

    @Query("SELECT * FROM applied_loans WHERE customerNumber = :customerNumber ORDER BY timestamp DESC")
    fun getLoansByCustomerNumberFlow(customerNumber: String): Flow<List<WBankLoanEntity>>

    @Query("DELETE FROM applied_loans")
    suspend fun clearAllLoans()
}

