package com.example.domain

import com.example.data.model.AuditIndicators
import com.example.data.model.PrescriptionEntity
import com.example.data.model.SampleSizeEstimate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

object AuditReportExporter {

    /**
     * Calculates recommended sample size for NABH / WHO prescription audit
     * using Cochran's formula for finite population:
     * n0 = (Z^2 * p * (1-p)) / e^2, where Z = 1.96 (95% CI), p = 0.5, e = 0.05 (5% margin of error)
     * n = n0 / (1 + (n0 - 1) / N)
     */
    fun calculateSampleSize(monthlyOpdPrescriptions: Int, currentSample: Int): SampleSizeEstimate {
        val n0 = (1.96 * 1.96 * 0.5 * 0.5) / (0.05 * 0.05) // ~384.16
        val N = monthlyOpdPrescriptions.toDouble()
        val recommended = if (monthlyOpdPrescriptions <= 0) {
            100 // default minimum WHO recommendation for facility audit
        } else {
            val adjusted = n0 / (1.0 + (n0 - 1.0) / N)
            ceil(adjusted).toInt().coerceAtLeast(30)
        }

        val isAdequate = currentSample >= recommended
        val note = if (isAdequate) {
            "Current audit sample ($currentSample) meets 95% CI statistical significance (Recommended: $recommended) for NABH 5th Edition."
        } else {
            "Audit sample ($currentSample) is below the statistically recommended 95% CI target of $recommended prescriptions. More samples advised."
        }

        return SampleSizeEstimate(
            populationSize = monthlyOpdPrescriptions,
            confidenceLevelPct = 95.0,
            marginOfErrorPct = 5.0,
            recommendedSampleSize = recommended,
            currentSampleSize = currentSample,
            isAdequate = isAdequate,
            standardNote = note
        )
    }

    /**
     * Formats NABH NHSRC compliant CSV audit register
     */
    fun generateCsvAuditReport(prescriptions: List<PrescriptionEntity>, hospitalName: String = "National Medical Institute"): String {
        val sb = StringBuilder()
        sb.append("NABH / WHO PRESCRIPTION AUDIT REPORT (NHSRC FORMAT)\n")
        sb.append("Hospital / Facility: ,\"$hospitalName\"\n")
        sb.append("Audit Date: ,\"${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\"\n")
        sb.append("Standards: ,\"NABH 5th Edition MOM.5 & WHO Core Prescribing Indicators\"\n")
        sb.append("Total Audited Prescriptions: ,${prescriptions.size}\n\n")

        // CSV Header
        sb.append("Rx Number,Date,Department,Patient Name,Age,Sex,UHID,Doctor Name,MCI/NMC Reg No,Diagnosis,Allergy Documented,Total Drugs,Generic Count,Antibiotic Count,Injection Count,EDL Count,Schedule H1,Polypharmacy,Irrational FDC,Completeness %,Rationality %,Status,Deficiencies,Tamper-Evident DocHash\n")

        for (rx in prescriptions) {
            val status = if (rx.isCompliantOverall) "COMPLIANT" else "NON-COMPLIANT"
            val defs = rx.deficienciesList.replace("\"", "\"\"")
            val diagnosisClean = rx.diagnosis.replace("\"", "\"\"")
            val patientClean = rx.patientName.replace("\"", "\"\"")
            val docClean = rx.doctorName.replace("\"", "\"\"")
            
            sb.append("\"${rx.rxNumber}\",")
            sb.append("\"${rx.dateString}\",")
            sb.append("\"${rx.department}\",")
            sb.append("\"$patientClean\",")
            sb.append("${rx.patientAge},")
            sb.append("\"${rx.patientGender}\",")
            sb.append("\"${rx.uhid}\",")
            sb.append("\"$docClean\",")
            sb.append("\"${rx.doctorRegNumber}\",")
            sb.append("\"$diagnosisClean\",")
            sb.append(if (rx.allergyStatusDocumented) "YES" else "NO").append(",")
            sb.append("${rx.totalDrugsCount},")
            sb.append("${rx.genericCount},")
            sb.append("${rx.antibioticCount},")
            sb.append("${rx.injectionCount},")
            sb.append("${rx.edlCount},")
            sb.append(if (rx.scheduleH1Count > 0) "YES (${rx.scheduleH1Count})" else "NO").append(",")
            sb.append(if (rx.isPolypharmacy) "YES" else "NO").append(",")
            sb.append(if (rx.irrationalFdcCount > 0) "FLAGGED" else "NONE").append(",")
            sb.append("${rx.completenessScorePct}%,")
            sb.append("${rx.rationalityScorePct}%,")
            sb.append("\"$status\",")
            sb.append("\"$defs\",")
            sb.append("\"${rx.docHash.take(16)}...\"\n")
        }

        return sb.toString()
    }

