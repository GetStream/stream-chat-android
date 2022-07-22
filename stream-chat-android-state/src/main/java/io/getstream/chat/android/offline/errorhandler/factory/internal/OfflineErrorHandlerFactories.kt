/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.offline.errorhandler.factory.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errorhandler.ErrorHandler
import io.getstream.chat.android.client.errorhandler.factory.ErrorHandlerFactory
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.offline.errorhandler.internal.CreateChannelErrorHandlerImpl
import io.getstream.chat.android.offline.errorhandler.internal.DeleteReactionErrorHandlerImpl
import io.getstream.chat.android.offline.errorhandler.internal.QueryMembersErrorHandlerImpl
import io.getstream.chat.android.offline.errorhandler.internal.SendReactionErrorHandlerImpl
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry

/**
 * Provides all offline support related error handler factories.
 */
internal object OfflineErrorHandlerFactoriesProvider {

    /**
     * Creates a list of available offline support related error handler factories.
     *
     * @return A List of [ErrorHandlerFactory].
     */
    fun createErrorHandlerFactories(channelRepository: ChannelRepository): List<ErrorHandlerFactory> = listOf(
        DeleteReactionErrorHandlerFactory(),
        SendReactionErrorHandlerFactory(),
        QueryMembersErrorHandlerFactory(channelRepository),
        CreateChannelErrorHandlerFactory(channelRepository),
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
            clientState = ChatClient.instance().clientState,
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
            clientState = ChatClient.instance().clientState,
        )
    }
}

/**
 * Factory for [QueryMembersErrorHandlerImpl].
 */
private class QueryMembersErrorHandlerFactory(
    private val channelRepository: ChannelRepository,
) : ErrorHandlerFactory {

    override fun create(): ErrorHandler = QueryMembersErrorHandlerImpl(
        scope = StateRegistry.get().scope,
        clientState = ChatClient.instance().clientState,
        channelRepository = channelRepository
    )
}

/**
 * Factory for [CreateChannelErrorHandlerImpl].
 */
private class CreateChannelErrorHandlerFactory(
    private val channelRepository: ChannelRepository,
) : ErrorHandlerFactory {

    override fun create(): ErrorHandler = CreateChannelErrorHandlerImpl(
        scope = StateRegistry.get().scope,
        clientState = ChatClient.instance().clientState,
        channelRepository = channelRepository,
    )
}
