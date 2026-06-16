package com.app.printf.domain.repository

import com.app.printf.data.entity.Customer
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun observeCustomers(): Flow<List<Customer>>
    suspend fun addCustomer(name: String, address: String, gstin: String = "", mobile: String = "")
    suspend fun updateCustomer(customer: Customer)
    suspend fun deleteCustomer(customer: Customer)
}
