package com.example.ui.investment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TopBarProfileActions
import com.example.ui.theme.SleekCreditGreen
import com.example.ui.theme.SleekDebitRed
import com.example.ui.theme.SleekDigitalBlue
import com.example.ui.theme.SleekNavyHeader
import com.example.ui.theme.SleekSecondaryBlue
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondarySlate
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentScreen(
    viewModel: InvestmentViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedAssetForTrade by remember { mutableStateOf<AssetTicker?>(null) }
    var tradeTypeIsBuy by remember { mutableStateOf(true) } // true = Buy, false = Sell
    var showTradeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is InvestmentUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                InvestmentUiEvent.RefreshComplete -> {}
            }
        }
    }

    // Calculate dynamic values
    val totalHoldingsValue = uiState.holdings.sumOf { it.amount * it.currentPrice }
    val averagePL = uiState.holdings.sumOf { (it.currentPrice - it.averageCost) * it.amount }
    val plIsPositive = averagePL >= 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "W PORTFOLIO INVESTMENT",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 1.2.sp,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = viewModel::simulateTickerPriceVolatility) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh interactive market",
                            tint = Color.White
                        )
                    }
                    TopBarProfileActions()
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        modifier = modifier.testTag("investment_screen_root")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Portfolio Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("portfolio_summary_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161618)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "TOTAL PORTFOLIO ECOSYSTEM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondarySlate,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.US, "$%,.2f", totalHoldingsValue),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (plIsPositive) "▲" else "▼",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = if (plIsPositive) SleekCreditGreen else SleekDebitRed
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "Daily P&L: %s$%,.2f (%s%.2f%%)",
                                    if (plIsPositive) "+" else "",
                                    averagePL,
                                    if (plIsPositive) "+" else "",
                                    if (totalHoldingsValue > 0) (averagePL / totalHoldingsValue) * 100 else 0.0
                                ),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (plIsPositive) SleekCreditGreen else SleekDebitRed
                            )
                        }
                    }
                }
            }

            // Available Cash Box
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF161618))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Liquidity Core (USD Account)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondarySlate
                    )
                    Text(
                        text = String.format(Locale.US, "$%,.2f", uiState.account?.balance ?: 0.0),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Current Holdings List Section
            if (uiState.holdings.isNotEmpty()) {
                item {
                    Text(
                        text = "YOUR ACTIVE HOLDINGS",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp
                    )
                }

                items(uiState.holdings) { holding ->
                    val holdingTotal = holding.amount * holding.currentPrice
                    val PL = (holding.currentPrice - holding.averageCost) * holding.amount
                    val itemIsPositive = PL >= 0

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161618)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = holding.assetName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.4f", holding.amount)} units @ avg $${String.format(Locale.US, "%,.2f", holding.averageCost)}",
                                    fontSize = 11.sp,
                                    color = TextSecondarySlate
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format(Locale.US, "$%,.2f", holdingTotal),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (itemIsPositive) "▲" else "▼",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (itemIsPositive) SleekCreditGreen else SleekDebitRed
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = String.format(
                                            Locale.US,
                                            "%s$%,.2f",
                                            if (itemIsPositive) "+" else "",
                                            PL
                                        ),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (itemIsPositive) SleekCreditGreen else SleekDebitRed
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Live Market Rates Boards Section
            item {
                Text(
                    text = "LIVE ASSET BOARD",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.2.sp
                )
            }

            items(uiState.tickers) { ticker ->
                val trendIsUp = ticker.dailyChangePercent >= 0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedAssetForTrade = ticker
                            tradeTypeIsBuy = true
                            showTradeDialog = true
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161618)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Badge(
                                    containerColor = when (ticker.assetType) {
                                        "GOLD" -> Color(0xFFEAB308).copy(alpha = 0.15f)
                                        "FX" -> SleekDigitalBlue.copy(alpha = 0.12f)
                                        else -> SleekNavyHeader.copy(alpha = 0.12f)
                                    },
                                    contentColor = when (ticker.assetType) {
                                        "GOLD" -> Color(0xFFCA8A04)
                                        "FX" -> SleekDigitalBlue
                                        else -> SleekNavyHeader
                                    }
                                ) {
                                    Text(
                                        text = ticker.assetType,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 8.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ticker.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }

                            // Interactive Sparkline visual
                            SparklineChart(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(30.dp)
                                    .padding(horizontal = 4.dp),
                                trendLine = ticker.historySample,
                                isSuccess = trendIsUp
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format(Locale.US, "$%,.2f", ticker.currentPrice),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = String.format(Locale.US, "%s%.2f%%", if (trendIsUp) "+" else "", ticker.dailyChangePercent),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = if (trendIsUp) SleekCreditGreen else SleekDebitRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Trade Dialog (Buy/Sell)
    if (showTradeDialog && selectedAssetForTrade != null) {
        val asset = selectedAssetForTrade!!
        var tradeAmountStr by remember { mutableStateOf("") }
        val focusManager = LocalFocusManager.current

        val tradeAmountDouble = tradeAmountStr.toDoubleOrNull() ?: 0.0
        val totalTradeCost = tradeAmountDouble * asset.currentPrice
        val userHolding = uiState.holdings.find { it.assetId == asset.assetId }?.amount ?: 0.0

        AlertDialog(
            onDismissRequest = { showTradeDialog = false },
            title = {
                Text(
                    text = if (tradeTypeIsBuy) "Buy ${asset.name}" else "Sell ${asset.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SleekNavyHeader
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Quick stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ticker Price:", fontSize = 12.sp, color = TextSecondarySlate)
                        Text("$${String.format(Locale.US, "%,.2f", asset.currentPrice)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Your Holding:", fontSize = 12.sp, color = TextSecondarySlate)
                        Text("${String.format(Locale.US, "%.4f", userHolding)} units", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    }

                    // Toggle trade type
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SleekSurfaceVariant)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (tradeTypeIsBuy) SleekDigitalBlue else Color.Transparent)
                                .clickable { tradeTypeIsBuy = true }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "BUY MARKET",
                                color = if (tradeTypeIsBuy) Color.White else TextSecondarySlate,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!tradeTypeIsBuy) SleekDebitRed else Color.Transparent)
                                .clickable { tradeTypeIsBuy = false }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "SELL MARKET",
                                color = if (!tradeTypeIsBuy) Color.White else TextSecondarySlate,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    OutlinedTextField(
                        value = tradeAmountStr,
                        onValueChange = { tradeAmountStr = it },
                        label = { Text("Volume (Units / Share)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("trade_amount_input"),
                        singleLine = true
                    )

                    // Trade cost stats summary
                    if (tradeAmountDouble > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SleekSecondaryBlue.copy(alpha = 0.2f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ESTIMATED TOTAL:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekNavyHeader
                            )
                            Text(
                                text = String.format(Locale.US, "$%,.2f", totalTradeCost),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimaryDark
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        showTradeDialog = false
                        if (tradeTypeIsBuy) {
                            viewModel.executeBuy(asset.assetId, tradeAmountDouble)
                        } else {
                            viewModel.executeSell(asset.assetId, tradeAmountDouble)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tradeTypeIsBuy) SleekDigitalBlue else SleekDebitRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("execute_trade_button")
                ) {
                    Text(if (tradeTypeIsBuy) "Buy Asset Order" else "Sell Asset Order")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTradeDialog = false }) {
                    Text("Abort")
                }
            }
        )
    }
}

// Sparkline Visual renderer from scratch using canvas
@Composable
fun SparklineChart(
    trendLine: List<Float>,
    isSuccess: Boolean,
    modifier: Modifier = Modifier
) {
    val strokeColor = if (isSuccess) SleekCreditGreen else SleekDebitRed
    Canvas(modifier = modifier) {
        if (trendLine.size < 2) return@Canvas
        val width = size.width
        val height = size.height

        val minVal = trendLine.minOrNull() ?: 0f
        val maxVal = trendLine.maxOrNull() ?: 1f
        val range = if (maxVal == minVal) 1f else maxVal - minVal

        val points = trendLine.mapIndexed { index, value ->
            val x = index * (width / (trendLine.size - 1))
            val y = height - ((value - minVal) / range) * height
            Offset(x, y)
        }

        val strokePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = strokePath,
            color = strokeColor,
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}
