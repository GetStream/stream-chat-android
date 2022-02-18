package io.getstream.chat.android.client.experimental.errorhandler.factory

import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * Factory used to provide an [ErrorHandler] that will be used to handle plugins' errors.
 *
 * @see [io.getstream.chat.android.client.experimental.plugin.Plugin]
 */
@ExperimentalStreamChatApi
public interface ErrorHandlerFactory {

    /**
     * Provides a single instance of [ErrorHandler].
     *
     * @return The [ErrorHandler] instance.
     */
    public fun getOrCreate(): ErrorHandler
}
