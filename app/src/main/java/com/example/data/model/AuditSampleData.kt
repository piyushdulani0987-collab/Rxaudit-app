package com.example.data.model

import com.example.domain.AuditRulesEngine

object AuditSampleData {

    fun getInitialClinicalSamplePrescriptions(auditorName: String = "Dr. S. Mukherjee, MD", auditorRole: String = "Convener, Hospital Clinical Audit Committee"): List<PrescriptionEntity> {
        val list = mutableListOf<PrescriptionEntity>()
        var lastHash = "0000000000000000000000000000000000000000000000000000000000000000"

        // 1. Medicine OPD (Compliant Chronic Disease)
        val drugs1 = listOf(
            AuditRulesEngine.evaluateDrug("Tab Metformin (Generic)", "500 mg", "Oral", "BD", 30, "After food"),
            AuditRulesEngine.evaluateDrug("Tab Amlodipine (Generic)", "5 mg", "Oral", "OD", 30, "Morning"),
            AuditRulesEngine.evaluateDrug("Tab Atorvastatin (Generic)", "10 mg", "Oral", "HS", 30, "Night")
        )
        val rx1 = AuditRulesEngine.auditPrescription(
            rxNumber = "RX-2026-MED-101",
            patientName = "Ramesh Chand Sharma",
            patientAge = 58,
            patientGender = "Male",
            uhid = "UHID-88219",
            department = "General Medicine",
            dateString = "04/09/2026",
            diagnosis = "Essential Hypertension with Type 2 Diabetes Mellitus",
            allergyStatusDocumented = true,
            allergyDetails = "NKA (No Known Drug Allergies)",
            historyDocumented = true,
            legibleHandwritingOrTyped = true,
            doctorName = "Dr. P. K. Verma, MD",
            doctorRegNumber = "MCI-48201",
            hasDoctorSignature = true,
            drugs = drugs1,
            auditorName = auditorName,
            auditorRole = auditorRole,
            prevHash = lastHash
        )
        list.add(rx1)
        lastHash = rx1.docHash

        // 2. Paediatric Clinic (Deficiency: Allergy not documented)
        val drugs2 = listOf(
            AuditRulesEngine.evaluateDrug("Syp Augmentin (Amoxicillin + Clavulanate)", "228.5 mg / 5ml", "Oral", "BD", 5, "After food"),
            AuditRulesEngine.evaluateDrug("Syp Paracetamol (Generic)", "250 mg / 5ml", "Oral", "TDS", 3, "SOS fever"),
            AuditRulesEngine.evaluateDrug("Salbutamol Respirator Solution", "2.5 mg", "Inhalation", "TDS", 3, "Nebulization")
        )
        val rx2 = AuditRulesEngine.auditPrescription(
            rxNumber = "RX-2026-PED-204",
            patientName = "Aarav Kulkarni",
            patientAge = 4,
            patientGender = "Male",
            uhid = "UHID-91402",
            department = "Paediatrics",
            dateString = "04/09/2026",
            diagnosis = "Acute Bronchiolitis with Wheeze",
            allergyStatusDocumented = false, // Deficiency!
            allergyDetails = "Not Documented",
            historyDocumented = true,
            legibleHandwritingOrTyped = true,
            doctorName = "Dr. Ananya Sen, DCH",
            doctorRegNumber = "WBMC-68421",
            hasDoctorSignature = true,
            drugs = drugs2,
            auditorName = auditorName,
            auditorRole = auditorRole,
            prevHash = lastHash
        )
        list.add(rx2)
        lastHash = rx2.docHash

        // 3. Casualty/Emergency (Schedule H1 & Injections)
        val drugs3 = listOf(
            AuditRulesEngine.evaluateDrug("Inj Ceftriaxone (Monocef)", "1 g", "IV", "BD", 3, "IV infusion slowly"),
            AuditRulesEngine.evaluateDrug("Inj Tramadol", "50 mg", "IV", "SOS", 2, "Diluted in 100ml NS"),
            AuditRulesEngine.evaluateDrug("Inj Pantoprazole", "40 mg", "IV", "OD", 3, "Slow IV push"),
            AuditRulesEngine.evaluateDrug("IV Normal Saline 0.9%", "500 ml", "IV", "Once", 1, "Infusion 80ml/hr")
        )
        val rx3 = AuditRulesEngine.auditPrescription(
            rxNumber = "RX-2026-EMG-309",
            patientName = "Sunita Devi",
            patientAge = 42,
            patientGender = "Female",
            uhid = "UHID-77319",
            department = "Emergency/Casualty",
            dateString = "05/09/2026",
            diagnosis = "Acute Abdomen with Suspected Peritonitis",
            allergyStatusDocumented = true,
            allergyDetails = "Sulfa drugs allergy noted",
            historyDocumented = true,
            legibleHandwritingOrTyped = true,
            doctorName = "Dr. Vikram Rathore, MS",
            doctorRegNumber = "DMC-59302",
            hasDoctorSignature = true,
            drugs = drugs3,
            auditorName = auditorName,
            auditorRole = auditorRole,
            prevHash = lastHash
        )
        list.add(rx3)
        lastHash = rx3.docHash

        // 4. Orthopaedics (Deficiency: Irrational FDC)
        val drugs4 = listOf(
            AuditRulesEngine.evaluateDrug("Tab Aceclofenac + Paracetamol + Rabeprazole", "100/325/20 mg", "Oral", "BD", 5, "After food"), // Irrational FDC
            AuditRulesEngine.evaluateDrug("Tab Calcium + Vitamin D3 (Generic)", "500 mg / 400 IU", "Oral", "OD", 30, "With milk"),
            AuditRulesEngine.evaluateDrug("Tab Thiocolchicoside", "4 mg", "Oral", "BD", 5, "Muscle spasm")
        )
        val rx4 = AuditRulesEngine.auditPrescription(
            rxNumber = "RX-2026-ORT-415",
            patientName = "Jagdish Prasad",
            patientAge = 64,
            patientGender = "Male",
            uhid = "UHID-66291",
            department = "Orthopaedics",
            dateString = "05/09/2026",
            diagnosis = "Bilateral Knee Osteoarthritis with Muscle Spasm",
            allergyStatusDocumented = true,
            allergyDetails = "NKA",
            historyDocumented = false,
            legibleHandwritingOrTyped = true,
            doctorName = "Dr. S. K. Nair, MS Ortho",
            doctorRegNumber = "TCMC-39182",
            hasDoctorSignature = true,
            drugs = drugs4,
            auditorName = auditorName,
            auditorRole = auditorRole,
            prevHash = lastHash
        )
        list.add(rx4)
        lastHash = rx4.docHash

        // 5. Chest Medicine (Deficiency: Polypharmacy >= 5 drugs)
        val drugs5 = listOf(
            AuditRulesEngine.evaluateDrug("Tab Azithromycin", "500 mg", "Oral", "OD", 5, "1 hr before food"),
            AuditRulesEngine.evaluateDrug("Tab Prednisolone", "20 mg", "Oral", "OD", 5, "Morning with breakfast"),
            AuditRulesEngine.evaluateDrug("Inhaler Budesonide + Formoterol", "200/6 mcg", "Inhalation", "BD", 30, "2 puffs"),
            AuditRulesEngine.evaluateDrug("Tab Pantoprazole", "40 mg", "Oral", "OD", 10, "Empty stomach"),
            AuditRulesEngine.evaluateDrug("Tab Deriphyllin Retard", "150 mg", "Oral", "BD", 7, "After food"),
            AuditRulesEngine.evaluateDrug("Tab Montelukast", "10 mg", "Oral", "HS", 15, "Bedtime")
        )
        val rx5 = AuditRulesEngine.auditPrescription(
            rxNumber = "RX-2026-PUL-512",
            patientName = "Mohammad Ismail",
            patientAge = 69,
            patientGender = "Male",
            uhid = "UHID-55018",
            department = "General Medicine",
            dateString = "06/09/2026",
            diagnosis = "Acute Exacerbation of COPD with Secondary Bronchial Infection",
            allergyStatusDocumented = true,
            allergyDetails = "NKA",
            historyDocumented = true,
            legibleHandwritingOrTyped = true,
            doctorName = "Dr. Rajesh Gupta, MD Chest",
            doctorRegNumber = "MMC-41209",
            hasDoctorSignature = true,
            drugs = drugs5,
            auditorName = auditorName,
            auditorRole = auditorRole,
            prevHash = lastHash
        )
        list.add(rx5)
        lastHash = rx5.docHash

        // 6. Community Pharmacy Walk-in (Deficiency: Missing doctor registration & diagnosis)
        val drugs6 = listOf(
            AuditRulesEngine.evaluateDrug("Tab Ciprobid (Ciprofloxacin)", "500 mg", "Oral", "BD", 5, "After food"),
            AuditRulesEngine.evaluateDrug("Tab Dolo-650 (Paracetamol)", "650 mg", "Oral", "TDS", 3, "SOS fever")
        )
        val rx6 = AuditRulesEngine.auditPrescription(
            rxNumber = "RX-2026-PHR-088",
            patientName = "Kavita Rao",
            patientAge = 29,
            patientGender = "Female",
            uhid = "EXT-1049",
            department = "Community Pharmacy",
            dateString = "06/09/2026",
            diagnosis = "", // Missing diagnosis!
            allergyStatusDocumented = false, // Missing allergy!
            allergyDetails = "Not Documented",
            historyDocumented = false,
            legibleHandwritingOrTyped = false, // Illegible
            doctorName = "Dr. Verma",
            doctorRegNumber = "", // Missing Reg No!
            hasDoctorSignature = true,
            drugs = drugs6,
            auditorName = auditorName,
            auditorRole = auditorRole,
            prevHash = lastHash
        )
        list.add(rx6)

        return list
    }
}
