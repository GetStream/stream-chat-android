package io.getstream.chat.android.client.notifications.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.user.CredentialConfig
import io.getstream.chat.android.client.user.storage.UserCredentialStorage

internal class EncryptedPushNotificationsConfigStore(context: Context) : UserCredentialStorage {
    private val prefs: SharedPreferences
    private val logger = ChatLogger.get("EncryptedBackgroundSyncConfigStore")

    init {
        prefs = try {
            val masterKey = MasterKey.Builder(context, MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_SYNC_CONFIG_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } catch (e: Exception) {
            logger.logE("Error creating encrypted shared preferences", e)
            context.applicationContext.getSharedPreferences(
                SYNC_CONFIG_PREFS_NAME,
                Context.MODE_PRIVATE,
            )
        }
    }

    override fun get(): CredentialConfig? = prefs.run {
        val userId = getString(KEY_USER_ID, "") ?: ""
        val userToken = getString(KEY_USER_TOKEN, "") ?: ""
        val userName = getString(KEY_USER_NAME, "") ?: ""

        val config = CredentialConfig(userId = userId, userToken = userToken, userName = userName)

        return config.takeIf(CredentialConfig::isValid)
    }

    override fun clear() = prefs.edit().clear().apply()

    override fun put(credentialConfig: CredentialConfig) {
        prefs.edit().apply {
            putString(KEY_USER_ID, credentialConfig.userId)
            putString(KEY_USER_TOKEN, credentialConfig.userToken)
            putString(KEY_USER_NAME, credentialConfig.userName)
        }.apply()
    }

    companion object {
        private const val MASTER_KEY_ALIAS = "_stream_sync_config_master_key_"
        private const val ENCRYPTED_SYNC_CONFIG_PREFS_NAME = "stream_livedata_sync_config_store"
        private const val SYNC_CONFIG_PREFS_NAME = ".stream_livedata_sync_config_store"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_TOKEN = "user_token"
        private const val KEY_USER_NAME = "user_name"
    }
}
