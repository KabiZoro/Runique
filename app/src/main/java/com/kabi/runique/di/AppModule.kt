package com.kabi.runique.di

import android.content.Context
import androidx.datastore.dataStore
import com.kabi.runique.MainViewModel
import com.kabi.runique.RuniqueApp
import com.kabi.runique.datastore.AuthPrefSerializer
import kotlinx.coroutines.CoroutineScope
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

//val Context.dataStore by preferencesDataStore(name = "auth_pref")

private val Context.dataStore by dataStore(
    fileName = "auth-pref",
    serializer = AuthPrefSerializer
)

val appModule = module {
    /*single<SharedPreferences> {
        EncryptedSharedPreferences(
            androidApplication(),
            "auth_pref",
            MasterKey(androidApplication()),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }*/
    single { androidApplication().dataStore }
    single<CoroutineScope> {
        (androidApplication() as RuniqueApp).applicationScope
    }
    viewModelOf(::MainViewModel)
}
