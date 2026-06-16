package com.app.printf.ui.state

import com.app.printf.data.entity.Product

data class SelectProductUiState(
    val products: List<Product> = emptyList(),
    val searchQuery: String = "",
    val selectedProductId: Long? = null,
    val quantityText: String = "1",
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

    val selectedProduct: Product?
        get() = products.find { it.id == selectedProductId }
}
