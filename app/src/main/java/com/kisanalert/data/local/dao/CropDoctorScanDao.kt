package com.kisanalert.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kisanalert.data.local.entity.CropDoctorScanEntity

@Dao
interface CropDoctorScanDao {
    @Query(
        "SELECT * FROM crop_doctor_scans WHERE userId = :userId ORDER BY scannedAt DESC LIMIT 20"
    )
    suspend fun getRecentScans(userId: String): List<CropDoctorScanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: CropDoctorScanEntity)
}
