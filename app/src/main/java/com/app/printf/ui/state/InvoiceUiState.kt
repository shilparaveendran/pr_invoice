package com.app.printf.ui.state

import com.app.printf.data.entity.Customer
import com.app.printf.data.entity.Product
import com.app.printf.data.model.DraftLineItem
import com.app.printf.util.TaxCalculator
import com.app.printf.util.TaxConstants

data class InvoiceUiState(
    val editingInvoiceId: Long? = null,
    val invoiceNumberInput: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val customers: List<Customer> = emptyList(),
    val customerSearchQuery: String = "",
    val selectedCustomer: Customer? = null,
    val shipCustomerSearchQuery: String = "",
    val selectedShipCustomer: Customer? = null,
    val customerSectionExpanded: Boolean = false,
    val billTo: PartyDetails = PartyDetails(),
    val shipTo: PartyDetails = PartyDetails(),
    val shipSameAsBill: Boolean = true,
    val editingBillTo: Boolean = true,
    val editingShipTo: Boolean = true,
    val partySectionExpanded: Boolean = false,
    val itemsSectionExpanded: Boolean = false,
    val salesType: String = TaxConstants.STATE_SALE,
    val ewayBillNo: String = "",
    val buyerPoNo: String = "",
    val lineItems: List<DraftLineItem> = emptyList(),
    val products: List<Product> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val isEditingInvoice: Boolean get() = editingInvoiceId != null
    val isValidInvoiceNumber: Boolean get() = invoiceNumberInput.isNotBlank()
    val taxableTotal: Double get() = lineItems.sumOf { it.lineTotal }
    val grandTotal: Double
        get() = TaxCalculator.invoiceTotals(
            taxableTotal,
            TaxCalculator.isInterstateSale(salesType),
        ).grandTotal
    val hasProducts: Boolean get() = products.isNotEmpty()
    val hasCustomers: Boolean get() = customers.isNotEmpty()
    val effectiveShipTo: PartyDetails
        get() = if (shipSameAsBill) billTo else shipTo

    val filteredCustomers: List<Customer>
        get() = filterCustomers(customerSearchQuery)

    val filteredShipCustomers: List<Customer>
        get() = filterCustomers(shipCustomerSearchQuery)

    private fun filterCustomers(query: String): List<Customer> {
        val normalized = query.trim().lowercase()
        if (normalized.isEmpty()) return customers
        return customers.filter { customer ->
            customer.name.lowercase().contains(normalized) ||
                customer.address.lowercase().contains(normalized) ||
                customer.gstin.lowercase().contains(normalized) ||
                customer.mobile.lowercase().contains(normalized)
        }
    }
}
