package com.example

import com.example.data.model.AuditHistoryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuditHistorySchemaTest {

    @Test
    fun testAuditHistoryRecordCreationAndHashing() {
        val timestamp = 1726000000000L
        val record = AuditHistoryEntity.createRecord(
            prescriptionId = 1L,
            rxNumber = "RX-2026-MED-101",
            prescriberName = "Dr. P. K. Verma, MD",
            prescriberRegNumber = "MCI-48201",
            prescriberDepartment = "General Medicine",
            hasPrescriberSignature = true,
            patientName = "Ramesh Chand Sharma",
            patientUhid = "UHID-88219",
            diagnosis = "Hypertension and Type 2 Diabetes",
            detectedDrugInteractions = "Metformin + Contrast Agent: Risk of lactic acidosis",
            completenessScorePct = 100,
            rationalityScorePct = 95,
            isCompliant = true,
            deficienciesSummary = "",
            auditedMedicationsSummary = "3 medications audited",
            auditorName = "Dr. S. Mukherjee",
            auditorRole = "Lead Auditor",
            previousRecordHash = AuditHistoryEntity.GENESIS_PREV_HASH,
            timestamp = timestamp
        )

        // Verify record contains all required fields
        assertEquals(timestamp, record.timestamp)
        assertEquals("Dr. P. K. Verma, MD", record.prescriberName)
        assertEquals("MCI-48201", record.prescriberRegNumber)
        assertTrue(record.hasPrescriberSignature)
        assertEquals("General Medicine", record.prescriberDepartment)
        assertTrue(record.hasDrugInteractions)
        assertEquals("Metformin + Contrast Agent: Risk of lactic acidosis", record.detectedDrugInteractions)

        // Verify SHA-256 hash formatting (64 hex characters)
        assertNotNull(record.recordHash)
        assertEquals(64, record.recordHash.length)
        assertEquals(AuditHistoryEntity.GENESIS_PREV_HASH, record.previousRecordHash)

        // Verify integrity passes
        val isVerified = AuditHistoryEntity.verifyRecordIntegrity(record)
        assertTrue(isVerified)
    }

    @Test
    fun testTamperDetectionOnPrescriberDetailsOrInteractions() {
        val original = AuditHistoryEntity.createRecord(
            prescriptionId = 2L,
            rxNumber = "RX-2026-CARD-501",
            prescriberName = "Dr. Rajesh Gupta, MD (Cardiology)",
            prescriberRegNumber = "DMC-77312",
            prescriberDepartment = "Cardiology",
            hasPrescriberSignature = true,
            patientName = "Suman Devi",
            patientUhid = "UHID-65123",
            diagnosis = "Atrial Fibrillation",
            detectedDrugInteractions = "Warfarin + Ibuprofen: Severe GI bleeding risk (CONTRAINDICATED)",
            completenessScorePct = 95,
            rationalityScorePct = 70,
            isCompliant = false,
            auditorName = "Dr. S. Mukherjee",
            auditorRole = "Lead Auditor",
            previousRecordHash = "abc1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
        )

        // Untampered record must pass
        assertTrue(AuditHistoryEntity.verifyRecordIntegrity(original))

        // Tamper 1: Modify Prescriber Name
        val tamperedDoctor = original.copy(prescriberName = "Dr. Fake Doctor")
        assertFalse(AuditHistoryEntity.verifyRecordIntegrity(tamperedDoctor))

        // Tamper 2: Modify Prescriber Reg Number
        val tamperedReg = original.copy(prescriberRegNumber = "MCI-99999")
        assertFalse(AuditHistoryEntity.verifyRecordIntegrity(tamperedReg))

        // Tamper 3: Modify Detected Drug Interactions
        val tamperedInteractions = original.copy(detectedDrugInteractions = "None")
        assertFalse(AuditHistoryEntity.verifyRecordIntegrity(tamperedInteractions))

        // Tamper 4: Modify Timestamp
        val tamperedTime = original.copy(timestamp = original.timestamp + 10000L)
        assertFalse(AuditHistoryEntity.verifyRecordIntegrity(tamperedTime))
    }

    @Test
    fun testAuditHistoryCryptographicChainLinking() {
        val record1 = AuditHistoryEntity.createRecord(
            prescriptionId = 1L,
            rxNumber = "RX-001",
            prescriberName = "Dr. Alpha",
            prescriberRegNumber = "REG-001",
            detectedDrugInteractions = "None",
            auditorName = "Auditor 1",
            auditorRole = "Reviewer",
            previousRecordHash = AuditHistoryEntity.GENESIS_PREV_HASH
        )

        val record2 = AuditHistoryEntity.createRecord(
            prescriptionId = 2L,
            rxNumber = "RX-002",
            prescriberName = "Dr. Beta",
            prescriberRegNumber = "REG-002",
            detectedDrugInteractions = "Tramadol + Alprazolam: Respiratory depression risk",
            auditorName = "Auditor 1",
            auditorRole = "Reviewer",
            previousRecordHash = record1.recordHash
        )

        // Verify chain link between record 1 and record 2
        assertEquals(record1.recordHash, record2.previousRecordHash)
        assertTrue(AuditHistoryEntity.verifyRecordIntegrity(record1))
        assertTrue(AuditHistoryEntity.verifyRecordIntegrity(record2, expectedPreviousHash = record1.recordHash))

        // If record1's hash is changed, record2's link will fail verification
        assertFalse(AuditHistoryEntity.verifyRecordIntegrity(record2, expectedPreviousHash = "tampered_hash_0000000000000000000000000000000000000000000000000000"))
    }
}
