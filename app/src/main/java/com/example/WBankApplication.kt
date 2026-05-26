package com.example

import android.app.Application
import com.example.data.database.WBankDatabase
import com.example.data.repository.WBankRepositoryImpl
import com.example.domain.repository.WBankRepository

class WBankApplication : Application() {
    lateinit var repository: WBankRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = WBankDatabase.getDatabase(this)
        repository = WBankRepositoryImpl(
            userDao = database.userDao(),
            accountDao = database.accountDao(),
            ledgerDao = database.ledgerDao(),
            investmentHoldingDao = database.investmentHoldingDao(),
            loanDao = database.loanDao()
        )
    }
}
