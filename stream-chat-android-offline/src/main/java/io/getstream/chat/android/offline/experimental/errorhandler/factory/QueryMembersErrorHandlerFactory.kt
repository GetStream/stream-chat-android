package io.getstream.chat.android.offline.experimental.errorhandler.factory

import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.experimental.errorhandler.factory.ErrorHandlerFactory
import io.getstream.chat.android.offline.experimental.errorhandler.listener.QueryMembersErrorHandlerImpl
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import io.getstream.chat.android.offline.repository.RepositoryFacade

/**
 * Factory for [QueryMembersErrorHandlerImpl]
 */
public class QueryMembersErrorHandlerFactory : ErrorHandlerFactory {

    override fun create(): ErrorHandler {
        return QueryMembersErrorHandlerImpl(
            scope = StateRegistry.get().scope,
            globalState = GlobalMutableState.getOrCreate(),
            channelRepository = RepositoryFacade.get()
        )
    }
}
