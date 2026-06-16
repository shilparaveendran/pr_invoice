package com.app.printf.ui.state

import com.app.printf.data.entity.Customer

data class CustomerFormState(
    val name: String = "",
    val address: String = "",
    val gstin: String = "",
    val mobile: String = "",
    val editingCustomerId: Long? = null,
    val errorMessage: String? = null,
) {
    val isEditing: Boolean get() = editingCustomerId != null
}

data class CustomerUiState(
    val customers: List<Customer> = emptyList(),
    val searchQuery: String = "",
    val form: CustomerFormState = CustomerFormState(),
    val isSaving: Boolean = false,
    val successMessage: String? = null,
) {
    val filteredCustomers: List<Customer>
        get() {
            val query = searchQuery.trim().lowercase()
            if (query.isEmpty()) return customers
            return customers.filter { customer ->
                customer.name.lowercase().contains(query) ||
                    customer.address.lowercase().contains(query) ||
                    customer.gstin.lowercase().contains(query) ||
                    customer.mobile.lowercase().contains(query)
            }
        }
}
