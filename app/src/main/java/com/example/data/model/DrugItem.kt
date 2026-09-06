package com.example.data.model

data class DrugItem(
    val brandName: String,
    val genericName: String,
    val dose: String,
    val frequency: String,      // e.g. "OD", "BD", "TDS", "QID", "SOS"
    val route: String,          // e.g. "Oral", "IV", "IM", "Topical", "Inhalation"
    val durationDays: Int,
    val isGeneric: Boolean,
    val isAntibiotic: Boolean,
    val antibioticClass: String = "", // "Access", "Watch", "Reserve" (WHO AWaRe)
    val isInjection: Boolean,
    val isEdl: Boolean,         // National List of Essential Medicines (NLEM) / Hospital EDL
    val isScheduleH: Boolean,   // India Drugs & Cosmetics Rules Schedule H
    val isScheduleH1: Boolean,  // India Drugs & Cosmetics Rules Schedule H1 (requires warning & record)
    val isFdc: Boolean = false, // Fixed Dose Combination
    val isIrrationalFdc: Boolean = false, // Flagged under DCGI / CDSCO banned list
    val instructions: String = "" // e.g. "After food"
)
