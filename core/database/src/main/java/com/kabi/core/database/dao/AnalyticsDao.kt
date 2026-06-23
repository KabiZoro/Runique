package com.kabi.core.database.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface AnalyticsDao {

    @Query("SELECT SUM(distanceMeters) FROM RUNENTITY")
    suspend fun getTotalDistanceRun(): Int

    @Query("SELECT SUM(durationMillis) FROM RUNENTITY")
    suspend fun getTotalTimeRun(): Long

    @Query("SELECT MAX(maxSpeedKmh) FROM RUNENTITY")
    suspend fun getMaxRunSpeed(): Double

    @Query("SELECT AVG(distanceMeters) FROM RUNENTITY")
    suspend fun getAvgDistancePerRun(): Double

    @Query("SELECT AVG((durationMillis / 60000.0) / (distanceMeters / 1000.0)) FROM RUNENTITY")
    suspend fun getAvgPacePerRun(): Double

}