package com.example.data.model

data class DrugInteractionAlert(
    val drugA: String,
    val drugB: String,
    val severity: InteractionSeverity, // SEVERE, MODERATE, CONTRAINDICATED
    val mechanism: String,
    val clinicalConsequence: String,
    val recommendation: String
)

enum class InteractionSeverity {
    CONTRAINDICATED, SEVERE, MODERATE
}

object DrugInteractionEngine {
    fun checkInteractions(drugs: List<DrugItem>): List<DrugInteractionAlert> {
        val alerts = mutableListOf<DrugInteractionAlert>()
        val names = drugs.map { "${it.genericName} ${it.brandName}".lowercase() }

        fun hasDrug(keywords: List<String>): Boolean {
            return names.any { name -> keywords.any { keyword -> name.contains(keyword) } }
        }

        fun findMatchingDrug(keywords: List<String>): String {
            for (drug in drugs) {
                val combined = "${drug.genericName} ${drug.brandName}".lowercase()
                if (keywords.any { combined.contains(it) }) {
                    val gName = drug.genericName
                    return if (gName.isNotBlank()) gName else drug.brandName
                }
            }
            return ""
        }

        // Rule 1: NSAID + Anticoagulant (GI Bleeding Risk)
        val nsaids = listOf("ibuprofen", "diclofenac", "naproxen", "aspirin", "ketorolac")
        val anticoagulants = listOf("warfarin", "heparin", "apixaban", "rivaroxaban", "dabigatran")
        if (hasDrug(nsaids) && hasDrug(anticoagulants)) {
            val d1 = findMatchingDrug(nsaids)
            val d2 = findMatchingDrug(anticoagulants)
            alerts.add(
                DrugInteractionAlert(
                    drugA = d1,
                    drugB = d2,
                    severity = InteractionSeverity.CONTRAINDICATED,
                    mechanism = "Additive antiplatelet / anticoagulant and ulcerogenic gastric mucosal damage.",
                    clinicalConsequence = "Severe Gastrointestinal Bleeding, Peptic Ulcer Hemorrhage, and prolonged bleeding time.",
                    recommendation = "Avoid concurrent use. If necessary, co-prescribe a Proton Pump Inhibitor (PPI) like Pantoprazole or substitute with safer analgesics (e.g. Paracetamol)."
                )
            )
        }

        // Rule 2: Opioid + Benzodiazepine / Sedative (CNS & Respiratory Depression)
        val opioids = listOf("tramadol", "morphine", "codeine", "oxycodone", "fentanyl")
        val sedatives = listOf("alprazolam", "diazepam", "lorazepam", "clonazepam", "zolpidem")
        if (hasDrug(opioids) && hasDrug(sedatives)) {
            val d1 = findMatchingDrug(opioids)
            val d2 = findMatchingDrug(sedatives)
            alerts.add(
                DrugInteractionAlert(
                    drugA = d1,
                    drugB = d2,
                    severity = InteractionSeverity.SEVERE,
                    mechanism = "Synergistic central nervous system (CNS) depression and respiratory center suppression.",
                    clinicalConsequence = "Profound sedation, respiratory depression, coma, and potential fatal overdose.",
                    recommendation = "Avoid co-prescription unless strictly indicated. Monitor respiratory rate and oxygen saturation closely; use lowest effective doses for shortest duration."
                )
            )
        }

        // Rule 3: SSRI / SNRI + Tramadol / MAOI (Serotonin Syndrome)
        val ssris = listOf("sertraline", "fluoxetine", "citalopram", "escitalopram", "paroxetine", "duloxetine")
        val serotonergics = listOf("tramadol", "linezolid", "lithium")
        if (hasDrug(ssris) && hasDrug(serotonergics)) {
            val d1 = findMatchingDrug(ssris)
            val d2 = findMatchingDrug(serotonergics)
            alerts.add(
                DrugInteractionAlert(
                    drugA = d1,
                    drugB = d2,
                    severity = InteractionSeverity.SEVERE,
                    mechanism = "Excessive central serotonergic agonism across synaptic clefts.",
                    clinicalConsequence = "Serotonin Syndrome: Hyperthermia, neuromuscular hyperactivity (clonus, tremor), autonomic instability, and mental status changes.",
                    recommendation = "Monitor for signs of serotonin toxicity. Avoid combination or taper serotonergic agents; consider alternative analgesics."
                )
            )
        }

        // Rule 4: ACE Inhibitor / ARB + Potassium-Sparing Diuretic (Severe Hyperkalemia)
        val aceInhibitors = listOf("enalapril", "lisinopril", "ramipril", "losartan", "valsartan")
        val potassiumDiuretics = listOf("spironolactone", "eplerenone", "amiloride", "potassium chloride")
        if (hasDrug(aceInhibitors) && hasDrug(potassiumDiuretics)) {
            val d1 = findMatchingDrug(aceInhibitors)
            val d2 = findMatchingDrug(potassiumDiuretics)
            alerts.add(
                DrugInteractionAlert(
                    drugA = d1,
                    drugB = d2,
                    severity = InteractionSeverity.SEVERE,
                    mechanism = "Impaired aldosterone-mediated renal potassium excretion combined with decreased angiotensin II.",
                    clinicalConsequence = "Life-threatening Hyperkalemia leading to cardiac arrhythmias and conduction blocks.",
                    recommendation = "Monitor serum potassium and renal function (eGFR) within 1-2 weeks of initiation. Avoid potassium supplements."
                )
            )
        }

        // Rule 5: Macrolide Antibiotic (Azithromycin/Erythromycin) + QT Prolonging Agents
        val macrolides = listOf("azithromycin", "clarithromycin", "erythromycin")
        val qtProlonging = listOf("ondansetron", "haloperidol", "amiodarone", "chloroquine", "tramadol")
        if (hasDrug(macrolides) && hasDrug(qtProlonging)) {
            val d1 = findMatchingDrug(macrolides)
            val d2 = findMatchingDrug(qtProlonging)
            alerts.add(
                DrugInteractionAlert(
                    drugA = d1,
                    drugB = d2,
                    severity = InteractionSeverity.SEVERE,
                    mechanism = "Additive cardiac potassium channel (hERG) blockade and ventricular repolarization delay.",
                    clinicalConsequence = "QT interval prolongation, Torsades de Pointes (TdP), and fatal ventricular arrhythmias.",
                    recommendation = "Check baseline ECG if patient has cardiac history or electrolyte disturbances. Avoid concurrent use if QT interval exceeds 450ms."
                )
            )
        }

        return alerts
    }
}
