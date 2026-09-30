package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PrescriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrescriptionDao {

    @Query("SELECT * FROM prescriptions ORDER BY auditTimestamp DESC")
    fun getAllPrescriptions(): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions WHERE id = :id")
    fun getPrescriptionById(id: Long): Flow<PrescriptionEntity?>

    @Query("SELECT * FROM prescriptions WHERE department = :dept ORDER BY auditTimestamp DESC")
    fun getPrescriptionsByDepartment(dept: String): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions WHERE isCompliantOverall = 0 ORDER BY auditTimestamp DESC")
    fun getNonCompliantPrescriptions(): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions WHERE antibioticCount > 0 ORDER BY auditTimestamp DESC")
    fun getAntibioticPrescriptions(): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions WHERE injectionCount > 0 ORDER BY auditTimestamp DESC")
    fun getInjectionPrescriptions(): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions WHERE isPolypharmacy = 1 ORDER BY auditTimestamp DESC")
    fun getPolypharmacyPrescriptions(): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions WHERE scheduleH1Count > 0 ORDER BY auditTimestamp DESC")
    fun getScheduleH1Prescriptions(): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions WHERE patientName LIKE '%' || :searchQuery || '%' OR doctorName LIKE '%' || :searchQuery || '%' OR drugsJson LIKE '%' || :searchQuery || '%' ORDER BY auditTimestamp DESC")
    fun searchPrescriptions(searchQuery: String): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions ORDER BY id DESC LIMIT 1")
    suspend fun getLatestPrescription(): PrescriptionEntity?

    @Query("SELECT COUNT(*) FROM prescriptions")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prescription: PrescriptionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prescriptions: List<PrescriptionEntity>)

    @Query("DELETE FROM prescriptions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM prescriptions")
    suspend fun clearAll()
}
