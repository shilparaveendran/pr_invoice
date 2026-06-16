package com.app.printf.domain.repository

import com.app.printf.data.model.DraftLineItem
import com.app.printf.data.model.InvoiceWithItems
import kotlinx.coroutines.flow.Flow
import java.io.File

interface InvoiceRepository {
    fun observeInvoices(): Flow<List<InvoiceWithItems>>
    suspend fun getNextInvoiceNumber(dateMillis: Long = System.currentTimeMillis()): String
    suspend fun isInvoiceNumberTaken(invoiceNumber: String, excludeInvoiceId: Long? = null): Boolean
    suspend fun createInvoice(
        invoiceNumber: String,
        dateMillis: Long,
        billToName: String,
        billToAddress: String,
        billToGstin: String,
        shipToName: String,
        shipToAddress: String,
        shipToGstin: String,
        salesType: String,
        ewayBillNo: String,
        buyerPoNo: String,
        lineItems: List<DraftLineItem>,
    ): Long
    suspend fun updateInvoice(
        invoiceId: Long,
        invoiceNumber: String,
        dateMillis: Long,
        billToName: String,
        billToAddress: String,
        billToGstin: String,
        shipToName: String,
        shipToAddress: String,
        shipToGstin: String,
        salesType: String,
        ewayBillNo: String,
        buyerPoNo: String,
        lineItems: List<DraftLineItem>,
    )
    suspend fun deleteInvoice(invoiceId: Long)
    suspend fun getInvoice(invoiceId: Long): InvoiceWithItems?
    suspend fun generateAndSavePdf(invoiceId: Long): File
    suspend fun resolvePdfFile(invoiceWithItems: InvoiceWithItems): File
}
