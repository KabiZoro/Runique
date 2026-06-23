package com.kabi.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kabi.core.database.dao.AnalyticsDao
import com.kabi.core.database.dao.RunDao
import com.kabi.core.database.dao.RunPendingSyncDao
import com.kabi.core.database.entity.DeletedRunSyncEntity
import com.kabi.core.database.entity.RunEntity
import com.kabi.core.database.entity.RunPendingSyncEntity

@Database(
    entities = [
        RunEntity::class,
        RunPendingSyncEntity::class,
        DeletedRunSyncEntity::class
    ],
    version = 2
)
abstract class RunDatabase : RoomDatabase() {

    abstract val runDao: RunDao
    abstract val runPendingSyncDao: RunPendingSyncDao
    abstract val analyticsDao: AnalyticsDao

}