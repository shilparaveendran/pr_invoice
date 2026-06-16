package com.app.printf.ui.event

import java.io.File

sealed interface InvoiceUiEvent {
    data class SharePdf(val file: File) : InvoiceUiEvent
}

sealed interface SelectProductUiEvent {
    data object ProductAdded : SelectProductUiEvent
}
