package com.app.printf.ui.state

import com.app.printf.data.entity.Product

data class ProductFormState(
    val name: String = "",
    val hsn: String = "",
    val priceText: String = "",
    val editingProductId: Long? = null,
    val errorMessage: String? = null,
) {
    val isEditing: Boolean get() = editingProductId != null
}

data class ProductUiState(
    val products: List<Product> = emptyList(),
    val searchQuery: String = "",
    val form: ProductFormState = ProductFormState(),
    val isSaving: Boolean = false,
    val successMessage: String? = null,
) {
    val filteredProducts: List<Product>
        get() {
            val query = searchQuery.trim().lowercase()
            if (query.isEmpty()) return products
            return products.filter { product ->
                product.name.lowercase().contains(query) ||
                    product.hsn.lowercase().contains(query)
            }
        }
}
