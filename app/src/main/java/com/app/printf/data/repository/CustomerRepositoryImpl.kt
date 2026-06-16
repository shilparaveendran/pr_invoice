package com.app.printf.data.repository

import com.app.printf.data.dao.CustomerDao
import com.app.printf.data.entity.Customer
import com.app.printf.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow

class CustomerRepositoryImpl(
    private val customerDao: CustomerDao,
) : CustomerRepository {
    override fun observeCustomers(): Flow<List<Customer>> = customerDao.observeAll()

    override suspend fun addCustomer(name: String, address: String, gstin: String, mobile: String) {
        customerDao.insert(
            Customer(
                name = name.trim(),
                address = address.trim(),
                gstin = gstin.trim().uppercase(),
                mobile = mobile.trim(),
            ),
        )
    }

    override suspend fun updateCustomer(customer: Customer) {
        customerDao.update(customer)
    }

    override suspend fun deleteCustomer(customer: Customer) {
        customerDao.delete(customer)
    }
}
