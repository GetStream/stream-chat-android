package io.getstream.chat.android.offline.internal.errorhandler.factory

import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.experimental.errorhandler.factory.ErrorHandlerFactory
import io.getstream.chat.android.offline.internal.errorhandler.CreateChannelErrorHandlerImpl
import io.getstream.chat.android.offline.internal.errorhandler.DeleteReactionErrorHandlerImpl
import io.getstream.chat.android.offline.internal.errorhandler.QueryMembersErrorHandlerImpl
import io.getstream.chat.android.offline.internal.errorhandler.SendReactionErrorHandlerImpl
import io.getstream.chat.android.offline.internal.repository.RepositoryFacade
import io.getstream.chat.android.offline.plugin.internal.logic.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState

/**
 * Provides all offline support related error handler factories.
 */
internal object OfflineErrorHandlerFactoriesProvider {

    /**
     * Creates a list of available offline support related error handler factories.
     *
     * @return A List of [ErrorHandlerFactory].
     */
    fun createErrorHandlerFactories(): List<ErrorHandlerFactory> = listOf(
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
