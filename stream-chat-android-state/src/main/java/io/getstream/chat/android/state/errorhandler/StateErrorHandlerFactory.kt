package io.getstream.chat.android.state.errorhandler

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.offline.errorhandler.internal.CreateChannelErrorHandlerImpl
import io.getstream.chat.android.offline.errorhandler.internal.DeleteReactionErrorHandlerImpl
import io.getstream.chat.android.offline.errorhandler.internal.QueryMembersErrorHandlerImpl
import io.getstream.chat.android.offline.errorhandler.internal.SendReactionErrorHandlerImpl
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry

internal class StateErrorHandlerFactory {

    fun create(channelRepository: ChannelRepository): StateErrorHandler {
        val scope = StateRegistry.get().scope
        val logicRegistry = LogicRegistry.get()
        val clientState = ChatClient.instance().clientState

        val deleteReactionErrorHandler = DeleteReactionErrorHandlerImpl(
            scope = scope,
            logic = logicRegistry,
            clientState = clientState,
        )

        val createChannelErrorHandler = CreateChannelErrorHandlerImpl(
            scope = scope,
            clientState = clientState,
            channelRepository = channelRepository,
        )

        val queryMembersErrorHandler = QueryMembersErrorHandlerImpl(
            scope = scope,
            clientState = clientState,
            channelRepository = channelRepository
        )

        val sendReactionErrorHandler = SendReactionErrorHandlerImpl(scope = scope, clientState = clientState)

        return StateErrorHandler(
            deleteReactionErrorHandler = deleteReactionErrorHandler,
            createChannelErrorHandler = createChannelErrorHandler,
            queryMembersErrorHandler = queryMembersErrorHandler,
            sendReactionErrorHandler = sendReactionErrorHandler,
        )
    }
}
