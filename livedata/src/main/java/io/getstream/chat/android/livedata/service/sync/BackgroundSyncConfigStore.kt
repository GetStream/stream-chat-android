package io.getstream.chat.android.livedata.service.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

interface BackgroundSyncConfigStore {
    fun put(config: BackgroundSyncConfig)
    fun get(): BackgroundSyncConfig
    fun clear()
}

class EncryptedBackgroundSyncConfigStore(context: Context) : BackgroundSyncConfigStore {
    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context, MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            context,
            SYNC_CONFIG_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun put(config: BackgroundSyncConfig) {
        prefs.edit().apply {
            putString(KEY_API_KEY, config.apiKey)
            putString(KEY_USER_ID, config.userId)
            putString(KEY_USER_TOKEN, config.userToken)
        }.apply()
    }

    override fun get(): BackgroundSyncConfig = prefs.run {
        val apiKey = getString(KEY_API_KEY, "")
        val userId = getString(KEY_USER_ID, "")
        val userToken = getString(KEY_USER_TOKEN, "")

        if (apiKey == null || userId == null || userToken == null) {
            BackgroundSyncConfig.UNAVAILABLE
        } else {
            BackgroundSyncConfig(apiKey, userId, userToken)
        }
    }

    override fun clear() = prefs.edit().clear().apply()

    companion object {
        const val MASTER_KEY_ALIAS = "_stream_sync_config_master_key_"
        const val SYNC_CONFIG_PREFS_NAME = "stream_livedata_sync_config_store"
        const val KEY_API_KEY = "api_key"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_TOKEN = "user_token"
    }
}