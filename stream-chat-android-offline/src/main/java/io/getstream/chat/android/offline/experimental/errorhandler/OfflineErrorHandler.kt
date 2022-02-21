package io.getstream.chat.android.offline.experimental.errorhandler

import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.experimental.errorhandler.listeners.DeleteReactionErrorHandler
import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * Implementation of [ErrorHandler] that handles offline support errors.
 * This class work as a delegator of calls for one of its dependencies, so avoid to add logic here.
 *
 * @property name The name of the error handler.
 * @property DeleteReactionErrorHandler [DeleteReactionErrorHandler]
 */
@ExperimentalStreamChatApi
internal class OfflineErrorHandler(
    private val deleteReactionErrorHandler: DeleteReactionErrorHandler,
) : ErrorHandler,
    DeleteReactionErrorHandler by deleteReactionErrorHandler {

    override val name: String = "OfflineErrorHandler"
}
