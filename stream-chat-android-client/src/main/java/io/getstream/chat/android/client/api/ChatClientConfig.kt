package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.logger.ChatLogger

public class ChatClientConfig(
    public val apiKey: String,
    public var httpUrl: String,
    public var cdnHttpUrl: String,
    public var wssUrl: String,
    public val warmUp: Boolean,
    public val loggerConfig: ChatLogger.Config,
) {
    public var isAnonymous: Boolean = false
}
