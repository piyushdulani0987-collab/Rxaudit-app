package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AuditIndicators
import com.example.data.model.PrescriptionEntity
import com.example.data.model.SampleSizeEstimate
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CompliantGreen
import com.example.ui.theme.CompliantGreenBg
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.NonCompliantRed
import com.example.ui.theme.NonCompliantRedBg
import com.example.ui.theme.ScheduleH1Orange
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg
import com.example.ui.viewmodel.AuditViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AuditReportScreen(
    viewModel: AuditViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val indicators by viewModel.indicators.collectAsStateWithLifecycle()
    val allPrescriptions by viewModel.allPrescriptions.collectAsStateWithLifecycle()
    val facilityName by viewModel.facilityName.collectAsStateWithLifecycle()
    val auditorName by viewModel.auditorName.collectAsStateWithLifecycle()
    val auditorRole by viewModel.auditorRole.collectAsStateWithLifecycle()
    val sampleSize by viewModel.sampleSizeEstimate.collectAsStateWithLifecycle()
    val monthlyFootfall by viewModel.monthlyFootfall.collectAsStateWithLifecycle()

    var footfallSlider by remember(monthlyFootfall) { mutableFloatStateOf(monthlyFootfall.toFloat()) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "One-Tap Audit Report",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "NABH MOM.5 & WHO Core Register",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val csv = viewModel.getExportableCsv()
                            shareText(context, csv, "NABH_Prescription_Audit_Report.csv")
                        },
                        modifier = Modifier.testTag("share_csv_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share CSV Register")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Report Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HOSPITAL CLINICAL AUDIT REPORT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = facilityName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Lead Auditor: $auditorName ($auditorRole)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CompliantGreenBg)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = CompliantGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cryptographic Audit Ledger: ${indicators.verifiedChainLength}/${indicators.totalPrescriptions} verified SHA-256 blocks",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CompliantGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // One-Tap Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val csv = viewModel.getExportableCsv()
                        shareText(context, csv, "NABH_Prescription_Audit.csv")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_excel_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Excel/CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val summary = viewModel.getExecutiveSummary()
                        clipboardManager.setText(AnnotatedString(summary))
                        Toast.makeText(context, "Executive summary copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("copy_summary_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Summary", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sample Size Calculator (Cochran 95% CI)
            SampleSizeCalculatorCard(
                sampleSize = sampleSize,
                footfall = footfallSlider.toInt(),
                onFootfallChange = {
                    footfallSlider = it
                    viewModel.setMonthlyFootfall(it.toInt())
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // WHO Core Prescribing Indicators Table
            WhoIndicatorsTableCard(indicators = indicators)

            Spacer(modifier = Modifier.height(16.dp))

            // NABH MOM.5 & COP Completeness Table
            NabhCompletenessCard(indicators = indicators)

            Spacer(modifier = Modifier.height(16.dp))

            // Indian Drug Regulatory Analysis (Schedule H/H1, FDC, Polypharmacy)
            RegulatoryComplianceCard(indicators = indicators)

            Spacer(modifier = Modifier.height(16.dp))

            // Non-Compliant Flagged Prescriptions Detail
            val flaggedPrescriptions = allPrescriptions.filter { !it.isCompliantOverall }
            FlaggedPrescriptionsCard(flaggedList = flaggedPrescriptions)
        }
    }
}

