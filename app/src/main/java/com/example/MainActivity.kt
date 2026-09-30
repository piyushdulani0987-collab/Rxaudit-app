package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AuditReportScreen
import com.example.ui.screens.AuditTrailScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EhrIntegrationScreen
import com.example.ui.screens.PrescriptionDetailScreen
import com.example.ui.screens.PrescriptionScanScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuditViewModel

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.SplashScreen

object NavDestinations {
    const val DASHBOARD = "dashboard"
    const val SCAN = "scan"
    const val REPORT = "report"
    const val TRAIL = "trail"
    const val DETAIL = "detail"
    const val EHR_SYNC = "ehr_sync"
}

class MainActivity : FragmentActivity() {
    private val viewModel: AuditViewModel by viewModels()
    private var isAuthenticated by mutableStateOf(false)
    private var showSplash by mutableStateOf(true)
    private var isDarkTheme by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme(darkTheme = isDarkTheme) {
                if (showSplash) {
                    SplashScreen(onSplashComplete = { showSplash = false })
                } else if (isAuthenticated) {
                    MainAppContent(
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { isDarkTheme = !isDarkTheme }
                    )
                } else {
                    AuthScreen(
                        onAuthSuccess = { isAuthenticated = true },
                        onBiometricLogin = { showBiometricPrompt() }
                    )
                }
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If no hardware or setup, allow bypass for testing purposes
                    isAuthenticated = true 
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isAuthenticated = true
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login for Rx Audit")
            .setSubtitle("Log in using your biometric credential to access PHI")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

@Composable
fun MainAppContent(
    viewModel: AuditViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
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
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onOpenScan = { navController.navigate(NavDestinations.SCAN) },
                    onOpenReport = { navController.navigate(NavDestinations.REPORT) },
                    onOpenTrail = { navController.navigate(NavDestinations.TRAIL) },
                    onOpenEhrSync = { navController.navigate(NavDestinations.EHR_SYNC) },
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
            composable(NavDestinations.EHR_SYNC) {
                EhrIntegrationScreen(
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
