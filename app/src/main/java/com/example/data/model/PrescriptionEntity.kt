package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rxNumber: String,
    val patientName: String,
    val patientAge: Int,
    val patientGender: String, // "Male", "Female", "Other"
    val uhid: String,
    val department: String,    // "General Medicine", "Paediatrics", "Emergency/Casualty", "Surgery", "OBG", "Orthopaedics", "Community Pharmacy"
    val dateString: String,
    
    // Completeness Checks (NABH MOM.5 & COP.3 standards)
    val diagnosis: String,
    val hasDiagnosis: Boolean,
    val allergyStatusDocumented: Boolean,
    val allergyDetails: String,
    val historyDocumented: Boolean,
    val legibleHandwritingOrTyped: Boolean,
    val doctorName: String,
    val doctorRegNumber: String,
    val hasDoctorSignature: Boolean,
    
    // Medications list serialized via Room TypeConverter
    val drugsJson: String,
    
    // Computed Rationality & Indicators
    val totalDrugsCount: Int,
    val genericCount: Int,
    val antibioticCount: Int,
    val injectionCount: Int,
    val edlCount: Int,
    val scheduleHCount: Int,
    val scheduleH1Count: Int,
    val fdcCount: Int,
    val irrationalFdcCount: Int,
    val isPolypharmacy: Boolean,
    
    // Compliance Scores
    val completenessScorePct: Int,  // 0 - 100
    val rationalityScorePct: Int,   // 0 - 100
    val isCompliantOverall: Boolean,
    val deficienciesList: String,   // Semicolon-delimited list of audit flags
    
    // NABH Tamper-Evident Audit Trail (6-Year Retention standard)
    val auditorName: String,
    val auditorRole: String,
    val auditTimestamp: Long = System.currentTimeMillis(),
    val retentionExpiryTimestamp: Long = System.currentTimeMillis() + (6L * 365L * 24L * 60L * 60L * 1000L), // 6 years
    val docHash: String,            // SHA-256 hash of this prescription
    val prevHash: String,           // SHA-256 hash of preceding entry in ledger
    val isHashVerified: Boolean = true,
    
    // Image or source
    val imageUri: String? = null,
    val rawOcrText: String? = null
)
