package com.example.data.remote

import com.example.domain.ParsedPrescription
import com.example.data.model.DrugItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object FhirClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun fetchMedicationRequests(serverUrl: String): List<ParsedPrescription> = withContext(Dispatchers.IO) {
        val baseUrl = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
        // Adding specific search parameter or just fetching recent MedicationRequests
        val requestUrl = "${baseUrl}MedicationRequest?_count=10&_format=json"
        
        val request = Request.Builder()
            .url(requestUrl)
            .header("Accept", "application/json")
            .build()
            
        val response = client.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw Exception("Failed to fetch from FHIR Server: ${response.code}")
        }
        
        val body = response.body?.string() ?: throw Exception("Empty response body")
        val json = JSONObject(body)
        
        val parsedList = mutableListOf<ParsedPrescription>()
        
        if (json.has("entry")) {
            val entries = json.getJSONArray("entry")
            for (i in 0 until entries.length()) {
                try {
                    val entry = entries.getJSONObject(i)
                    if (!entry.has("resource")) continue
                    
                    val resource = entry.getJSONObject("resource")
                    if (resource.optString("resourceType") != "MedicationRequest") continue
                    
                    val id = resource.optString("id", "FHIR-${(1000..9999).random()}")
                    
                    // Parse medication name
                    var medicationName = "Unknown Medication"
                    if (resource.has("medicationCodeableConcept")) {
                        val cc = resource.getJSONObject("medicationCodeableConcept")
                        medicationName = cc.optString("text", "Unknown Medication")
                        if (medicationName == "Unknown Medication" && cc.has("coding")) {
                            val coding = cc.getJSONArray("coding")
                            if (coding.length() > 0) {
                                medicationName = coding.getJSONObject(0).optString("display", "Unknown Medication")
                            }
                        }
                    } else if (resource.has("medicationReference")) {
                         val ref = resource.getJSONObject("medicationReference")
                         medicationName = ref.optString("display", ref.optString("reference", "Unknown Medication"))
                    }

                    // Parse patient ref
                    var patientId = "PT-UNKNOWN"
                    if (resource.has("subject")) {
                        val subject = resource.getJSONObject("subject")
                        patientId = subject.optString("reference", "PT-UNKNOWN").replace("Patient/", "PT-")
                    }

                    // Parse date
                    var authoredOn = resource.optString("authoredOn", "")
                    if (authoredOn.length > 10) {
                        authoredOn = authoredOn.substring(0, 10)
                    }
                    if (authoredOn.isEmpty()) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        authoredOn = sdf.format(Date())
                    }

                    // For demo purposes since public test servers often lack full detail, we simulate some context
                    val drugItem = DrugItem(
                        brandName = "N/A",
                        genericName = medicationName,
                        dose = "As prescribed",
                        frequency = "OD",
                        durationDays = 5,
                        route = "Oral",
                        isGeneric = true,
                        isAntibiotic = false,
                        isInjection = false,
                        isEdl = true,
                        isScheduleH = false,
                        isScheduleH1 = false
                    )

                    val parsedRx = ParsedPrescription(
                        rxNumber = "RX-$id",
                        patientName = "FHIR Patient ($patientId)",
                        patientAge = 45,
                        patientGender = "Unknown",
                        uhid = patientId,
                        department = "Internal Medicine",
                        dateString = authoredOn,
                        diagnosis = "FHIR Imported Condition",
                        icd10Code = "",
                        allergyStatusDocumented = true,
                        allergyDetails = "N/A",
                        historyDocumented = true,
                        legibleHandwritingOrTyped = true,
                        doctorName = "EHR System",
                        doctorRegNumber = "EHR-9999",
                        hasDoctorSignature = true,
                        drugs = listOf(drugItem),
                        rawText = "Imported from EHR FHIR Interface"
                    )
                    parsedList.add(parsedRx)
                } catch (e: Exception) {
                    // Skip malformed entries
                    e.printStackTrace()
                }
            }
        }
        
        parsedList
    }
}
