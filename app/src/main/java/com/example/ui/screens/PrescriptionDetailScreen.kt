package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.Converters
import com.example.data.model.DrugItem
import com.example.data.model.PrescriptionEntity
import com.example.ui.components.AntibioticBadge
import com.example.ui.components.ScheduleH1Badge
import com.example.ui.components.ScheduleHBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CompliantGreen
import com.example.ui.theme.CompliantGreenBg
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.NonCompliantRed
import com.example.ui.theme.NonCompliantRedBg
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg
import com.example.ui.viewmodel.AuditViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PrescriptionDetailScreen(
    prescription: PrescriptionEntity,
    viewModel: AuditViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val converters = remember { Converters() }
    val drugList: List<DrugItem> = remember(prescription.drugsJson) {
        converters.toDrugList(prescription.drugsJson)
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Audit Record?") },
            text = { Text("Are you sure you want to remove prescription #${prescription.rxNumber} from the audit register? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deletePrescription(prescription.id)
                        onBack()
                    }
                ) {
                    Text("Delete", color = NonCompliantRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = prescription.rxNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${prescription.department} • ${prescription.dateString}",
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
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete prescription", tint = NonCompliantRed)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header status card
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
                            text = "Prescription Audit Dossier",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        StatusBadge(isCompliant = prescription.isCompliantOverall)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScoreCard(
                            title = "Completeness Score",
                            score = prescription.completenessScorePct,
                            target = "≥ 85%",
                            isPassing = prescription.completenessScorePct >= 85,
                            modifier = Modifier.weight(1f)
                        )
                        ScoreCard(
                            title = "Rationality Score",
                            score = prescription.rationalityScorePct,
                            target = "≥ 80%",
                            isPassing = prescription.rationalityScorePct >= 80,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            val interactionAlerts = remember(drugList) {
                com.example.data.model.DrugInteractionEngine.checkInteractions(drugList)
            }

            // Patient-Level Severe ADR & DDI Alerts
            if (interactionAlerts.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NonCompliantRedBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NonCompliantRed.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = NonCompliantRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PATIENT-LEVEL SEVERE ADR & DDI ALERTS (${interactionAlerts.size})",
                                fontWeight = FontWeight.Bold,
                                color = NonCompliantRed,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        interactionAlerts.forEach { alert ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${alert.drugA.uppercase()} + ${alert.drugB.uppercase()}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val badgeColor = when (alert.severity) {
                                            com.example.data.model.InteractionSeverity.CONTRAINDICATED -> NonCompliantRed
                                            com.example.data.model.InteractionSeverity.SEVERE -> WarningAmber
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                        Text(
                                            text = alert.severity.name,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeColor,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(badgeColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Mechanism: ${alert.mechanism}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Severe ADR: ${alert.clinicalConsequence}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NonCompliantRed
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Action: ${alert.recommendation}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Deficiencies Callout
            if (prescription.deficienciesList.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NonCompliantRedBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = NonCompliantRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Flagged Deficiencies & Non-Compliance:",
                                fontWeight = FontWeight.Bold,
                                color = NonCompliantRed,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        prescription.deficienciesList.split(";").forEach { item ->
                            Text(
                                text = "• ${item.trim()}",
                                fontSize = 12.sp,
                                color = NonCompliantRed,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            // Patient & Doctor Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Patient & Prescriber Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    DetailRow("Patient Name", prescription.patientName)
                    DetailRow("Age & Gender", "${prescription.patientAge} Years / ${prescription.patientGender}")
                    DetailRow("UHID / OPD No.", prescription.uhid)
                    DetailRow("Department", prescription.department)
                    DetailRow("Date of Prescription", prescription.dateString)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 6.dp))
                    DetailRow("Consultant Doctor", prescription.doctorName)
                    DetailRow("MCI / NMC Reg. No.", if (prescription.doctorRegNumber.isNotBlank()) prescription.doctorRegNumber else "NOT DOCUMENTED (NON-COMPLIANT)")
                    DetailRow("Doctor Signature", if (prescription.hasDoctorSignature) "Verified Present" else "MISSING (NON-COMPLIANT)")
                    DetailRow("Diagnosis Documented", if (prescription.diagnosis.isNotBlank()) prescription.diagnosis else "NOT DOCUMENTED (MANDATORY)")
                    if (prescription.icd10Code.isNotBlank()) {
                        DetailRow("ICD-10 Code", prescription.icd10Code)
                    }
                    DetailRow("Allergy Status", prescription.allergyDetails)
                    
                    if (prescription.drugInteractionsFound.isNotBlank() && prescription.drugInteractionsFound != "None") {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 6.dp))
                        Text("Clinical Insights", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        DetailRow("DDI Checker", prescription.drugInteractionsFound)
                    }
                    if (prescription.genericRecommendations.isNotBlank() && prescription.genericRecommendations != "None") {
                        DetailRow("Generic Alts.", prescription.genericRecommendations)
                    }
                }
            }

            // Completeness Checklist (NABH MOM.5)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "NABH Completeness Checklist (MOM.5 / COP)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ChecklistItem("Patient Name & Demographics", prescription.patientName.isNotBlank() && prescription.patientAge > 0)
                    ChecklistItem("UHID / Hospital Identifier", prescription.uhid.isNotBlank())
                    ChecklistItem("Prescription Date Recorded", prescription.dateString.isNotBlank())
                    ChecklistItem("Provisional / Confirmed Diagnosis", prescription.diagnosis.isNotBlank())
                    ChecklistItem("Allergy Status Documented (MOM.5)", prescription.allergyStatusDocumented)
                    ChecklistItem("Doctor NMC/SMC Registration Number", prescription.doctorRegNumber.isNotBlank())
                    ChecklistItem("Doctor Signature Validated", prescription.hasDoctorSignature)
                    ChecklistItem("Legible Handwriting / Typed", prescription.legibleHandwritingOrTyped)
                }
            }

            // Prescribed Medicines Table
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
                            text = "Prescribed Medicines (${drugList.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${prescription.genericCount} Generic • ${prescription.antibioticCount} Antibiotic",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    drugList.forEachIndexed { idx, drug ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${idx + 1}. ${drug.brandName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    if (drug.isGeneric) {
                                        Text(
                                            text = "GENERIC",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CompliantGreen
                                        )
                                    }
                                }

                                if (drug.genericName != drug.brandName) {
                                    Text(
                                        text = "INN: ${drug.genericName}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }

                                Text(
                                    text = "Dose: ${drug.dose} • Route: ${drug.route} • Freq: ${drug.frequency} for ${drug.durationDays} days",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                )

                                if (drug.instructions.isNotBlank()) {
                                    Text(
                                        text = "Instructions: ${drug.instructions}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                FlowRow(
                                    modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (drug.isScheduleH1) ScheduleH1Badge(1)
                                    if (drug.isScheduleH) ScheduleHBadge()
                                    if (drug.isAntibiotic) AntibioticBadge(drug.antibioticClass)
                                    if (drug.isEdl) {
                                        Text(
                                            text = "EDL / NLEM",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CompliantGreen,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(CompliantGreenBg)
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (drug.isIrrationalFdc) {
                                        Text(
                                            text = "Banned FDC (DCGI 26A)",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NonCompliantRed,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(NonCompliantRedBg)
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tamper-Evident SHA-256 Ledger Seal
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = CompliantGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tamper-Evident SHA-256 Verification",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Auditor: ${prescription.auditorName} (${prescription.auditorRole})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Timestamp: ${SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).format(Date(prescription.auditTimestamp))}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "DocHash: ${prescription.docHash}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "PrevHash: ${prescription.prevHash.take(24)}...",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.8f)
        )
    }
}

@Composable
fun ChecklistItem(label: String, isPassed: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isPassed) MaterialTheme.colorScheme.onSurface else NonCompliantRed
        )

        if (isPassed) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CompliantGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "PASS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CompliantGreen)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Cancel, contentDescription = null, tint = NonCompliantRed, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "DEFICIENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NonCompliantRed)
            }
        }
    }
}
