package com.app.printf.ui.state

import androidx.compose.ui.graphics.ImageBitmap
import java.io.File

sealed interface PdfPreviewUiState {
    data object Loading : PdfPreviewUiState
    data class Success(
        val pages: List<ImageBitmap>,
        val pdfFile: File,
        val invoiceNumber: String,
    ) : PdfPreviewUiState
    data class Error(val message: String) : PdfPreviewUiState
}
