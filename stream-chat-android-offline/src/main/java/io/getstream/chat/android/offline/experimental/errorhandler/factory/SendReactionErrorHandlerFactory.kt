package io.getstream.chat.android.offline.experimental.errorhandler.factory

import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.experimental.errorhandler.factory.ErrorHandlerFactory
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.experimental.errorhandler.listener.SendReactionErrorHandlerImpl
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import kotlinx.coroutines.CoroutineScope

/**
 * Factory for [SendReactionErrorHandlerImpl].
 */
public class SendReactionErrorHandlerFactory : ErrorHandlerFactory {
    override fun create(): ErrorHandler {
        return SendReactionErrorHandlerImpl(
            scope = CoroutineScope(DispatcherProvider.IO),
            globalState = GlobalMutableState.getOrCreate(),
        )
    }
}
