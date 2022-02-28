package io.getstream.chat.android.offline.experimental.errorhandler.factory

import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.experimental.errorhandler.factory.ErrorHandlerFactory
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.errorhandler.listener.SendReactionErrorHandlerImpl
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry

@ExperimentalStreamChatApi
/**
 * Factory for [SendReactionErrorHandlerImpl].
 */
public class SendReactionErrorHandlerFactory : ErrorHandlerFactory {
    override fun create(): ErrorHandler {
        return SendReactionErrorHandlerImpl(
            scope = LogicRegistry.get().scope,
            globalState = GlobalMutableState.getOrCreate(),
        )
    }
}
