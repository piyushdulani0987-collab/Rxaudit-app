package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Local Room Database entity representing an immutable, tamper-evident audit history record.
 * Complies with NABH Quality Standards (MOM.5 / COP.3) and statutory clinical audit retention.
 *
 * Stores:
 * 1. Timestamp of the audit event.
 * 2. Complete Prescriber details (Doctor Name, Medical Registration Number, Department, Signature status).
 * 3. Detected Drug Interactions (Interacting drug pairs, clinical mechanisms, and severity alerts).
 * 4. Cryptographic SHA-256 hash chaining (previousRecordHash -> recordHash) ensuring tamper-evidence.
 */
@Entity(
    tableName = "audit_history",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["prescriberRegNumber"]),
        Index(value = ["rxNumber"]),
        Index(value = ["recordHash"], unique = true)
    ]
)
data class AuditHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val auditId: Long = 0,
    val prescriptionId: Long? = null,
    val rxNumber: String,

    // 1. Audit Timestamp
    val timestamp: Long = System.currentTimeMillis(),
    val auditDateFormatted: String = "",

    // 2. Prescriber Details
    val prescriberName: String,
    val prescriberRegNumber: String,
    val prescriberDepartment: String = "General Medicine",
    val hasPrescriberSignature: Boolean = true,

    // Clinical and Patient Traceability
    val patientName: String = "",
    val patientUhid: String = "",
    val diagnosis: String = "",

    // 3. Detected Drug Interactions
    val detectedDrugInteractions: String = "",
    val hasDrugInteractions: Boolean = false,
    val interactionCount: Int = 0,

    // Audit Findings & Compliance Metrics
    val completenessScorePct: Int = 0,
    val rationalityScorePct: Int = 0,
    val isCompliant: Boolean = false,
    val deficienciesSummary: String = "",
    val auditedMedicationsSummary: String = "",

    // 4. Tamper-Evident Cryptographic Ledger Fields
    val auditorName: String,
    val auditorRole: String,
    val previousRecordHash: String,
    val recordHash: String,
    val canonicalPayload: String = "",
    val isTamperEvidentVerified: Boolean = true,
    val retentionExpiryTimestamp: Long = System.currentTimeMillis() + (6L * 365L * 24L * 60L * 60L * 1000L) // 6-year NABH rule
) {
    companion object {
        const val GENESIS_PREV_HASH = "0000000000000000000000000000000000000000000000000000000000000000"

        /**
         * Builds the canonical payload string containing timestamp, prescriber details,
         * detected drug interactions, clinical findings, and previous record hash.
         */
        fun buildCanonicalPayload(
            timestamp: Long,
            prescriberName: String,
            prescriberRegNumber: String,
            hasPrescriberSignature: Boolean,
            prescriberDepartment: String,
            rxNumber: String,
            patientUhid: String,
            diagnosis: String,
            detectedDrugInteractions: String,
            completenessScorePct: Int,
            rationalityScorePct: Int,
            isCompliant: Boolean,
            auditorName: String,
            previousRecordHash: String
        ): String {
            return buildString {
                append("TIMESTAMP=").append(timestamp).append(";")
                append("PRESCRIBER_NAME=").append(prescriberName.trim()).append(";")
                append("PRESCRIBER_REG=").append(prescriberRegNumber.trim()).append(";")
                append("PRESCRIBER_SIG=").append(hasPrescriberSignature).append(";")
                append("PRESCRIBER_DEPT=").append(prescriberDepartment.trim()).append(";")
                append("RX_NUM=").append(rxNumber.trim()).append(";")
                append("PATIENT_UHID=").append(patientUhid.trim()).append(";")
                append("DIAGNOSIS=").append(diagnosis.trim()).append(";")
                append("DRUG_INTERACTIONS=").append(detectedDrugInteractions.trim()).append(";")
                append("COMPLETENESS=").append(completenessScorePct).append(";")
                append("RATIONALITY=").append(rationalityScorePct).append(";")
                append("COMPLIANT=").append(isCompliant).append(";")
                append("AUDITOR=").append(auditorName.trim()).append(";")
                append("PREV_HASH=").append(previousRecordHash.trim())
            }
        }

        /**
         * Computes a SHA-256 hash for any string payload.
         */
        fun computeSha256(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }

        /**
         * Factory function to create a validated, tamper-evident audit history record.
         */
        fun createRecord(
            prescriptionId: Long? = null,
            rxNumber: String,
            prescriberName: String,
            prescriberRegNumber: String,
            prescriberDepartment: String = "General Medicine",
            hasPrescriberSignature: Boolean = true,
            patientName: String = "",
            patientUhid: String = "",
            diagnosis: String = "",
            detectedDrugInteractions: String = "",
            completenessScorePct: Int = 0,
            rationalityScorePct: Int = 0,
            isCompliant: Boolean = false,
            deficienciesSummary: String = "",
            auditedMedicationsSummary: String = "",
            auditorName: String,
            auditorRole: String,
            previousRecordHash: String = GENESIS_PREV_HASH,
            timestamp: Long = System.currentTimeMillis()
        ): AuditHistoryEntity {
            val hasInteractions = detectedDrugInteractions.isNotBlank() && !detectedDrugInteractions.equals("None", ignoreCase = true)
            val interactionCount = if (hasInteractions) {
                detectedDrugInteractions.split(";").filter { it.isNotBlank() }.size.coerceAtLeast(1)
            } else {
                0
            }

            val formatter = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault())
            val dateFormatted = formatter.format(Date(timestamp))

            val canonicalPayload = buildCanonicalPayload(
                timestamp = timestamp,
                prescriberName = prescriberName,
                prescriberRegNumber = prescriberRegNumber,
                hasPrescriberSignature = hasPrescriberSignature,
                prescriberDepartment = prescriberDepartment,
                rxNumber = rxNumber,
                patientUhid = patientUhid,
                diagnosis = diagnosis,
                detectedDrugInteractions = detectedDrugInteractions,
                completenessScorePct = completenessScorePct,
                rationalityScorePct = rationalityScorePct,
                isCompliant = isCompliant,
                auditorName = auditorName,
                previousRecordHash = previousRecordHash
            )

            val recordHash = computeSha256(canonicalPayload)

            return AuditHistoryEntity(
                prescriptionId = prescriptionId,
                rxNumber = rxNumber,
                timestamp = timestamp,
                auditDateFormatted = dateFormatted,
                prescriberName = prescriberName,
                prescriberRegNumber = prescriberRegNumber,
                prescriberDepartment = prescriberDepartment,
                hasPrescriberSignature = hasPrescriberSignature,
                patientName = patientName,
                patientUhid = patientUhid,
                diagnosis = diagnosis,
                detectedDrugInteractions = detectedDrugInteractions,
                hasDrugInteractions = hasInteractions,
                interactionCount = interactionCount,
                completenessScorePct = completenessScorePct,
                rationalityScorePct = rationalityScorePct,
                isCompliant = isCompliant,
                deficienciesSummary = deficienciesSummary,
                auditedMedicationsSummary = auditedMedicationsSummary,
                auditorName = auditorName,
                auditorRole = auditorRole,
                previousRecordHash = previousRecordHash,
                recordHash = recordHash,
                canonicalPayload = canonicalPayload,
                isTamperEvidentVerified = true
            )
        }

        /**
         * Verifies the cryptographic integrity of an audit record.
         * Returns true if the calculated SHA-256 matches the stored recordHash and chain predecessor.
         */
        fun verifyRecordIntegrity(
            record: AuditHistoryEntity,
            expectedPreviousHash: String? = null
        ): Boolean {
            val payload = buildCanonicalPayload(
                timestamp = record.timestamp,
                prescriberName = record.prescriberName,
                prescriberRegNumber = record.prescriberRegNumber,
                hasPrescriberSignature = record.hasPrescriberSignature,
                prescriberDepartment = record.prescriberDepartment,
                rxNumber = record.rxNumber,
                patientUhid = record.patientUhid,
                diagnosis = record.diagnosis,
                detectedDrugInteractions = record.detectedDrugInteractions,
                completenessScorePct = record.completenessScorePct,
                rationalityScorePct = record.rationalityScorePct,
                isCompliant = record.isCompliant,
                auditorName = record.auditorName,
                previousRecordHash = record.previousRecordHash
            )
            val computedHash = computeSha256(payload)
            val isHashValid = computedHash.equals(record.recordHash, ignoreCase = true)
            val isChainValid = if (expectedPreviousHash != null) {
                record.previousRecordHash.equals(expectedPreviousHash, ignoreCase = true)
            } else {
                true
            }
            return isHashValid && isChainValid
        }
    }
}
