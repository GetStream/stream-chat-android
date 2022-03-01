package io.getstream.chat.android.offline.experimental.errorhandler.factory

import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.experimental.errorhandler.factory.ErrorHandlerFactory
import io.getstream.chat.android.offline.experimental.errorhandler.listener.DeleteReactionErrorHandlerImpl
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry

/**
 * Factory for [DeleteReactionErrorHandlerImpl]
 */
public class DeleteReactionErrorHandlerFactory : ErrorHandlerFactory {

    override fun create(): ErrorHandler {
        return DeleteReactionErrorHandlerImpl(
            scope = StateRegistry.get().scope,
            logic = LogicRegistry.get(),
            globalState = GlobalMutableState.getOrCreate(),
        )
    }
}
