package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MedicalTeal
import com.example.ui.viewmodel.AuditViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EhrIntegrationScreen(
    viewModel: AuditViewModel,
    onBack: () -> Unit
) {
    var serverUrl by remember { mutableStateOf("https://hapi.fhir.org/baseR4/") }
    var isLoading by remember { mutableStateOf(false) }
    var syncResult by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EHR Integration (FHIR)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MedicalTeal
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "SMART on FHIR Sync",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            
            Text(
                "Connect to your Electronic Health Record (EHR) system to automatically pull recent MedicationRequests into the audit register.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("FHIR Server Base URL") },
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MedicalTeal
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isLoading = true
                    syncResult = null
                    coroutineScope.launch {
                        val result = viewModel.syncFhirRecords(serverUrl)
                        isLoading = false
                        syncResult = result
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && serverUrl.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MedicalTeal)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Syncing with EHR...", color = Color.White)
                } else {
                    Text("PULL HEALTH RECORDS", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(visible = syncResult != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (syncResult?.contains("Error") == true) 
                            MaterialTheme.colorScheme.errorContainer 
                        else 
                            Color(0xFFE0F2F1)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (syncResult?.contains("Error") == true) Icons.Default.Warning else Icons.Default.Done,
                            contentDescription = null,
                            tint = if (syncResult?.contains("Error") == true) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MedicalTeal
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = syncResult ?: "",
                            color = if (syncResult?.contains("Error") == true) 
                                MaterialTheme.colorScheme.onErrorContainer 
                            else 
                                Color(0xFF004D40)
                        )
                    }
                }
            }
        }
    }
}
