package com.ketantech.kalkulatorservis.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@Database(
    entities = [Receipt::class, Customer::class, ServiceTemplate::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
    abstract fun customerDao(): CustomerDao
    abstract fun templateDao(): TemplateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ketantech_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Pre-populate dengan template default
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDefaultTemplates(database.templateDao())
                }
            }
        }

        private suspend fun populateDefaultTemplates(dao: TemplateDao) {
            dao.insert(ServiceTemplate(
                name = "Ganti LCD Samsung",
                deviceName = "Samsung",
                sparepartCost = 450000,
                serviceLevel = 2,
                isDefault = true
            ))
            dao.insert(ServiceTemplate(
                name = "Ganti Baterai",
                deviceName = "Universal",
                sparepartCost = 150000,
                serviceLevel = 2,
                isDefault = true
            ))
            dao.insert(ServiceTemplate(
                name = "Flash / Software",
                deviceName = "Universal",
                sparepartCost = 0,
                serviceLevel = 1,
                isDefault = true
            ))
        }
    }
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
