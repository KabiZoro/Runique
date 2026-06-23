package com.kabi.analytics.data.di

import com.kabi.analytics.data.RoomAnalyticsRepository
import com.kabi.analytics.domain.AnalyticsRepository
import com.kabi.core.database.RunDatabase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val analyticsModule = module {
    singleOf(::RoomAnalyticsRepository).bind<AnalyticsRepository>()
    single { get<RunDatabase>().analyticsDao }
}