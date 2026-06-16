package com.app.printf.ui.state

import com.app.printf.data.model.InvoiceWithItems

data class HistoryUiState(
    val invoices: List<InvoiceWithItems> = emptyList(),
    val isSharing: Boolean = false,
)
