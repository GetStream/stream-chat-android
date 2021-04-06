package io.getstream.chat.android.livedata.service.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.getstream.chat.android.client.logger.ChatLogger

private const val MASTER_KEY_ALIAS = "_stream_sync_config_master_key_"
private const val ENCRYPTED_SYNC_CONFIG_PREFS_NAME = "stream_livedata_sync_config_store"
private const val SYNC_CONFIG_PREFS_NAME = ".stream_livedata_sync_config_store"
private const val KEY_API_KEY = "api_key"
private const val KEY_USER_ID = "user_id"
private const val KEY_USER_TOKEN = "user_token"

internal class EncryptedBackgroundSyncConfigStore(context: Context) {
    private val prefs: SharedPreferences
    private val logger = ChatLogger.get("EncryptedBackgroundSyncConfigStore")

    init {
        val masterKey = MasterKey.Builder(context, MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = try {
            EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_SYNC_CONFIG_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            logger.logE("Error creating encrypted shared preferences", e)
            context.applicationContext.getSharedPreferences(
                SYNC_CONFIG_PREFS_NAME,
                Context.MODE_PRIVATE
            )
        }
    }

    fun put(config: BackgroundSyncConfig) {
        prefs.edit().apply {
            putString(KEY_API_KEY, config.apiKey)
            putString(KEY_USER_ID, config.userId)
            putString(KEY_USER_TOKEN, config.userToken)
        }.apply()
    }

    fun get(): BackgroundSyncConfig? = prefs.run {
        val apiKey = getString(KEY_API_KEY, "") ?: ""
        val userId = getString(KEY_USER_ID, "") ?: ""
        val userToken = getString(KEY_USER_TOKEN, "") ?: ""

        val config = BackgroundSyncConfig(apiKey, userId, userToken)
        return if (config.isValid()) config else { null }
    }

    fun clear() = prefs.edit().clear().apply()
}
