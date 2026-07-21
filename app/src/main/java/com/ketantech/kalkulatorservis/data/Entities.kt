package com.ketantech.kalkulatorservis.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity untuk riwayat nota servis.
 */
@Entity(tableName = "receipts")
data class Receipt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val receiptNumber: String,
    val deviceName: String,
    val customerName: String,
    val sparepartCost: Long,
    val riskFund: Long,
    val operationalCost: Long,
    val serviceFee: Long,
    val total: Long,
    val serviceLevel: Int,
    val warrantyDays: Int,
    val estimatedHours: Int = 0,
    val createdAt: Date = Date()
)

/**
 * Entity untuk pelanggan (database pelanggan).
 */
@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val createdAt: Date = Date(),
    val lastServiceAt: Date = Date()
)

/**
 * Entity untuk template servis (input cepat).
 */
@Entity(tableName = "templates")
data class ServiceTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val deviceName: String,
    val sparepartCost: Long,
    val serviceLevel: Int,
    val isDefault: Boolean = false,
    val createdAt: Date = Date()
)
