package com.kabi.core.data.auth

import androidx.datastore.core.DataStore
import com.kabi.core.domain.AuthInfo
import com.kabi.core.domain.SessionStorage
import kotlinx.coroutines.flow.first

class EncryptedSessionStorage(
    private val dataStore: DataStore<AuthInfoSerializable?>
) : SessionStorage {

    override suspend fun get(): AuthInfo? {
        return dataStore.data.first()?.toAuthInfo()
    }

    override suspend fun set(info: AuthInfo?) {
        dataStore.updateData {
            info?.toAuthInfoSerializable()
        }
    }
}

/*class EncryptedSessionStorage(
    private val dataStore: DataStore<Preferences>
) : SessionStorage {

    override suspend fun get(): AuthInfo? {
        val json = dataStore.data.first()[KEY_AUTH_INFO]
        return json?.let {
            Json.decodeFromString<AuthInfoSerializable>(it).toAuthInfo()
        }
    }

    override suspend fun set(info: AuthInfo?) {
        if (info == null) {
            dataStore.edit { preferences ->
                preferences.remove(KEY_AUTH_INFO)
            }
            return
        }

        val json = Json.encodeToString(info.toAuthInfoSerializable())
        dataStore.edit { preferences ->
            preferences[KEY_AUTH_INFO] = json
        }
    }

    companion object {
        private val KEY_AUTH_INFO = stringPreferencesKey("KEY_AUTH_INFO")
    }
}*/

/*class EncryptedSessionStorage(
    private val sharedPreferences: SharedPreferences
) : SessionStorage {
    override suspend fun get(): AuthInfo? {
        return withContext(Dispatchers.IO) {
            val json = sharedPreferences.getString(KEY_AUTH_INFO, null)
            json?.let {
                Json.decodeFromString<AuthInfoSerializable>(it).toAuthInfo()
            }
        }
    }

    override suspend fun set(info: AuthInfo?) {
        withContext(Dispatchers.IO) {
            if (info == null) {
                sharedPreferences
                    .edit(commit = true) {
                        remove(KEY_AUTH_INFO)
                    }
                return@withContext
            }

            val json = Json.encodeToString(info.toAuthInfoSerializable())
            sharedPreferences
                .edit(commit = true) {
                    putString(KEY_AUTH_INFO, json)
                }
        }
    }

    companion object {
        private const val KEY_AUTH_INFO = "KEY_AUTH_INFO"
    }
}*/