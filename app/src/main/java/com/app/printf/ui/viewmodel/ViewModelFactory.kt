package com.app.printf.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.printf.di.AppContainer

class ViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ProductViewModel::class.java) ->
                ProductViewModel(container.productRepository) as T
            modelClass.isAssignableFrom(CustomerViewModel::class.java) ->
                CustomerViewModel(container.customerRepository) as T
            modelClass.isAssignableFrom(InvoiceViewModel::class.java) ->
                InvoiceViewModel(
                    container.productRepository,
                    container.customerRepository,
                    container.invoiceRepository,
                    container.companyProfileRepository,
                ) as T
            modelClass.isAssignableFrom(SelectProductViewModel::class.java) ->
                SelectProductViewModel(container.productRepository) as T
            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(container.invoiceRepository) as T
            modelClass.isAssignableFrom(PdfPreviewViewModel::class.java) ->
                PdfPreviewViewModel(container.invoiceRepository) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel(
                    container.companyProfileRepository,
                    container.appContext,
                ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
