package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.AuthViewModel
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.ledger.LedgerScreen
import com.example.ui.ledger.LedgerViewModel
import com.example.ui.success.TransactionSuccessScreen
import com.example.ui.transfer.TransferScreen
import com.example.ui.transfer.TransferViewModel
import com.example.ui.investment.InvestmentScreen
import com.example.ui.investment.InvestmentViewModel
import com.example.ui.applications.ApplicationsScreen
import com.example.ui.applications.ApplicationsViewModel
import com.example.ui.theme.MyApplicationTheme
import java.net.URLDecoder
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }

                // Dynamic bottom bar appearance depending on active login state
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                // Get repository reference from custom Application context
                val app = application as WBankApplication
                val repository = app.repository

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    bottomBar = {
                        if (currentRoute != null && currentRoute != "auth" && !currentRoute.startsWith("success")) {
                            NavigationBar {
                                val items = listOf(
                                    BottomNavItem("Home", "dashboard", Icons.Default.Home),
                                    BottomNavItem("Investments", "investments", Icons.Default.Star),
                                    BottomNavItem("Transfers", "transfer", Icons.Default.Send),
                                    BottomNavItem("Applications", "applications", Icons.Default.Info)
                                )
                                items.forEach { item ->
                                    val isSelected = currentRoute == item.route
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            if (!isSelected) {
                                                navController.navigate(item.route) {
                                                    popUpTo("dashboard") { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                                        label = { Text(item.label) }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "auth",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Screen 1: Secure Auth & Onboarding
                        composable("auth") {
                            val authViewModel: AuthViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return AuthViewModel(repository) as T
                                    }
                                }
                            )
                            AuthScreen(
                                viewModel = authViewModel,
                                snackbarHostState = snackbarHostState,
                                onNavigateToDashboard = {
                                    navController.navigate("dashboard") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Screen 2: Dynamic Core Dashboard
                        composable("dashboard") {
                            val dashboardViewModel: DashboardViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return DashboardViewModel(repository) as T
                                    }
                                }
                            )
                            DashboardScreen(
                                viewModel = dashboardViewModel,
                                snackbarHostState = snackbarHostState,
                                onNavigateToTransfer = {
                                    navController.navigate("transfer")
                                },
                                onNavigateToLedger = {
                                    navController.navigate("ledger")
                                },
                                onNavigateToAuth = {
                                    navController.navigate("auth") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Screen 3: P2P Money Transfer Location
                        composable("transfer") {
                            val transferViewModel: TransferViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return TransferViewModel(repository) as T
                                    }
                                }
                            )
                            TransferScreen(
                                viewModel = transferViewModel,
                                snackbarHostState = snackbarHostState,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToSuccess = { sender, rcvr, amt, name, desc, time, id ->
                                    val encName = name.safeUrlEncode()
                                    val encDesc = desc.safeUrlEncode()
                                    navController.navigate("success/$sender/$rcvr/$amt/$encName/$encDesc/$time/$id") {
                                        popUpTo("dashboard") { inclusive = false }
                                    }
                                }
                            )
                        }

                        // Screen 4: Advanced Account Ledger
                        composable("ledger") {
                            val ledgerViewModel: LedgerViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return LedgerViewModel(repository) as T
                                    }
                                }
                            )
                            LedgerScreen(
                                viewModel = ledgerViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // Added Screen 5: Investment Portfolio
                        composable("investments") {
                            val investmentViewModel: InvestmentViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return InvestmentViewModel(repository) as T
                                    }
                                }
                            )
                            InvestmentScreen(
                                viewModel = investmentViewModel,
                                snackbarHostState = snackbarHostState
                            )
                        }

                        // Added Screen 6: Applications & Loan Kalkulator
                        composable("applications") {
                            val applicationsViewModel: ApplicationsViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return ApplicationsViewModel(repository) as T
                                    }
                                }
                            )
                            ApplicationsScreen(
                                viewModel = applicationsViewModel,
                                snackbarHostState = snackbarHostState
                            )
                        }

                        // Dedicated Receipt success display route
                        composable(
                            route = "success/{sender}/{receiver}/{amount}/{name}/{desc}/{time}/{id}",
                            arguments = listOf(
                                navArgument("sender") { type = NavType.StringType },
                                navArgument("receiver") { type = NavType.StringType },
                                navArgument("amount") { type = NavType.FloatType },
                                navArgument("name") { type = NavType.StringType },
                                navArgument("desc") { type = NavType.StringType },
                                navArgument("time") { type = NavType.LongType },
                                navArgument("id") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val sender = backStackEntry.arguments?.getString("sender") ?: ""
                            val receiver = backStackEntry.arguments?.getString("receiver") ?: ""
                            val amount = backStackEntry.arguments?.getFloat("amount")?.toDouble() ?: 0.0
                            val encodedName = backStackEntry.arguments?.getString("name") ?: ""
                            val encodedDesc = backStackEntry.arguments?.getString("desc") ?: ""
                            val time = backStackEntry.arguments?.getLong("time") ?: 0L
                            val id = backStackEntry.arguments?.getString("id") ?: ""

                            val decName = encodedName.safeUrlDecode()
                            val decDesc = encodedDesc.safeUrlDecode()

                            TransactionSuccessScreen(
                                senderIban = sender,
                                receiverIban = receiver,
                                amount = amount,
                                receiverName = decName,
                                description = decDesc,
                                timestamp = time,
                                transactionId = id,
                                onNavigateHome = {
                                    navController.navigate("dashboard") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Custom URL coder extensions to avoid route navigation pattern crash errors
fun String.safeUrlEncode(): String {
    return try {
        URLEncoder.encode(this, "UTF-8")
    } catch (e: Exception) {
        this
    }
}

fun String.safeUrlDecode(): String {
    return try {
        URLDecoder.decode(this, "UTF-8")
    } catch (e: Exception) {
        this
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)

