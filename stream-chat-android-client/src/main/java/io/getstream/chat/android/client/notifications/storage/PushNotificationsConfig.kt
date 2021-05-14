package io.getstream.chat.android.client.notifications.storage

internal data class PushNotificationsConfig(
    val userId: String,
    val userToken: String,
) {
    internal fun isValid(): Boolean = userId.isNotEmpty() && userToken.isNotEmpty()
}
