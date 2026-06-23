package com.kabi.wear.run.presentation.di

import com.kabi.wear.run.presentation.TrackerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val wearRunPresentationModule = module {
    viewModelOf(::TrackerViewModel)
}