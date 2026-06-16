package com.app.printf.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.printf.data.model.InvoiceWithItems
import com.app.printf.domain.repository.InvoiceRepository
import com.app.printf.ui.event.InvoiceUiEvent
import com.app.printf.ui.state.HistoryUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val invoiceRepository: InvoiceRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _events = Channel<InvoiceUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            invoiceRepository.observeInvoices().collect { invoices ->
                _uiState.update { it.copy(invoices = invoices) }
            }
        }
    }

    fun shareInvoice(invoiceWithItems: InvoiceWithItems) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSharing = true) }
            try {
                val pdf = invoiceRepository.resolvePdfFile(invoiceWithItems)
                _events.send(InvoiceUiEvent.SharePdf(pdf))
            } catch (_: Exception) {
                // Share failures surface via system; keep UI simple
            } finally {
                _uiState.update { it.copy(isSharing = false) }
            }
        }
    }

    fun deleteInvoice(invoiceWithItems: InvoiceWithItems) {
        viewModelScope.launch {
            invoiceRepository.deleteInvoice(invoiceWithItems.invoice.id)
        }
    }
}
