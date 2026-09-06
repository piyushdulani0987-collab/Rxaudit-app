package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AuditReportScreen
import com.example.ui.screens.AuditTrailScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PrescriptionDetailScreen
import com.example.ui.screens.PrescriptionScanScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuditViewModel

object NavDestinations {
    const val DASHBOARD = "dashboard"
    const val SCAN = "scan"
    const val REPORT = "report"
    const val TRAIL = "trail"
    const val DETAIL = "detail"
}

class MainActivity : ComponentActivity() {

    private val viewModel: AuditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: AuditViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val selectedPrescription by viewModel.selectedPrescription.collectAsStateWithLifecycle()

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavDestinations.DASHBOARD,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavDestinations.DASHBOARD) {
                DashboardScreen(
                    viewModel = viewModel,
                    onOpenScan = { navController.navigate(NavDestinations.SCAN) },
                    onOpenReport = { navController.navigate(NavDestinations.REPORT) },
                    onOpenTrail = { navController.navigate(NavDestinations.TRAIL) },
                    onSelectPrescription = { rx ->
                        viewModel.selectPrescription(rx)
                        navController.navigate(NavDestinations.DETAIL)
                    }
                )
            }

            composable(NavDestinations.SCAN) {
                PrescriptionScanScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavDestinations.REPORT) {
                AuditReportScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavDestinations.TRAIL) {
                AuditTrailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavDestinations.DETAIL) {
                if (selectedPrescription != null) {
                    PrescriptionDetailScreen(
                        prescription = selectedPrescription!!,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}
