package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.PrescriptionEntity

@Database(entities = [PrescriptionEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AuditDatabase : RoomDatabase() {

    abstract fun prescriptionDao(): PrescriptionDao

    companion object {
        @Volatile
        private var INSTANCE: AuditDatabase? = null

        fun getDatabase(context: Context): AuditDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AuditDatabase::class.java,
                    "prescription_audit.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
