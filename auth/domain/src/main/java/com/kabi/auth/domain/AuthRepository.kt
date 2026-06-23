package com.kabi.auth.domain

import com.kabi.core.domain.util.DataError
import com.kabi.core.domain.util.EmptyResult

interface AuthRepository {
    suspend fun login(email: String, password: String): EmptyResult<DataError.Network>
    suspend fun register(email: String, password: String): EmptyResult<DataError.Network>
}