package com.ketantech.kalkulatorservis.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Repository untuk akses data nota, pelanggan, dan template.
 */
class AppRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val receiptDao = db.receiptDao()
    private val customerDao = db.customerDao()
    private val templateDao = db.templateDao()

    // Receipt
    fun getRecentReceipts(): Flow<List<Receipt>> = receiptDao.getRecentReceipts()
    fun searchReceipts(query: String): Flow<List<Receipt>> = receiptDao.search("%$query%")
    suspend fun saveReceipt(receipt: Receipt): Long = receiptDao.insert(receipt)
    suspend fun deleteReceipt(id: Long) = receiptDao.deleteById(id)

    // Laporan
    suspend fun getTodayCount(): Int = receiptDao.getTodayCount()
    suspend fun getTodayTotal(): Long = receiptDao.getTodayTotal() ?: 0
    suspend fun getTodayByLevel(): List<LevelSummary> = receiptDao.getTodayByLevel()
    suspend fun getWeekTotal(): Long = receiptDao.getWeekTotal() ?: 0
    suspend fun getMonthTotal(): Long = receiptDao.getMonthTotal() ?: 0

    // Customer
    fun getAllCustomers(): Flow<List<Customer>> = customerDao.getAll()
    fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.search("%$query%")
    suspend fun saveCustomer(customer: Customer): Long = customerDao.insert(customer)
    suspend fun updateCustomerLastService(id: Long) = customerDao.updateLastService(id, java.util.Date())
    suspend fun deleteCustomer(customer: Customer) = customerDao.delete(customer)

    /** Simpan pelanggan baru atau perbarui lastServiceAt jika sudah ada. */
    suspend fun upsertCustomer(name: String) {
        val existing = customerDao.getByName(name)
        if (existing != null) {
            customerDao.updateLastService(existing.id, java.util.Date())
        } else {
            customerDao.insert(Customer(name = name))
        }
    }

    /** Hitung berapa kali pelanggan servis (dari tabel nota). */
    suspend fun countServicesForCustomer(name: String): Int = receiptDao.countByCustomer(name)

    /** Riwayat nota milik satu pelanggan. */
    fun getReceiptsByCustomer(name: String): Flow<List<Receipt>> = receiptDao.getByCustomer(name)

    // Template
    fun getAllTemplates(): Flow<List<ServiceTemplate>> = templateDao.getAll()
    suspend fun saveTemplate(template: ServiceTemplate): Long = templateDao.insert(template)
    suspend fun updateTemplate(template: ServiceTemplate) = templateDao.update(template)
    suspend fun deleteTemplate(id: Long) = templateDao.deleteById(id)
}
