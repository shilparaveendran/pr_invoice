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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
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
import androidx.compose.ui.unit.dp
import com.app.printf.R
import com.app.printf.data.entity.Customer
import com.app.printf.ui.components.PrintfCard
import com.app.printf.ui.components.PrintfEmptyState
import com.app.printf.ui.components.PrintfOutlinedButton
import com.app.printf.ui.components.PrintfPrimaryButton
import com.app.printf.ui.components.PrintfScreenBackground
import com.app.printf.ui.components.PrintfSectionHeader
import com.app.printf.ui.components.PrintfTextField
import com.app.printf.ui.viewmodel.CustomerViewModel

@Composable
fun CustomersScreen(
    viewModel: CustomerViewModel,
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
                            stringResource(R.string.edit_customer)
                        } else {
                            stringResource(R.string.add_customer)
                        },
                    )
                    PrintfTextField(
                        value = form.name,
                        onValueChange = viewModel::onNameChange,
                        label = stringResource(R.string.customer_name),
                    )
                    PrintfTextField(
                        value = form.address,
                        onValueChange = viewModel::onAddressChange,
                        label = stringResource(R.string.customer_address),
                        singleLine = false,
                        minLines = 3,
                    )
                    PrintfTextField(
                        value = form.gstin,
                        onValueChange = viewModel::onGstinChange,
                        label = stringResource(R.string.customer_gstin),
                    )
                    PrintfTextField(
                        value = form.mobile,
                        onValueChange = viewModel::onMobileChange,
                        label = stringResource(R.string.customer_mobile),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
                        ),
                    )
                    form.errorMessage?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (form.isEditing) {
                            PrintfOutlinedButton(
                                text = stringResource(R.string.cancel),
                                onClick = viewModel::cancelEdit,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        PrintfPrimaryButton(
                            text = if (form.isEditing) {
                                stringResource(R.string.update_customer)
                            } else {
                                stringResource(R.string.save_customer)
                            },
                            onClick = viewModel::saveCustomer,
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isSaving,
                        )
                    }
                }
            }

            PrintfSectionHeader(title = stringResource(R.string.saved_customers))

            if (uiState.customers.isNotEmpty()) {
                PrintfTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    label = stringResource(R.string.search_customers),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                )
            }

            when {
                uiState.customers.isEmpty() -> PrintfEmptyState(
                    message = stringResource(R.string.no_customers),
                    icon = Icons.Default.People,
                )
                uiState.filteredCustomers.isEmpty() -> PrintfEmptyState(
                    message = stringResource(R.string.no_customer_search_results),
                    icon = Icons.Default.Search,
                )
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(uiState.filteredCustomers, key = { it.id }) { customer ->
                        CustomerCard(
                            customer = customer,
                            isEditing = customer.id == form.editingCustomerId,
                            onEdit = { viewModel.startEdit(customer) },
                            onDelete = { viewModel.deleteCustomer(customer) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerCard(
    customer: Customer,
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
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = customer.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = customer.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (customer.gstin.isNotBlank()) {
                    Text(
                        text = "${stringResource(R.string.customer_gstin)}: ${customer.gstin}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (customer.mobile.isNotBlank()) {
                    Text(
                        text = "${stringResource(R.string.customer_mobile)}: ${customer.mobile}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
