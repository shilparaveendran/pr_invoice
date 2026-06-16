package com.app.printf.domain.repository

import com.app.printf.data.entity.CompanyProfile

interface CompanyProfileRepository {
    suspend fun getProfile(): CompanyProfile
    suspend fun upsertProfile(profile: CompanyProfile)
}

