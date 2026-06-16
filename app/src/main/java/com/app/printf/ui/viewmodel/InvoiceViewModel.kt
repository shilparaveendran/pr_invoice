package com.app.printf.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.printf.data.entity.Customer
import com.app.printf.data.entity.Product
import com.app.printf.data.model.DraftLineItem
import com.app.printf.domain.repository.CompanyProfileRepository
import com.app.printf.domain.repository.CustomerRepository
import com.app.printf.domain.repository.InvoiceRepository
import com.app.printf.domain.repository.ProductRepository
import com.app.printf.util.InvoiceNumberUtils
import com.app.printf.util.TaxConstants
import com.app.printf.ui.event.InvoiceUiEvent
import com.app.printf.ui.state.InvoiceUiState
import com.app.printf.ui.state.PartyDetails
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InvoiceViewModel(
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val invoiceRepository: InvoiceRepository,
    private val companyProfileRepository: CompanyProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState: StateFlow<InvoiceUiState> = _uiState.asStateFlow()

    private val _events = Channel<InvoiceUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        refreshInvoiceMeta()
        viewModelScope.launch {
            productRepository.observeProducts().collect { products ->
                _uiState.update { it.copy(products = products) }
            }
        }
        viewModelScope.launch {
            customerRepository.observeCustomers().collect { customers ->
                _uiState.update { it.copy(customers = customers) }
            }
        }
    }

    fun refreshInvoiceMeta() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.editingInvoiceId != null) return@launch
            val next = invoiceRepository.getNextInvoiceNumber(state.dateMillis)
            val profileSalesType = runCatching { companyProfileRepository.getProfile().salesType }
                .getOrDefault(TaxConstants.STATE_SALE)
                .ifBlank { TaxConstants.STATE_SALE }
            _uiState.update {
                it.copy(
                    invoiceNumberInput = next,
                    salesType = profileSalesType,
                )
            }
        }
    }

    fun onDateChange(millis: Long) {
        _uiState.update { it.copy(dateMillis = millis, errorMessage = null) }
        if (_uiState.value.editingInvoiceId == null) {
            viewModelScope.launch {
                val next = invoiceRepository.getNextInvoiceNumber(millis)
                _uiState.update { it.copy(invoiceNumberInput = next) }
            }
        }
    }

    fun loadInvoiceForEdit(invoiceId: Long) {
        viewModelScope.launch {
            loadInvoiceForEditInternal(invoiceId)
        }
    }

    suspend fun loadInvoiceForEditAndAwait(invoiceId: Long) {
        loadInvoiceForEditInternal(invoiceId)
    }

    private suspend fun loadInvoiceForEditInternal(invoiceId: Long) {
        val data = invoiceRepository.getInvoice(invoiceId) ?: return
        val invoice = data.invoice
        val products = _uiState.value.products
        val customers = _uiState.value.customers
        val lineItems = data.lineItems.map { lineItem ->
            val product = products.find { it.id == lineItem.productId }
                ?: Product(
                    id = lineItem.productId,
                    name = lineItem.productName,
                    hsn = lineItem.hsn,
                    price = lineItem.unitPrice,
                )
            DraftLineItem(product, lineItem.quantity)
        }
        val billTo = PartyDetails(
            name = invoice.billToName.ifBlank { invoice.customerName },
            address = invoice.billToAddress.ifBlank { invoice.customerAddress },
            gstin = invoice.billToGstin,
            mobile = invoice.billToMobile,
        )
        val shipTo = PartyDetails(
            name = invoice.shipToName.ifBlank { billTo.name },
            address = invoice.shipToAddress.ifBlank { billTo.address },
            gstin = invoice.shipToGstin,
            mobile = invoice.shipToMobile,
        )
        val shipSameAsBill = billTo == shipTo
        _uiState.update {
            it.copy(
                editingInvoiceId = invoiceId,
                invoiceNumberInput = invoice.invoiceNumber,
                dateMillis = invoice.dateMillis,
                billTo = billTo,
                shipTo = shipTo,
                shipSameAsBill = shipSameAsBill,
                salesType = invoice.salesType.ifBlank { TaxConstants.STATE_SALE },
                ewayBillNo = invoice.ewayBillNo,
                buyerPoNo = invoice.buyerPoNo,
                lineItems = lineItems,
                selectedCustomer = customers.find { customer ->
                    customer.name == billTo.name && customer.address == billTo.address
                },
                selectedShipCustomer = if (shipSameAsBill) {
                    null
                } else {
                    customers.find { customer ->
                        customer.name == shipTo.name && customer.address == shipTo.address
                    }
                },
                customerSectionExpanded = false,
                partySectionExpanded = true,
                itemsSectionExpanded = lineItems.isNotEmpty(),
                editingBillTo = false,
                editingShipTo = false,
                errorMessage = null,
            )
        }
    }

    fun onSalesTypeChange(value: String) {
        _uiState.update { it.copy(salesType = value, errorMessage = null) }
    }

    fun onEwayBillNoChange(value: String) {
        _uiState.update { it.copy(ewayBillNo = value, errorMessage = null) }
    }

    fun onBuyerPoNoChange(value: String) {
        _uiState.update { it.copy(buyerPoNo = value, errorMessage = null) }
    }

    fun onInvoiceNumberChange(input: String) {
        _uiState.update {
            it.copy(
                invoiceNumberInput = InvoiceNumberUtils.sanitizeInput(input),
                errorMessage = null,
            )
        }
    }

    fun setCustomerSectionExpanded(expanded: Boolean) {
        _uiState.update { it.copy(customerSectionExpanded = expanded) }
    }

    fun setPartySectionExpanded(expanded: Boolean) {
        _uiState.update { it.copy(partySectionExpanded = expanded) }
    }

    fun setItemsSectionExpanded(expanded: Boolean) {
        _uiState.update { it.copy(itemsSectionExpanded = expanded) }
    }

    fun onCustomerSearchChange(query: String) {
        _uiState.update { it.copy(customerSearchQuery = query, errorMessage = null) }
    }

    fun selectCustomer(customer: Customer) {
        val party = PartyDetails(
            name = customer.name,
            address = customer.address,
            gstin = customer.gstin,
            mobile = customer.mobile,
        )
        _uiState.update { state ->
            state.copy(
                selectedCustomer = customer,
                customerSearchQuery = "",
                customerSectionExpanded = false,
                billTo = party,
                shipTo = if (state.shipSameAsBill) party else state.shipTo,
                editingBillTo = false,
                partySectionExpanded = true,
                errorMessage = null,
            )
        }
    }

    fun clearSelectedCustomer() {
        _uiState.update {
            it.copy(
                selectedCustomer = null,
                customerSearchQuery = "",
                customerSectionExpanded = true,
            )
        }
    }

    fun onShipCustomerSearchChange(query: String) {
        _uiState.update { it.copy(shipCustomerSearchQuery = query, errorMessage = null) }
    }

    fun selectShipCustomer(customer: Customer) {
        val party = PartyDetails(
            name = customer.name,
            address = customer.address,
            gstin = customer.gstin,
            mobile = customer.mobile,
        )
        _uiState.update {
            it.copy(
                selectedShipCustomer = customer,
                shipCustomerSearchQuery = "",
                shipTo = party,
                shipSameAsBill = false,
                editingShipTo = false,
                partySectionExpanded = true,
                errorMessage = null,
            )
        }
    }

    fun clearSelectedShipCustomer() {
        _uiState.update {
            it.copy(
                selectedShipCustomer = null,
                shipCustomerSearchQuery = "",
            )
        }
    }

    fun setEditingBillTo(editing: Boolean) {
        _uiState.update { it.copy(editingBillTo = editing, errorMessage = null) }
    }

    fun setEditingShipTo(editing: Boolean) {
        _uiState.update { it.copy(editingShipTo = editing, errorMessage = null) }
    }

    fun setShipSameAsBill(same: Boolean) {
        _uiState.update { state ->
            state.copy(
                shipSameAsBill = same,
                shipTo = if (same) state.billTo else state.shipTo,
                selectedShipCustomer = if (same) null else state.selectedShipCustomer,
                shipCustomerSearchQuery = if (same) "" else state.shipCustomerSearchQuery,
                editingShipTo = if (same) false else state.editingShipTo,
            )
        }
    }

    fun onBillToNameChange(value: String) {
        _uiState.update { it.copy(billTo = it.billTo.copy(name = value), errorMessage = null) }
    }

    fun onBillToAddressChange(value: String) {
        _uiState.update { it.copy(billTo = it.billTo.copy(address = value), errorMessage = null) }
    }

    fun onBillToGstinChange(value: String) {
        _uiState.update { it.copy(billTo = it.billTo.copy(gstin = value), errorMessage = null) }
    }

    fun onBillToMobileChange(value: String) {
        _uiState.update { it.copy(billTo = it.billTo.copy(mobile = value), errorMessage = null) }
    }

    fun onShipToNameChange(value: String) {
        _uiState.update { it.copy(shipTo = it.shipTo.copy(name = value), errorMessage = null) }
    }

    fun onShipToAddressChange(value: String) {
        _uiState.update { it.copy(shipTo = it.shipTo.copy(address = value), errorMessage = null) }
    }

    fun onShipToGstinChange(value: String) {
        _uiState.update { it.copy(shipTo = it.shipTo.copy(gstin = value), errorMessage = null) }
    }

    fun onShipToMobileChange(value: String) {
        _uiState.update { it.copy(shipTo = it.shipTo.copy(mobile = value), errorMessage = null) }
    }

    fun addLineItem(product: Product, quantity: Int) {
        if (quantity <= 0) return
        _uiState.update { state ->
            val existing = state.lineItems.find { it.product.id == product.id }
            val updated = if (existing != null) {
                state.lineItems.map {
                    if (it.product.id == product.id) {
                        it.copy(quantity = it.quantity + quantity)
                    } else {
                        it
                    }
                }
            } else {
                state.lineItems + DraftLineItem(product, quantity)
            }
            state.copy(
                lineItems = updated,
                itemsSectionExpanded = true,
                errorMessage = null,
            )
        }
    }

    fun updateLineQuantity(productId: Long, quantity: Int) {
        if (quantity <= 0) {
            removeLineItem(productId)
            return
        }
        _uiState.update { state ->
            state.copy(
                lineItems = state.lineItems.map {
                    if (it.product.id == productId) it.copy(quantity = quantity) else it
                },
            )
        }
    }

    fun removeLineItem(productId: Long) {
        _uiState.update { state ->
            state.copy(lineItems = state.lineItems.filter { it.product.id != productId })
        }
    }

    fun onAddItemClicked() {
        if (!_uiState.value.hasProducts) {
            _uiState.update { it.copy(errorMessage = "Add products in the Products tab first") }
        }
    }

    fun onSelectCustomerClicked() {
        if (!_uiState.value.hasCustomers) {
            _uiState.update { it.copy(errorMessage = "Add customers in the Customers tab first") }
        }
    }

    fun createInvoice() {
        val state = _uiState.value
        val billTo = state.billTo
        val shipTo = state.effectiveShipTo
        when {
            !state.isValidInvoiceNumber ->
                _uiState.update { it.copy(errorMessage = "Enter a valid invoice number") }
            !billTo.isFilled ->
                _uiState.update {
                    it.copy(
                        errorMessage = "Enter Bill To name and address",
                        partySectionExpanded = true,
                        editingBillTo = true,
                    )
                }
            !shipTo.isFilled ->
                _uiState.update {
                    it.copy(
                        errorMessage = "Enter Ship To name and address",
                        partySectionExpanded = true,
                        editingShipTo = true,
                        shipSameAsBill = false,
                    )
                }
            state.lineItems.isEmpty() ->
                _uiState.update {
                    it.copy(
                        errorMessage = "Add at least one product",
                        itemsSectionExpanded = true,
                    )
                }
            else -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isSaving = true, errorMessage = null) }
                    try {
                        val invoiceNumber = state.invoiceNumberInput.trim().uppercase()
                        val editingId = state.editingInvoiceId
                        if (invoiceRepository.isInvoiceNumberTaken(invoiceNumber, editingId)) {
                            _uiState.update {
                                it.copy(
                                    errorMessage = "Invoice number $invoiceNumber is already used",
                                    isSaving = false,
                                )
                            }
                            return@launch
                        }
                        val invoiceId = if (editingId != null) {
                            invoiceRepository.updateInvoice(
                                invoiceId = editingId,
                                invoiceNumber = invoiceNumber,
                                dateMillis = state.dateMillis,
                                billToName = billTo.name,
                                billToAddress = billTo.address,
                                billToGstin = billTo.gstin,
                                billToMobile = billTo.mobile,
                                shipToName = shipTo.name,
                                shipToAddress = shipTo.address,
                                shipToGstin = shipTo.gstin,
                                shipToMobile = shipTo.mobile,
                                salesType = state.salesType,
                                ewayBillNo = state.ewayBillNo,
                                buyerPoNo = state.buyerPoNo,
                                lineItems = state.lineItems,
                            )
                            editingId
                        } else {
                            invoiceRepository.createInvoice(
                                invoiceNumber = invoiceNumber,
                                dateMillis = state.dateMillis,
                                billToName = billTo.name,
                                billToAddress = billTo.address,
                                billToGstin = billTo.gstin,
                                billToMobile = billTo.mobile,
                                shipToName = shipTo.name,
                                shipToAddress = shipTo.address,
                                shipToGstin = shipTo.gstin,
                                shipToMobile = shipTo.mobile,
                                salesType = state.salesType,
                                ewayBillNo = state.ewayBillNo,
                                buyerPoNo = state.buyerPoNo,
                                lineItems = state.lineItems,
                            )
                        }
                        val pdf = invoiceRepository.generateAndSavePdf(invoiceId)
                        _events.send(InvoiceUiEvent.SharePdf(pdf))
                        prepareFreshDraft(justCreatedNumber = invoiceNumber)
                    } catch (e: Exception) {
                        _uiState.update {
                            it.copy(
                                errorMessage = e.message ?: "Failed to save invoice",
                                isSaving = false,
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun prepareFreshDraft(justCreatedNumber: String? = null) {
        val snapshot = _uiState.value
        val dateMillis = System.currentTimeMillis()
        val nextNumber = invoiceRepository.getNextInvoiceNumber(
            dateMillis = dateMillis,
            lastUsedNumber = justCreatedNumber,
        )
        val profileSalesType = runCatching { companyProfileRepository.getProfile().salesType }
            .getOrDefault(TaxConstants.STATE_SALE)
            .ifBlank { TaxConstants.STATE_SALE }
        _uiState.value = InvoiceUiState(
            products = snapshot.products,
            customers = snapshot.customers,
            dateMillis = dateMillis,
            invoiceNumberInput = nextNumber,
            salesType = profileSalesType,
        )
    }
}
