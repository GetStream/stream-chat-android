/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.internal.state.plugin.state.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.event.ChatEventHandlerFactory
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.client.api.state.QueryChannelsState
import io.getstream.chat.android.client.api.state.QueryThreadsState
import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.client.api.state.ThreadState
import io.getstream.chat.android.client.api.state.state
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.internal.state.model.querychannels.pagination.internal.QueryChannelPaginationRequest
import io.getstream.chat.android.models.Message
import io.getstream.log.taggedLogger
import io.getstream.result.call.Call
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
    private val scope: CoroutineScope,
) {
    private val logger by taggedLogger("Chat:ClientStateCalls")

    /**
     * Deferred value of StateRegistry.
     * It needs to be accessed after the user is connected to be sure needed plugins are initialized.
     */
    private val deferredState: Deferred<StateRegistry> = scope.async(start = CoroutineStart.LAZY) {
        chatClient.clientState.user.first { it != null }
        chatClient.state
    }

    /** Reference request of the channels query. */
    internal suspend fun queryChannels(
        request: QueryChannelsRequest,
        chatEventHandlerFactory: ChatEventHandlerFactory,
    ): QueryChannelsState {
        logger.d { "[queryChannels] request: $request" }
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
        logger.v { "[queryChannel] cid: $channelType:$channelId, request: $request" }
        chatClient.queryChannel(channelType, channelId, request).launch(scope)
        return deferredState
            .await()
            .channel(channelType, channelId)
    }

    /** Reference request of the watch channel query. */
    internal suspend fun watchChannel(cid: String, messageLimit: Int, userPresence: Boolean): ChannelState {
        logger.d { "[watchChannel] cid: $cid, messageLimit: $messageLimit, userPresence: $userPresence" }
        val (channelType, channelId) = cid.cidToTypeAndId()
        val request = QueryChannelPaginationRequest(messageLimit)
            .toWatchChannelRequest(userPresence)
            .apply {
                this.shouldRefresh = false
                this.isWatchChannel = true
            }
        return queryChannel(channelType, channelId, request)
    }

    /**
     * Runs the [ChatClient.queryThreads] operation with the provided [QueryThreadsRequest], and returns the
     * [QueryThreadsState].
     *
     * @param request The [QueryThreadsRequest] used to perform the query threads operation.
     */
    internal suspend fun queryThreads(request: QueryThreadsRequest): QueryThreadsState {
        chatClient.queryThreads(request).launch(scope)
        return deferredState
            .await()
            .queryThreads(request.filter, request.sort)
    }

    /** Reference request of the get thread replies query. */
    internal suspend fun getReplies(
        messageId: String,
        messageLimit: Int,
        olderToNewer: Boolean,
    ): ThreadState {
        logger.d { "[getReplies] messageId: $messageId, messageLimit: $messageLimit" }
        repliesCall(olderToNewer, messageId, messageLimit).launch(scope)
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
     * @param olderToNewer If true, replies will be fetched from older to newer, otherwise from newer to older.
     *
     * @return The replies in the form of [ThreadState].
     */
    internal suspend fun awaitReplies(
        messageId: String,
        messageLimit: Int,
        olderToNewer: Boolean,
    ): ThreadState {
        logger.d { "[awaitReplies] messageId: $messageId, messageLimit: $messageLimit" }
        repliesCall(olderToNewer, messageId, messageLimit).await()
        return deferredState
            .await()
            .thread(messageId)
    }

    private fun repliesCall(
        olderToNewer: Boolean,
        messageId: String,
        messageLimit: Int,
    ): Call<List<Message>> = when (olderToNewer) {
        true -> chatClient.getNewerReplies(messageId, messageLimit)
        false -> chatClient.getReplies(messageId, messageLimit)
    }
}
