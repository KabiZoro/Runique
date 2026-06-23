package com.kabi.core.data.di

import com.kabi.core.data.auth.EncryptedSessionStorage
import com.kabi.core.data.networking.HttpClientFactory
import com.kabi.core.data.run.OfflineFirstRunRepository
import com.kabi.core.domain.SessionStorage
import com.kabi.core.domain.run.RunRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
    single {
        HttpClientFactory(get()).build()
    }

    singleOf(::EncryptedSessionStorage).bind<SessionStorage>()
    singleOf(::OfflineFirstRunRepository).bind<RunRepository>()
}