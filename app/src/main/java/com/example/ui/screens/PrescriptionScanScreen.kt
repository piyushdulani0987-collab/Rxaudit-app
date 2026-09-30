package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.DrugItem
import com.example.domain.AuditRulesEngine
import com.example.domain.ParsedPrescription
import com.example.domain.PrescriptionOcrParser
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PrescriptionScanScreen(
    viewModel: AuditViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val pendingParsed by viewModel.pendingParsedPrescription.collectAsStateWithLifecycle()

    val rxTextInput by viewModel.rxTextInput.collectAsStateWithLifecycle()
    val selectedImageUri by viewModel.selectedImageUri.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        viewModel.setSelectedImageUri(uri)
        if (uri != null) {
            viewModel.setRxTextInput("Prescription image attached for Multimodal AI OCR.")
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val file = java.io.File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
                file.outputStream().use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, out)
                }
                viewModel.setSelectedImageUri(android.net.Uri.fromFile(file))
                viewModel.setRxTextInput("Prescription image attached for Multimodal AI OCR.")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "1-Tap Prescription Audit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // If we have a pending parsed audit, show the instant review and commit view
            if (pendingParsed != null) {
                AuditResultReviewSection(
                    parsed = pendingParsed!!,
                    imageUri = selectedImageUri?.toString(),
                    onCommit = {
                        viewModel.commitAudit(pendingParsed!!, selectedImageUri?.toString())
                        onBack()
                    },
                    onDiscard = {
                        viewModel.clearPendingParsed()
                        viewModel.setSelectedImageUri(null)
                        viewModel.setRxTextInput("")
                    }
                )
            } else {
                // Presets Section
                Text(
                    text = "Quick Sample Clinical Cases (1-Tap Test)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetChip("Hypertension OPD (Compliant)") {
                        viewModel.setRxTextInput("""
                            Dr. P. K. Verma, MD (Med)
                            Reg No: MCI-48201
                            Date: 06/09/2026
                            Pt: Mohan Lal, 54 Yrs, Male, UHID-93821
                            Dx: Essential Hypertension with Grade 1 Angina
                            Allergies: NKA (No Known Drug Allergies)
                            
                            Rx:
                            1. Tab Telmisartan 40mg - 1 Tab OD x 30 days
                            2. Tab Amlodipine 5mg - 1 Tab OD x 30 days
                            3. Tab Atorvastatin 20mg - 1 Tab HS x 30 days
                            
                            Dr. Signature: [Signed]
                        """.trimIndent())
                    }

                    PresetChip("Apex Hospital (Mr. Piyush)") {
                        viewModel.setSelectedImageUri(null)
                        viewModel.setRxTextInput("""
                            Apex Research Centre & Hospital Pvt. Ltd.
                            Hospital: APEX HOSPITAL
                            Pt: Mr. Piyush, 19 Yrs, Male, UHID-54207
                            Date: 04/09/2026
                            Dx: Pain in Abdomen radiate Lt Flank Region, HTN, DM II
                            BP: 120/70, P: 89, SpO2: 96%, T: 97.1F
                            Allergies: NKA
                            Doctor: Dr. Sudhir Lokwani Sir (MS)
                            Reg No: MP-29401
                            Adv: USG Whole Abdomen
                            
                            Rx:
                            1. IVF NS 1 Bottle STAT
                            2. Inj Razo 1 Vial STAT
                            3. Inj Ketonav 1 Amp STAT
                            4. Tab Pantocid 40mg OD x 2 days (Before food)
                            5. Tab Drolgan 1 Tab BD x 2 days (After food)
                            
                            Dr. Signature: [Signed]
                        """.trimIndent())
                    }

                    PresetChip("Schedule H1 Ceftriaxone Rx") {
                        viewModel.setRxTextInput("""
                            Dr. Vikram Rathore, MS
                            Reg No: DMC-59302
                            Date: 06/09/2026
                            Pt: Smt. Sheela, 38 Yrs, Female, UHID-88190
                            Dx: Acute Pelvic Inflammatory Disease
                            Allergy: NKA
                            
                            Rx:
                            1. Inj Ceftriaxone 1g IV BD x 5 days
                            2. Tab Metronidazole 400mg TDS x 5 days
                            3. Tab Doxycycline 100mg BD x 14 days
                            4. Tab Pantoprazole 40mg OD x 7 days
                            
                            Dr. Signature: [Signed]
                        """.trimIndent())
                    }

                    PresetChip("Banned FDC Non-Compliant") {
                        viewModel.setRxTextInput("""
                            Dr. R. Gupta
                            Date: 06/09/2026
                            Pt: Rajesh, 45 Yrs, Male, UHID-33910
                            Dx: Joint pain
                            Allergy: Not Documented
                            
                            Rx:
                            1. Tab Aceclofenac + Paracetamol + Rabeprazole BD x 5 days
                            2. Tab Nimesulide + Paracetamol SOS x 3 days
                            
                            Dr. Signature: [Signed]
                        """.trimIndent())
                    }

                    PresetChip("Polypharmacy (6 Drugs)") {
                        viewModel.setRxTextInput("""
                            Dr. Rajesh Gupta, MD
                            Reg No: MMC-41209
                            Date: 06/09/2026
                            Pt: Ramdev Yadav, 71 Yrs, Male, UHID-66012
                            Dx: COPD with Congestive Heart Failure
                            Allergies: Penicillin allergy
                            
                            Rx:
                            1. Tab Azithromycin 500mg OD x 5 days
                            2. Tab Prednisolone 20mg OD x 5 days
                            3. Inhaler Budesonide 200mcg BD x 30 days
                            4. Tab Pantoprazole 40mg OD x 15 days
                            5. Tab Deriphyllin Retard 150mg BD x 7 days
                            6. Tab Furosemide 40mg OD morning x 10 days
                            
                            Dr. Signature: [Signed]
                        """.trimIndent())
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Image Upload & Camera Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("upload_prescription_photo_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Gallery",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                cameraLauncher.launch(null)
                            }
                            .testTag("take_prescription_photo_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Camera",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (selectedImageUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected Prescription",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (PrescriptionOcrParser.isGeminiConfigured()) Icons.Default.CheckCircle else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (PrescriptionOcrParser.isGeminiConfigured()) CompliantGreen else WarningAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (PrescriptionOcrParser.isGeminiConfigured()) "Gemini Vision OCR Active" else "Gemini API Key Required for Photo OCR",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (PrescriptionOcrParser.isGeminiConfigured()) CompliantGreen else WarningAmber,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (!PrescriptionOcrParser.isGeminiConfigured()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "To scan directly from photos, add GEMINI_API_KEY in the AI Studio Secrets panel (Settings ⚙️ → Secrets). You can also tap a preset or type prescription text below to audit immediately offline.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Prescription OCR / e-Rx Text Box
                Text(
                    text = "Prescription Text / OCR Content",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = rxTextInput,
                    onValueChange = { viewModel.setRxTextInput(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .testTag("prescription_text_field"),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Paste e-Rx or OCR text here...") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 1-Tap Audit Action Button
                Button(
                    onClick = {
                        viewModel.processPrescriptionInput(
                            rawInput = rxTextInput,
                            imageUri = selectedImageUri?.toString()
                        )
                    },
                    enabled = !isProcessing && rxTextInput.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("run_audit_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Auditing Prescription...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Run 1-Tap Prescription Audit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PresetChip(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuditResultReviewSection(
    parsed: ParsedPrescription,
    imageUri: String?,
    onCommit: () -> Unit,
    onDiscard: () -> Unit
) {
    // Live evaluate using clinical rules
    val previewEntity = remember(parsed) {
        AuditRulesEngine.auditPrescription(
            rxNumber = parsed.rxNumber,
            patientName = parsed.patientName,
            patientAge = parsed.patientAge,
            patientGender = parsed.patientGender,
            uhid = parsed.uhid,
            department = parsed.department,
            dateString = parsed.dateString,
            diagnosis = parsed.diagnosis,
            allergyStatusDocumented = parsed.allergyStatusDocumented,
            allergyDetails = parsed.allergyDetails,
            historyDocumented = parsed.historyDocumented,
            legibleHandwritingOrTyped = parsed.legibleHandwritingOrTyped,
            doctorName = parsed.doctorName,
            doctorRegNumber = parsed.doctorRegNumber,
            hasDoctorSignature = parsed.hasDoctorSignature,
            drugs = parsed.drugs,
            auditorName = "Clinical Audit Lead",
            auditorRole = "Pharmacy & Therapeutics Committee",
            prevHash = "PREVIEW-HASH"
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audit_review_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Instant Audit Assessment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rx #${parsed.rxNumber} • ${parsed.department}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                StatusBadge(isCompliant = previewEntity.isCompliantOverall)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Score Badges
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScoreCard(
                    title = "Completeness",
                    score = previewEntity.completenessScorePct,
                    target = "≥ 85%",
                    isPassing = previewEntity.completenessScorePct >= 85,
                    modifier = Modifier.weight(1f)
                )
                ScoreCard(
                    title = "Rationality",
                    score = previewEntity.rationalityScorePct,
                    target = "≥ 80%",
                    isPassing = previewEntity.rationalityScorePct >= 80,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Patient & Doctor Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Patient: ${parsed.patientName}, ${parsed.patientAge}y (${parsed.patientGender})",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "UHID: ${parsed.uhid} | Date: ${parsed.dateString}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Doctor: ${parsed.doctorName} [Reg: ${if (parsed.doctorRegNumber.isNotBlank()) parsed.doctorRegNumber else "MISSING"}]",
                        fontSize = 12.sp,
                        color = if (parsed.doctorRegNumber.isNotBlank()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else NonCompliantRed
                    )
                    Text(
                        text = "Diagnosis: ${if (parsed.diagnosis.isNotBlank()) parsed.diagnosis else "NOT SPECIFIED"}",
                        fontSize = 12.sp,
                        color = if (parsed.diagnosis.isNotBlank()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) else NonCompliantRed
                    )
                    Text(
                        text = "Allergy Status: ${if (parsed.allergyStatusDocumented) parsed.allergyDetails else "NOT DOCUMENTED"}",
                        fontSize = 12.sp,
                        color = if (parsed.allergyStatusDocumented) CompliantGreen else NonCompliantRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Deficiencies Banner
            if (previewEntity.deficienciesList.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = NonCompliantRedBg)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = NonCompliantRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Audit Deficiencies Detected:",
                                fontWeight = FontWeight.Bold,
                                color = NonCompliantRed,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        previewEntity.deficienciesList.split(";").forEach { def ->
                            Text(
                                text = "• ${def.trim()}",
                                fontSize = 11.sp,
                                color = NonCompliantRed,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Extracted Medicines
            Text(
                text = "Extracted Medicines (${parsed.drugs.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            parsed.drugs.forEachIndexed { index, drug ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}. ${drug.brandName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            if (drug.isGeneric) {
                                Text(
                                    text = "Generic (INN)",
                                    color = CompliantGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (drug.genericName != drug.brandName) {
                            Text(
                                text = "Generic Name: ${drug.genericName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }

                                                Text(
                            text = "Dosage: ${drug.dose} • ${drug.route} • ${drug.frequency} for ${drug.durationDays} days",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        if (drug.instructions.isNotBlank()) {
                            Text(
                                text = "Usage Instructions: ${drug.instructions}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                            )
                        }

                        // Tags
                        FlowRow(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (drug.isScheduleH1) ScheduleH1Badge(count = 1)
                            if (drug.isScheduleH) ScheduleHBadge()
                            if (drug.isAntibiotic) AntibioticBadge(awarClass = drug.antibioticClass)
                            if (drug.isIrrationalFdc) {
                                Text(
                                    text = "Irrational FDC",
                                    color = NonCompliantRed,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NonCompliantRedBg)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Blockchain tamper-evident seal preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Commit will generate SHA-256 docHash linked to preceding ledger block with 6-year retention.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions: Commit & Discard
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDiscard,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Discard")
                }

                Button(
                    onClick = onCommit,
                    modifier = Modifier
                        .weight(2f)
                        .testTag("commit_audit_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sign Hash & Save Audit", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ScoreCard(
    title: String,
    score: Int,
    target: String,
    isPassing: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isPassing) CompliantGreen else WarningAmber
    val bg = if (isPassing) CompliantGreenBg else WarningAmberBg

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$score%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(Target $target)",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = color.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}
