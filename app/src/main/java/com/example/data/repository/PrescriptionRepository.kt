package com.example.data.repository

import com.example.data.local.PrescriptionDao
import com.example.data.model.AuditIndicators
import com.example.data.model.AuditSampleData
import com.example.data.model.PrescriptionEntity
import com.example.domain.AuditRulesEngine
import com.example.domain.ParsedPrescription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PrescriptionRepository(private val dao: PrescriptionDao) {

    val allPrescriptions: Flow<List<PrescriptionEntity>> = dao.getAllPrescriptions()

    val computedIndicators: Flow<AuditIndicators> = allPrescriptions.map { list ->
        calculateIndicators(list)
    }

    suspend fun loadSampleBatchIfEmpty() {
        if (dao.getCount() == 0) {
            val samples = AuditSampleData.getInitialClinicalSamplePrescriptions()
            dao.insertAll(samples)
        }
    }

    suspend fun auditAndInsert(
        parsed: ParsedPrescription,
        auditorName: String,
        auditorRole: String,
        imageUri: String? = null
    ): Long {
        val latest = dao.getLatestPrescription()
        val prevHash = latest?.docHash ?: "0000000000000000000000000000000000000000000000000000000000000000"

        val auditedEntity = AuditRulesEngine.auditPrescription(
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
            auditorName = auditorName,
            auditorRole = auditorRole,
            prevHash = prevHash,
            imageUri = imageUri,
            rawOcrText = parsed.rawText
        )

        return dao.insert(auditedEntity)
    }

    suspend fun deletePrescription(id: Long) = dao.deleteById(id)

    suspend fun resetWithSamples() {
        dao.clearAll()
        val samples = AuditSampleData.getInitialClinicalSamplePrescriptions()
        dao.insertAll(samples)
    }

    fun calculateIndicators(prescriptions: List<PrescriptionEntity>): AuditIndicators {
        if (prescriptions.isEmpty()) {
            return AuditIndicators()
        }

        val total = prescriptions.size
        val compliantCount = prescriptions.count { it.isCompliantOverall }
        val nonCompliantCount = total - compliantCount
        val complianceRate = (compliantCount.toDouble() / total) * 100.0

        val totalDrugsAllRx = prescriptions.sumOf { it.totalDrugsCount }
        val avgDrugsPerRx = if (total > 0) totalDrugsAllRx.toDouble() / total else 0.0

        val totalGenericDrugs = prescriptions.sumOf { it.genericCount }
        val genericPct = if (totalDrugsAllRx > 0) (totalGenericDrugs.toDouble() / totalDrugsAllRx) * 100.0 else 0.0

        val antibioticEncounters = prescriptions.count { it.antibioticCount > 0 }
        val antibioticPct = (antibioticEncounters.toDouble() / total) * 100.0

        val injectionEncounters = prescriptions.count { it.injectionCount > 0 }
        val injectionPct = (injectionEncounters.toDouble() / total) * 100.0

        val totalEdlDrugs = prescriptions.sumOf { it.edlCount }
        val edlPct = if (totalDrugsAllRx > 0) (totalEdlDrugs.toDouble() / totalDrugsAllRx) * 100.0 else 0.0

        val completenessAvg = prescriptions.map { it.completenessScorePct }.average()

        val allergyCount = prescriptions.count { it.allergyStatusDocumented }
        val allergyPct = (allergyCount.toDouble() / total) * 100.0

        val docRegCount = prescriptions.count { it.doctorRegNumber.isNotBlank() }
        val docRegPct = (docRegCount.toDouble() / total) * 100.0

        val diagCount = prescriptions.count { it.hasDiagnosis }
        val diagPct = (diagCount.toDouble() / total) * 100.0

        val sigCount = prescriptions.count { it.hasDoctorSignature }
        val sigPct = (sigCount.toDouble() / total) * 100.0

        val legibleCount = prescriptions.count { it.legibleHandwritingOrTyped }
        val legiblePct = (legibleCount.toDouble() / total) * 100.0

        val polypharmacyCount = prescriptions.count { it.isPolypharmacy }
        val polypharmacyPct = (polypharmacyCount.toDouble() / total) * 100.0

        val schedHCount = prescriptions.count { it.scheduleHCount > 0 }
        val schedH1Count = prescriptions.count { it.scheduleH1Count > 0 }
        val irrationalFdcCount = prescriptions.sumOf { it.irrationalFdcCount }

        // Hash chain verification
        var chainValid = true
        var verifiedCount = 0
        // list is ordered by timestamp DESC, so reverse for chain validation
        val chronological = prescriptions.sortedBy { it.id }
        var expectedPrev = "0000000000000000000000000000000000000000000000000000000000000000"
        for (item in chronological) {
            if (item.prevHash != expectedPrev && expectedPrev != "0000000000000000000000000000000000000000000000000000000000000000") {
                chainValid = false
            }
            val expectedContent = "${item.rxNumber}|${item.patientName}|${item.uhid}|${item.doctorRegNumber}|${item.totalDrugsCount}|${item.completenessScorePct}|${item.rationalityScorePct}|${item.prevHash}"
            val recalculated = AuditRulesEngine.sha256(expectedContent)
            if (recalculated == item.docHash) {
                verifiedCount++
            } else {
                chainValid = false
            }
            expectedPrev = item.docHash
        }

        return AuditIndicators(
            totalPrescriptions = total,
            compliantCount = compliantCount,
            nonCompliantCount = nonCompliantCount,
            complianceRatePct = complianceRate,
            avgDrugsPerPrescription = avgDrugsPerRx,
            genericPrescribingPct = genericPct,
            antibioticEncountersPct = antibioticPct,
            injectionEncountersPct = injectionPct,
            edlPrescribingPct = edlPct,
            completenessAvgPct = completenessAvg,
            allergyDocumentationPct = allergyPct,
            doctorRegDocumentedPct = docRegPct,
            diagnosisSpecifiedPct = diagPct,
            signaturePresentPct = sigPct,
            legibilityCompliancePct = legiblePct,
            polypharmacyEncountersCount = polypharmacyCount,
            polypharmacyEncountersPct = polypharmacyPct,
            scheduleHEncountersCount = schedHCount,
            scheduleH1EncountersCount = schedH1Count,
            irrationalFdcCount = irrationalFdcCount,
            isChainValid = chainValid,
            verifiedChainLength = verifiedCount
        )
    }
}
