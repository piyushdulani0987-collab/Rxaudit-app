package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AuditHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for querying and persisting tamper-evident audit history records.
 */
@Dao
interface AuditHistoryDao {

    @Query("SELECT * FROM audit_history ORDER BY timestamp DESC")
    fun getAllAuditHistory(): Flow<List<AuditHistoryEntity>>

    @Query("SELECT * FROM audit_history WHERE auditId = :auditId")
    fun getAuditRecordById(auditId: Long): Flow<AuditHistoryEntity?>

    @Query("SELECT * FROM audit_history WHERE rxNumber = :rxNumber ORDER BY timestamp DESC")
    fun getAuditHistoryByRxNumber(rxNumber: String): Flow<List<AuditHistoryEntity>>

    @Query("SELECT * FROM audit_history WHERE prescriberRegNumber = :regNumber ORDER BY timestamp DESC")
    fun getAuditRecordsByPrescriber(regNumber: String): Flow<List<AuditHistoryEntity>>

    @Query("SELECT * FROM audit_history WHERE hasDrugInteractions = 1 ORDER BY timestamp DESC")
    fun getAuditRecordsWithDrugInteractions(): Flow<List<AuditHistoryEntity>>

    @Query("SELECT * FROM audit_history WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getAuditHistoryByDateRange(startTime: Long, endTime: Long): Flow<List<AuditHistoryEntity>>

    @Query("SELECT * FROM audit_history ORDER BY auditId DESC LIMIT 1")
    suspend fun getLatestAuditRecord(): AuditHistoryEntity?

    @Query("SELECT COUNT(*) FROM audit_history")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AuditHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<AuditHistoryEntity>)

    @Query("DELETE FROM audit_history WHERE auditId = :auditId")
    suspend fun deleteById(auditId: Long)

    @Query("DELETE FROM audit_history")
    suspend fun clearAll()
}
