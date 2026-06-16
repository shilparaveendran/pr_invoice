package com.app.printf.data.repository

import com.app.printf.data.dao.InvoiceDao
import com.app.printf.data.entity.Invoice
import com.app.printf.data.entity.InvoiceLineItem
import com.app.printf.data.model.DraftLineItem
import com.app.printf.data.model.InvoiceWithItems
import com.app.printf.data.source.InvoicePdfDataSource
import com.app.printf.domain.repository.InvoiceRepository
import com.app.printf.util.InvoiceNumberUtils
import com.app.printf.util.TaxCalculator
import com.app.printf.util.TaxConstants
import kotlinx.coroutines.flow.Flow
import java.io.File

class InvoiceRepositoryImpl(
    private val invoiceDao: InvoiceDao,
    private val pdfDataSource: InvoicePdfDataSource,
) : InvoiceRepository {
    override fun observeInvoices(): Flow<List<InvoiceWithItems>> = invoiceDao.observeAllWithItems()

    override suspend fun getNextInvoiceNumber(dateMillis: Long): String {
        return InvoiceNumberUtils.suggestNext(invoiceDao.getAllInvoiceNumbers(), dateMillis)
    }

    override suspend fun isInvoiceNumberTaken(invoiceNumber: String, excludeInvoiceId: Long?): Boolean {
        val normalized = invoiceNumber.trim().uppercase()
        return if (excludeInvoiceId == null) {
            invoiceDao.countByInvoiceNumber(normalized) > 0
        } else {
            invoiceDao.countByInvoiceNumberExcluding(normalized, excludeInvoiceId) > 0
        }
    }

    override suspend fun createInvoice(
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
    ): Long {
        val totals = calculateTotals(lineItems, salesType)
        val billName = billToName.trim()
        val billAddress = billToAddress.trim()
        val invoiceId = invoiceDao.insertInvoice(
            Invoice(
                invoiceNumber = invoiceNumber.trim().uppercase(),
                dateMillis = dateMillis,
                customerName = billName,
                customerAddress = billAddress,
                billToName = billName,
                billToAddress = billAddress,
                billToGstin = billToGstin.trim(),
                shipToName = shipToName.trim(),
                shipToAddress = shipToAddress.trim(),
                shipToGstin = shipToGstin.trim(),
                salesType = totals.resolvedSalesType,
                ewayBillNo = ewayBillNo.trim(),
                buyerPoNo = buyerPoNo.trim(),
                totalAmount = totals.grandTotal,
                pdfPath = null,
            ),
        )
        invoiceDao.insertLineItems(lineItems.toPersistedItems(invoiceId))
        return invoiceId
    }

    override suspend fun updateInvoice(
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
    ) {
        val existing = invoiceDao.getWithItems(invoiceId)
            ?: throw IllegalStateException("Invoice not found")
        val totals = calculateTotals(lineItems, salesType)
        val billName = billToName.trim()
        val billAddress = billToAddress.trim()
        invoiceDao.updateInvoice(
            existing.invoice.copy(
                invoiceNumber = invoiceNumber.trim().uppercase(),
                dateMillis = dateMillis,
                customerName = billName,
                customerAddress = billAddress,
                billToName = billName,
                billToAddress = billAddress,
                billToGstin = billToGstin.trim(),
                shipToName = shipToName.trim(),
                shipToAddress = shipToAddress.trim(),
                shipToGstin = shipToGstin.trim(),
                salesType = totals.resolvedSalesType,
                ewayBillNo = ewayBillNo.trim(),
                buyerPoNo = buyerPoNo.trim(),
                totalAmount = totals.grandTotal,
            ),
        )
        invoiceDao.deleteLineItemsForInvoice(invoiceId)
        invoiceDao.insertLineItems(lineItems.toPersistedItems(invoiceId))
    }

    override suspend fun deleteInvoice(invoiceId: Long) {
        val invoiceWithItems = invoiceDao.getWithItems(invoiceId) ?: return
        invoiceWithItems.invoice.pdfPath
            ?.takeIf { it.isNotBlank() }
            ?.let { path -> File(path).delete() }
        invoiceDao.deleteLineItemsForInvoice(invoiceId)
        invoiceDao.deleteInvoiceById(invoiceId)
    }

    override suspend fun getInvoice(invoiceId: Long): InvoiceWithItems? {
        return invoiceDao.getWithItems(invoiceId)
    }

    override suspend fun generateAndSavePdf(invoiceId: Long): File {
        val invoiceWithItems = invoiceDao.getWithItems(invoiceId)
            ?: throw IllegalStateException("Invoice not found")
        val pdf = pdfDataSource.generate(invoiceWithItems)
        invoiceDao.updateInvoice(invoiceWithItems.invoice.copy(pdfPath = pdf.absolutePath))
        return pdf
    }

    override suspend fun resolvePdfFile(invoiceWithItems: InvoiceWithItems): File {
        val previousPath = invoiceWithItems.invoice.pdfPath
        val pdf = pdfDataSource.generate(invoiceWithItems)
        if (!previousPath.isNullOrBlank() && previousPath != pdf.absolutePath) {
            File(previousPath).delete()
        }
        invoiceDao.updateInvoice(invoiceWithItems.invoice.copy(pdfPath = pdf.absolutePath))
        return pdf
    }

    private data class InvoiceTotals(
        val resolvedSalesType: String,
        val grandTotal: Double,
    )

    private fun calculateTotals(lineItems: List<DraftLineItem>, salesType: String): InvoiceTotals {
        val taxableAmount = lineItems.sumOf { it.lineTotal }
        val resolvedSalesType = salesType.trim().ifBlank { TaxConstants.STATE_SALE }
        val isInterstate = TaxCalculator.isInterstateSale(resolvedSalesType)
        val totals = TaxCalculator.invoiceTotals(taxableAmount, isInterstate)
        return InvoiceTotals(resolvedSalesType, totals.grandTotal)
    }

    private fun List<DraftLineItem>.toPersistedItems(invoiceId: Long): List<InvoiceLineItem> {
        return map { draft ->
            InvoiceLineItem(
                invoiceId = invoiceId,
                productId = draft.product.id,
                productName = draft.product.name,
                hsn = draft.product.hsn,
                unitPrice = draft.product.price,
                quantity = draft.quantity,
            )
        }
    }
}
