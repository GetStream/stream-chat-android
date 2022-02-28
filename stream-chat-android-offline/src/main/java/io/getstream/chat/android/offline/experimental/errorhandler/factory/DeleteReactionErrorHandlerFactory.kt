package io.getstream.chat.android.offline.experimental.errorhandler.factory

import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.experimental.errorhandler.factory.ErrorHandlerFactory
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.errorhandler.listener.DeleteReactionErrorHandlerImpl
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry

@ExperimentalStreamChatApi
/**
 * Factory for [DeleteReactionErrorHandlerImpl]
 */
public class DeleteReactionErrorHandlerFactory : ErrorHandlerFactory {

    override fun create(): ErrorHandler {
        return DeleteReactionErrorHandlerImpl(
            logic = LogicRegistry.get(),
            globalState = GlobalMutableState.getOrCreate(),
        )
    }
}
