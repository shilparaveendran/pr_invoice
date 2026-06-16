package com.app.printf.di

import android.app.Application
import com.app.printf.data.AppDatabase
import com.app.printf.data.repository.CompanyProfileRepositoryImpl
import com.app.printf.data.repository.CustomerRepositoryImpl
import com.app.printf.data.repository.InvoiceRepositoryImpl
import com.app.printf.data.repository.ProductRepositoryImpl
import com.app.printf.data.source.InvoicePdfDataSource
import com.app.printf.domain.repository.CompanyProfileRepository
import com.app.printf.domain.repository.CustomerRepository
import com.app.printf.domain.repository.InvoiceRepository
import com.app.printf.domain.repository.ProductRepository

class AppContainer(application: Application) {
    val appContext = application.applicationContext
    private val database = AppDatabase.get(application)

    private val companyProfileRepositoryImpl: CompanyProfileRepository =
        CompanyProfileRepositoryImpl(companyProfileDao = database.companyProfileDao())

    private val pdfDataSource = InvoicePdfDataSource(
        appContext = application.applicationContext,
        companyProfileRepository = companyProfileRepositoryImpl,
    )

    val productRepository: ProductRepository = ProductRepositoryImpl(
        productDao = database.productDao(),
    )

    val customerRepository: CustomerRepository = CustomerRepositoryImpl(
        customerDao = database.customerDao(),
    )

    val invoiceRepository: InvoiceRepository = InvoiceRepositoryImpl(
        invoiceDao = database.invoiceDao(),
        pdfDataSource = pdfDataSource,
    )

    val companyProfileRepository: CompanyProfileRepository = companyProfileRepositoryImpl
}
