package com.app.printf.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.printf.domain.repository.InvoiceRepository
import com.app.printf.ui.state.PdfPreviewUiState
import com.app.printf.util.PdfRenderUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PdfPreviewViewModel(
    private val invoiceRepository: InvoiceRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<PdfPreviewUiState>(PdfPreviewUiState.Loading)
    val uiState: StateFlow<PdfPreviewUiState> = _uiState.asStateFlow()

    fun loadInvoice(invoiceId: Long) {
        viewModelScope.launch {
            _uiState.value = PdfPreviewUiState.Loading
            try {
                val invoiceWithItems = invoiceRepository.getInvoice(invoiceId)
                    ?: throw IllegalStateException("Invoice not found")
                val pdfFile = invoiceRepository.resolvePdfFile(invoiceWithItems)
                val pages = withContext(Dispatchers.IO) {
                    PdfRenderUtils.renderPages(pdfFile)
                }
                _uiState.value = PdfPreviewUiState.Success(
                    pages = pages,
                    pdfFile = pdfFile,
                    invoiceNumber = invoiceWithItems.invoice.invoiceNumber,
                )
            } catch (e: Exception) {
                _uiState.value = PdfPreviewUiState.Error(
                    e.message ?: "Unable to load PDF",
                )
            }
        }
    }
}
