package com.app.printf.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.printf.domain.repository.ProductRepository
import com.app.printf.ui.state.SelectProductUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SelectProductViewModel(
    productRepository: ProductRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SelectProductUiState())
    val uiState: StateFlow<SelectProductUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            productRepository.observeProducts().collect { products ->
                _uiState.update { it.copy(products = products) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onProductSelected(productId: Long) {
        _uiState.update { it.copy(selectedProductId = productId) }
    }

    fun onQuantityChange(quantity: String) {
        _uiState.update { it.copy(quantityText = quantity.filter { it.isDigit() }) }
    }

    fun increaseQuantity() {
        val current = _uiState.value.quantityText.toIntOrNull()?.coerceAtLeast(1) ?: 1
        _uiState.update { it.copy(quantityText = (current + 1).toString()) }
    }

    fun decreaseQuantity() {
        val current = _uiState.value.quantityText.toIntOrNull()?.coerceAtLeast(1) ?: 1
        if (current > 1) {
            _uiState.update { it.copy(quantityText = (current - 1).toString()) }
        }
    }
}
