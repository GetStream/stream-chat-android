package io.getstream.chat.android.client.notifications.storage

import io.getstream.chat.android.client.user.CredentialConfig

internal class PushNotificationsConfig(private val credentialConfig: CredentialConfig) {

    internal constructor(userId: String, userToken: String, userName: String) : this(CredentialConfig(userId, userToken, userName))

    val userId: String by credentialConfig::userId
    val userToken: String by credentialConfig::userToken
    val userName: String by credentialConfig::userName

    internal fun isValid(): Boolean = credentialConfig.isValid()
}
