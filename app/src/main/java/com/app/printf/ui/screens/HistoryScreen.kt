package com.app.printf.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.printf.R
import com.app.printf.data.model.InvoiceWithItems
import com.app.printf.ui.common.CollectUiEvents
import com.app.printf.ui.components.PrintfAccentBadge
import com.app.printf.ui.components.PrintfCard
import com.app.printf.ui.components.PrintfEmptyState
import com.app.printf.ui.components.PrintfScreenBackground
import com.app.printf.ui.event.InvoiceUiEvent
import com.app.printf.ui.viewmodel.HistoryViewModel
import com.app.printf.util.Formatters
import com.app.printf.util.ShareUtils

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onViewPdf: (Long) -> Unit,
    onEditInvoice: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    CollectUiEvents(viewModel.events) { event ->
        when (event) {
            is InvoiceUiEvent.SharePdf -> ShareUtils.sharePdf(context, event.file)
        }
    }

    PrintfScreenBackground(modifier = modifier) {
        if (uiState.invoices.isEmpty()) {
            PrintfEmptyState(
                message = stringResource(R.string.no_invoices),
                icon = Icons.Default.History,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item { Text(text = "", modifier = Modifier.padding(top = 8.dp)) }
                items(uiState.invoices, key = { it.invoice.id }) { invoice ->
                    HistoryInvoiceCard(
                        invoiceWithItems = invoice,
                        isBusy = uiState.isSharing,
                        onView = { onViewPdf(invoice.invoice.id) },
                        onShare = { viewModel.shareInvoice(invoice) },
                        onEdit = { onEditInvoice(invoice.invoice.id) },
                        onDelete = { viewModel.deleteInvoice(invoice) },
                    )
                }
                item { Text(text = "", modifier = Modifier.padding(bottom = 16.dp)) }
            }
        }
    }
}

@Composable
private fun HistoryInvoiceCard(
    invoiceWithItems: InvoiceWithItems,
    isBusy: Boolean,
    onView: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val invoice = invoiceWithItems.invoice
    PrintfCard {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PrintfAccentBadge(
                    text = stringResource(R.string.invoice_number, invoice.invoiceNumber),
                )
                Text(
                    text = Formatters.formatDate(invoice.dateMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(text = invoice.customerName, style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = Formatters.formatCurrency(invoice.totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Row {
                    IconButton(onClick = onEdit, enabled = !isBusy) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_invoice),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(onClick = onView, enabled = !isBusy) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = stringResource(R.string.view_pdf),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(onClick = onShare, enabled = !isBusy) {
                        if (isBusy) {
                            CircularProgressIndicator(strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = stringResource(R.string.share_invoice),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    IconButton(onClick = onDelete, enabled = !isBusy) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_invoice),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}
