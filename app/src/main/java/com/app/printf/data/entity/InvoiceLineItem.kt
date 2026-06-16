package com.app.printf.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoice_line_items",
    foreignKeys = [
        ForeignKey(
            entity = Invoice::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("invoiceId")],
)
data class InvoiceLineItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceId: Long,
    val productId: Long,
    val productName: String,
    val hsn: String,
    val unitPrice: Double,
    val quantity: Int,
)
