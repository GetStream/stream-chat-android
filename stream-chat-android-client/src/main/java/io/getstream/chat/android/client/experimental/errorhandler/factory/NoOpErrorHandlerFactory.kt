package io.getstream.chat.android.client.experimental.errorhandler.factory

import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * Implementation of [ErrorHandlerFactory] that creates an no-op [ErrorHandler].
 */
@ExperimentalStreamChatApi
internal class NoOpErrorHandlerFactory : ErrorHandlerFactory {

    private val noOpErrorHandler by lazy {
        object : ErrorHandler {
            override val name: String = "NoOpErrorHandler"
        }
    }

    /**
     * Returns a no-op [ErrorHandler] implementation.
     */
    override fun getOrCreate() = noOpErrorHandler
}
