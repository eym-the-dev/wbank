package com.example.ui.applications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TopBarProfileActions
import com.example.ui.theme.SleekCreditGreen
import com.example.ui.theme.SleekDigitalBlue
import com.example.ui.theme.SleekNavyHeader
import com.example.ui.theme.SleekSecondaryBlue
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondarySlate
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationsScreen(
    viewModel: ApplicationsViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Slider parameters depending on selected type
    var loanAmount by remember { mutableStateOf(10000.0) }
    var maturityMonths by remember { mutableStateOf(12) }

    // Constants based on selected type
    val rateAndRanges = remember(uiState.selectedLoanType) {
        when (uiState.selectedLoanType) {
            "Consumer" -> LoanTypeConfig(
                rate = 2.15,
                minAmount = 1000.0,
                maxAmount = 50000.0,
                defaultAmount = 10000.0,
                minMaturity = 6,
                maxMaturity = 36,
                defaultMaturity = 12
            )
            "Vehicle" -> LoanTypeConfig(
                rate = 1.89,
                minAmount = 5000.0,
                maxAmount = 150000.0,
                defaultAmount = 40000.0,
                minMaturity = 12,
                maxMaturity = 60,
                defaultMaturity = 24
            )
            else -> LoanTypeConfig(
                rate = 1.59,
                minAmount = 50000.0,
                maxAmount = 1000000.0,
                defaultAmount = 200000.0,
                minMaturity = 24,
                maxMaturity = 120,
                defaultMaturity = 60
            )
        }
    }

    // Reset weights on tab change
    LaunchedEffect(uiState.selectedLoanType) {
        loanAmount = rateAndRanges.defaultAmount
        maturityMonths = rateAndRanges.defaultMaturity
    }

    // Trigger instant recalculation
    LaunchedEffect(key1 = loanAmount, key2 = maturityMonths, key3 = uiState.selectedLoanType) {
        viewModel.calculateLoan(loanAmount, maturityMonths, rateAndRanges.rate)
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is ApplicationsUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                ApplicationsUiEvent.LoanApproved -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "W PORTFOLIO CREDIT ENG",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 1.2.sp,
                        color = Color.White
                    )
                },
                actions = {
                    TopBarProfileActions()
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        modifier = modifier.testTag("applications_screen_root")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Loan Selection Hub Tab headers
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF161618))
                        .padding(4.dp)
                ) {
                    listOf("Consumer", "Vehicle", "Housing").forEach { type ->
                        val isSelected = uiState.selectedLoanType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SleekDigitalBlue else Color.Transparent)
                                .clickable { viewModel.selectLoanType(type) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TextSecondarySlate,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Calculation Panel Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161618)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CALCULATOR PARAMETERS",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                            Badge(containerColor = Color(0xFF28282B), contentColor = GoldAccent) {
                                Text(
                                    text = "${rateAndRanges.rate}% Monthly Rate",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Amount section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Desired Credits", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                text = String.format(Locale.US, "$%,.0f", loanAmount),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SleekDigitalBlue
                            )
                        }
                        Slider(
                            value = loanAmount.toFloat(),
                            onValueChange = { loanAmount = it.toDouble() },
                            valueRange = rateAndRanges.minAmount.toFloat()..rateAndRanges.maxAmount.toFloat(),
                            modifier = Modifier.fillMaxWidth().testTag("loan_amount_slider")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Maturity section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Payment Term", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$maturityMonths Months",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SleekDigitalBlue
                            )
                        }
                        Slider(
                            value = maturityMonths.toFloat(),
                            onValueChange = { maturityMonths = it.toInt() },
                            valueRange = rateAndRanges.minMaturity.toFloat()..rateAndRanges.maxMaturity.toFloat(),
                            steps = rateAndRanges.maxMaturity - rateAndRanges.minMaturity - 1,
                            modifier = Modifier.fillMaxWidth().testTag("loan_maturity_slider")
                        )
                    }
                }
            }

            // Calculations Result Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekNavyHeader),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "ESTIMATED PLAN BREAKDOWN",
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format(Locale.US, "$%,.2f / mo", uiState.calculatedMonthlyPayment),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Interest Paid", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(
                                    text = String.format(Locale.US, "$%,.2f", uiState.calculatedTotalInterest),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Return Sum", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(
                                    text = String.format(Locale.US, "$%,.2f", uiState.calculatedTotalRepayment),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.applyForLoan(
                                    principal = loanAmount,
                                    interestRate = rateAndRanges.rate,
                                    maturityMonths = maturityMonths,
                                    monthlyPayment = uiState.calculatedMonthlyPayment
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = SleekNavyHeader
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_loan_button")
                        ) {
                            Text("Fast Approval & Disbursal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Active Loans List
            item {
                Text(
                    text = "ACTIVE INSTANT LOANS",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.2.sp
                )
            }

            if (uiState.activeLoans.isEmpty()) {
                item {
                    Text(
                        text = "No active credits on database. Apply above to generate instantly approved funds.",
                        color = TextSecondarySlate,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                }
            } else {
                items(uiState.activeLoans) { loan ->
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                    val dateFormatted = dateFormat.format(Date(loan.timestamp))

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("active_loan_item"),
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (loan.type) {
                                            "Housing" -> Icons.Default.Home
                                            "Vehicle" -> Icons.Default.Star
                                            else -> Icons.Default.Info
                                        },
                                        contentDescription = "Loan type",
                                        tint = SleekDigitalBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${loan.type} Credit",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                                Badge(
                                    containerColor = SleekCreditGreen.copy(alpha = 0.15f),
                                    contentColor = SleekCreditGreen
                                ) {
                                    Text(
                                        text = loan.status,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Disbursed Amount", color = TextSecondarySlate, fontSize = 11.sp)
                                    Text(
                                        text = String.format(Locale.US, "$%,.2f", loan.principalAmount),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Monthly Repayment", color = TextSecondarySlate, fontSize = 11.sp)
                                    Text(
                                        text = String.format(Locale.US, "$%,.2f/mo", loan.monthlyPayment),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFF28282B))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Term: ${loan.maturityMonths} months | Rate: ${loan.interestRate}%",
                                    fontSize = 11.sp,
                                    color = TextSecondarySlate
                                )
                                Text(
                                    text = dateFormatted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondarySlate
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class LoanTypeConfig(
    val rate: Double,
    val minAmount: Double,
    val maxAmount: Double,
    val defaultAmount: Double,
    val minMaturity: Int,
    val maxMaturity: Int,
    val defaultMaturity: Int
)
