package io.getstream.chat.android.client.logger

/**
 * A logging config to be used by the client.
 */
public interface ChatLoggerConfig {
    public val level: ChatLogLevel
    public val handler: ChatLoggerHandler?
}

internal data class ChatLoggerConfigImpl(
    override val level: ChatLogLevel,
    override val handler: ChatLoggerHandler?
) : ChatLoggerConfig