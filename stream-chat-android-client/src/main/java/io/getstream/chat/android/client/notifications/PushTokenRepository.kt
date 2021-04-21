package io.getstream.chat.android.client.notifications

import android.content.Context
import android.content.SharedPreferences
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.getNonNullString
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler

internal class PushTokenRepository(
    context: Context,
    private val handler: ChatNotificationHandler,
) {
    private val logger = ChatLogger.get("ChatNotifications")

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private var userFirebaseToken: UserFirebaseToken
        set(value) {
            prefs.edit()
                .putString(KEY_USER_ID, value.userId)
                .putString(KEY_FIREBASE_TOKEN, value.firebaseToken)
                .apply()
        }
        get() {
            return UserFirebaseToken(
                userId = prefs.getNonNullString(KEY_USER_ID, ""),
                firebaseToken = prefs.getNonNullString(KEY_FIREBASE_TOKEN, "")
            )
        }

    /**
     * Registers the current device on the server if necessary. Does no do
     * anything if the token has already been sent to the server previously.
     */
    fun updateTokenIfNecessary(firebaseToken: String) {
        val userFirebaseToken = UserFirebaseToken(
            userId = ChatClient.instance().getCurrentUser()?.id ?: "",
            firebaseToken = firebaseToken
        )
        if (this.userFirebaseToken != userFirebaseToken) {
            ChatClient.instance().addDevice(firebaseToken).enqueue { result ->
                if (result.isSuccess) {
                    this.userFirebaseToken = userFirebaseToken

                    handler.getDeviceRegisteredListener()?.onDeviceRegisteredSuccess()
                    logger.logI("Device registered with token $firebaseToken")
                } else {
                    handler.getDeviceRegisteredListener()?.onDeviceRegisteredError(result.error())
                    logger.logE("Error registering device ${result.error().message}")
                }
            }
        }
    }

    private data class UserFirebaseToken(
        val userId: String,
        val firebaseToken: String,
    )

    companion object {
        private const val PREFS_NAME = "stream_firebase_token_store"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_FIREBASE_TOKEN = "firebase_token"
    }
}
