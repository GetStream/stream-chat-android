package io.getstream.chat.android.offline.experimental.errorhandler.factory

import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.experimental.errorhandler.factory.ErrorHandlerFactory
import io.getstream.chat.android.offline.experimental.errorhandler.listener.CreateChannelErrorHandlerImpl
import io.getstream.chat.android.offline.experimental.errorhandler.listener.DeleteReactionErrorHandlerImpl
import io.getstream.chat.android.offline.experimental.errorhandler.listener.QueryMembersErrorHandlerImpl
import io.getstream.chat.android.offline.experimental.errorhandler.listener.SendReactionErrorHandlerImpl
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import io.getstream.chat.android.offline.repository.RepositoryFacade

/**
 * Provides all offline support related error handler factories.
 */
public object OfflineErrorHandlerFactoriesProvider {

    /**
     * Creates a list of available offline support related error handler factories.
     *
     * @return A List of [ErrorHandlerFactory].
     */
    public fun createErrorHandlerFactories(): List<ErrorHandlerFactory> = listOf(
        DeleteReactionErrorHandlerFactory(),
        SendReactionErrorHandlerFactory(),
        QueryMembersErrorHandlerFactory(),
        CreateChannelErrorHandlerFactory(),
    )
}

/**
 * Factory for [DeleteReactionErrorHandlerImpl].
 */
private class DeleteReactionErrorHandlerFactory : ErrorHandlerFactory {

    override fun create(): ErrorHandler {
        return DeleteReactionErrorHandlerImpl(
            scope = StateRegistry.get().scope,
            logic = LogicRegistry.get(),
            globalState = GlobalMutableState.getOrCreate(),
        )
    }
}

/**
 * Factory for [SendReactionErrorHandlerImpl].
 */
private class SendReactionErrorHandlerFactory : ErrorHandlerFactory {
    override fun create(): ErrorHandler {
        return SendReactionErrorHandlerImpl(
            scope = StateRegistry.get().scope,
            globalState = GlobalMutableState.getOrCreate(),
        )
    }
}

/**
 * Factory for [QueryMembersErrorHandlerImpl].
 */
private class QueryMembersErrorHandlerFactory : ErrorHandlerFactory {

    override fun create(): ErrorHandler {
        return QueryMembersErrorHandlerImpl(
            scope = StateRegistry.get().scope,
            globalState = GlobalMutableState.getOrCreate(),
            channelRepository = RepositoryFacade.get()
        )
    }
}

/**
 * Factory for [CreateChannelErrorHandlerImpl].
 */
private class CreateChannelErrorHandlerFactory : ErrorHandlerFactory {

    override fun create(): ErrorHandler {
        val repos = RepositoryFacade.get()
        return CreateChannelErrorHandlerImpl(
            scope = StateRegistry.get().scope,
            globalState = GlobalMutableState.getOrCreate(),
            channelRepository = repos
        )
    }
}
