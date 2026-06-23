package com.kabi.auth.data.di

import com.kabi.auth.data.AuthRepositoryImpl
import com.kabi.auth.data.EmailPatternValidator
import com.kabi.auth.domain.AuthRepository
import com.kabi.auth.domain.PatternValidator
import com.kabi.auth.domain.UserDataValidator
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authDataModule = module {
    single<PatternValidator>{
        EmailPatternValidator
    }
    singleOf(::UserDataValidator)
    singleOf(::AuthRepositoryImpl).bind<AuthRepository>()

}