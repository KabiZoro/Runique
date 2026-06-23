package com.kabi.auth.data

import com.kabi.auth.domain.AuthRepository
import com.kabi.core.data.networking.post
import com.kabi.core.domain.AuthInfo
import com.kabi.core.domain.SessionStorage
import com.kabi.core.domain.util.DataError
import com.kabi.core.domain.util.EmptyResult
import com.kabi.core.domain.util.Result
import com.kabi.core.domain.util.asEmptyDataResult
import io.ktor.client.HttpClient
import io.ktor.utils.io.core.toByteArray

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val sessionStorage: SessionStorage
) : AuthRepository {

    override suspend fun login(
        email: String,
        password: String
    ): EmptyResult<DataError.Network> {
        val result = httpClient.post<LoginRequest, LoginResponse>(
            route = "/login",
            body = LoginRequest(
                email = email,
                password = password
            )
        )
        if (result is Result.Success){
            sessionStorage.set(
                AuthInfo(
                    accessToken = result.data.accessToken,
                    refreshToken = result.data.refreshToken,
                    userId = result.data.userId
                )
            )
        }
        return result.asEmptyDataResult()
    }

    override suspend fun register(
        email: String,
        password: String
    ): EmptyResult<DataError.Network> {
        return httpClient.post<RegisterRequest, Unit>(
            route = "/register",
            body = RegisterRequest(
                email = email,
                password = password
            )
        )
    }
}