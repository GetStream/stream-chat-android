package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig
import io.getstream.chat.android.client.token.CachedTokenProvider
import io.getstream.chat.android.client.token.CachedTokenProviderImpl


class ChatClientConfig(
    val apiKey: String,
    var httpUrl: String,
    var cdnHttpUrl: String,
    var wssUrl: String,
    var baseTimeout: Long,
    var cdnTimeout: Long,
    val logLevel: ChatLogLevel,
    val notificationsConfig: ChatNotificationConfig
) {

    val tokenProvider: CachedTokenProvider = CachedTokenProviderImpl()
    var isAnonymous: Boolean = false
}