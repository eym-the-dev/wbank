package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.dao.WBankAccountDao
import com.example.data.dao.WBankLedgerDao
import com.example.data.dao.WBankUserDao
import com.example.data.dao.WBankInvestmentHoldingDao
import com.example.data.dao.WBankLoanDao
import com.example.data.entity.WBankAccountEntity
import com.example.data.entity.WBankLedgerEntryEntity
import com.example.data.entity.WBankUserEntity
import com.example.data.entity.WBankInvestmentHoldingEntity
import com.example.data.entity.WBankLoanEntity
import com.example.data.entity.WBankTypeConverters

@Database(
    entities = [
        WBankUserEntity::class,
        WBankAccountEntity::class,
        WBankLedgerEntryEntity::class,
        WBankInvestmentHoldingEntity::class,
        WBankLoanEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(WBankTypeConverters::class)
abstract class WBankDatabase : RoomDatabase() {
    abstract fun userDao(): WBankUserDao
    abstract fun accountDao(): WBankAccountDao
    abstract fun ledgerDao(): WBankLedgerDao
    abstract fun investmentHoldingDao(): WBankInvestmentHoldingDao
    abstract fun loanDao(): WBankLoanDao

    companion object {
        @Volatile
        private var INSTANCE: WBankDatabase? = null

        fun getDatabase(context: Context): WBankDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WBankDatabase::class.java,
                    "wbank_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
