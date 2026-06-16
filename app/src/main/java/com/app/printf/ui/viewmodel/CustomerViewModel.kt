package com.app.printf.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.printf.data.entity.Customer
import com.app.printf.domain.repository.CustomerRepository
import com.app.printf.ui.state.CustomerFormState
import com.app.printf.ui.state.CustomerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val customerRepository: CustomerRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            customerRepository.observeCustomers().collect { customers ->
                _uiState.update { it.copy(customers = customers) }
            }
        }
    }

    fun onNameChange(value: String) {
        updateForm { it.copy(name = value, errorMessage = null) }
    }

    fun onAddressChange(value: String) {
        updateForm { it.copy(address = value, errorMessage = null) }
    }

    fun onGstinChange(value: String) {
        updateForm { it.copy(gstin = value, errorMessage = null) }
    }

    fun onMobileChange(value: String) {
        updateForm { it.copy(mobile = value, errorMessage = null) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun startEdit(customer: Customer) {
        updateForm {
            CustomerFormState(
                name = customer.name,
                address = customer.address,
                gstin = customer.gstin,
                mobile = customer.mobile,
                editingCustomerId = customer.id,
            )
        }
    }

    fun cancelEdit() {
        updateForm { CustomerFormState() }
    }

    fun saveCustomer() {
        val form = _uiState.value.form
        when {
            form.name.isBlank() -> setFormError("Customer name is required")
            form.address.isBlank() -> setFormError("Customer address is required")
            else -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isSaving = true, successMessage = null) }
                    if (form.isEditing) {
                        val customer = _uiState.value.customers.find { it.id == form.editingCustomerId }
                        if (customer != null) {
                            customerRepository.updateCustomer(
                                customer.copy(
                                    name = form.name.trim(),
                                    address = form.address.trim(),
                                    gstin = form.gstin.trim().uppercase(),
                                    mobile = form.mobile.trim(),
                                ),
                            )
                        }
                    } else {
                        customerRepository.addCustomer(
                            name = form.name,
                            address = form.address,
                            gstin = form.gstin,
                            mobile = form.mobile,
                        )
                    }
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            form = CustomerFormState(),
                            successMessage = if (form.isEditing) "Customer updated" else "Customer added",
                        )
                    }
                }
            }
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            if (_uiState.value.form.editingCustomerId == customer.id) {
                updateForm { CustomerFormState() }
            }
            customerRepository.deleteCustomer(customer)
        }
    }

    private fun updateForm(transform: (CustomerFormState) -> CustomerFormState) {
        _uiState.update { state -> state.copy(form = transform(state.form)) }
    }

    private fun setFormError(message: String) {
        updateForm { it.copy(errorMessage = message) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
