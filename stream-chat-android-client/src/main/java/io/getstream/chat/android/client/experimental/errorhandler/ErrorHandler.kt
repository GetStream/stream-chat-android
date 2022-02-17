package io.getstream.chat.android.client.experimental.errorhandler

import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * Extension for [io.getstream.chat.android.client.ChatClient] that allows handling plugins' errors.
 *
 * @see [io.getstream.chat.android.client.experimental.plugin.Plugin]
 */
@ExperimentalStreamChatApi
public interface ErrorHandler {

    /**
     * The name of this plugin.
     */
    public val name: String
}
