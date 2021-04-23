package io.getstream.chat.android.livedata.service.sync

import android.content.Context
import android.content.SharedPreferences
import io.getstream.chat.android.client.extensions.getNonNullString
import io.getstream.chat.android.client.notifications.handler.NotificationConfig

private const val NOTIFICATION_CONFIG_PREFS_NAME = "stream_notification_config_store"
private const val KEY_ERROR_CONTENT = "key_error_content"
private const val KEY_ERROR_TITLE = "key_error_title"
private const val KEY_FIREBASE_CHANNEL_ID_KEY = "key_firebase_channel_id"
private const val KEY_FIREBASE_CHANNEL_TYPE_KEY = "key_firebase_channel_type_key"
private const val KEY_FIREBASE_CHANNEL_NAME_KEY = "key_firebase_channel_name_key"
private const val KEY_FIREBASE_MESSAGE_ID_KEY = "key_firebase_message_id_key"
private const val KEY_FIREBASE_MESSAGE_TEXT_KEY = "key_firebase_message_text_key"
private const val KEY_NOTIFICATION_CHANNEL_ID = "key_firebase_notification_channel_id"
private const val KEY_NOTIFICATION_CHANNEL_NAME = "key_notification_channel_name"
private const val KEY_SMALL_ICON = "key_small_icon"
private const val DEFAULT_INT = -1

internal class NotificationConfigStore(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        NOTIFICATION_CONFIG_PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun put(config: NotificationConfig) {
        prefs.edit().apply {
            putInt(KEY_ERROR_CONTENT, config.errorCaseNotificationContent)
            putInt(KEY_ERROR_TITLE, config.errorCaseNotificationTitle)
            putInt(KEY_NOTIFICATION_CHANNEL_ID, config.notificationChannelId)
            putInt(KEY_NOTIFICATION_CHANNEL_NAME, config.notificationChannelName)
            putInt(KEY_SMALL_ICON, config.smallIcon)
            putString(KEY_FIREBASE_CHANNEL_ID_KEY, config.firebaseChannelIdKey)
            putString(KEY_FIREBASE_CHANNEL_TYPE_KEY, config.firebaseChannelTypeKey)
            putString(KEY_FIREBASE_MESSAGE_ID_KEY, config.firebaseMessageIdKey)
        }.apply()
    }

    fun get(): NotificationConfig {
        val errorContentResId = prefs.getInt(KEY_ERROR_CONTENT, DEFAULT_INT)
        val errorCaseTitleResId = prefs.getInt(KEY_ERROR_TITLE, DEFAULT_INT)
        val notificationChannelIdResId = prefs.getInt(KEY_NOTIFICATION_CHANNEL_ID, DEFAULT_INT)
        val notificationChannelNameResId = prefs.getInt(KEY_NOTIFICATION_CHANNEL_NAME, DEFAULT_INT)
        val smallIconDrawableResId = prefs.getInt(KEY_SMALL_ICON, DEFAULT_INT)
        val firebaseMessageIdKey = prefs.getNonNullString(KEY_FIREBASE_MESSAGE_ID_KEY, "")
        val firebaseMessageTextKey = prefs.getNonNullString(KEY_FIREBASE_MESSAGE_TEXT_KEY, "")
        val firebaseChannelIdKey = prefs.getNonNullString(KEY_FIREBASE_CHANNEL_ID_KEY, "")
        val firebaseChannelTypeKey = prefs.getNonNullString(KEY_FIREBASE_CHANNEL_TYPE_KEY, "")
        val firebaseChannelNameKey = prefs.getNonNullString(KEY_FIREBASE_CHANNEL_NAME_KEY, "")

        return NotificationConfig(
            notificationChannelIdResId,
            notificationChannelNameResId,
            smallIconDrawableResId,
            firebaseMessageIdKey,
            firebaseMessageTextKey,
            firebaseChannelIdKey,
            firebaseChannelTypeKey,
            firebaseChannelNameKey,
            errorCaseTitleResId,
            errorContentResId
        )
    }

    fun clear() = prefs.edit().clear().apply()

    companion object {
        val NotificationConfigUnavailable: NotificationConfig = NotificationConfig(
            DEFAULT_INT,
            DEFAULT_INT,
            DEFAULT_INT,
            "",
            "",
            "",
            "",
            "",
            DEFAULT_INT,
            DEFAULT_INT
        )
    }
}
