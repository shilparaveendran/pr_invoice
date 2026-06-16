package com.app.printf.data.source

import android.content.Context
import com.app.printf.domain.repository.CompanyProfileRepository
import com.app.printf.data.model.InvoiceWithItems
import com.app.printf.pdf.InvoicePdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class InvoicePdfDataSource(
    private val appContext: Context,
    private val companyProfileRepository: CompanyProfileRepository,
) {
    suspend fun generate(invoiceWithItems: InvoiceWithItems): File = withContext(Dispatchers.IO) {
        val profile = companyProfileRepository.getProfile()
        InvoicePdfGenerator.generate(appContext, invoiceWithItems, profile)
    }
}
