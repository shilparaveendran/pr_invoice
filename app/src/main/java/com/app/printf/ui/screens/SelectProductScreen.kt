package com.app.printf.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.app.printf.R
import com.app.printf.ui.components.PrintfCard
import com.app.printf.ui.components.PrintfEmptyState
import com.app.printf.ui.components.PrintfPrimaryButton
import com.app.printf.ui.components.PrintfScreenBackground
import com.app.printf.ui.components.PrintfSecondaryTopAppBar
import com.app.printf.ui.components.PrintfTextField
import com.app.printf.ui.viewmodel.InvoiceViewModel
import com.app.printf.ui.viewmodel.SelectProductViewModel
import com.app.printf.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectProductScreen(
    selectProductViewModel: SelectProductViewModel,
    invoiceViewModel: InvoiceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by selectProductViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun dismissKeyboard() {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PrintfSecondaryTopAppBar(
                title = stringResource(R.string.select_product),
                onBack = onBack,
            )
        },
        bottomBar = {
            val selected = uiState.selectedProduct
            if (selected != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                ) {
                    PrintfCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = selectProductViewModel::decreaseQuantity) {
                                Text(
                                    text = "−",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                )
                            }
                            PrintfTextField(
                                value = uiState.quantityText,
                                onValueChange = selectProductViewModel::onQuantityChange,
                                label = stringResource(R.string.quantity),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = selectProductViewModel::increaseQuantity) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(R.string.increase),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                            PrintfPrimaryButton(
                                text = stringResource(R.string.add),
                                onClick = {
                                    val qty = uiState.quantityText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                    dismissKeyboard()
                                    invoiceViewModel.addLineItem(selected, qty)
                                    onBack()
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        PrintfScreenBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PrintfTextField(
                    value = uiState.searchQuery,
                    onValueChange = selectProductViewModel::onSearchQueryChange,
                    label = stringResource(R.string.search_products),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                )

                when {
                    uiState.filteredProducts.isEmpty() -> PrintfEmptyState(
                        message = if (uiState.products.isEmpty()) {
                            stringResource(R.string.no_products)
                        } else {
                            stringResource(R.string.no_search_results)
                        },
                        icon = Icons.Default.Search,
                        modifier = Modifier.weight(1f),
                    )
                    else -> LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.filteredProducts, key = { it.id }) { product ->
                            val isSelected = uiState.selectedProductId == product.id
                            PrintfCard(
                                highlighted = isSelected,
                                modifier = Modifier.clickable {
                                    dismissKeyboard()
                                    selectProductViewModel.onProductSelected(product.id)
                                },
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(text = product.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = "HSN: ${product.hsn}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = Formatters.formatCurrency(product.price),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
