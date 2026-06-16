package com.app.printf.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.printf.R
import com.app.printf.data.entity.Customer
import com.app.printf.data.model.DraftLineItem
import com.app.printf.ui.common.CollectUiEvents
import com.app.printf.ui.components.PrintfCard
import com.app.printf.ui.components.PrintfEmptyState
import com.app.printf.ui.components.PrintfExpandableSection
import com.app.printf.ui.components.PrintfPrimaryButton
import com.app.printf.ui.components.PrintfScreenBackground
import com.app.printf.ui.components.PrintfSectionHeader
import com.app.printf.ui.components.PrintfTextField
import com.app.printf.ui.components.SalesTypeSelector
import com.app.printf.ui.event.InvoiceUiEvent
import com.app.printf.ui.state.InvoiceUiState
import com.app.printf.ui.state.PartyDetails
import com.app.printf.ui.viewmodel.InvoiceViewModel
import com.app.printf.util.Formatters
import com.app.printf.util.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    viewModel: InvoiceViewModel,
    onSelectProduct: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.dateMillis)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let(viewModel::onDateChange)
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    CollectUiEvents(viewModel.events) { event ->
        when (event) {
            is InvoiceUiEvent.SharePdf -> ShareUtils.sharePdf(context, event.file)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.errorMessage?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                    PrintfPrimaryButton(
                        text = stringResource(
                            if (uiState.isEditingInvoice) {
                                R.string.update_and_share_pdf
                            } else {
                                R.string.create_and_share_pdf
                            },
                        ),
                        onClick = viewModel::createInvoice,
                        enabled = !uiState.isSaving,
                    )
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        },
    ) { scaffoldPadding ->
        PrintfScreenBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
            PrintfCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PrintfSectionHeader(
                        title = stringResource(
                            if (uiState.isEditingInvoice) R.string.edit_invoice else R.string.create_invoice,
                        ),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        PrintfTextField(
                            value = uiState.invoiceNumberInput,
                            onValueChange = viewModel::onInvoiceNumberChange,
                            label = stringResource(R.string.invoice_no_label),
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDatePicker = true },
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.invoice_date_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = Formatters.formatDate(uiState.dateMillis),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                    SalesTypeSelector(
                        selected = uiState.salesType,
                        onSelected = viewModel::onSalesTypeChange,
                    )
                    PrintfTextField(
                        value = uiState.ewayBillNo,
                        onValueChange = viewModel::onEwayBillNoChange,
                        label = stringResource(R.string.eway_bill_no),
                    )
                    PrintfTextField(
                        value = uiState.buyerPoNo,
                        onValueChange = viewModel::onBuyerPoNoChange,
                        label = stringResource(R.string.buyer_po_no),
                    )
                }
            }

            PrintfExpandableSection(
                title = stringResource(R.string.select_bill_to_customer),
                subtitle = stringResource(R.string.add_customers_hint),
                expanded = uiState.customerSectionExpanded,
                onExpandedChange = viewModel::setCustomerSectionExpanded,
                summary = customerSectionSummary(uiState),
                highlighted = uiState.selectedCustomer != null,
            ) {
                when (val selected = uiState.selectedCustomer) {
                    null -> CustomerSearchSection(
                        searchQuery = uiState.customerSearchQuery,
                        customers = uiState.customers,
                        filteredCustomers = uiState.filteredCustomers,
                        hasCustomers = uiState.hasCustomers,
                        onSearchChange = viewModel::onCustomerSearchChange,
                        onSelect = viewModel::selectCustomer,
                        onNoCustomers = viewModel::onSelectCustomerClicked,
                    )
                    else -> SelectedCustomerCard(
                        customer = selected,
                        onClear = viewModel::clearSelectedCustomer,
                    )
                }
            }

            PrintfExpandableSection(
                title = stringResource(R.string.bill_to_ship_to),
                subtitle = stringResource(R.string.bill_to_ship_to_hint),
                expanded = uiState.partySectionExpanded,
                onExpandedChange = viewModel::setPartySectionExpanded,
                summary = partySectionSummary(uiState),
                highlighted = uiState.billTo.isFilled,
            ) {
                PartyDetailsBlock(
                    title = stringResource(R.string.bill_to),
                    details = uiState.billTo,
                    editing = uiState.editingBillTo,
                    onEditClick = { viewModel.setEditingBillTo(!uiState.editingBillTo) },
                    onNameChange = viewModel::onBillToNameChange,
                    onAddressChange = viewModel::onBillToAddressChange,
                    onGstinChange = viewModel::onBillToGstinChange,
                    onMobileChange = viewModel::onBillToMobileChange,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = uiState.shipSameAsBill,
                        onCheckedChange = viewModel::setShipSameAsBill,
                    )
                    Text(
                        text = stringResource(R.string.ship_same_as_bill),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable {
                            viewModel.setShipSameAsBill(!uiState.shipSameAsBill)
                        },
                    )
                }

                if (!uiState.shipSameAsBill) {
                    Text(
                        text = stringResource(R.string.select_ship_to_customer),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(R.string.ship_to_customer_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    when (val shipCustomer = uiState.selectedShipCustomer) {
                        null -> CustomerSearchSection(
                            searchQuery = uiState.shipCustomerSearchQuery,
                            customers = uiState.customers,
                            filteredCustomers = uiState.filteredShipCustomers,
                            hasCustomers = uiState.hasCustomers,
                            onSearchChange = viewModel::onShipCustomerSearchChange,
                            onSelect = viewModel::selectShipCustomer,
                            onNoCustomers = viewModel::onSelectCustomerClicked,
                        )
                        else -> SelectedCustomerCard(
                            customer = shipCustomer,
                            onClear = viewModel::clearSelectedShipCustomer,
                        )
                    }
                    PartyDetailsBlock(
                        title = stringResource(R.string.ship_to),
                        details = uiState.shipTo,
                        editing = uiState.editingShipTo,
                        onEditClick = { viewModel.setEditingShipTo(!uiState.editingShipTo) },
                        onNameChange = viewModel::onShipToNameChange,
                        onAddressChange = viewModel::onShipToAddressChange,
                        onGstinChange = viewModel::onShipToGstinChange,
                        onMobileChange = viewModel::onShipToMobileChange,
                    )
                }
            }

            PrintfExpandableSection(
                title = stringResource(R.string.invoice_items),
                subtitle = stringResource(R.string.add_products_hint),
                expanded = uiState.itemsSectionExpanded,
                onExpandedChange = viewModel::setItemsSectionExpanded,
                summary = itemsSectionSummary(uiState),
                highlighted = uiState.lineItems.isNotEmpty(),
            ) {
                PrintfPrimaryButton(
                    text = stringResource(R.string.add_products_to_invoice),
                    onClick = {
                        if (uiState.hasProducts) {
                            onSelectProduct()
                        } else {
                            viewModel.onAddItemClicked()
                        }
                    },
                )

                if (!uiState.hasProducts) {
                    Text(
                        text = stringResource(R.string.add_products_first),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                uiState.lineItems.forEach { item ->
                    DraftLineItemCard(
                        item = item,
                        onIncrease = {
                            viewModel.updateLineQuantity(item.product.id, item.quantity + 1)
                        },
                        onDecrease = {
                            viewModel.updateLineQuantity(item.product.id, item.quantity - 1)
                        },
                        onQuantityInput = { text ->
                            val qty = text.filter { it.isDigit() }.toIntOrNull()
                            if (qty != null && qty > 0) {
                                viewModel.updateLineQuantity(item.product.id, qty)
                            }
                        },
                        onRemove = { viewModel.removeLineItem(item.product.id) },
                    )
                }

                if (uiState.lineItems.isEmpty() && uiState.hasProducts) {
                    PrintfEmptyState(
                        message = stringResource(R.string.no_items_added),
                        icon = Icons.Default.Add,
                    )
                }
            }

            PrintfCard(highlighted = true) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.total_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = Formatters.formatCurrency(uiState.grandTotal),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun partySectionSummary(uiState: InvoiceUiState): String {
    val bill = uiState.billTo
    return when {
        !bill.isFilled -> stringResource(R.string.bill_to_ship_to_hint)
        uiState.shipSameAsBill ->
            "${stringResource(R.string.bill_to)}: ${bill.name}"
        else -> {
            val ship = uiState.shipTo
            if (ship.isFilled) {
                "${bill.name} → ${ship.name}"
            } else {
                "${stringResource(R.string.bill_to)}: ${bill.name}"
            }
        }
    }
}

@Composable
private fun PartyDetailsBlock(
    title: String,
    details: PartyDetails,
    editing: Boolean,
    onEditClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onGstinChange: (String) -> Unit,
    onMobileChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            TextButton(onClick = onEditClick) {
                Text(
                    text = stringResource(
                        if (editing) R.string.done_editing else R.string.edit_details,
                    ),
                )
            }
        }
        if (editing || !details.isFilled) {
            PrintfTextField(
                value = details.name,
                onValueChange = onNameChange,
                label = stringResource(R.string.customer_name),
            )
            PrintfTextField(
                value = details.address,
                onValueChange = onAddressChange,
                label = stringResource(R.string.customer_address),
                modifier = Modifier.heightIn(min = 120.dp),
                singleLine = false,
                minLines = 5,
                maxLines = 7,
            )
            PrintfTextField(
                value = details.gstin,
                onValueChange = onGstinChange,
                label = stringResource(R.string.party_gstin),
            )
            PrintfTextField(
                value = details.mobile,
                onValueChange = onMobileChange,
                label = stringResource(R.string.customer_mobile),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )
        } else {
            Text(text = details.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = details.address,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (details.gstin.isNotBlank()) {
                Text(
                    text = "${stringResource(R.string.party_gstin)}: ${details.gstin}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (details.mobile.isNotBlank()) {
                Text(
                    text = "${stringResource(R.string.customer_mobile)}: ${details.mobile}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun customerSectionSummary(uiState: InvoiceUiState): String {
    val selected = uiState.selectedCustomer
    return when {
        selected != null -> "${selected.name} · ${selected.address}"
        !uiState.hasCustomers -> stringResource(R.string.no_customers)
        uiState.customerSearchQuery.isNotBlank() ->
            stringResource(R.string.customer_search_summary, uiState.filteredCustomers.size)
        else -> stringResource(R.string.tap_to_select_customer)
    }
}

@Composable
private fun itemsSectionSummary(uiState: InvoiceUiState): String {
    return when {
        uiState.lineItems.isEmpty() -> stringResource(R.string.no_items_added)
        else -> stringResource(
            R.string.items_summary,
            uiState.lineItems.size,
            Formatters.formatCurrency(uiState.grandTotal),
        )
    }
}

@Composable
private fun CustomerSearchSection(
    searchQuery: String,
    customers: List<Customer>,
    filteredCustomers: List<Customer>,
    hasCustomers: Boolean,
    onSearchChange: (String) -> Unit,
    onSelect: (Customer) -> Unit,
    onNoCustomers: () -> Unit,
) {
    if (!hasCustomers) {
        Text(
            text = stringResource(R.string.no_customers),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onNoCustomers) {
            Text(stringResource(R.string.go_add_customers))
        }
        return
    }

    PrintfTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        label = stringResource(R.string.search_customers),
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
    )

    val list = if (searchQuery.isBlank()) {
        customers
    } else {
        filteredCustomers
    }

    if (list.isEmpty()) {
        Text(
            text = stringResource(R.string.no_customer_search_results),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            list.forEach { customer ->
                PrintfCard(
                    modifier = Modifier.clickable { onSelect(customer) },
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
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
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedCustomerCard(
    customer: Customer,
    onClear: () -> Unit,
) {
    PrintfCard(highlighted = true) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.selected_customer),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
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
            }
            IconButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.change_customer),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DraftLineItemCard(
    item: DraftLineItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onQuantityInput: (String) -> Unit,
    onRemove: () -> Unit,
) {
    var quantityText by remember(item.product.id) { mutableStateOf(item.quantity.toString()) }
    LaunchedEffect(item.quantity) {
        val parsed = quantityText.toIntOrNull()
        if (parsed != item.quantity) {
            quantityText = item.quantity.toString()
        }
    }

    PrintfCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.product.name, style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "HSN: ${item.product.hsn} · ${Formatters.formatCurrency(item.product.price)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = Formatters.formatCurrency(item.lineTotal),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.remove_from_invoice),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDecrease) {
                    Text(
                        text = "−",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                PrintfTextField(
                    value = quantityText,
                    onValueChange = { value ->
                        quantityText = value.filter { it.isDigit() }
                        onQuantityInput(quantityText)
                    },
                    label = stringResource(R.string.quantity),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    singleLine = true,
                )
                IconButton(onClick = onIncrease) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.increase),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
