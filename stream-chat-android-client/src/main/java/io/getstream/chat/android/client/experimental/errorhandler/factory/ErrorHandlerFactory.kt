package io.getstream.chat.android.client.experimental.errorhandler.factory

import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Factory used to provide an [ErrorHandler] that will be used to handle plugins' errors.
 *
 * @see [io.getstream.chat.android.client.experimental.plugin.Plugin]
 */
@InternalStreamChatApi
public interface ErrorHandlerFactory {

    /**
     * Provides a single instance of [ErrorHandler].
     *
     * @return The [ErrorHandler] instance.
     */
    public fun create(): ErrorHandler
}
