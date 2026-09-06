package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AuditIndicators
import com.example.data.model.PrescriptionEntity
import com.example.ui.components.PrescriptionItemCard
import com.example.ui.components.WhoMetricCard
import com.example.ui.theme.CompliantGreen
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.MedicalTealDark
import com.example.ui.theme.NonCompliantRed
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.AuditViewModel
import com.example.ui.viewmodel.FilterOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AuditViewModel,
    onOpenScan: () -> Unit,
    onOpenReport: () -> Unit,
    onOpenTrail: () -> Unit,
    onSelectPrescription: (PrescriptionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val indicators by viewModel.indicators.collectAsStateWithLifecycle()
    val filteredList by viewModel.filteredPrescriptions.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val selectedDepartment by viewModel.selectedDepartment.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val facilityName by viewModel.facilityName.collectAsStateWithLifecycle()

    val departments = listOf("All", "General Medicine", "Paediatrics", "Emergency/Casualty", "Surgery", "Orthopaedics", "Community Pharmacy")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Prescription Audit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "NABH 5th Ed. & WHO Core Indicators",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenTrail,
                        modifier = Modifier.testTag("audit_trail_button")
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = "Audit Trail & Hash Chain",
                            tint = if (indicators.isChainValid) CompliantGreen else WarningAmber
                        )
                    }
                    IconButton(
                        onClick = onOpenReport,
                        modifier = Modifier.testTag("audit_report_button")
                    ) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = "One-Tap Full Audit Report",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.resetToClinicalSamples() },
                        modifier = Modifier.testTag("reset_samples_button")
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reset Clinical Sample Data",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenScan,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("1-Tap Audit Rx", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("scan_prescription_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // 1. Facility & Audit Summary Banner
            item {
                HospitalAuditHeaderBanner(
                    facilityName = facilityName,
                    indicators = indicators,
                    onOpenReport = onOpenReport,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // 2. WHO Core Prescribing Indicators Section
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WHO Core Prescribing Indicators",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ref: WHO/DAP/93.1",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2x2 or row grid for WHO Metrics
                    Row(modifier = Modifier.fillMaxWidth()) {
                        WhoMetricCard(
                            title = "Avg Drugs / Rx",
                            value = "%.2f".format(indicators.avgDrugsPerPrescription),
                            targetRange = "1.6 - 1.8",
                            isCompliant = indicators.avgDrugsPerPrescription in 1.4..2.5,
                            progress = (indicators.avgDrugsPerPrescription / 4.0).toFloat(),
                            statusText = if (indicators.avgDrugsPerPrescription in 1.4..2.5) "Standard" else "High Polypharmacy",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("who_metric_avg_drugs")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        WhoMetricCard(
                            title = "% Generic Prescribed",
                            value = "%.1f".format(indicators.genericPrescribingPct),
                            unit = "%",
                            targetRange = "100%",
                            isCompliant = indicators.genericPrescribingPct >= 70.0,
                            progress = (indicators.genericPrescribingPct / 100.0).toFloat(),
                            statusText = if (indicators.genericPrescribingPct >= 70.0) "Compliant" else "Low Generic",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("who_metric_generic")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        WhoMetricCard(
                            title = "% Antibiotics",
                            value = "%.1f".format(indicators.antibioticEncountersPct),
                            unit = "%",
                            targetRange = "20 - 26.8%",
                            isCompliant = indicators.antibioticEncountersPct <= 35.0,
                            progress = (indicators.antibioticEncountersPct / 60.0).toFloat(),
                            statusText = if (indicators.antibioticEncountersPct <= 27.0) "Optimal" else "Overuse Alert",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("who_metric_antibiotics")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        WhoMetricCard(
                            title = "% Injections",
                            value = "%.1f".format(indicators.injectionEncountersPct),
                            unit = "%",
                            targetRange = "13.4 - 24.1%",
                            isCompliant = indicators.injectionEncountersPct <= 30.0,
                            progress = (indicators.injectionEncountersPct / 50.0).toFloat(),
                            statusText = if (indicators.injectionEncountersPct <= 25.0) "Standard" else "High Injection",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("who_metric_injections")
                        )
                    }
                }
            }

            // 3. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("search_prescription_input"),
                    placeholder = { Text("Search by patient, doctor, UHID, or diagnosis...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // 4. Department Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    departments.forEach { dept ->
                        FilterChip(
                            selected = selectedDepartment == dept,
                            onClick = { viewModel.setDepartment(dept) },
                            label = { Text(dept, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // 5. Audit Criteria Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterOption.entries.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { viewModel.setFilter(filter) },
                            label = { Text(filter.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (filter == FilterOption.NON_COMPLIANT) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = if (filter == FilterOption.NON_COMPLIANT) NonCompliantRed else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // 6. Section Header with count
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Audited Prescriptions (${filteredList.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${indicators.compliantCount} Compliant • ${indicators.nonCompliantCount} Flagged",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // 7. Prescription Cards List
            if (filteredList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.FactCheck,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No prescriptions match current filters",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try changing the filter or click '1-Tap Audit Rx' to scan a new prescription.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { rx ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        PrescriptionItemCard(
                            prescription = rx,
                            onClick = { onSelectPrescription(rx) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HospitalAuditHeaderBanner(
    facilityName: String,
    indicators: AuditIndicators,
    onOpenReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(MedicalTealDark, MedicalTeal)
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NABH ACCREDITED AUDIT CELL",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F),
                            letterSpacing = 1.sp
                        )
                    }

                    // Hash validity chip
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (indicators.isChainValid) CompliantGreen else WarningAmber)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (indicators.isChainValid) "CHAIN VERIFIED" else "TAMPER DETECTED",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = facilityName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${indicators.totalPrescriptions}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Total Audited Rx",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Column {
                        Text(
                            text = "%.1f%%".format(indicators.complianceRatePct),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (indicators.complianceRatePct >= 75.0) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
                        )
                        Text(
                            text = "NABH Compliance",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Column {
                        Text(
                            text = "%.1f%%".format(indicators.completenessAvgPct),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Avg Completeness",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Retention disclaimer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.HistoryEdu,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mandatory 6-Year Legal Audit Retention Active",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }

                    Text(
                        text = "View Report →",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF80DEEA),
                        fontSize = 10.sp,
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).padding(2.dp)
                    )
                }
            }
        }
    }
}
