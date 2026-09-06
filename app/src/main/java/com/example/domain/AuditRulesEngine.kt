package com.example.domain

import com.example.data.model.DrugItem
import com.example.data.model.PrescriptionEntity
import java.security.MessageDigest

object AuditRulesEngine {

    // Common Schedule H1 Drugs under Indian Drugs & Cosmetics Rules (3rd Schedule)
    private val SCHEDULE_H1_KEYWORDS = setOf(
        "ceftriaxone", "meropenem", "linezolid", "cefotaxime", "cefepime", "doripenem",
        "ertapenem", "faropenem", "imipenem", "levofloxacin", "moxifloxacin", "gatifloxacin",
        "gemifloxacin", "sparfloxacin", "balofloxacin", "alprazolam", "clobazam", "clonazepam",
        "diazepam", "lorazepam", "midazolam", "nitrazepam", "oxazepam", "chlordiazepoxide",
        "pentazocine", "tramadol", "zolpidem", "codeine", "monocef" // trade name often audited
    )

    // Schedule H Drugs (India)
    private val SCHEDULE_H_KEYWORDS = setOf(
        "amoxicillin", "ampicillin", "azithromycin", "ciprofloxacin", "ofloxacin", "metronidazole",
        "doxycycline", "gentamicin", "amikacin", "paracetamol iv", "diclofenac", "aceclofenac",
        "ibuprofen", "pantoprazole", "rabeprazole", "omeprazole", "esomeprazole", "telmisartan",
        "amlodipine", "metformin", "glimepiride", "atorvastatin", "rosuvastatin", "salbutamol",
        "budesonide", "insulin", "prednisolone", "dexamethasone", "hydrocortisone", "clavulanate",
        "augmentin", "pan", "pantocid", "calpol", "dolo", "azithral", "taxim", "ciprobid"
    )

    // WHO AWaRe Antibiotics list
    private val ANTIBIOTICS_MAP = mapOf(
        "amoxicillin" to "Access",
        "ampicillin" to "Access",
        "doxycycline" to "Access",
        "metronidazole" to "Access",
        "gentamicin" to "Access",
        "trimethoprim" to "Access",
        "cefazolin" to "Access",
        "cloxacillin" to "Access",
        "ciprofloxacin" to "Watch",
        "ceftriaxone" to "Watch",
        "cefotaxime" to "Watch",
        "azithromycin" to "Watch",
        "clarithromycin" to "Watch",
        "levofloxacin" to "Watch",
        "moxifloxacin" to "Watch",
        "piperacillin" to "Watch",
        "tazobactam" to "Watch",
        "vancomycin" to "Watch",
        "meropenem" to "Watch",
        "colistin" to "Reserve",
        "linezolid" to "Reserve",
        "tigecycline" to "Reserve",
        "polymyxin" to "Reserve"
    )

    // Banned / Irrational Fixed Dose Combinations (FDCs) in India
    private val IRRATIONAL_FDC_PATTERNS = listOf(
        Pair("nimesulide", "paracetamol"),
        Pair("aceclofenac", "rabeprazole"),
        Pair("ciprofloxacin", "tinidazole"),
        Pair("norfloxacin", "metronidazole"),
        Pair("ofloxacin", "ornidazole"),
        Pair("amoxicillin", "bromhexine")
    )

    // Core NLEM / EDL Drugs in India
    private val NLEM_EDL_KEYWORDS = setOf(
        "paracetamol", "amoxicillin", "metformin", "amlodipine", "telmisartan", "atorvastatin",
        "ors", "zinc", "ceftriaxone", "pantoprazole", "salbutamol", "budesonide", "ibuprofen",
        "metronidazole", "doxycycline", "ciprofloxacin", "azithromycin", "insulin", "glimepiride",
        "levothyroxine", "cetirizine", "omeprazole", "iron", "folic acid", "calcium", "vitamin d3"
    )

