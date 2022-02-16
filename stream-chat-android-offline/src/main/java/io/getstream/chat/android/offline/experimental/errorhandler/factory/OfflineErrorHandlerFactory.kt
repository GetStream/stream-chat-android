package io.getstream.chat.android.offline.experimental.errorhandler.factory

import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.experimental.errorhandler.factory.ErrorHandlerFactory
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.errorhandler.OfflineErrorHandler
import io.getstream.chat.android.offline.experimental.errorhandler.listener.DeleteReactionErrorHandlerImpl
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry

/**
 * Implementation of [ErrorHandlerFactory] that provides [ErrorHandler].
 * Assumes that [OfflinePlugin] is already initialized
 */
@ExperimentalStreamChatApi
public class OfflineErrorHandlerFactory : ErrorHandlerFactory {

    private val offlineErrorHandler: OfflineErrorHandler by lazy(::createOfflineErrorHandler)

    override fun getOrCreate(): ErrorHandler = offlineErrorHandler

    private fun createOfflineErrorHandler(): OfflineErrorHandler {
        val deleteReactionErrorHandler = DeleteReactionErrorHandlerImpl(
            logic = LogicRegistry.get(),
            globalState = GlobalMutableState.getOrCreate(),
        )
        return OfflineErrorHandler(
            deleteReactionErrorHandler = deleteReactionErrorHandler,
        )
    }
}