@Composable
fun SampleSizeCalculatorCard(
    sampleSize: SampleSizeEstimate,
    footfall: Int,
    onFootfallChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sample Size Calculator (95% CI)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                val statusColor = if (sampleSize.isAdequate) CompliantGreen else WarningAmber
                Text(
                    text = if (sampleSize.isAdequate) "Sample Adequate" else "Sample Sub-Optimal",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cochran Formula: Margin of Error: 5% • Confidence Level: 95%",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Monthly OPD / Footfall: $footfall prescriptions", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Target: ${sampleSize.recommendedSampleSize} Rx", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Slider(
                value = footfall.toFloat(),
                onValueChange = onFootfallChange,
                valueRange = 200f..5000f,
                steps = 48,
                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Current Audit Sample: ${sampleSize.currentSampleSize} prescriptions", fontSize = 11.sp)
                Text(
                    text = "${"%.1f".format((sampleSize.currentSampleSize.toDouble() / sampleSize.recommendedSampleSize) * 100)}% of target",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (sampleSize.isAdequate) CompliantGreen else WarningAmber
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = sampleSize.standardNote,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
fun WhoIndicatorsTableCard(indicators: AuditIndicators) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "WHO Core Prescribing Indicators",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Standard Reference: WHO/DAP/93.1 Core Drug Use Indicators",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            AuditTableRow(
                indicator = "Average drugs per prescription",
                observed = "%.2f".format(indicators.avgDrugsPerPrescription),
                reference = "1.6 - 1.8",
                isCompliant = indicators.avgDrugsPerPrescription in 1.4..2.5
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

            AuditTableRow(
                indicator = "% Prescribed by generic name",
                observed = "%.1f%%".format(indicators.genericPrescribingPct),
                reference = "100%",
                isCompliant = indicators.genericPrescribingPct >= 70.0
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

            AuditTableRow(
                indicator = "% Prescriptions with an antibiotic",
                observed = "%.1f%%".format(indicators.antibioticEncountersPct),
                reference = "20.0 - 26.8%",
                isCompliant = indicators.antibioticEncountersPct <= 35.0
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

            AuditTableRow(
                indicator = "% Prescriptions with an injection",
                observed = "%.1f%%".format(indicators.injectionEncountersPct),
                reference = "13.4 - 24.1%",
                isCompliant = indicators.injectionEncountersPct <= 30.0
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

            AuditTableRow(
                indicator = "% Prescribed from EDL / NLEM",
                observed = "%.1f%%".format(indicators.edlPrescribingPct),
                reference = "100%",
                isCompliant = indicators.edlPrescribingPct >= 80.0
            )
        }
    }
}

@Composable
fun NabhCompletenessCard(indicators: AuditIndicators) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "NABH Prescription Completeness (MOM.5 / COP)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            CompletenessBarRow("Allergy Status Documented", indicators.allergyDocumentationPct, target = "100% Mandatory")
            Spacer(modifier = Modifier.height(6.dp))
            CompletenessBarRow("Doctor NMC/SMC Reg No Recorded", indicators.doctorRegDocumentedPct, target = "100% Mandatory")
            Spacer(modifier = Modifier.height(6.dp))
            CompletenessBarRow("Provisional/Confirmed Diagnosis", indicators.diagnosisSpecifiedPct, target = "≥ 90%")
            Spacer(modifier = Modifier.height(6.dp))
            CompletenessBarRow("Doctor Signature Present", indicators.signaturePresentPct, target = "100% Mandatory")
            Spacer(modifier = Modifier.height(6.dp))
            CompletenessBarRow("Legible Handwriting / Capital Letters", indicators.legibilityCompliancePct, target = "100% (MCI)")
        }
    }
}

@Composable
fun CompletenessBarRow(label: String, pct: Double, target: String) {
    val isGood = pct >= 85.0
    val color = if (isGood) CompliantGreen else WarningAmber

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(text = "${"%.1f".format(pct)}% (Ref: $target)", fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { (pct / 100.0).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

@Composable
fun RegulatoryComplianceCard(indicators: AuditIndicators) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Indian Drug Regulatory Compliance (CDSCO & Rules)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RegulatoryStatBox(
                    title = "Schedule H1",
                    count = indicators.scheduleH1EncountersCount,
                    subtitle = "Red Line Register",
                    color = ScheduleH1Orange,
                    modifier = Modifier.weight(1f)
                )
                RegulatoryStatBox(
                    title = "Schedule H (Rx)",
                    count = indicators.scheduleHEncountersCount,
                    subtitle = "Restricted Rx",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RegulatoryStatBox(
                    title = "Polypharmacy",
                    count = indicators.polypharmacyEncountersCount,
                    subtitle = "≥ 5 Drugs Prescribed",
                    color = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
                RegulatoryStatBox(
                    title = "Irrational FDCs",
                    count = indicators.irrationalFdcCount,
                    subtitle = "CDSCO Flagged Combinations",
                    color = if (indicators.irrationalFdcCount > 0) NonCompliantRed else CompliantGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun RegulatoryStatBox(title: String, count: Int, subtitle: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = "$count", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = subtitle, fontSize = 9.sp, color = color.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun FlaggedPrescriptionsCard(flaggedList: List<PrescriptionEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Non-Compliant Prescriptions Flagged (${flaggedList.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (flaggedList.isEmpty()) CompliantGreen else NonCompliantRed
                )
                if (flaggedList.isEmpty()) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CompliantGreen, modifier = Modifier.size(18.dp))
                }
            }

            if (flaggedList.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No non-compliant prescriptions found in this audit cycle. 100% adherence to NABH & WHO benchmarks.",
                    fontSize = 11.sp,
                    color = CompliantGreen
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                flaggedList.forEach { rx ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = NonCompliantRedBg.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${rx.rxNumber} • ${rx.patientName} (${rx.department})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = NonCompliantRed
                                )
                                Text(
                                    text = "Comp: ${rx.completenessScorePct}% | Rat: ${rx.rationalityScorePct}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NonCompliantRed
                                )
                            }
                            Text(
                                text = "Doctor: ${rx.doctorName} [${if (rx.doctorRegNumber.isNotBlank()) rx.doctorRegNumber else "Reg No Missing"}]",
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Deficiencies: ${rx.deficienciesList}",
                                fontSize = 10.sp,
                                color = NonCompliantRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuditTableRow(
    indicator: String,
    observed: String,
    reference: String,
    isCompliant: Boolean
) {
    val statusColor = if (isCompliant) CompliantGreen else WarningAmber
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.8f)) {
            Text(text = indicator, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(text = "Target: $reference", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }

        Row(
            modifier = Modifier.weight(1.2f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = observed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isCompliant) "OK" else "VAR",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}

private fun shareText(context: Context, text: String, title: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, title)
    context.startActivity(shareIntent)
}
