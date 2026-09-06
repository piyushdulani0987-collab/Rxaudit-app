package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.DrugItem
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    @TypeConverter
    fun fromDrugList(drugs: List<DrugItem>?): String {
        if (drugs.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        for (drug in drugs) {
            val obj = JSONObject()
            obj.put("brandName", drug.brandName)
            obj.put("genericName", drug.genericName)
            obj.put("dose", drug.dose)
            obj.put("frequency", drug.frequency)
            obj.put("route", drug.route)
            obj.put("durationDays", drug.durationDays)
            obj.put("isGeneric", drug.isGeneric)
            obj.put("isAntibiotic", drug.isAntibiotic)
            obj.put("antibioticClass", drug.antibioticClass)
            obj.put("isInjection", drug.isInjection)
            obj.put("isEdl", drug.isEdl)
            obj.put("isScheduleH", drug.isScheduleH)
            obj.put("isScheduleH1", drug.isScheduleH1)
            obj.put("isFdc", drug.isFdc)
            obj.put("isIrrationalFdc", drug.isIrrationalFdc)
            obj.put("instructions", drug.instructions)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toDrugList(json: String?): List<DrugItem> {
        if (json.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<DrugItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    DrugItem(
                        brandName = obj.optString("brandName", ""),
                        genericName = obj.optString("genericName", ""),
                        dose = obj.optString("dose", ""),
                        frequency = obj.optString("frequency", ""),
                        route = obj.optString("route", ""),
                        durationDays = obj.optInt("durationDays", 0),
                        isGeneric = obj.optBoolean("isGeneric", false),
                        isAntibiotic = obj.optBoolean("isAntibiotic", false),
                        antibioticClass = obj.optString("antibioticClass", ""),
                        isInjection = obj.optBoolean("isInjection", false),
                        isEdl = obj.optBoolean("isEdl", false),
                        isScheduleH = obj.optBoolean("isScheduleH", false),
                        isScheduleH1 = obj.optBoolean("isScheduleH1", false),
                        isFdc = obj.optBoolean("isFdc", false),
                        isIrrationalFdc = obj.optBoolean("isIrrationalFdc", false),
                        instructions = obj.optString("instructions", "")
                    )
                )
            }
        } catch (_: Exception) {
            // Return empty list on parse error
        }
        return list
    }
}
