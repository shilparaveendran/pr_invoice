package com.app.printf.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.printf.data.entity.CompanyProfile

@Dao
interface CompanyProfileDao {
    @Query("SELECT * FROM company_profile WHERE id = 1")
    suspend fun getProfile(): CompanyProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: CompanyProfile)
}

