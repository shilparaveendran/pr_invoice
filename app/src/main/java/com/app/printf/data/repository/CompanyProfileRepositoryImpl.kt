package com.app.printf.data.repository

import com.app.printf.data.dao.CompanyProfileDao
import com.app.printf.data.entity.CompanyProfile
import com.app.printf.domain.repository.CompanyProfileRepository

class CompanyProfileRepositoryImpl(
    private val companyProfileDao: CompanyProfileDao,
) : CompanyProfileRepository {
    override suspend fun getProfile(): CompanyProfile {
        return companyProfileDao.getProfile() ?: defaultProfile()
    }

    override suspend fun upsertProfile(profile: CompanyProfile) {
        companyProfileDao.upsert(profile)
    }

    private fun defaultProfile(): CompanyProfile {
        return CompanyProfile(
            companyName = "PR ENGINEERING",
            address = "PR ENGINEERING\nPuttekkad, Feroke",
            gstin = "",
            pan = "",
            phone = "",
            email = "",
            state = "",
            salesType = "State Sale",
            accountHolderName = "PR ENGINEERING",
            accountNumber = "",
            ifscCode = "",
            accountType = "CURRENT",
            bankName = "",
        )
    }
}

