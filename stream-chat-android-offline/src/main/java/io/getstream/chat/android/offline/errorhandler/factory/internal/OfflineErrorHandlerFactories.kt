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

import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.experimental.errorhandler.factory.ErrorHandlerFactory
import io.getstream.chat.android.offline.errorhandler.internal.CreateChannelErrorHandlerImpl
import io.getstream.chat.android.offline.errorhandler.internal.DeleteReactionErrorHandlerImpl
import io.getstream.chat.android.offline.errorhandler.internal.QueryMembersErrorHandlerImpl
import io.getstream.chat.android.offline.errorhandler.internal.SendReactionErrorHandlerImpl
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade

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
