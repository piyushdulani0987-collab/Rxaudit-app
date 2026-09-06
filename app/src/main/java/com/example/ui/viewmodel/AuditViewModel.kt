package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AuditDatabase
import com.example.data.model.AuditIndicators
import com.example.data.model.PrescriptionEntity
import com.example.data.model.SampleSizeEstimate
import com.example.data.repository.PrescriptionRepository
import com.example.domain.AuditReportExporter
import com.example.domain.ParsedPrescription
import com.example.domain.PrescriptionOcrParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FilterOption(val label: String) {
    ALL("All Prescriptions"),
    NON_COMPLIANT("Non-Compliant Flagged"),
    COMPLIANT("NABH Compliant"),
    ANTIBIOTICS("With Antibiotics (AWaRe)"),
    INJECTIONS("With Injections"),
    POLYPHARMACY("Polypharmacy (≥5)"),
    SCHEDULE_H1("Schedule H1 Register")
}

class AuditViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PrescriptionRepository
    init {
        val database = AuditDatabase.getDatabase(application)
        repository = PrescriptionRepository(database.prescriptionDao())
        viewModelScope.launch {
            repository.loadSampleBatchIfEmpty()
        }
    }

    private val _selectedFilter = MutableStateFlow(FilterOption.ALL)
    val selectedFilter: StateFlow<FilterOption> = _selectedFilter.asStateFlow()

    private val _selectedDepartment = MutableStateFlow("All")
    val selectedDepartment: StateFlow<String> = _selectedDepartment.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _auditorName = MutableStateFlow("Dr. S. Mukherjee, MD")
    val auditorName: StateFlow<String> = _auditorName.asStateFlow()

    private val _auditorRole = MutableStateFlow("Convener, Clinical Audit Committee")
    val auditorRole: StateFlow<String> = _auditorRole.asStateFlow()

    private val _facilityName = MutableStateFlow("District Hospital & Medical College")
    val facilityName: StateFlow<String> = _facilityName.asStateFlow()

    private val _monthlyFootfall = MutableStateFlow(1500)
    val monthlyFootfall: StateFlow<Int> = _monthlyFootfall.asStateFlow()

    // Parsing / Scan state
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _pendingParsedPrescription = MutableStateFlow<ParsedPrescription?>(null)
    val pendingParsedPrescription: StateFlow<ParsedPrescription?> = _pendingParsedPrescription.asStateFlow()

    private val _selectedPrescription = MutableStateFlow<PrescriptionEntity?>(null)
    val selectedPrescription: StateFlow<PrescriptionEntity?> = _selectedPrescription.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val allPrescriptions: StateFlow<List<PrescriptionEntity>> = repository.allPrescriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val indicators: StateFlow<AuditIndicators> = repository.computedIndicators
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuditIndicators())

    val sampleSizeEstimate: StateFlow<SampleSizeEstimate> = combine(indicators, _monthlyFootfall) { ind, footfall ->
        AuditReportExporter.calculateSampleSize(footfall, ind.totalPrescriptions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleSizeEstimate(1500, 95.0, 5.0, 306, 0, false, ""))

    val filteredPrescriptions: StateFlow<List<PrescriptionEntity>> = combine(
        allPrescriptions,
        _selectedFilter,
        _selectedDepartment,
        _searchQuery
    ) { list, filter, dept, query ->
        list.filter { rx ->
            val matchesFilter = when (filter) {
                FilterOption.ALL -> true
                FilterOption.NON_COMPLIANT -> !rx.isCompliantOverall
                FilterOption.COMPLIANT -> rx.isCompliantOverall
                FilterOption.ANTIBIOTICS -> rx.antibioticCount > 0
                FilterOption.INJECTIONS -> rx.injectionCount > 0
                FilterOption.POLYPHARMACY -> rx.isPolypharmacy
                FilterOption.SCHEDULE_H1 -> rx.scheduleH1Count > 0
            }

            val matchesDept = dept == "All" || rx.department.equals(dept, ignoreCase = true)

            val matchesQuery = query.isBlank() ||
                    rx.patientName.contains(query, ignoreCase = true) ||
                    rx.uhid.contains(query, ignoreCase = true) ||
                    rx.doctorName.contains(query, ignoreCase = true) ||
                    rx.rxNumber.contains(query, ignoreCase = true) ||
                    rx.diagnosis.contains(query, ignoreCase = true)

            matchesFilter && matchesDept && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: FilterOption) {
        _selectedFilter.value = filter
    }

    fun setDepartment(dept: String) {
        _selectedDepartment.value = dept
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFacilityName(name: String) {
        _facilityName.value = name
    }

    fun setAuditorInfo(name: String, role: String) {
        _auditorName.value = name
        _auditorRole.value = role
    }

    fun setMonthlyFootfall(footfall: Int) {
        _monthlyFootfall.value = footfall
    }

    fun selectPrescription(rx: PrescriptionEntity?) {
        _selectedPrescription.value = rx
    }

    fun clearPendingParsed() {
        _pendingParsedPrescription.value = null
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    /**
     * One-Tap Prescription Audit Trigger:
     * Parses text/image, checks rules, calculates scores, and shows review dialog
     */
    fun processPrescriptionInput(rawInput: String, imageUri: String? = null) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                // Try Gemini first if online & configured, otherwise fallback to high-precision local medical parser
                val geminiResult = PrescriptionOcrParser.analyzeWithGeminiIfAvailable(rawInput)
                val parsed = geminiResult ?: PrescriptionOcrParser.parsePrescriptionText(rawInput)
                _pendingParsedPrescription.value = parsed
            } catch (e: Exception) {
                _userMessage.value = "OCR Parsing note: Using standard clinical rules engine (${e.localizedMessage})"
                _pendingParsedPrescription.value = PrescriptionOcrParser.parsePrescriptionText(rawInput)
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /**
     * Confirms and commits the audited prescription to the Room database with SHA-256 hash chaining
     */
    fun commitAudit(parsed: ParsedPrescription, imageUri: String? = null) {
        viewModelScope.launch {
            val newId = repository.auditAndInsert(
                parsed = parsed,
                auditorName = _auditorName.value,
                auditorRole = _auditorRole.value,
                imageUri = imageUri
            )
            _pendingParsedPrescription.value = null
            _userMessage.value = "Prescription #${parsed.rxNumber} audited and committed with SHA-256 docHash"
        }
    }

    fun deletePrescription(id: Long) {
        viewModelScope.launch {
            repository.deletePrescription(id)
            if (_selectedPrescription.value?.id == id) {
                _selectedPrescription.value = null
            }
            _userMessage.value = "Prescription record removed from audit register"
        }
    }

    fun resetToClinicalSamples() {
        viewModelScope.launch {
            repository.resetWithSamples()
            _userMessage.value = "Restored standard hospital clinical audit dataset"
        }
    }

    fun getExportableCsv(): String {
        return AuditReportExporter.generateCsvAuditReport(allPrescriptions.value, _facilityName.value)
    }

    fun getExecutiveSummary(): String {
        return AuditReportExporter.generateExecutiveSummary(indicators.value, _facilityName.value)
    }
}
