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

package io.getstream.chat.android.client.plugin

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.plugin.listeners.ChannelMarkReadListener
import io.getstream.chat.android.client.plugin.listeners.DeleteMessageListener
import io.getstream.chat.android.client.plugin.listeners.DeleteReactionListener
import io.getstream.chat.android.client.plugin.listeners.EditMessageListener
import io.getstream.chat.android.client.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.plugin.listeners.MarkAllReadListener
import io.getstream.chat.android.client.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.client.plugin.listeners.QueryChannelsListener
import io.getstream.chat.android.client.plugin.listeners.SendGiphyListener
import io.getstream.chat.android.client.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.plugin.listeners.ShuffleGiphyListener
import io.getstream.chat.android.client.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.client.plugin.listeners.ThreadQueryListenerFull
import io.getstream.chat.android.client.plugin.listeners.TypingEventListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.util.Date
import kotlin.reflect.KClass

@Suppress("TooManyFunctions")
internal class CombinedPlugin(
    override val name: String,
    private val threadQueryListenerFull: ThreadQueryListenerFull,
) : Plugin,
    DependencyResolver,
    QueryChannelsListener,
    QueryChannelListener,
    ThreadQueryListener by threadQueryListenerFull,
    ChannelMarkReadListener,
    EditMessageListener,
    HideChannelListener,
    MarkAllReadListener,
    DeleteReactionListener,
    SendReactionListener,
    DeleteMessageListener,
    SendGiphyListener,
    ShuffleGiphyListener,
    SendMessageListener,
    TypingEventListener {

    @InternalStreamChatApi
    override fun <T : Any> resolveDependency(klass: KClass<T>): T? = null

    override suspend fun onChannelMarkReadPrecondition(channelType: String, channelId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun onMessageDeletePrecondition(messageId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun onMessageDeleteRequest(messageId: String) {
        // Nothing to do.
    }

    override suspend fun onMessageDeleteResult(originalMessageId: String, result: Result<Message>) {
        // Nothing to do.
    }

    override suspend fun onDeleteReactionRequest(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
    ) {
        // Nothing to do.
    }

    override suspend fun onDeleteReactionResult(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
        result: Result<Message>,
    ) {
        // Nothing to do.
    }

    override fun onDeleteReactionPrecondition(currentUser: User?): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun onMessageEditRequest(message: Message) {
        // Nothing to do.
    }

    override suspend fun onMessageEditResult(originalMessage: Message, result: Result<Message>) {
        // Nothing to do.
    }

    override suspend fun onHideChannelPrecondition(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun onHideChannelRequest(channelType: String, channelId: String, clearHistory: Boolean) {
        // Nothing to do.
    }

    override suspend fun onHideChannelResult(
        result: Result<Unit>,
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ) {
        // Nothing to do.
    }

    override suspend fun onMarkAllReadRequest() {
        // Nothing to do.
    }

    override suspend fun onQueryChannelPrecondition(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun onQueryChannelRequest(channelType: String, channelId: String, request: QueryChannelRequest) {
        // Nothing to do.
    }

    override suspend fun onQueryChannelResult(
        result: Result<Channel>,
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {
        // Nothing to do.
    }

    override suspend fun onQueryChannelsPrecondition(request: QueryChannelsRequest): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun onQueryChannelsRequest(request: QueryChannelsRequest) {
        // Nothing to do.
    }

    override suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {
        // Nothing to do.
    }

    override fun onGiphySendResult(cid: String, result: Result<Message>) {
        // Nothing to do.
    }

    override suspend fun onMessageSendResult(
        result: Result<Message>,
        channelType: String,
        channelId: String,
        message: Message,
    ) {
        // Nothing to do.
    }

    override suspend fun onSendReactionRequest(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
    ) {
        // Nothing to do.
    }

    override suspend fun onSendReactionResult(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
        result: Result<Reaction>,
    ) {
        // Nothing to do.
    }

    override fun onSendReactionPrecondition(currentUser: User?, reaction: Reaction): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun onShuffleGiphyResult(cid: String, result: Result<Message>) {
        // Nothing to do.
    }

    override fun onTypingEventPrecondition(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override fun onTypingEventRequest(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    ) {
        // Nothing to do.
    }

    override fun onTypingEventResult(
        result: Result<ChatEvent>,
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    ) {
        // Nothing to do.
    }
}
