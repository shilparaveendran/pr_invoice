package com.app.printf.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.app.printf.data.dao.CustomerDao
import com.app.printf.data.dao.CompanyProfileDao
import com.app.printf.data.dao.InvoiceDao
import com.app.printf.data.dao.ProductDao
import com.app.printf.data.entity.CompanyProfile
import com.app.printf.data.entity.Customer
import com.app.printf.data.entity.Invoice
import com.app.printf.data.entity.InvoiceLineItem
import com.app.printf.data.entity.Product

@Database(
    entities = [Product::class, Customer::class, Invoice::class, InvoiceLineItem::class, CompanyProfile::class],
    version = 10,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun companyProfileDao(): CompanyProfileDao
    abstract fun invoiceDao(): InvoiceDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "printf_db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
