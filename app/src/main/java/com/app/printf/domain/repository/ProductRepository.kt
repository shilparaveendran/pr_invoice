package com.app.printf.domain.repository

import com.app.printf.data.entity.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeProducts(): Flow<List<Product>>
    suspend fun addProduct(name: String, hsn: String, price: Double)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
}
