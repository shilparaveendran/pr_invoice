package com.app.printf.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.printf.data.entity.Product
import com.app.printf.domain.repository.ProductRepository
import com.app.printf.ui.state.ProductFormState
import com.app.printf.ui.state.ProductUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductViewModel(
    private val productRepository: ProductRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            productRepository.observeProducts().collect { products ->
                _uiState.update { it.copy(products = products) }
            }
        }
    }

    fun onNameChange(value: String) {
        updateForm { it.copy(name = value, errorMessage = null) }
    }

    fun onHsnChange(value: String) {
        updateForm { it.copy(hsn = value, errorMessage = null) }
    }

    fun onPriceChange(value: String) {
        updateForm { it.copy(priceText = value, errorMessage = null) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun startEdit(product: Product) {
        updateForm {
            ProductFormState(
                name = product.name,
                hsn = product.hsn,
                priceText = product.price.toString(),
                editingProductId = product.id,
            )
        }
    }

    fun cancelEdit() {
        updateForm { ProductFormState() }
    }

    fun saveProduct() {
        val form = _uiState.value.form
        val price = form.priceText.toDoubleOrNull()
        when {
            form.name.isBlank() -> setFormError("Product name is required")
            form.hsn.isBlank() -> setFormError("HSN number is required")
            price == null || price <= 0 -> setFormError("Enter a valid price")
            else -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isSaving = true, successMessage = null) }
                    if (form.isEditing) {
                        val product = _uiState.value.products.find { it.id == form.editingProductId }
                        if (product != null) {
                            productRepository.updateProduct(
                                product.copy(
                                    name = form.name.trim(),
                                    hsn = form.hsn.trim(),
                                    price = price,
                                ),
                            )
                        }
                    } else {
                        productRepository.addProduct(form.name, form.hsn, price)
                    }
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            form = ProductFormState(),
                            successMessage = if (form.isEditing) "Product updated" else "Product added",
                        )
                    }
                }
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            if (_uiState.value.form.editingProductId == product.id) {
                updateForm { ProductFormState() }
            }
            productRepository.deleteProduct(product)
        }
    }

    private fun updateForm(transform: (ProductFormState) -> ProductFormState) {
        _uiState.update { state -> state.copy(form = transform(state.form)) }
    }

    private fun setFormError(message: String) {
        updateForm { it.copy(errorMessage = message) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
