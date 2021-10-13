package io.getstream.chat.android.client.notifications.storage

internal data class PushNotificationsConfig(
    val userId: String,
    val userToken: String,
    val userName: String,
) {
    internal fun isValid(): Boolean = userId.isNotEmpty() && userToken.isNotEmpty() && userName.isNotEmpty()
}
