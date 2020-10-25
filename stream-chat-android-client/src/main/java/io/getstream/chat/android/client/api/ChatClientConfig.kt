package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.token.TokenManagerImpl
import io.getstream.chat.android.client.uploader.FileUploader

internal class ChatClientConfig(
    val apiKey: String,
    var httpUrl: String,
    var cdnHttpUrl: String,
    var wssUrl: String,
    var baseTimeout: Long,
    var cdnTimeout: Long,
    val warmUp: Boolean,
    val loggerConfig: ChatLogger.Config,
    val notificationsHandler: ChatNotificationHandler,
    val fileUploader: FileUploader? = null,
    val tokenManager: TokenManager = TokenManagerImpl()
) {

    var isAnonymous: Boolean = false
}