    fun evaluateDrug(
        rawName: String,
        dose: String,
        route: String,
        frequency: String,
        durationDays: Int,
        instructions: String = ""
    ): DrugItem {
        val lower = rawName.lowercase()
        val isInjection = route.equals("IV", ignoreCase = true) ||
                route.equals("IM", ignoreCase = true) ||
                route.equals("SC", ignoreCase = true) ||
                lower.contains("inj") ||
                lower.contains("injection") ||
                lower.contains("vial") ||
                lower.contains("ampoule")

        var isAntibiotic = false
        var awarClass = ""
        for ((antibiotic, awar) in ANTIBIOTICS_MAP) {
            if (lower.contains(antibiotic)) {
                isAntibiotic = true
                awarClass = awar
                break
            }
        }
        if (!isAntibiotic && (lower.contains("antibiotic") || lower.contains("cillin") || lower.contains("mycin") || lower.contains("floxacin") || lower.contains("penem"))) {
            isAntibiotic = true
            awarClass = "Watch"
        }

        val isScheduleH1 = SCHEDULE_H1_KEYWORDS.any { lower.contains(it) }
        val isScheduleH = isScheduleH1 || SCHEDULE_H_KEYWORDS.any { lower.contains(it) }
        val isEdl = NLEM_EDL_KEYWORDS.any { lower.contains(it) }

        // Check FDC
        val isFdc = lower.contains("+") || lower.contains("/") || lower.contains("with") || lower.contains("comb")
        var isIrrationalFdc = false
        for ((d1, d2) in IRRATIONAL_FDC_PATTERNS) {
            if (lower.contains(d1) && lower.contains(d2)) {
                isIrrationalFdc = true
                break
            }
        }

        // Generic vs Brand
        val isExplicitGeneric = lower.contains("(generic)") || lower.startsWith("tab ") && NLEM_EDL_KEYWORDS.any { lower.contains(it) } && !hasBrandIndicators(lower)
        val isGeneric = isExplicitGeneric || isKnownGenericName(lower)

        val brandName = if (isGeneric) rawName else rawName.substringBefore("(").trim()
        val genericName = if (isGeneric) rawName else extractOrGuessGeneric(rawName)

        return DrugItem(
            brandName = brandName,
            genericName = genericName,
            dose = dose.ifEmpty { "Standard" },
            frequency = frequency.ifEmpty { "OD" },
            route = route.ifEmpty { if (isInjection) "IV" else "Oral" },
            durationDays = if (durationDays <= 0) 5 else durationDays,
            isGeneric = isGeneric,
            isAntibiotic = isAntibiotic,
            antibioticClass = awarClass,
            isInjection = isInjection,
            isEdl = isEdl,
            isScheduleH = isScheduleH,
            isScheduleH1 = isScheduleH1,
            isFdc = isFdc,
            isIrrationalFdc = isIrrationalFdc,
            instructions = instructions
        )
    }

    private fun hasBrandIndicators(name: String): Boolean {
        val brands = listOf("dolo", "calpol", "augmentin", "pan-", "pantocid", "monocef", "zifi", "azithral", "telma", "glycomet", "amlovas", "atorva", "cpm", "crocin")
        return brands.any { name.contains(it) }
    }

    private fun isKnownGenericName(name: String): Boolean {
        val generics = listOf(
            "paracetamol", "amoxicillin", "metformin", "amlodipine", "telmisartan",
            "atorvastatin", "ceftriaxone", "pantoprazole", "salbutamol", "metronidazole",
            "doxycycline", "ciprofloxacin", "azithromycin", "cetirizine", "ibuprofen", "zinc"
        )
        return generics.any { name.contains(it) } && !hasBrandIndicators(name)
    }

    private fun extractOrGuessGeneric(raw: String): String {
        val lower = raw.lowercase()
        if (lower.contains("dolo") || lower.contains("calpol") || lower.contains("crocin")) return "Paracetamol"
        if (lower.contains("augmentin")) return "Amoxicillin + Clavulanic Acid"
        if (lower.contains("pan") || lower.contains("pantocid")) return "Pantoprazole"
        if (lower.contains("monocef")) return "Ceftriaxone"
        if (lower.contains("zifi")) return "Cefixime"
        if (lower.contains("azithral")) return "Azithromycin"
        if (lower.contains("telma")) return "Telmisartan"
        if (lower.contains("glycomet")) return "Metformin"
        if (lower.contains("amlovas")) return "Amlodipine"
        if (lower.contains("atorva")) return "Atorvastatin"
        return raw
    }

