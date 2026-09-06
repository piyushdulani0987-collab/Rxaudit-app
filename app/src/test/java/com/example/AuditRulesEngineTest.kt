package com.example

import com.example.domain.AuditReportExporter
import com.example.domain.AuditRulesEngine
import com.example.domain.PrescriptionOcrParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuditRulesEngineTest {

    @Test
    fun testScheduleH1AndAntibioticDetection() {
        val drug = AuditRulesEngine.evaluateDrug(
            rawName = "Inj Ceftriaxone (Monocef)",
            dose = "1 g",
            route = "IV",
            frequency = "BD",
            durationDays = 3
        )

        assertTrue(drug.isScheduleH1)
        assertTrue(drug.isScheduleH)
        assertTrue(drug.isAntibiotic)
        assertEquals("Watch", drug.antibioticClass)
        assertTrue(drug.isInjection)
    }

    @Test
    fun testIrrationalFdcDetection() {
        val drug = AuditRulesEngine.evaluateDrug(
            rawName = "Tab Aceclofenac + Paracetamol + Rabeprazole",
            dose = "100/325/20 mg",
            route = "Oral",
            frequency = "BD",
            durationDays = 5
        )

        assertTrue(drug.isIrrationalFdc)
        assertTrue(drug.isFdc)
    }

    @Test
    fun testGenericDetection() {
        val drug = AuditRulesEngine.evaluateDrug(
            rawName = "Tab Paracetamol (Generic)",
            dose = "650 mg",
            route = "Oral",
            frequency = "TDS",
            durationDays = 3
        )

        assertTrue(drug.isGeneric)
        assertTrue(drug.isEdl)
    }

    @Test
    fun testCochranSampleSizeCalculator() {
        val estimate = AuditReportExporter.calculateSampleSize(
            monthlyOpdPrescriptions = 1500,
            currentSample = 120
        )

        assertEquals(95.0, estimate.confidenceLevelPct, 0.01)
        assertEquals(5.0, estimate.marginOfErrorPct, 0.01)
        assertTrue(estimate.recommendedSampleSize in 280..350)
        assertFalse(estimate.isAdequate)
    }

    @Test
    fun testOcrParserOfflineFallback() {
        val rxText = """
            Dr. P. K. Verma, MD
            Reg No: MCI-48201
            Date: 06/09/2026
            Pt: Mohan Lal, 54 Yrs, Male, UHID-93821
            Dx: Essential Hypertension
            Allergies: NKA
            
            Rx:
            1. Tab Telmisartan 40mg - 1 Tab OD x 30 days
            2. Tab Amlodipine 5mg - 1 Tab OD x 30 days
        """.trimIndent()

        val parsed = PrescriptionOcrParser.parsePrescriptionText(rxText)
        assertEquals("Mohan Lal", parsed.patientName)
        assertEquals(54, parsed.patientAge)
        assertEquals("Male", parsed.patientGender)
        assertEquals("UHID-93821", parsed.uhid)
        assertTrue(parsed.allergyStatusDocumented)
        assertEquals("MCI-48201", parsed.doctorRegNumber)
        assertEquals(2, parsed.drugs.size)
    }

    @Test
    fun testSha256DocHashChain() {
        val prevHash = "0000000000000000000000000000000000000000000000000000000000000000"
        val drugs = listOf(
            AuditRulesEngine.evaluateDrug("Tab Paracetamol (Generic)", "650 mg", "Oral", "TDS", 3)
        )
        val audited = AuditRulesEngine.auditPrescription(
            rxNumber = "RX-TEST-001",
            patientName = "Test Patient",
            patientAge = 40,
            patientGender = "Male",
            uhid = "UHID-TEST",
            department = "General Medicine",
            dateString = "06/09/2026",
            diagnosis = "Viral Fever",
            allergyStatusDocumented = true,
            allergyDetails = "NKA",
            historyDocumented = true,
            legibleHandwritingOrTyped = true,
            doctorName = "Dr. Test",
            doctorRegNumber = "MCI-12345",
            hasDoctorSignature = true,
            drugs = drugs,
            auditorName = "Auditor Lead",
            auditorRole = "QA Committee",
            prevHash = prevHash
        )

        assertNotNull(audited.docHash)
        assertEquals(64, audited.docHash.length) // 256 bits = 64 hex characters
        assertEquals(prevHash, audited.prevHash)
        assertTrue(audited.isCompliantOverall)
    }
}
