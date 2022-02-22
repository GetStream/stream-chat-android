package io.getstream.chat.android.offline.experimental.errorhandler.factory

import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.experimental.errorhandler.factory.ErrorHandlerFactory
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.experimental.errorhandler.listener.DeleteReactionErrorHandlerProposalImpl
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import kotlinx.coroutines.CoroutineScope

@ExperimentalStreamChatApi
/**
 * Factory for [DeleteReactionErrorHandlerProposalImpl]
 */
public class DeleteReactionErrorHandlerFactory : ErrorHandlerFactory {

    override fun create(): ErrorHandler {
        return DeleteReactionErrorHandlerProposalImpl(
            scope = CoroutineScope(DispatcherProvider.IO),
            logic = LogicRegistry.get(),
            globalState = GlobalMutableState.getOrCreate(),
        )
    }
}
