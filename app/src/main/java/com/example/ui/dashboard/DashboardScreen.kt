package com.example.ui.dashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.TransactionType
import com.example.domain.model.WBankAccount
import com.example.domain.model.WBankLedgerEntry
import com.example.domain.model.WBankUser
import com.example.ui.components.TopBarProfileActions
import com.example.ui.theme.SleekDebitRed
import com.example.ui.theme.SleekDebitRedLight
import com.example.ui.theme.SleekCreditGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SleekNavyHeader
import com.example.ui.theme.SleekDigitalBlue
import com.example.ui.theme.SleekSecondaryBlue
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondarySlate
import com.example.ui.theme.CharcoalGrayStart
import com.example.ui.theme.CharcoalGrayMid
import com.example.ui.theme.CharcoalGrayEnd
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    snackbarHostState: SnackbarHostState,
    onNavigateToTransfer: () -> Unit,
    onNavigateToLedger: () -> Unit,
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Dialog & payment states for VakıfBank actions
    var showQrAtmDialog by remember { mutableStateOf(false) }
    var qrAtmActionIsDeposit by remember { mutableStateOf(true) }
    var qrAtmAmountStr by remember { mutableStateOf("100") }
    var showQrCodePreview by remember { mutableStateOf(false) }

    var showPaymentsHubDialog by remember { mutableStateOf(false) }
    var paymentsSelectedTab by remember { mutableStateOf(0) } // 0 = Bill, 1 = GSM

    LaunchedEffect(key1 = Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is DashboardUiEvent.NavigateToAuth -> {
                    onNavigateToAuth()
                }
                is DashboardUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "W BANK PLATINUM",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color.White,
                            letterSpacing = 1.2.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::simulateCardRefresh,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = SleekDigitalBlue)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Card")
                    }
                    TopBarProfileActions(
                        onLogout = viewModel::logout
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToTransfer,
                containerColor = SleekDigitalBlue,
                contentColor = Color.White,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("transfer_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Transfer Icon")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Transfer", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
         Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Accounts Selector Carousel State Parameters
            var selectedAccountIndex by remember { mutableStateOf(0) } // Defaults to TL-Current as requested
            val accountTypes = listOf("TL-Current", "TL-Savings", "USD-Current")
            val accountCurrencies = listOf("TRY", "TRY", "USD")
            val accountSymbols = listOf("₺", "₺", "$")
            val accountBalances = remember(uiState.account?.balance) {
                val usdBalance = uiState.account?.balance ?: 10000.0
                listOf(
                    usdBalance * 32.54, // TL-Current converted automatically
                    1250000.0,          // TL-Savings high-fidelity preloaded value
                    usdBalance          // USD-Current from secure database
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Greeting Widget (Sleek Corporate Styling)
                uiState.user?.let { user ->
                    Text(
                        text = "WELCOME BACK",
                        fontSize = 11.sp,
                        color = TextSecondarySlate,
                        fontWeight = FontWeight.Light, // Light weight cap as requested
                        letterSpacing = 1.8.sp
                    )
                    Text(
                        text = "${user.firstName} ${user.lastName}".uppercase(), // Dynamic name
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold, // Rigid bold
                        color = Color.White, // High contrast crisp white
                        letterSpacing = 0.5.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.testTag("welcome_back_username")
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Account card loading check
                if (uiState.isLoading && uiState.account == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SleekDigitalBlue)
                    }
                } else {
                    uiState.account?.let { account ->
                        // W Bank Platinum Card Element with dynamic carousel mapping layers
                        PlatinumCard(
                            account = account,
                            user = uiState.user,
                            onCopyIban = {
                                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val targetIban = if (selectedAccountIndex == 0) {
                                    account.iban.replace("USD", "TRY").replace("-34", "-TL")
                                } else if (selectedAccountIndex == 1) {
                                    account.iban.replace("USD", "SAV").replace("-34", "-SV")
                                } else {
                                    account.iban
                                }
                                val clip = ClipData.newPlainText("W Bank Clean IBAN", targetIban)
                                clipboardManager.setPrimaryClip(clip)
                                viewModel.simulateCardRefresh() // triggers snackbar message internally
                            },
                            selectedBalance = accountBalances[selectedAccountIndex],
                            selectedCurrency = accountCurrencies[selectedAccountIndex],
                            selectedCurrencySymbol = accountSymbols[selectedAccountIndex],
                            selectedAccountType = accountTypes[selectedAccountIndex]
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // High-Fidelity ACCOUNTS switcher carousel widget
                        Text(
                            text = "ACCOUNTS PORTFOLIO",
                            color = TextSecondarySlate,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF161618))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            accountTypes.forEachIndexed { index, type ->
                                val isSelected = selectedAccountIndex == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) SleekDigitalBlue else Color.Transparent)
                                        .clickable { selectedAccountIndex = index }
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = type,
                                            color = if (isSelected) Color.White else TextSecondarySlate,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        val bal = accountBalances[index]
                                        val sym = accountSymbols[index]
                                        Text(
                                            text = String.format(Locale.US, "%s%,.0f", sym, bal),
                                            color = if (isSelected) Color.White.copy(alpha = 0.9f) else GoldAccent,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Modern Asymmetric Feature Grid Shortcuts representing Sleek Interface HTML
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionShortcutItem(
                        iconLabel = "Transfer",
                        icon = Icons.Default.Send,
                        containerColor = SleekSecondaryBlue,
                        contentColor = SleekNavyHeader,
                        onClick = onNavigateToTransfer,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ActionShortcutItem(
                        iconLabel = "QR ATM",
                        icon = Icons.Default.Refresh,
                        containerColor = SleekSurfaceVariant,
                        contentColor = TextPrimaryDark,
                        onClick = { showQrAtmDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ActionShortcutItem(
                        iconLabel = "Ledger",
                        icon = Icons.Default.Info,
                        containerColor = SleekSurfaceVariant,
                        contentColor = TextPrimaryDark,
                        onClick = onNavigateToLedger,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ActionShortcutItem(
                        iconLabel = "Payments",
                        icon = Icons.Default.Menu,
                        containerColor = SleekSurfaceVariant,
                        contentColor = TextPrimaryDark,
                        onClick = { showPaymentsHubDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Navigation Quick Links Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT ACTIVITY",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp,
                        color = Color.White
                    )
                    Text(
                        text = "View Ledger",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekDigitalBlue,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToLedger)
                            .testTag("view_statements_link")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Timeline List
                if (uiState.recentTransactions.isEmpty()) {
                    // Empty state helper
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info Empty",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Your Ledger Statement is Empty.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Make a secure P2P transfer or request a promotional credit to see activities.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recent_transactions_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column {
                            uiState.recentTransactions.forEachIndexed { index, entry ->
                                TransactionRowItem(entry = entry, currentIban = uiState.account?.iban ?: "")
                                if (index < uiState.recentTransactions.size - 1) {
                                    Divider(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB space
            }
        }

    // --- QR ATM Dialog ---
    if (showQrAtmDialog) {
        AlertDialog(
            modifier = Modifier.testTag("qr_atm_dialog"),
            containerColor = Color(0xFF161618),
            onDismissRequest = { 
                showQrAtmDialog = false
                showQrCodePreview = false
            },
            title = {
                Text(
                    text = "QR ATM Operations",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Action type toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF28282B))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (qrAtmActionIsDeposit) SleekDigitalBlue else Color.Transparent)
                                .clickable { qrAtmActionIsDeposit = true }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "ATM Deposit",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!qrAtmActionIsDeposit) SleekDigitalBlue else Color.Transparent)
                                .clickable { qrAtmActionIsDeposit = false }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "ATM Withdraw",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Transaction Amount ($)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondarySlate
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Quick amount chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("20", "50", "100", "200", "550").forEach { amt ->
                            val isSelected = qrAtmAmountStr == amt
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) SleekDigitalBlue else Color(0xFF28282B),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .background(if (isSelected) SleekDigitalBlue.copy(alpha = 0.1f) else Color.Transparent)
                                    .clickable { qrAtmAmountStr = amt }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "$$amt",
                                    color = if (isSelected) SleekDigitalBlue else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (showQrCodePreview) {
                        // Simulated QR canvas graphic as required!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF28282B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(100.dp)) {
                                val size = size.width
                                val steps = 10
                                val stepSize = size / steps
                                // Let's draw standard QR finder pattern boxes in corners!
                                drawRect(
                                    color = Color.White,
                                    topLeft = Offset(0f, 0f),
                                    size = androidx.compose.ui.geometry.Size(stepSize * 3, stepSize * 3)
                                )
                                drawRect(
                                    color = Color(0xFF28282B),
                                    topLeft = Offset(stepSize, stepSize),
                                    size = androidx.compose.ui.geometry.Size(stepSize, stepSize)
                                )
                                drawRect(
                                    color = Color.White,
                                    topLeft = Offset(size - stepSize * 3, 0f),
                                    size = androidx.compose.ui.geometry.Size(stepSize * 3, stepSize * 3)
                                )
                                drawRect(
                                    color = Color(0xFF28282B),
                                    topLeft = Offset(size - stepSize * 2, stepSize),
                                    size = androidx.compose.ui.geometry.Size(stepSize, stepSize)
                                )
                                drawRect(
                                    color = Color.White,
                                    topLeft = Offset(0f, size - stepSize * 3),
                                    size = androidx.compose.ui.geometry.Size(stepSize * 3, stepSize * 3)
                                )
                                drawRect(
                                    color = Color(0xFF28282B),
                                    topLeft = Offset(stepSize, size - stepSize * 2),
                                    size = androidx.compose.ui.geometry.Size(stepSize, stepSize)
                                )

                                // Draw some matrix dots
                                for (i in 0 until steps) {
                                    for (j in 0 until steps) {
                                        if ((i in 0..2 && j in 0..2) || (i in 7..9 && j in 0..2) || (i in 0..2 && j in 7..9)) {
                                            continue
                                        }
                                        if ((i + j) % 2 == 0 || (i * j) % 3 == 1) {
                                            drawRect(
                                                color = Color.White,
                                                topLeft = Offset(i * stepSize, j * stepSize),
                                                size = androidx.compose.ui.geometry.Size(stepSize - 2f, stepSize - 2f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { showQrCodePreview = true },
                            modifier = Modifier.fillMaxWidth().testTag("generate_qr_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekDigitalBlue)
                        ) {
                            Text("Generate QR Security Token", color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                if (showQrCodePreview) {
                    Button(
                        modifier = Modifier.testTag("simulate_qr_scan_button"),
                        onClick = {
                            val amount = qrAtmAmountStr.toDoubleOrNull() ?: 100.0
                            val iban = uiState.account?.iban ?: ""
                            if (qrAtmActionIsDeposit) {
                                viewModel.executeAtmDeposit(iban, amount, "ATM QR Deposit")
                            } else {
                                viewModel.executeAtmWithdrawal(iban, amount, "ATM QR Cash Withdrawal")
                            }
                            showQrAtmDialog = false
                            showQrCodePreview = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekCreditGreen)
                    ) {
                        Text("Simulate Scanner Success", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showQrAtmDialog = false
                    showQrCodePreview = false
                }) {
                    Text("Close", color = SleekDigitalBlue)
                }
            }
        )
    }

    // --- Payments Hub Dialog (Bill & GSM) ---
    if (showPaymentsHubDialog) {
        var billSubscriberId by remember { mutableStateOf("") }
        var billProviderSelected by remember { mutableStateOf("VakıfEnerji (Electricity)") }
        var gsmNumber by remember { mutableStateOf("") }
        var gsmProviderSelected by remember { mutableStateOf("VakıfMobile") }
        var gsmPackageSelected by remember { mutableStateOf("Promo Pack 20GB ($25)") }

        AlertDialog(
            modifier = Modifier.testTag("payments_hub_dialog"),
            containerColor = Color(0xFF161618),
            onDismissRequest = { showPaymentsHubDialog = false },
            title = {
                Text(
                    text = "VakıfBank Quick Payments",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Selection tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF28282B))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (paymentsSelectedTab == 0) SleekDigitalBlue else Color.Transparent)
                                .clickable { paymentsSelectedTab = 0 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Bill Payment",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (paymentsSelectedTab == 1) SleekDigitalBlue else Color.Transparent)
                                .clickable { paymentsSelectedTab = 1 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "GSM Top-up",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (paymentsSelectedTab == 0) {
                        // Bill Section
                        Text(
                            "Select Utility Provider",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondarySlate
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf("VakıfEnerji", "VakıfGas", "AquaCity", "VakıfFiber").forEach { prov ->
                                val isSelected = billProviderSelected.startsWith(prov)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) SleekDigitalBlue else Color(0xFF28282B),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .background(if (isSelected) SleekDigitalBlue.copy(alpha = 0.1f) else Color.Transparent)
                                        .clickable { billProviderSelected = "$prov Utility" }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        prov,
                                        color = if (isSelected) SleekDigitalBlue else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = billSubscriberId,
                            onValueChange = { billSubscriberId = it },
                            label = { Text("Subscriber Invoice ID") },
                            modifier = Modifier.fillMaxWidth().testTag("subscriber_id_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SleekDigitalBlue,
                                unfocusedBorderColor = Color(0xFF28282B),
                                cursorColor = SleekDigitalBlue,
                                focusedLabelColor = SleekDigitalBlue,
                                unfocusedLabelColor = TextSecondarySlate
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Simulated amount logic
                        val isSubKeyNotEmpty = billSubscriberId.isNotBlank()
                        val simulatedBillAmount = if (isSubKeyNotEmpty) {
                            val hash = billSubscriberId.hashCode().coerceAtLeast(1)
                            (hash % 90) + 15.50
                        } else 0.0

                        if (isSubKeyNotEmpty) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF28282B))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Outstanding Bill Invoice:", fontSize = 11.sp, color = TextSecondarySlate)
                                    Text(
                                        String.format(Locale.US, "$%,.2f", simulatedBillAmount),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = SleekDebitRed
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (billSubscriberId.isBlank()) return@Button
                                val iban = uiState.account?.iban ?: ""
                                viewModel.executeBillPayment(iban, billProviderSelected, billSubscriberId, simulatedBillAmount)
                                showPaymentsHubDialog = false
                            },
                            enabled = billSubscriberId.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().testTag("confirm_bill_pay_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekDigitalBlue, disabledContainerColor = Color(0xFF28282B))
                        ) {
                            Text("Confirm & Pay Invoice", color = Color.White)
                        }
                    } else {
                        // GSM Top-up Section
                        OutlinedTextField(
                            value = gsmNumber,
                            onValueChange = { gsmNumber = it },
                            label = { Text("Phone Number (+90 5XX)") },
                            modifier = Modifier.fillMaxWidth().testTag("gsm_phone_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SleekDigitalBlue,
                                unfocusedBorderColor = Color(0xFF28282B),
                                cursorColor = SleekDigitalBlue,
                                focusedLabelColor = SleekDigitalBlue,
                                unfocusedLabelColor = TextSecondarySlate
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "Select GSM Provider",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondarySlate
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf("Turkcell", "Vodafone", "TürkTelekom", "VakıfMobile").forEach { prov ->
                                val isSelected = gsmProviderSelected == prov
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) SleekDigitalBlue else Color(0xFF28282B),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .background(if (isSelected) SleekDigitalBlue.copy(alpha = 0.1f) else Color.Transparent)
                                        .clickable { gsmProviderSelected = prov }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        prov,
                                        color = if (isSelected) SleekDigitalBlue else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            "Select Credit bundle",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondarySlate
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        listOf("Base Starter Pack ($15.00)", "Gold Social Media 40GB ($25.00)", "Ultimate Elite Unlimited ($45.00)").forEach { pack ->
                            val isSelected = gsmPackageSelected == pack
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { gsmPackageSelected = pack }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { gsmPackageSelected = pack },
                                    colors = RadioButtonDefaults.colors(selectedColor = SleekDigitalBlue, unselectedColor = Color.White)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(pack, fontSize = 12.sp, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (gsmNumber.isBlank()) return@Button
                                val iban = uiState.account?.iban ?: ""
                                val amt = if (gsmPackageSelected.contains("15")) 15.00 else if (gsmPackageSelected.contains("25")) 25.00 else 45.00
                                viewModel.executeGsmTopUp(iban, gsmProviderSelected, gsmNumber, amt, gsmPackageSelected)
                                showPaymentsHubDialog = false
                            },
                            enabled = gsmNumber.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().testTag("confirm_gsm_topup_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekDigitalBlue, disabledContainerColor = Color(0xFF28282B))
                        ) {
                            Text("Confirm & Refill Line", color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPaymentsHubDialog = false }) {
                    Text("Cancel", color = SleekDigitalBlue)
                }
            }
        )
    }
}
}

@Composable
fun PlatinumCard(
    account: WBankAccount,
    user: WBankUser?,
    onCopyIban: () -> Unit,
    selectedBalance: Double = account.balance,
    selectedCurrency: String = "USD",
    selectedCurrencySymbol: String = "$",
    selectedAccountType: String = "USD-Current"
) {
    // Highly polished luxury graphite card background
    val cardBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF161618), // deep velvet black charcoal
            Color(0xFF28282B), // chrome metallic grey
            Color(0xFF1E1E20)  // charcoal end
        ),
        start = Offset(0f, 0f),
        end = Offset(450f, 450f)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE5A93C), // Luxury Gold Accent
                        Color(0xFFFFFFFF), // Silver Highlights
                        Color(0xFFFFD700)  // Shining Gold
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("platinum_card_element"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardBrush)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "W BANK PLATINUM",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "VIP CARD SERVICES DIVISION",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "W",
                            color = GoldAccent,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }

                // Balance with dynamic locale currency formatting
                Column {
                    Text(
                        text = "SECURED LIQUID MARGINS (${selectedCurrency})",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    
                    val formattedBalance = String.format(Locale.US, "%s%,.2f", selectedCurrencySymbol, selectedBalance)
                    Text(
                        text = formattedBalance,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.testTag("card_balance_text")
                    )
                }

                // Customer Name & IBAN copy row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (user != null) "${user.firstName} ${user.lastName}".uppercase() else "W BANK CUSTOMER", // Dynamic user name
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // Masked IBAN representation
                        val displayIban = if (selectedAccountType == "TL-Current") {
                            account.iban.replace("USD", "TRY").replace("-34", "-TL")
                        } else if (selectedAccountType == "TL-Savings") {
                            account.iban.replace("USD", "SAV").replace("-34", "-SV")
                        } else {
                            account.iban
                        }
                        val maskedIban = maskIbanString(displayIban)
                        Text(
                            text = maskedIban,
                            color = GoldAccent.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("masked_iban_text")
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Small Clear Copy Icon-Button
                    IconButton(
                        onClick = onCopyIban,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .testTag("copy_iban_button")
                    ) {
                        Text(
                            text = "📋", // elegant small copy icon emoji
                            fontSize = 12.sp,
                            color = GoldAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRowItem(
    entry: WBankLedgerEntry,
    currentIban: String
) {
    val isDebit = entry.type is TransactionType.TransferOut || entry.type is TransactionType.Withdrawal
    val amountColor = if (isDebit) SleekDebitRed else SleekCreditGreen
    val amountBgColor = if (isDebit) SleekDebitRedLight else SleekSecondaryBlue
    val amountPrefix = if (isDebit) "-" else "+"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Indicator
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(amountBgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isDebit) "↙" else "↗",
                color = amountColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Content - Remapped to 100% realistic corporate banking names
        Column(modifier = Modifier.weight(1f)) {
            val title = remember(entry.receiverName) {
                val rawName = entry.receiverName ?: ""
                when {
                    rawName.contains("Me (ATM QR Deposit)", ignoreCase = true) -> "ATM CASH DEPOSIT"
                    rawName.contains("Me (ATM QR Withdrawal)", ignoreCase = true) -> "ATM WITHDRAWAL: HAKAN MARKET"
                    rawName.contains("Escrow", ignoreCase = true) -> "W BANK ESCROW SERVICE"
                    rawName.contains("Savings Reserve", ignoreCase = true) -> "W BANK SAVINGS RESERVE"
                    rawName.contains("Elena Thorne", ignoreCase = true) -> "ELENA THORNE"
                    rawName.contains("Bill Payment: Water", ignoreCase = true) -> "VAKIFENERJI UTILITY"
                    rawName.contains("Bill Payment: Internet", ignoreCase = true) -> "SUPERONLINE FIBRE"
                    rawName.contains("GSM Refill", ignoreCase = true) -> "TURKCELL MOBIL GSM"
                    rawName.contains("Apple", ignoreCase = true) -> "APPLE CORP INC."
                    rawName.contains("Gold", ignoreCase = true) -> "VIP BULLION GOLD PORTFOLIO"
                    rawName.contains("Purchase", ignoreCase = true) -> "STARBUCKS COFFEE CO."
                    rawName.equals("Me", ignoreCase = true) -> "MY ACCOUNT"
                    else -> rawName.uppercase()
                }
            }
            Text(
                text = title,
                fontSize = 14.sp,
                color = TextPrimaryDark,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = entry.description ?: "Bank Ledger Clearance",
                fontSize = 11.sp,
                color = TextSecondarySlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Amount & Date
        Spacer(modifier = Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            val formattedAmount = String.format(Locale.US, "$%,.2f", entry.amount)
            Text(
                text = "$amountPrefix$formattedAmount",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            
            val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(entry.timestamp))
            Text(
                text = formattedDate,
                fontSize = 10.sp,
                color = TextSecondarySlate
            )
        }
    }
}

// Action Shortcut Grid item Composable matching design html
@Composable
fun ActionShortcutItem(
    iconLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Column(
        modifier = modifier
            .testTag(testTag)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = iconLabel,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = iconLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimaryDark
        )
    }
}

fun maskIbanString(iban: String): String {
    // Input format: WBNK-TRXX-XXXX-XXXX-XXXX-XXXX-XX
    val parts = iban.split("-")
    if (parts.size < 7) return iban
    // Mask core blocks
    return "${parts[0]}-${parts[1]}-••••-••••-••••-••••-${parts[6]}"
}
