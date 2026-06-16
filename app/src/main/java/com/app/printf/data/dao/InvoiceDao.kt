package com.app.printf.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.app.printf.data.entity.Invoice
import com.app.printf.data.entity.InvoiceLineItem
import com.app.printf.data.model.InvoiceWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Transaction
    @Query("SELECT * FROM invoices ORDER BY invoiceNumber DESC")
    fun observeAllWithItems(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :invoiceId")
    suspend fun getWithItems(invoiceId: Long): InvoiceWithItems?

    @Query("SELECT invoiceNumber FROM invoices")
    suspend fun getAllInvoiceNumbers(): List<String>

    @Query("SELECT COUNT(*) FROM invoices WHERE invoiceNumber = :invoiceNumber")
    suspend fun countByInvoiceNumber(invoiceNumber: String): Int

    @Query("SELECT COUNT(*) FROM invoices WHERE invoiceNumber = :invoiceNumber AND id != :excludeId")
    suspend fun countByInvoiceNumberExcluding(invoiceNumber: String, excludeId: Long): Int

    @Query("DELETE FROM invoice_line_items WHERE invoiceId = :invoiceId")
    suspend fun deleteLineItemsForInvoice(invoiceId: Long)

    @Query("DELETE FROM invoices WHERE id = :invoiceId")
    suspend fun deleteInvoiceById(invoiceId: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLineItems(items: List<InvoiceLineItem>)

    @Update
    suspend fun updateInvoice(invoice: Invoice)
}
