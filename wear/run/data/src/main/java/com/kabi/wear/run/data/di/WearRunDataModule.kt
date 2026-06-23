package com.kabi.wear.run.data.di

import com.kabi.wear.run.data.HealthServicesExerciseTracker
import com.kabi.wear.run.data.WatchToPhoneConnector
import com.kabi.wear.run.domain.ExerciseTracker
import com.kabi.wear.run.domain.PhoneConnector
import com.kabi.wear.run.domain.RunningTracker
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val wearRunDataModule = module {
    singleOf(::HealthServicesExerciseTracker).bind<ExerciseTracker>()
    singleOf(::WatchToPhoneConnector).bind<PhoneConnector>()
    singleOf(::RunningTracker)
    single {
        get<RunningTracker>().elapsedTime
    }
}