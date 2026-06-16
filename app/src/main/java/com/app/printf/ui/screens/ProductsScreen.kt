package com.app.printf.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.app.printf.R
import com.app.printf.data.entity.Product
import com.app.printf.ui.components.PrintfCard
import com.app.printf.ui.components.PrintfEmptyState
import com.app.printf.ui.components.PrintfOutlinedButton
import com.app.printf.ui.components.PrintfPrimaryButton
import com.app.printf.ui.components.PrintfScreenBackground
import com.app.printf.ui.components.PrintfSectionHeader
import com.app.printf.ui.components.PrintfTextField
import com.app.printf.ui.viewmodel.ProductViewModel
import com.app.printf.util.Formatters

@Composable
fun ProductsScreen(
    viewModel: ProductViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val form = uiState.form
    val context = LocalContext.current

    LaunchedEffect(uiState.successMessage) {
        val msg = uiState.successMessage ?: return@LaunchedEffect
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        viewModel.clearSuccessMessage()
    }

    PrintfScreenBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PrintfCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PrintfSectionHeader(
                        title = if (form.isEditing) {
                            stringResource(R.string.edit_product)
                        } else {
                            stringResource(R.string.add_product)
                        },
                    )
                    PrintfTextField(
                        value = form.name,
                        onValueChange = viewModel::onNameChange,
                        label = stringResource(R.string.product_name),
                    )
                    PrintfTextField(
                        value = form.hsn,
                        onValueChange = viewModel::onHsnChange,
                        label = stringResource(R.string.hsn_no),
                    )
                    PrintfTextField(
                        value = form.priceText,
                        onValueChange = viewModel::onPriceChange,
                        label = stringResource(R.string.product_price),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    form.errorMessage?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (form.isEditing) {
                            PrintfOutlinedButton(
                                text = stringResource(R.string.cancel),
                                onClick = viewModel::cancelEdit,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        PrintfPrimaryButton(
                            text = if (form.isEditing) {
                                stringResource(R.string.update_product)
                            } else {
                                stringResource(R.string.save_product)
                            },
                            onClick = viewModel::saveProduct,
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isSaving,
                        )
                    }
                }
            }

            PrintfSectionHeader(title = stringResource(R.string.saved_products))

            if (uiState.products.isNotEmpty()) {
                PrintfTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    label = stringResource(R.string.search_products),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                )
            }

            when {
                uiState.products.isEmpty() -> PrintfEmptyState(
                    message = stringResource(R.string.no_products),
                    icon = Icons.Default.Inventory2,
                )
                uiState.filteredProducts.isEmpty() -> PrintfEmptyState(
                    message = stringResource(R.string.no_search_results),
                    icon = Icons.Default.Search,
                )
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(uiState.filteredProducts, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            isEditing = product.id == form.editingProductId,
                            onEdit = { viewModel.startEdit(product) },
                            onDelete = { viewModel.deleteProduct(product) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    isEditing: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    PrintfCard(highlighted = isEditing) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
