package com.kabi.auth.presentation.di

import com.kabi.auth.presentation.login.LoginViewModel
import com.kabi.auth.presentation.register.RegisterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authViewModelModule = module {
    viewModelOf(::RegisterViewModel)
    viewModelOf(::LoginViewModel)
}