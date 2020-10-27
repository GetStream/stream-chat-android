package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.token.TokenManagerImpl
import io.getstream.chat.android.client.uploader.FileUploader

public class ChatClientConfig(
    public val apiKey: String,
    public var httpUrl: String,
    public var cdnHttpUrl: String,
    public var wssUrl: String,
    public var baseTimeout: Long,
    public var cdnTimeout: Long,
    public val warmUp: Boolean,
    public val loggerConfig: ChatLogger.Config,
) {

    public var isAnonymous: Boolean = false
}
