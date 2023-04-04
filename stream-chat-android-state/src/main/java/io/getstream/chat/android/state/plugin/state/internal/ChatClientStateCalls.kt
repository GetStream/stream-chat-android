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

package io.getstream.chat.android.state.plugin.state.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory
import io.getstream.chat.android.state.extensions.state
import io.getstream.chat.android.state.model.querychannels.pagination.internal.QueryChannelPaginationRequest
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.channel.thread.ThreadState
import io.getstream.chat.android.state.plugin.state.global.GlobalState
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import io.getstream.log.taggedLogger
import io.getstream.result.call.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first

/**
 * Adapter for [ChatClient] that wraps some of it's request.
 */
internal class ChatClientStateCalls(
    private val chatClient: ChatClient,
    private val globalState: GlobalState,
    private val scope: CoroutineScope,
) {
    private val logger by taggedLogger("ChatClientState")

    /**
     * Deferred value of StateRegistry.
     * It needs to be accessed after the user is connected to be sure needed plugins are initialized.
     */
    private val deferredState: Deferred<StateRegistry> = scope.async(start = CoroutineStart.LAZY) {
        globalState.user.first { it != null }
        chatClient.state
    }

    /** Reference request of the channels query. */
    internal suspend fun queryChannels(
        request: QueryChannelsRequest,
        chatEventHandlerFactory: ChatEventHandlerFactory,
    ): QueryChannelsState {
        logger.d { "querying state for channels" }
        chatClient.queryChannels(request).launch(scope)
        return deferredState
            .await()
            .queryChannels(request.filter, request.querySort)
            .also { queryChannelsState -> queryChannelsState.chatEventHandlerFactory = chatEventHandlerFactory }
    }

    /** Reference request of the channel query. */
    private suspend fun queryChannel(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): ChannelState {
        logger.d { "querying state for channel with id: $channelId" }
        chatClient.queryChannel(channelType, channelId, request).launch(scope)
        return deferredState
            .await()
            .channel(channelType, channelId)
    }

    /** Reference request of the watch channel query. */
    internal suspend fun watchChannel(cid: String, messageLimit: Int, userPresence: Boolean): ChannelState {
        logger.d { "watching channel with cid: $cid" }
        val (channelType, channelId) = cid.cidToTypeAndId()
        val request = QueryChannelPaginationRequest(messageLimit)
            .toWatchChannelRequest(userPresence)
            .apply {
                this.shouldRefresh = false
                this.isWatchChannel = true
            }
        return queryChannel(channelType, channelId, request)
    }

    /** Reference request of the get thread replies query. */
    internal suspend fun getReplies(messageId: String, messageLimit: Int): ThreadState {
        logger.d { "getting replied for message with id: $messageId" }
        chatClient.getReplies(messageId, messageLimit).launch(scope)
        return deferredState
            .await()
            .thread(messageId)
    }

    /**
     * Fetches replies from the backend and returns them in the form of a [ThreadState].
     * Unlike [getReplies] which makes an API call and instantly returns [ThreadState], this function
     * will wait for the API call completion and then return [ThreadState].
     *
     * This is useful in situations such as when we want to focus on the last message in a thread after a PN
     * has been received, avoiding multiple [ThreadState] emissions simplifies handling it in the UI.
     *
     * @param messageId The id of the message we want to get replies for.
     * @param messageLimit The upper limit of how many replies should be fetched.
     *
     * @return The replies in the form of [ThreadState].
     */
    internal suspend fun awaitReplies(messageId: String, messageLimit: Int): ThreadState {
        logger.d { "getting replied for message with id: $messageId" }
        chatClient.getReplies(messageId, messageLimit).await()
        return deferredState
            .await()
            .thread(messageId)
    }
}
