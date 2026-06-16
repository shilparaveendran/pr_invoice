package com.app.printf.data.repository

import com.app.printf.data.dao.ProductDao
import com.app.printf.data.entity.Product
import com.app.printf.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

class ProductRepositoryImpl(
    private val productDao: ProductDao,
) : ProductRepository {
    override fun observeProducts(): Flow<List<Product>> = productDao.observeAll()

    override suspend fun addProduct(name: String, hsn: String, price: Double) {
        productDao.insert(Product(name = name.trim(), hsn = hsn.trim(), price = price))
    }

    override suspend fun updateProduct(product: Product) {
        productDao.update(product)
    }

    override suspend fun deleteProduct(product: Product) {
        productDao.delete(product)
    }
}
