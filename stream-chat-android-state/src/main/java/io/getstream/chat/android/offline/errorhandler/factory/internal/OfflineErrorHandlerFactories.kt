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
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.factory.RepositoryProvider
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.errorhandler.internal.CreateChannelErrorHandlerImpl
import io.getstream.chat.android.offline.errorhandler.internal.DeleteReactionErrorHandlerImpl
import io.getstream.chat.android.offline.errorhandler.internal.QueryMembersErrorHandlerImpl
import io.getstream.chat.android.offline.errorhandler.internal.SendReactionErrorHandlerImpl
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import kotlinx.coroutines.CoroutineScope

/**
 * Provides all offline support related error handler factories.
 */
@InternalStreamChatApi
public object OfflineErrorHandlerFactoriesProvider {

    /**
     * Creates a list of available offline support related error handler factories.
     *
     * @return A List of [ErrorHandlerFactory].
     */
    @InternalStreamChatApi
    public fun createErrorHandlerFactories(scope: CoroutineScope): List<ErrorHandlerFactory> = listOf(
        DeleteReactionErrorHandlerFactory(scope),
        SendReactionErrorHandlerFactory(scope),
        QueryMembersErrorHandlerFactory(scope),
        CreateChannelErrorHandlerFactory(scope),
    )
}

/**
 * Factory for [DeleteReactionErrorHandlerImpl].
 */
private class DeleteReactionErrorHandlerFactory(
    private val scope: CoroutineScope
) : ErrorHandlerFactory {

    override fun create(): ErrorHandler {
        return DeleteReactionErrorHandlerImpl(
            scope = scope,
            logic = LogicRegistry.get(),
            globalState = GlobalMutableState.getOrCreate(),
        )
    }
}

/**
 * Factory for [SendReactionErrorHandlerImpl].
 */
private class SendReactionErrorHandlerFactory(
    private val scope: CoroutineScope
) : ErrorHandlerFactory {
    override fun create(): ErrorHandler {
        return SendReactionErrorHandlerImpl(
            scope = scope,
            globalState = GlobalMutableState.getOrCreate(),
        )
    }
}

/**
 * Factory for [QueryMembersErrorHandlerImpl].
 */
private class QueryMembersErrorHandlerFactory(
    private val scope: CoroutineScope
) : ErrorHandlerFactory {

    override fun create(): ErrorHandler {
        val repositoryProvider = RepositoryProvider.get()

        return QueryMembersErrorHandlerImpl(
            scope = scope,
            globalState = GlobalMutableState.getOrCreate(),
            channelRepository = repositoryProvider.get(ChannelRepository::class.java)
        )
    }
}

/**
 * Factory for [CreateChannelErrorHandlerImpl].
 */
private class CreateChannelErrorHandlerFactory(
    private val scope: CoroutineScope
) : ErrorHandlerFactory {

    override fun create(): ErrorHandler {
        val repositoryProvider = RepositoryProvider.get()

        return CreateChannelErrorHandlerImpl(
            scope = scope,
            globalState = GlobalMutableState.getOrCreate(),
            channelRepository = repositoryProvider.get(ChannelRepository::class.java)
        )
    }
}
