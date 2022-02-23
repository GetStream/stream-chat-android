package io.getstream.chat.android.offline.experimental.errorhandler.factory

import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.experimental.errorhandler.factory.ErrorHandlerFactory
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.experimental.errorhandler.listener.QueryMembersErrorHandlerImpl
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import kotlinx.coroutines.CoroutineScope

@ExperimentalStreamChatApi
/**
 * Factory for [QueryMembersErrorHandlerImpl]
 */
public class QueryMembersErrorHandlerFactory : ErrorHandlerFactory {

    override fun create(): ErrorHandler {
        return QueryMembersErrorHandlerImpl(
            scope = CoroutineScope(DispatcherProvider.IO),
            globalState = GlobalMutableState.getOrCreate(),
            repos = (ChatDomain.instance as ChatDomainImpl).repos
        )
    }
}
