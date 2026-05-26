package com.example.data.repository

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
import com.example.domain.model.TransactionType
import com.example.domain.model.WBankAccount
import com.example.domain.model.WBankLedgerEntry
import com.example.domain.model.WBankUser
import com.example.domain.repository.AuthResult
import com.example.domain.repository.TransferResult
import com.example.domain.repository.WBankRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class WBankRepositoryImpl(
    private val userDao: WBankUserDao,
    private val accountDao: WBankAccountDao,
    private val ledgerDao: WBankLedgerDao,
    private val investmentHoldingDao: WBankInvestmentHoldingDao,
    private val loanDao: WBankLoanDao
) : WBankRepository {

    private val currentUser = MutableStateFlow<WBankUser?>(null)
    private val transactionMutex = Mutex()

    init {
        // We will load the current user session asynchronously on start using loadCurrentUser()
    }

    override fun getCurrentUserFlow(): Flow<WBankUser?> = currentUser

    override fun getAccountByCustomerNumberFlow(customerNumber: String): Flow<WBankAccount?> {
        return accountDao.getAccountByCustomerNumberFlow(customerNumber).map { it?.toDomain() }
    }

    override fun getLedgerEntriesFlow(iban: String): Flow<List<WBankLedgerEntry>> {
        return ledgerDao.getLedgerEntriesFlow(iban).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun loadCurrentUser(): WBankUser? {
        val loggedInUserEntity = userDao.getLoggedInUser()
        val user = loggedInUserEntity?.toDomain()
        currentUser.value = user
        return user
    }

    override suspend fun login(email: String, passwordHash: String): AuthResult {
        delay(1200) // Simulate network/api latency
        val userEntity = userDao.getUserByEmail(email)
        return if (userEntity != null && userEntity.passwordHash == passwordHash) {
            // Clear other active sessions to avoid dual logged in state
            userDao.clearAllUserSessions()
            // Mark this user as logged in
            val loggedInUser = userEntity.copy(isLoggedIn = true)
            userDao.updateUser(loggedInUser)
            val domainUser = loggedInUser.toDomain()
            currentUser.value = domainUser
            AuthResult.Success(domainUser)
        } else {
            AuthResult.Error("Transactional Refused: Invalid email or password.")
        }
    }

    override suspend fun signup(
        firstName: String,
        lastName: String,
        nationalId: String,
        email: String,
        passwordHash: String
    ): AuthResult {
        delay(1200) // Simulate network/api latency
        
        // Check if user already exists
        val existingUser = userDao.getUserByEmail(email)
        if (existingUser != null) {
            return AuthResult.Error("Transactional Refused: A user with this email already exists.")
        }

        // Generate pseudo-random unique 8-digit customerNumber
        var customerNumber = ""
        var isUnique = false
        while (!isUnique) {
            val randomNum = (10000000..99999999).random().toString()
            if (userDao.getUserByCustomerNumber(randomNum) == null) {
                customerNumber = randomNum
                isUnique = true
            }
        }

        // Generate structured mainAccountIban
        val r2 = (10..99).random()
        val r4_1 = (1000..9999).random()
        val r4_2 = (1000..9999).random()
        val r4_3 = (1000..9999).random()
        val r4_4 = (1000..9999).random()
        val r4_5 = (1000..9999).random()
        val r2_2 = (10..99).random()
        val mainAccountIban = "WBNK-TR$r2-$r4_1-$r4_2-$r4_3-$r4_4-$r4_5-$r2_2"

        val newUser = WBankUser(
            customerNumber = customerNumber,
            firstName = firstName,
            lastName = lastName,
            nationalId = nationalId,
            email = email,
            passwordHash = passwordHash,
            mainAccountIban = mainAccountIban
        )

        // Clear existing sessions
        userDao.clearAllUserSessions()

        // Insert into DB
        val userEntity = WBankUserEntity.fromDomain(newUser, isLoggedIn = true)
        userDao.insertUser(userEntity)

        // Create the associated account with a default balance of $10,000.00
        val defaultAccount = WBankAccount(
            iban = mainAccountIban,
            customerNumber = customerNumber,
            balance = 10000.00,
            currency = "USD"
        )
        accountDao.insertAccount(WBankAccountEntity.fromDomain(defaultAccount))

        // Create an initial deposit ledger entry
        val initialDeposit = WBankLedgerEntry(
            id = UUID.randomUUID().toString(),
            senderIban = "SYSTEM",
            receiverIban = mainAccountIban,
            receiverName = "$firstName $lastName",
            amount = Int.MAX_VALUE.toDouble(), // not used, we set exact onboarding balance
            type = TransactionType.Deposit,
            timestamp = System.currentTimeMillis(),
            description = "Welcome to W Bank! Onboarding Credit."
        )
        // Record welcome ledger entry
        val welcomeLedger = WBankLedgerEntryEntity(
            id = initialDeposit.id,
            senderIban = "WBNK-SYSTEM-ONBOARD",
            receiverIban = mainAccountIban,
            receiverName = "$firstName $lastName",
            amount = 10000.00,
            type = "DEPOSIT",
            timestamp = System.currentTimeMillis() - 10000, // slightly backdated
            description = "Welcome onboarding promotional credit"
        )
        ledgerDao.insertLedgerEntry(welcomeLedger)

        currentUser.value = newUser
        
        // Pre-seed some mock receiver accounts so transfers are fun inside our DB
        seedMockReceiverAccounts()

        return AuthResult.Success(newUser)
    }

    override suspend fun executeTransfer(
        senderIban: String,
        receiverIban: String,
        receiverName: String,
        amount: Double,
        description: String?
    ): TransferResult {
        // Enforce mutual exclusion (Mutex protected) to prevent race conditions during rapid simulated transfers
        return transactionMutex.withLock {
            delay(1200) // Network latency simulation

            // 1. Check Amount greater than 0
            if (amount <= 0) {
                return TransferResult.Error("Transactional Refused: Amount must be strictly greater than 0.")
            }

            // 2. Validate Sender IBAN form and pattern
            val wbankPattern = Regex("^WBNK-TR\\d{2}-\\d{4}-\\d{4}-\\d{4}-\\d{4}-\\d{4}-\\d{2}$")
            if (!receiverIban.matches(wbankPattern)) {
                return TransferResult.Error("Transactional Refused: Invalid format for W Bank destination IBAN.")
            }

            if (senderIban == receiverIban) {
                return TransferResult.Error("Transactional Refused: Cannot transfer funds to the same account.")
            }

            // 3. Retrieve Sender Account
            val senderAccountEntity = accountDao.getAccountByIban(senderIban)
                ?: return TransferResult.Error("Transactional Refused: Source account not found.")

            // 4. Verify Sender balance
            if (amount > senderAccountEntity.balance) {
                return TransferResult.Error("Transactional Refused: Insufficient Funds in W Bank account.")
            }

            // 5. Atomic balance update and ledger logging
            val updatedSenderBalance = senderAccountEntity.balance - amount
            val updatedSenderAccount = senderAccountEntity.copy(balance = updatedSenderBalance)
            accountDao.updateAccount(updatedSenderAccount)

            val ledgerId = UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()

            // Record TransferOut for Sender
            val senderLedgerEntry = WBankLedgerEntryEntity(
                id = ledgerId,
                senderIban = senderIban,
                receiverIban = receiverIban,
                receiverName = receiverName,
                amount = amount,
                type = "TRANSFER_OUT",
                timestamp = timestamp,
                description = description ?: "P2P Money Transfer"
            )
            ledgerDao.insertLedgerEntry(senderLedgerEntry)

            // 6. See if Receiver is inside our database. If yes, atomically update receiver balance & insert TransferIn ledger
            val receiverAccountEntity = accountDao.getAccountByIban(receiverIban)
            if (receiverAccountEntity != null) {
                val updatedReceiverBalance = receiverAccountEntity.balance + amount
                val updatedReceiverAccount = receiverAccountEntity.copy(balance = updatedReceiverBalance)
                accountDao.updateAccount(updatedReceiverAccount)

                // Record TransferIn for Receiver (which matches the same ledger id or distinct one)
                val receiverLedgerEntry = WBankLedgerEntryEntity(
                    id = UUID.randomUUID().toString(),
                    senderIban = senderIban,
                    receiverIban = receiverIban,
                    receiverName = receiverName,
                    amount = amount,
                    type = "TRANSFER_IN",
                    timestamp = timestamp,
                    description = description ?: "P2P Fund Deposit"
                )
                ledgerDao.insertLedgerEntry(receiverLedgerEntry)
            }

            TransferResult.Success
        }
    }

    override suspend fun getAccountByIbanDirect(iban: String): WBankAccount? {
        return accountDao.getAccountByIban(iban)?.toDomain()
    }

    override suspend fun logout() {
        delay(1200) // Latency simulation
        userDao.clearAllUserSessions()
        currentUser.value = null
    }

    override suspend fun seedMockReceiverAccounts() {
        // Check and seed 3 preloaded escrow/platinum service accounts
        // Useful for P2P transfers
        val mockAccounts = listOf(
            Triple("WBNK-TR44-1111-2222-3333-4444-5555-66", "W Bank Escrow Service", "90123456"),
            Triple("WBNK-TR88-9999-8888-7777-6666-5555-44", "W Bank Savings Reserve", "78901234"),
            Triple("WBNK-TR12-3456-7890-1234-5678-9012-34", "Elena Thorne (Platinum Executive)", "56789012")
        )

        for ((iban, name, custNo) in mockAccounts) {
            if (accountDao.getAccountByIban(iban) == null) {
                // Seed simulated User
                val u = WBankUserEntity(
                    customerNumber = custNo,
                    firstName = name.split(" ").getOrNull(0) ?: "Simulated",
                    lastName = name.split(" ").getOrNull(1) ?: "Receiver",
                    nationalId = "TR***888",
                    email = "${name.lowercase().replace(" ", "")}@wbank.com",
                    passwordHash = "n/a",
                    mainAccountIban = iban,
                    isLoggedIn = false
                )
                userDao.insertUser(u)

                // Seed Account
                val acc = WBankAccountEntity(
                    iban = iban,
                    customerNumber = custNo,
                    balance = 500000.0,
                    currency = "USD"
                )
                accountDao.insertAccount(acc)
            }
        }
    }

    override fun getAllHoldingsFlow(): Flow<List<WBankInvestmentHoldingEntity>> {
        return investmentHoldingDao.getAllHoldingsFlow()
    }

    override suspend fun buyAsset(
        assetId: String,
        assetName: String,
        assetType: String,
        amount: Double,
        pricePerUnit: Double
    ): TransferResult = transactionMutex.withLock {
        delay(800) // simulation latency
        val user = currentUser.value ?: return@withLock TransferResult.Error("Not Authenticated")
        val accountEntity = accountDao.getAccountByCustomerNumber(user.customerNumber)
            ?: return@withLock TransferResult.Error("No Account Found")

        val totalCost = amount * pricePerUnit
        if (accountEntity.balance < totalCost) {
            return@withLock TransferResult.Error("Insufficient Funds to complete asset buy order.")
        }

        // 1. Debit account balance
        val newBalance = accountEntity.balance - totalCost
        accountDao.updateAccount(accountEntity.copy(balance = newBalance))

        // 2. Insert/update holding
        val existing = investmentHoldingDao.getHoldingByAssetId(assetId)
        if (existing != null) {
            val newAmount = existing.amount + amount
            val newAvgCost = ((existing.amount * existing.averageCost) + (amount * pricePerUnit)) / newAmount
            investmentHoldingDao.insertHolding(
                existing.copy(amount = newAmount, averageCost = newAvgCost, currentPrice = pricePerUnit)
            )
        } else {
            investmentHoldingDao.insertHolding(
                WBankInvestmentHoldingEntity(
                    assetId = assetId,
                    assetName = assetName,
                    assetType = assetType,
                    amount = amount,
                    averageCost = pricePerUnit,
                    currentPrice = pricePerUnit,
                    dailyChangePercent = (-30..30).random() / 10.0 // small daily trends setup
                )
            )
        }

        // 3. Log into Ledger Box
        val ledgerId = UUID.randomUUID().toString()
        val entry = WBankLedgerEntryEntity(
            id = ledgerId,
            senderIban = accountEntity.iban,
            receiverIban = "PORTFOLIO-$assetId",
            receiverName = "Investment Portfolio ($assetName)",
            amount = totalCost,
            type = "TRANSFER_OUT",
            timestamp = System.currentTimeMillis(),
            description = "Bought ${String.format("%.4f", amount)} units of $assetName"
        )
        ledgerDao.insertLedgerEntry(entry)

        return@withLock TransferResult.Success
    }

    override suspend fun sellAsset(
        assetId: String,
        amountToSell: Double,
        pricePerUnit: Double
    ): TransferResult = transactionMutex.withLock {
        delay(800)
        val user = currentUser.value ?: return@withLock TransferResult.Error("Not Authenticated")
        val accountEntity = accountDao.getAccountByCustomerNumber(user.customerNumber)
            ?: return@withLock TransferResult.Error("No Account Found")

        val existing = investmentHoldingDao.getHoldingByAssetId(assetId)
        if (existing == null || existing.amount < amountToSell) {
            return@withLock TransferResult.Error("Insufficient Asset holdings to complete sell order.")
        }

        // 1. Credit account balance
        val totalRevenue = amountToSell * pricePerUnit
        val newBalance = accountEntity.balance + totalRevenue
        accountDao.updateAccount(accountEntity.copy(balance = newBalance))

        // 2. Reduce or delete holding
        val newAmount = existing.amount - amountToSell
        if (newAmount <= 1e-7) {
            investmentHoldingDao.deleteHolding(existing)
        } else {
            investmentHoldingDao.insertHolding(existing.copy(amount = newAmount))
        }

        // 3. Log into Ledger Box
        val ledgerId = UUID.randomUUID().toString()
        val entry = WBankLedgerEntryEntity(
            id = ledgerId,
            senderIban = "PORTFOLIO-$assetId",
            receiverIban = accountEntity.iban,
            receiverName = "Investment Portfolio (${existing.assetName})",
            amount = totalRevenue,
            type = "TRANSFER_IN",
            timestamp = System.currentTimeMillis(),
            description = "Sold ${String.format("%.4f", amountToSell)} units of ${existing.assetName}"
        )
        ledgerDao.insertLedgerEntry(entry)

        return@withLock TransferResult.Success
    }

    override suspend fun seedMockHoldings() {
        // We can pre-seed Apple & Gold holdings so the user starts with some assets to sell, for richer UI interaction
        if (investmentHoldingDao.getHoldingByAssetId("GOLD") == null) {
            investmentHoldingDao.insertHolding(WBankInvestmentHoldingEntity("GOLD", "Gold (oz)", "GOLD", 1.5, 2350.0, 2410.50, 0.45))
        }
        if (investmentHoldingDao.getHoldingByAssetId("AAPL") == null) {
            investmentHoldingDao.insertHolding(WBankInvestmentHoldingEntity("AAPL", "Apple Inc.", "STOCK", 5.0, 180.0, 185.20, 1.25))
        }
    }

    override fun getAppliedLoansFlow(customerNumber: String): Flow<List<WBankLoanEntity>> {
        return loanDao.getLoansByCustomerNumberFlow(customerNumber)
    }

    override suspend fun applyLoan(
        customerNumber: String,
        type: String,
        amount: Double,
        rate: Double,
        months: Int,
        monthlyPay: Double
    ): TransferResult = transactionMutex.withLock {
        delay(1000)
        val accountEntity = accountDao.getAccountByCustomerNumber(customerNumber)
            ?: return@withLock TransferResult.Error("Client account not found")

        // 1. Credit account balance
        val newBalance = accountEntity.balance + amount
        accountDao.updateAccount(accountEntity.copy(balance = newBalance))

        // 2. Insert into Loans
        val loanId = UUID.randomUUID().toString()
        val loan = WBankLoanEntity(
            id = loanId,
            customerNumber = customerNumber,
            type = type,
            principalAmount = amount,
            interestRate = rate,
            maturityMonths = months,
            monthlyPayment = monthlyPay,
            status = "APPROVED",
            timestamp = System.currentTimeMillis()
        )
        loanDao.insertLoan(loan)

        // 3. Log in Ledger Box
        val ledgerId = UUID.randomUUID().toString()
        val entry = WBankLedgerEntryEntity(
            id = ledgerId,
            senderIban = "WBNK-LOAN-CREDIT",
            receiverIban = accountEntity.iban,
            receiverName = "W Bank Loan Department",
            amount = amount,
            type = "TRANSFER_IN",
            timestamp = System.currentTimeMillis(),
            description = "Approved $type Loan Disbursal"
        )
        ledgerDao.insertLedgerEntry(entry)

        return@withLock TransferResult.Success
    }

    override suspend fun executeAtmDeposit(iban: String, amount: Double, description: String): TransferResult = transactionMutex.withLock {
        delay(600)
        val account = accountDao.getAccountByIban(iban) ?: return@withLock TransferResult.Error("Account not found")
        val newBalance = account.balance + amount
        accountDao.updateAccount(account.copy(balance = newBalance))

        val entry = WBankLedgerEntryEntity(
            id = UUID.randomUUID().toString(),
            senderIban = "WBNK-ATM-QR",
            receiverIban = iban,
            receiverName = "Me (ATM QR Deposit)",
            amount = amount,
            type = "DEPOSIT",
            timestamp = System.currentTimeMillis(),
            description = description
        )
        ledgerDao.insertLedgerEntry(entry)
        return@withLock TransferResult.Success
    }

    override suspend fun executeAtmWithdrawal(iban: String, amount: Double, description: String): TransferResult = transactionMutex.withLock {
        delay(600)
        val account = accountDao.getAccountByIban(iban) ?: return@withLock TransferResult.Error("Account not found")
        if (account.balance < amount) {
            return@withLock TransferResult.Error("Insufficient Funds at ATM Cash Drawer.")
        }
        val newBalance = account.balance - amount
        accountDao.updateAccount(account.copy(balance = newBalance))

        val entry = WBankLedgerEntryEntity(
            id = UUID.randomUUID().toString(),
            senderIban = iban,
            receiverIban = "WBNK-ATM-QR",
            receiverName = "Me (ATM QR Withdrawal)",
            amount = amount,
            type = "WITHDRAWAL",
            timestamp = System.currentTimeMillis(),
            description = description
        )
        ledgerDao.insertLedgerEntry(entry)
        return@withLock TransferResult.Success
    }
}

