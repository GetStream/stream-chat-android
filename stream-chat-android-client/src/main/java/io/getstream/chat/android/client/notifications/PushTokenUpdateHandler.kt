package io.getstream.chat.android.client.notifications

import android.content.Context
import android.content.SharedPreferences
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.extensions.getNonNullString
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushProvider
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler

internal class PushTokenUpdateHandler(
    context: Context,
    private val handler: ChatNotificationHandler,
) {
    private val logger = ChatLogger.get("ChatNotifications")

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private var userPushToken: UserPushToken
        set(value) {
            prefs.edit()
                .putString(KEY_USER_ID, value.userId)
                .putString(KEY_TOKEN, value.token)
                .putString(KEY_PUSH_PROVIDER, value.pushProvider)
                .apply()
        }
        get() {
            return UserPushToken(
                userId = prefs.getNonNullString(KEY_USER_ID, ""),
                token = prefs.getNonNullString(KEY_TOKEN, ""),
                pushProvider = prefs.getNonNullString(KEY_PUSH_PROVIDER, ""),
            )
        }

    /**
     * Registers the current device on the server if necessary. Does no do
     * anything if the token has already been sent to the server previously.
     */
    suspend fun updateDeviceIfNecessary(device: Device) {
        val userPushToken = device.toUserPushToken()
        if (device.isValid() && this.userPushToken != userPushToken) {
            removeStoredDevice()
            val result = ChatClient.instance().addDevice(device).await()
            if (result.isSuccess) {
                this.userPushToken = userPushToken
                handler.getDeviceRegisteredListener()?.onDeviceRegisteredSuccess()
                logger.logI("Device registered with token ${device.token} (${device.pushProvider.key})")
            } else {
                handler.getDeviceRegisteredListener()?.onDeviceRegisteredError(result.error())
                logger.logE("Error registering device ${result.error().message}")
            }
        }
    }

    suspend fun removeStoredDevice() {
        userPushToken.toDevice()
            .takeIf { it.isValid() }
            ?.let {
                if (ChatClient.instance().deleteDevice(it).await().isSuccess) {
                    userPushToken = UserPushToken("", "", "")
                }
            }
    }

    private data class UserPushToken(
        val userId: String,
        val token: String,
        val pushProvider: String,
    )

    companion object {
        private const val PREFS_NAME = "stream_firebase_token_store"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TOKEN = "token"
        private const val KEY_PUSH_PROVIDER = "push_provider"
    }

    private fun Device.toUserPushToken() = UserPushToken(
        userId = ChatClient.instance().getCurrentUser()?.id ?: "",
        token = token,
        pushProvider = pushProvider.key
    )

    private fun UserPushToken.toDevice() = Device(token = token, pushProvider = PushProvider.fromKey(pushProvider))

    private fun Device.isValid() = pushProvider != PushProvider.UNKNOWN
}
