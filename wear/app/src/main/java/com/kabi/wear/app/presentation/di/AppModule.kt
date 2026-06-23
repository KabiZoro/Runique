package com.kabi.wear.app.presentation.di

import com.kabi.wear.app.presentation.RuniqueApp
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {
    single {
        (androidApplication() as RuniqueApp).applicationScope
    }
}