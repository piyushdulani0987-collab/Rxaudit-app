package com.example.data.model

data class AuditIndicators(
    val totalPrescriptions: Int = 0,
    val compliantCount: Int = 0,
    val nonCompliantCount: Int = 0,
    val complianceRatePct: Double = 0.0,
    
    // WHO Core Prescribing Indicators
    val avgDrugsPerPrescription: Double = 0.0,       // Standard: 1.6 - 1.8
    val genericPrescribingPct: Double = 0.0,         // Standard: 100%
    val antibioticEncountersPct: Double = 0.0,       // Standard: 20.0 - 26.8%
    val injectionEncountersPct: Double = 0.0,        // Standard: 13.4 - 24.1%
    val edlPrescribingPct: Double = 0.0,             // Standard: 100%
    
    // NABH Quality Indicators
    val completenessAvgPct: Double = 0.0,
    val allergyDocumentationPct: Double = 0.0,
    val doctorRegDocumentedPct: Double = 0.0,
    val diagnosisSpecifiedPct: Double = 0.0,
    val signaturePresentPct: Double = 0.0,
    val legibilityCompliancePct: Double = 0.0,
    
    // Schedule H / H1 and Safety Checks
    val polypharmacyEncountersCount: Int = 0,
    val polypharmacyEncountersPct: Double = 0.0,
    val scheduleHEncountersCount: Int = 0,
    val scheduleH1EncountersCount: Int = 0,
    val irrationalFdcCount: Int = 0,
    
    // Hash Audit Chain Status
    val isChainValid: Boolean = true,
    val verifiedChainLength: Int = 0
)

data class SampleSizeEstimate(
    val populationSize: Int,
    val confidenceLevelPct: Double = 95.0,
    val marginOfErrorPct: Double = 5.0,
    val recommendedSampleSize: Int,
    val currentSampleSize: Int,
    val isAdequate: Boolean,
    val standardNote: String
)
