package com.app.printf.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val dateMillis: Long,
    val customerName: String,
    val customerAddress: String,
    val billToName: String,
    val billToAddress: String,
    val billToGstin: String,
    val billToMobile: String = "",
    val shipToName: String,
    val shipToAddress: String,
    val shipToGstin: String,
    val shipToMobile: String = "",
    val salesType: String,
    val ewayBillNo: String = "",
    val buyerPoNo: String = "",
    val totalAmount: Double,
    val pdfPath: String?,
)
