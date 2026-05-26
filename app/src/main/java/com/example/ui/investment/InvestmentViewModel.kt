package com.example.ui.investment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.WBankAccount
import com.example.data.entity.WBankInvestmentHoldingEntity
import com.example.domain.repository.TransferResult
import com.example.domain.repository.WBankRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

sealed interface InvestmentUiEvent {
    data class ShowSnackbar(val message: String) : InvestmentUiEvent
    object RefreshComplete : InvestmentUiEvent
}

data class AssetTicker(
    val assetId: String,
    val name: String,
    val assetType: String,
    val currentPrice: Double,
    val dailyChangePercent: Double,
    val historySample: List<Float> // sample for Sparkline trend chart
)

data class InvestmentUiState(
    val account: WBankAccount? = null,
    val holdings: List<WBankInvestmentHoldingEntity> = emptyList(),
    val tickers: List<AssetTicker> = emptyList(),
    val isTransacting: Boolean = false,
    val transactionMessage: String? = null
)

class InvestmentViewModel(private val repository: WBankRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(InvestmentUiState())
    val uiState: StateFlow<InvestmentUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<InvestmentUiEvent>()
    val uiEvent: SharedFlow<InvestmentUiEvent> = _uiEvent.asSharedFlow()

    // Initialize interactive ticker prices with sample premium trends
    private val staticTickers = listOf(
        AssetTicker("GOLD", "Gold (oz)", "GOLD", 2435.50, 1.22, listOf(2410f, 2415f, 2422f, 2419f, 2430f, 2435f)),
        AssetTicker("EUR", "Euro (EUR/USD)", "FX", 1.0865, -0.45, listOf(1.092f, 1.091f, 1.088f, 1.089f, 1.087f, 1.086f)),
        AssetTicker("AAPL", "Apple Inc.", "STOCK", 188.30, 2.15, listOf(181f, 183f, 182f, 185f, 187f, 188f)),
        AssetTicker("GOOGL", "Alphabet Inc.", "STOCK", 173.80, 0.85, listOf(171f, 172f, 171.5f, 174f, 173f, 173.8f)),
        AssetTicker("TSLA", "Tesla Inc.", "STOCK", 178.60, -3.42, listOf(189f, 185f, 180f, 182f, 177f, 178.6f))
    )

    init {
        // Pre-seed mock data for premium first-view portfolio
        viewModelScope.launch {
            repository.seedMockHoldings()
        }
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            // Load interactive live tickers
            _uiState.value = _uiState.value.copy(tickers = staticTickers)

            // Listen to current logged-in user
            repository.getCurrentUserFlow().collect { user ->
                if (user != null) {
                    // Monitor user account Balance
                    launch {
                        repository.getAccountByCustomerNumberFlow(user.customerNumber).collect { account ->
                            _uiState.value = _uiState.value.copy(account = account)
                        }
                    }
                    // Monitor holdings in Room
                    launch {
                        repository.getAllHoldingsFlow().collect { holdingsList ->
                            // Update our live holdings match with live ticker price changes
                            val enrichedHoldings = holdingsList.map { holding ->
                                val matchingTicker = staticTickers.find { it.assetId == holding.assetId }
                                if (matchingTicker != null) {
                                    holding.copy(
                                        currentPrice = matchingTicker.currentPrice,
                                        dailyChangePercent = matchingTicker.dailyChangePercent
                                    )
                                } else {
                                    holding
                                }
                            }
                            _uiState.value = _uiState.value.copy(holdings = enrichedHoldings)
                        }
                    }
                }
            }
        }
    }

    fun executeBuy(assetId: String, amount: Double) {
        if (amount <= 0) {
            viewModelScope.launch {
                _uiEvent.emit(InvestmentUiEvent.ShowSnackbar("Please enter a valid amount strictly greater than 0"))
            }
            return
        }

        val ticker = _uiState.value.tickers.find { it.assetId == assetId } ?: return
        _uiState.value = _uiState.value.copy(isTransacting = true)

        viewModelScope.launch {
            val result = repository.buyAsset(
                assetId = ticker.assetId,
                assetName = ticker.name,
                assetType = ticker.assetType,
                amount = amount,
                pricePerUnit = ticker.currentPrice
            )

            _uiState.value = _uiState.value.copy(isTransacting = false)
            when (result) {
                is TransferResult.Success -> {
                    _uiEvent.emit(InvestmentUiEvent.ShowSnackbar("Success! Bought $amount units of ${ticker.name}"))
                }
                is TransferResult.Error -> {
                    _uiEvent.emit(InvestmentUiEvent.ShowSnackbar(result.message))
                }
            }
        }
    }

    fun executeSell(assetId: String, amount: Double) {
        if (amount <= 0) {
            viewModelScope.launch {
                _uiEvent.emit(InvestmentUiEvent.ShowSnackbar("Please enter a valid amount strictly greater than 0"))
            }
            return
        }

        val ticker = _uiState.value.tickers.find { it.assetId == assetId } ?: return
        val currentHolding = _uiState.value.holdings.find { it.assetId == assetId }
        if (currentHolding == null || currentHolding.amount < amount) {
            viewModelScope.launch {
                _uiEvent.emit(InvestmentUiEvent.ShowSnackbar("Insufficient Asset holding balance."))
            }
            return
        }

        _uiState.value = _uiState.value.copy(isTransacting = true)

        viewModelScope.launch {
            val result = repository.sellAsset(
                assetId = ticker.assetId,
                amountToSell = amount,
                pricePerUnit = ticker.currentPrice
            )

            _uiState.value = _uiState.value.copy(isTransacting = false)
            when (result) {
                is TransferResult.Success -> {
                    _uiEvent.emit(InvestmentUiEvent.ShowSnackbar("Success! Sold $amount units of ${ticker.name}"))
                }
                is TransferResult.Error -> {
                    _uiEvent.emit(InvestmentUiEvent.ShowSnackbar(result.message))
                }
            }
        }
    }

    fun simulateTickerPriceVolatility() {
        viewModelScope.launch {
            // Apply subtle real-looking standard random fluctuations
            val randomizedTickers = _uiState.value.tickers.map { ticker ->
                val ratio = 1.0 + ((-15..15).random() / 1000.0) // +/- 1.5% fluctuation
                val rawNewPrice = ticker.currentPrice * ratio
                val roundPrice = (rawNewPrice * 1000.0).roundToInt() / 1000.0
                val dailyTrend = (ticker.dailyChangePercent + (-5..5).random() / 10.0).coerceIn(-10.0, 10.0)
                ticker.copy(currentPrice = roundPrice, dailyChangePercent = dailyTrend)
            }
            _uiState.value = _uiState.value.copy(tickers = randomizedTickers)
            _uiEvent.emit(InvestmentUiEvent.RefreshComplete)
            _uiEvent.emit(InvestmentUiEvent.ShowSnackbar("Live market boards updated automatically"))
        }
    }
}
