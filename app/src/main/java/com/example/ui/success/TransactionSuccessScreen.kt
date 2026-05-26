package com.example.ui.success

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekCreditGreen
import com.example.ui.theme.SleekDebitRed
import com.example.ui.theme.SleekNavyHeader
import com.example.ui.theme.SleekDigitalBlue
import com.example.ui.theme.SleekSecondaryBlue
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondarySlate
import com.example.ui.theme.GoldAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionSuccessScreen(
    senderIban: String,
    receiverIban: String,
    amount: Double,
    receiverName: String,
    description: String,
    timestamp: Long,
    transactionId: String,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Success animated ring ticks
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SleekCreditGreen.copy(alpha = 0.15f))
                    .testTag("success_checkmark_ring"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(SleekCreditGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TRANSACTION ROUTED SUCCESSFULLY",
                color = SleekCreditGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Simulated Cryptographical Ledger Cleared",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Digital Receipt Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("digital_receipt_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Header title receipt
                    Text(
                        text = "W BANK OFFICIAL LOGICAL RECEIPT",
                        color = SleekNavyHeader,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Amount block
                    Text(
                        text = "TOTAL VALUE DISPATCHED",
                        color = TextSecondarySlate,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    val formattedAmount = String.format(Locale.US, "$%,.2f", amount)
                    Text(
                        text = formattedAmount,
                        color = TextPrimaryDark,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("receipt_amount_text"),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "FEDERAL RESERVE STATUS: PROVISIONED",
                        color = SleekCreditGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(20.dp))

                    // Receipt Item details
                    ReceiptDetailRow(label = "Sender Account (Debit)", value = senderIban)
                    ReceiptDetailRow(label = "Receiver Account (Credit)", value = receiverIban)
                    ReceiptDetailRow(label = "Receiver Name", value = receiverName)
                    ReceiptDetailRow(label = "Reference / Description", value = description)
                    
                    val formattedDate = SimpleDateFormat("MMMM dd, yyyy - HH:mm:ss", Locale.US).format(Date(timestamp))
                    ReceiptDetailRow(label = "Ledger Timestamp", value = formattedDate)
                    ReceiptDetailRow(label = "Security Transaction ID", value = transactionId, isCode = true)

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Signature simulated note
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SleekSecondaryBlue.copy(alpha = 0.4f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Lock",
                            tint = SleekDigitalBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "This transaction was committed securely with instant execution on W Bank's simulated network ledger.",
                            fontSize = 10.sp,
                            color = TextSecondarySlate,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action button home
            Button(
                onClick = onNavigateHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("dismiss_receipt_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekDigitalBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "DISMISS & RETURN HOME",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun ReceiptDetailRow(
    label: String,
    value: String,
    isCode: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondarySlate,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            fontSize = if (isCode) 11.sp else 13.sp,
            fontFamily = if (isCode) FontFamily.Monospace else FontFamily.SansSerif,
            fontWeight = if (isCode) FontWeight.Medium else FontWeight.SemiBold,
            color = TextPrimaryDark,
            lineHeight = 18.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
