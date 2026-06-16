package com.app.printf.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.printf.R
import com.app.printf.ui.components.PrintfCard
import com.app.printf.ui.components.PrintfScreenBackground
import com.app.printf.ui.components.PrintfSecondaryTopAppBar
import com.app.printf.ui.state.PdfPreviewUiState
import com.app.printf.ui.viewmodel.PdfPreviewViewModel
import com.app.printf.util.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPreviewScreen(
    invoiceId: Long,
    viewModel: PdfPreviewViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(invoiceId) {
        viewModel.loadInvoice(invoiceId)
    }

    val title = when (val state = uiState) {
        is PdfPreviewUiState.Success ->
            stringResource(R.string.invoice_number, state.invoiceNumber)
        else -> stringResource(R.string.view_pdf)
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PrintfSecondaryTopAppBar(
                title = title,
                onBack = onBack,
                actions = {
                    if (uiState is PdfPreviewUiState.Success) {
                        IconButton(
                            onClick = {
                                ShareUtils.sharePdf(
                                    context,
                                    (uiState as PdfPreviewUiState.Success).pdfFile,
                                )
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = stringResource(R.string.share_invoice),
                                tint = androidx.compose.ui.graphics.Color.White,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        PrintfScreenBackground {
            when (val state = uiState) {
                PdfPreviewUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is PdfPreviewUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                is PdfPreviewUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item { Text(text = "", modifier = Modifier.padding(top = 4.dp)) }
                        itemsIndexed(state.pages) { index, page ->
                            PrintfCard {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    if (state.pages.size > 1) {
                                        Text(
                                            text = stringResource(
                                                R.string.pdf_page,
                                                index + 1,
                                                state.pages.size,
                                            ),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp),
                                        )
                                    }
                                    Image(
                                        bitmap = page,
                                        contentDescription = stringResource(R.string.view_pdf),
                                        modifier = Modifier.fillMaxWidth(),
                                        contentScale = ContentScale.FillWidth,
                                    )
                                }
                            }
                        }
                        item { Text(text = "", modifier = Modifier.padding(bottom = 16.dp)) }
                    }
                }
            }
        }
    }
}
