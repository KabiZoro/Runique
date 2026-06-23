package com.kabi.core.domain.run

import com.kabi.core.domain.util.DataError
import com.kabi.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

typealias RunId = String

interface LocalRunDataSource {
    suspend fun upsertRun(run: Run): Result<RunId, DataError.Local>
    suspend fun upsertRuns(runs: List<Run>): Result<List<RunId>, DataError.Local>
    fun getRuns(): Flow<List<Run>>
    suspend fun deleteRun(id: String)
    suspend fun deleteAllRuns()
}