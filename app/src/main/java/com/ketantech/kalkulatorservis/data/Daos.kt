package com.ketantech.kalkulatorservis.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ReceiptDao {
    @Query("SELECT * FROM receipts ORDER BY createdAt DESC LIMIT 50")
    fun getRecentReceipts(): Flow<List<Receipt>>

    @Query("SELECT * FROM receipts WHERE receiptNumber = :number")
    suspend fun getByNumber(number: String): Receipt?

    @Query("SELECT * FROM receipts WHERE customerName LIKE :query OR deviceName LIKE :query OR receiptNumber LIKE :query ORDER BY createdAt DESC")
    fun search(query: String): Flow<List<Receipt>>

    @Insert
    suspend fun insert(receipt: Receipt): Long

    @Delete
    suspend fun delete(receipt: Receipt)

    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun deleteById(id: Long)

    // Laporan harian
    @Query("SELECT COUNT(*) FROM receipts WHERE date(createdAt/1000, 'unixepoch') = date('now')")
    suspend fun getTodayCount(): Int

    @Query("SELECT SUM(total) FROM receipts WHERE date(createdAt/1000, 'unixepoch') = date('now')")
    suspend fun getTodayTotal(): Long?

    @Query("SELECT serviceLevel, COUNT(*) as count, SUM(total) as total FROM receipts WHERE date(createdAt/1000, 'unixepoch') = date('now') GROUP BY serviceLevel")
    suspend fun getTodayByLevel(): List<LevelSummary>

    @Query("SELECT SUM(total) FROM receipts WHERE date(createdAt/1000, 'unixepoch') >= date('now', '-7 days')")
    suspend fun getWeekTotal(): Long?

    @Query("SELECT SUM(total) FROM receipts WHERE date(createdAt/1000, 'unixepoch') >= date('now', '-30 days')")
    suspend fun getMonthTotal(): Long?

    @Query("SELECT COUNT(*) FROM receipts WHERE customerName = :name")
    suspend fun countByCustomer(name: String): Int

    @Query("SELECT * FROM receipts WHERE customerName = :name ORDER BY createdAt DESC")
    fun getByCustomer(name: String): Flow<List<Receipt>>
}

data class LevelSummary(
    val serviceLevel: Int,
    val count: Int,
    val total: Long
)

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY lastServiceAt DESC")
    fun getAll(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE name LIKE :query ORDER BY lastServiceAt DESC LIMIT 5")
    fun search(query: String): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: Customer): Long

    @Query("UPDATE customers SET lastServiceAt = :date WHERE id = :id")
    suspend fun updateLastService(id: Long, date: Date)

    @Delete
    suspend fun delete(customer: Customer)
}

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates ORDER BY isDefault DESC, createdAt DESC")
    fun getAll(): Flow<List<ServiceTemplate>>

    @Query("SELECT * FROM templates WHERE isDefault = 1")
    fun getDefaults(): Flow<List<ServiceTemplate>>

    @Insert
    suspend fun insert(template: ServiceTemplate): Long

    @Update
    suspend fun update(template: ServiceTemplate)

    @Delete
    suspend fun delete(template: ServiceTemplate)

    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun deleteById(id: Long)
}