    fun auditPrescription(
        rxNumber: String,
        patientName: String,
        patientAge: Int,
        patientGender: String,
        uhid: String,
        department: String,
        dateString: String,
        diagnosis: String,
        allergyStatusDocumented: Boolean,
        allergyDetails: String,
        historyDocumented: Boolean,
        legibleHandwritingOrTyped: Boolean,
        doctorName: String,
        doctorRegNumber: String,
        hasDoctorSignature: Boolean,
        drugs: List<DrugItem>,
        auditorName: String,
        auditorRole: String,
        prevHash: String,
        imageUri: String? = null,
        rawOcrText: String? = null
    ): PrescriptionEntity {
        val deficiencies = mutableListOf<String>()

        // 1. Completeness Evaluation (NABH MOM & COP standards)
        var completenessScore = 0
        if (patientName.isNotBlank() && patientName.length > 2) completenessScore += 10 else deficiencies.add("Incomplete patient name")
        if (patientAge > 0 && patientGender.isNotBlank()) completenessScore += 10 else deficiencies.add("Patient age/gender missing")
        if (uhid.isNotBlank()) completenessScore += 10 else deficiencies.add("UHID/OPD number missing")
        if (dateString.isNotBlank()) completenessScore += 10 else deficiencies.add("Prescription date missing")
        if (diagnosis.isNotBlank() && diagnosis.length > 2) completenessScore += 15 else deficiencies.add("Provisional/Confirmed diagnosis omitted")
        if (allergyStatusDocumented) completenessScore += 15 else deficiencies.add("Allergy status not documented (Mandatory for NABH MOM.5)")
        if (doctorName.isNotBlank() && doctorRegNumber.isNotBlank()) completenessScore += 15 else deficiencies.add("Doctor NMC/SMC registration number missing")
        if (hasDoctorSignature) completenessScore += 15 else deficiencies.add("Doctor signature/validation missing")

        // 2. Rationality Evaluation
        val totalDrugs = drugs.size
        val genericCount = drugs.count { it.isGeneric }
        val antibioticCount = drugs.count { it.isAntibiotic }
        val injectionCount = drugs.count { it.isInjection }
        val edlCount = drugs.count { it.isEdl }
        val scheduleHCount = drugs.count { it.isScheduleH }
        val scheduleH1Count = drugs.count { it.isScheduleH1 }
        val fdcCount = drugs.count { it.isFdc }
        val irrationalFdcCount = drugs.count { it.isIrrationalFdc }
        val isPolypharmacy = totalDrugs >= 5

        var rationalityScore = 100

        if (isPolypharmacy) {
            rationalityScore -= 15
            deficiencies.add("Polypharmacy: $totalDrugs drugs prescribed (NABH target < 5)")
        }

        if (irrationalFdcCount > 0) {
            rationalityScore -= 25
            deficiencies.add("Contains $irrationalFdcCount flagged irrational FDC (DCGI/CDSCO Section 26A)")
        }

        if (antibioticCount > 1) {
            rationalityScore -= 15
            deficiencies.add("Multiple antibiotics ($antibioticCount) prescribed simultaneously")
        }

        if (scheduleH1Count > 0 && diagnosis.isBlank()) {
            rationalityScore -= 20
            deficiencies.add("Schedule H1 antibiotic prescribed without documented clinical indication")
        }

        if (genericCount < (totalDrugs * 0.5) && totalDrugs > 0) {
            rationalityScore -= 15
            deficiencies.add("Low generic prescribing (< 50% generic names; MCI Code of Ethics)")
        }

        if (!legibleHandwritingOrTyped) {
            completenessScore = (completenessScore - 10).coerceAtLeast(0)
            deficiencies.add("Illegible handwriting (NMC guideline: Capital generic letters)")
        }

        val clampedCompleteness = completenessScore.coerceIn(0, 100)
        val clampedRationality = rationalityScore.coerceIn(0, 100)

        // NABH Benchmark: Completeness >= 85% and Rationality >= 80% with 0 irrational FDCs
        val isCompliant = clampedCompleteness >= 85 && clampedRationality >= 80 && irrationalFdcCount == 0

        // Calculate SHA-256 docHash
        val hashContent = "$rxNumber|$patientName|$uhid|$doctorRegNumber|$totalDrugs|$clampedCompleteness|$clampedRationality|$prevHash"
        val docHash = sha256(hashContent)

        val converters = com.example.data.local.Converters()
        val drugsJson = converters.fromDrugList(drugs)

        return PrescriptionEntity(
            rxNumber = rxNumber,
            patientName = patientName,
            patientAge = patientAge,
            patientGender = patientGender,
            uhid = uhid,
            department = department,
            dateString = dateString,
            diagnosis = diagnosis,
            hasDiagnosis = diagnosis.isNotBlank(),
            allergyStatusDocumented = allergyStatusDocumented,
            allergyDetails = allergyDetails.ifBlank { if (allergyStatusDocumented) "NKA (No Known Allergies)" else "Not Documented" },
            historyDocumented = historyDocumented,
            legibleHandwritingOrTyped = legibleHandwritingOrTyped,
            doctorName = doctorName,
            doctorRegNumber = doctorRegNumber,
            hasDoctorSignature = hasDoctorSignature,
            drugsJson = drugsJson,
            totalDrugsCount = totalDrugs,
            genericCount = genericCount,
            antibioticCount = antibioticCount,
            injectionCount = injectionCount,
            edlCount = edlCount,
            scheduleHCount = scheduleHCount,
            scheduleH1Count = scheduleH1Count,
            fdcCount = fdcCount,
            irrationalFdcCount = irrationalFdcCount,
            isPolypharmacy = isPolypharmacy,
            completenessScorePct = clampedCompleteness,
            rationalityScorePct = clampedRationality,
            isCompliantOverall = isCompliant,
            deficienciesList = deficiencies.joinToString("; "),
            auditorName = auditorName,
            auditorRole = auditorRole,
            auditTimestamp = System.currentTimeMillis(),
            docHash = docHash,
            prevHash = prevHash,
            isHashVerified = true,
            imageUri = imageUri,
            rawOcrText = rawOcrText
        )
    }

    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
