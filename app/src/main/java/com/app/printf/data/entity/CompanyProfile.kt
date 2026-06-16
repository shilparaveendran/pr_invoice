package com.app.printf.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "company_profile")
data class CompanyProfile(
    @PrimaryKey
    val id: Int = 1,
    val companyName: String,
    val address: String,
    val gstin: String,
    val pan: String,
    val phone: String,
    val email: String,
    val state: String,
    val salesType: String,
    val accountHolderName: String,
    val accountNumber: String,
    val ifscCode: String,
    val accountType: String,
    val bankName: String,
    val signaturePath: String = "",
)

