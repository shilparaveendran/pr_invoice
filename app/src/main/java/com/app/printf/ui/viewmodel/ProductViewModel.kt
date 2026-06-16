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
                priceText = formatPriceForEdit(product.price),
                editingProductId = product.id,
            )
        }
    }

    fun cancelEdit() {
        updateForm { ProductFormState() }
    }

    fun saveProduct() {
        val form = _uiState.value.form
        val price = parsePrice(form.priceText)
        when {
            form.name.isBlank() -> setFormError("Product name is required")
            form.hsn.isBlank() -> setFormError("HSN number is required")
            price == null || price <= 0 -> setFormError("Enter a valid price")
            form.isEditing && form.editingProductId == null -> setFormError("Product not found")
            else -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isSaving = true, successMessage = null) }
                    updateForm { it.copy(errorMessage = null) }
                    try {
                        if (form.isEditing) {
                            productRepository.updateProduct(
                                Product(
                                    id = form.editingProductId!!,
                                    name = form.name.trim(),
                                    hsn = form.hsn.trim(),
                                    price = price,
                                ),
                            )
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
                    } catch (e: Exception) {
                        _uiState.update { it.copy(isSaving = false) }
                        setFormError(e.message ?: "Failed to save product")
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

    private fun parsePrice(text: String): Double? {
        return text.trim().replace(",", "").toDoubleOrNull()
    }

    private fun formatPriceForEdit(price: Double): String {
        return if (price % 1.0 == 0.0) {
            price.toLong().toString()
        } else {
            price.toString()
        }
    }
}
