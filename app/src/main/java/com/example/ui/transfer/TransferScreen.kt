package com.example.ui.transfer

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TopBarProfileActions
import com.example.ui.theme.SleekNavyHeader
import com.example.ui.theme.SleekDigitalBlue
import com.example.ui.theme.SleekSecondaryBlue
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondarySlate
import com.example.ui.theme.GoldAccent
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    viewModel: TransferViewModel,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onNavigateToSuccess: (senderIban: String, receiverIban: String, amount: Double, name: String, desc: String, timestamp: Long, transId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is TransferUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is TransferUiEvent.NavigateToSuccess -> {
                    onNavigateToSuccess(
                        event.senderIban,
                        event.receiverIban,
                        event.amount,
                        event.receiverName,
                        event.description,
                        event.timestamp,
                        event.transactionId
                    )
                }
            }
        }
    }

    // List of pre-seeded accounts so users can quickly tap and test.
    val suggestReceivers = listOf(
        SuggestedReceiver("Elena Thorne", "WBNK-TR12-3456-7890-1234-5678-9012-34", "Executive Account"),
        SuggestedReceiver("W Bank Escrow", "WBNK-TR44-1111-2222-3333-4444-5555-66", "Escrow Service"),
        SuggestedReceiver("W Bank Savings", "WBNK-TR88-9999-8888-7777-6666-5555-44", "Savings Vault")
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("P2P SECURE TRANSFER", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, letterSpacing = 1.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("transfer_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TopBarProfileActions()
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .imePadding()
                    .padding(16.dp)
            ) {
                // Sender Card summary
                uiState.senderAccount?.let { account ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sender_account_card"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161618)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "SENDING FUNDS FROM PLATINUM HOLDING",
                                color = SleekDigitalBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Available Balance",
                                color = TextSecondarySlate,
                                fontSize = 11.sp
                            )
                            Text(
                                text = String.format(Locale.US, "$%,.2f", account.balance),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = account.iban,
                                color = TextSecondarySlate,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Suggestion Header
                Text(
                    text = "SANDBOX SUGGESTED RECEIVERS (TAP TO AUTOFILL)",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal Suggestion Cards
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    suggestReceivers.forEach { receiver ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onReceiverIbanChanged(receiver.iban)
                                    viewModel.onReceiverNameChanged(receiver.name)
                                    viewModel.onAmountChanged("750.0") // seed demo amount
                                }
                                .testTag("suggested_${receiver.name.replace(" ", "_")}"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161618)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(SleekDigitalBlue.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(receiver.name.take(1), color = SleekDigitalBlue, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(receiver.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(receiver.category, color = TextSecondarySlate, fontSize = 10.sp)
                                    }
                                }
                                Text("Select", color = SleekDigitalBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Form Action Panel
                Text(
                    text = "TRANSACTION REGISTRY DETAIL",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Destination IBAN
                OutlinedTextField(
                    value = uiState.receiverIban,
                    onValueChange = viewModel::onReceiverIbanChanged,
                    label = { Text("Destination Account IBAN") },
                    placeholder = { Text("WBNK-TRXX-XXXX-XXXX-XXXX-XXXX-XX") },
                    isError = uiState.receiverIbanError != null,
                    supportingText = { uiState.receiverIbanError?.let { Text(it) } ?: Text("Format: WBNK-TRXX-XXXX-XXXX-XXXX-XXXX-XX") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("receiver_iban_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekDigitalBlue,
                        cursorColor = SleekDigitalBlue,
                        focusedLabelColor = SleekDigitalBlue
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Receiver Name
                OutlinedTextField(
                    value = uiState.receiverName,
                    onValueChange = viewModel::onReceiverNameChanged,
                    label = { Text("Receiver Full Name") },
                    isError = uiState.receiverNameError != null,
                    supportingText = { uiState.receiverNameError?.let { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("receiver_name_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekDigitalBlue,
                        cursorColor = SleekDigitalBlue,
                        focusedLabelColor = SleekDigitalBlue
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Amount
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::onAmountChanged,
                    label = { Text("Transfer Ledger Amount (USD)") },
                    isError = uiState.amountError != null,
                    supportingText = { uiState.amountError?.let { Text(it) } },
                    prefix = { Text("$ ", color = SleekDigitalBlue, fontWeight = FontWeight.Bold) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transfer_amount_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekDigitalBlue,
                        cursorColor = SleekDigitalBlue,
                        focusedLabelColor = SleekDigitalBlue
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChanged,
                    label = { Text("Memo / Transfer Reference (Optional)") },
                    placeholder = { Text("P2P Money Transfer") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transfer_description_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekDigitalBlue,
                        cursorColor = SleekDigitalBlue,
                        focusedLabelColor = SleekDigitalBlue
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Transfer submission block
                Button(
                    onClick = viewModel::executeTransfer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("transfer_submit_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekDigitalBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send Icon")
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "EXECUTE SECURE WIRE TRANSFER",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        // Processing Loading HUD
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = SleekDigitalBlue,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Authenticating Ledger Transaction...",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Writing cryptographical blocks to simulation database...",
                        color = SleekDigitalBlue.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        }
    }
}

data class SuggestedReceiver(
    val name: String,
    val iban: String,
    val category: String
)