    /**
     * Generates a clinical audit committee executive summary
     */
    fun generateExecutiveSummary(indicators: AuditIndicators, facilityName: String): String {
        return buildString {
            append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("PRESCRIPTION AUDIT SUMMARY - $facilityName\n")
            append("NABH MOM.5 & WHO CORE PRESCRIBING INDICATORS\n")
            append("Generated: ${SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
            append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")

            append("1. OVERALL COMPLIANCE\n")
            append("• Total Sample Size: ${indicators.totalPrescriptions} prescriptions\n")
            append("• Compliant Prescriptions: ${indicators.compliantCount} (${"%.1f".format(indicators.complianceRatePct)}%)\n")
            append("• Non-Compliant / Flagged: ${indicators.nonCompliantCount} (${"%.1f".format(100 - indicators.complianceRatePct)}%)\n\n")

            append("2. WHO CORE PRESCRIBING INDICATORS (vs Standard Targets)\n")
            append("• Avg Drugs per Prescription: ${"%.2f".format(indicators.avgDrugsPerPrescription)} [WHO Target: 1.6 - 1.8]\n")
            append("• % Prescribed by Generic Name: ${"%.1f".format(indicators.genericPrescribingPct)}% [WHO Target: 100%]\n")
            append("• % Encounters with Antibiotic: ${"%.1f".format(indicators.antibioticEncountersPct)}% [WHO Target: 20.0 - 26.8%]\n")
            append("• % Encounters with Injection: ${"%.1f".format(indicators.injectionEncountersPct)}% [WHO Target: 13.4 - 24.1%]\n")
            append("• % Prescribed from EDL / NLEM: ${"%.1f".format(indicators.edlPrescribingPct)}% [WHO Target: 100%]\n\n")

            append("3. NABH QUALITY & PATIENT SAFETY METRICS\n")
            append("• Prescription Completeness Avg: ${"%.1f".format(indicators.completenessAvgPct)}%\n")
            append("• Allergy Status Documented: ${"%.1f".format(indicators.allergyDocumentationPct)}% [NABH Mandatory: 100%]\n")
            append("• Doctor NMC/SMC Reg No Recorded: ${"%.1f".format(indicators.doctorRegDocumentedPct)}% [MCI Mandatory: 100%]\n")
            append("• Diagnosis Stated on Rx: ${"%.1f".format(indicators.diagnosisSpecifiedPct)}%\n")
            append("• Doctor Signature Validated: ${"%.1f".format(indicators.signaturePresentPct)}%\n\n")

            append("4. DRUG REGULATORY (SCHEDULE H/H1 & CDSCO)\n")
            append("• Polypharmacy Encounters (>=5 drugs): ${indicators.polypharmacyEncountersCount} (${"%.1f".format(indicators.polypharmacyEncountersPct)}%)\n")
            append("• Schedule H Prescriptions: ${indicators.scheduleHEncountersCount}\n")
            append("• Schedule H1 (Restricted Antibiotics): ${indicators.scheduleH1EncountersCount} (Requires separate register under Rules)\n")
            append("• Irrational / Banned FDCs Flagged: ${indicators.irrationalFdcCount}\n\n")

            append("5. AUDIT INTEGRITY & RETENTION\n")
            append("• Blockchain Hash Chain Integrity: ${if (indicators.isChainValid) "VERIFIED VALID (SHA-256)" else "TAMPER DETECTED"}\n")
            append("• Verified Audit Records: ${indicators.verifiedChainLength}\n")
            append("• Mandatory Legal Retention: 6 Years (Drugs & Cosmetics Rules)\n")
            append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }
}
