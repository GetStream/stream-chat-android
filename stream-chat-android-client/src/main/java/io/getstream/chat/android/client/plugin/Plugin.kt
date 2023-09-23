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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.errorhandler.ErrorHandler
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.plugin.listeners.ChannelMarkReadListener
import io.getstream.chat.android.client.plugin.listeners.CreateChannelListener
import io.getstream.chat.android.client.plugin.listeners.DeleteMessageListener
import io.getstream.chat.android.client.plugin.listeners.DeleteReactionListener
import io.getstream.chat.android.client.plugin.listeners.EditMessageListener
import io.getstream.chat.android.client.plugin.listeners.FetchCurrentUserListener
import io.getstream.chat.android.client.plugin.listeners.GetMessageListener
import io.getstream.chat.android.client.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.plugin.listeners.MarkAllReadListener
import io.getstream.chat.android.client.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.client.plugin.listeners.QueryChannelsListener
import io.getstream.chat.android.client.plugin.listeners.QueryMembersListener
import io.getstream.chat.android.client.plugin.listeners.SendAttachmentListener
import io.getstream.chat.android.client.plugin.listeners.SendGiphyListener
import io.getstream.chat.android.client.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.plugin.listeners.ShuffleGiphyListener
import io.getstream.chat.android.client.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.client.plugin.listeners.TypingEventListener
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.result.Result
import java.util.Date

/**
 * Plugin is an extension for [ChatClient].
 */
@Suppress("TooManyFunctions")
public interface Plugin :
    QueryMembersListener,
    DeleteReactionListener,
    SendReactionListener,
    ThreadQueryListener,
    SendGiphyListener,
    ShuffleGiphyListener,
    DeleteMessageListener,
    SendMessageListener,
    SendAttachmentListener,
    EditMessageListener,
    QueryChannelListener,
    QueryChannelsListener,
    TypingEventListener,
    HideChannelListener,
    MarkAllReadListener,
    ChannelMarkReadListener,
    CreateChannelListener,
    GetMessageListener,
    FetchCurrentUserListener {

    public val errorHandler: ErrorHandler?

    override suspend fun onQueryMembersResult(
        result: Result<List<Member>>,
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member>,
    ) {
        /* No-Op */
    }

    override suspend fun onDeleteReactionRequest(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
    ) {
        /* No-Op */
    }

    override suspend fun onDeleteReactionResult(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
        result: Result<Message>,
    ) {
        /* No-Op */
    }

    override fun onDeleteReactionPrecondition(currentUser: User?): Result<Unit> = Result.Success(Unit)

    override suspend fun onSendReactionRequest(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
    ) {
        /* No-Op */
    }

    override suspend fun onSendReactionResult(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
        result: Result<Reaction>,
    ) {
        /* No-Op */
    }

    override fun onSendReactionPrecondition(
        currentUser: User?,
        reaction: Reaction,
    ): Result<Unit> = Result.Success(Unit)

    override suspend fun onGetRepliesPrecondition(
        messageId: String,
        limit: Int,
    ): Result<Unit> = Result.Success(Unit)

    override suspend fun onGetRepliesRequest(
        messageId: String,
        limit: Int,
    ) {
        /* No-Op */
    }

    override suspend fun onGetRepliesResult(
        result: Result<List<Message>>,
        messageId: String,
        limit: Int,
    ) {
        /* No-Op */
    }

    override suspend fun onGetRepliesMorePrecondition(
        messageId: String,
        firstId: String,
        limit: Int,
    ): Result<Unit> = Result.Success(Unit)

    override suspend fun onGetRepliesMoreRequest(
        messageId: String,
        firstId: String,
        limit: Int,
    ) {
        /* No-Op */
    }

    override suspend fun onGetRepliesMoreResult(
        result: Result<List<Message>>,
        messageId: String,
        firstId: String,
        limit: Int,
    ) {
        /* No-Op */
    }

    override fun onGiphySendResult(cid: String, result: Result<Message>) {
        /* No-Op */
    }

    override suspend fun onShuffleGiphyResult(cid: String, result: Result<Message>) {
        /* No-Op */
    }

    override suspend fun onMessageDeletePrecondition(messageId: String): Result<Unit> = Result.Success(Unit)

    override suspend fun onMessageDeleteRequest(messageId: String) {
        /* No-Op */
    }

    override suspend fun onMessageDeleteResult(
        originalMessageId: String,
        result: Result<Message>,
    ) {
        /* No-Op */
    }

    override suspend fun onMessageSendResult(
        result: Result<Message>,
        channelType: String,
        channelId: String,
        message: Message,
    ) {
        /* No-Op */
    }

    override suspend fun onMessageEditRequest(message: Message) {
        /* No-Op */
    }

    override suspend fun onMessageEditResult(originalMessage: Message, result: Result<Message>) {
        /* No-Op */
    }

    override suspend fun onQueryChannelPrecondition(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Result<Unit> = Result.Success(Unit)

    override suspend fun onQueryChannelRequest(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {
        /* No-Op */
    }

    override suspend fun onQueryChannelResult(
        result: Result<Channel>,
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {
        /* No-Op */
    }

    override suspend fun onQueryChannelsPrecondition(request: QueryChannelsRequest): Result<Unit> =
        Result.Success(Unit)

    override suspend fun onQueryChannelsRequest(request: QueryChannelsRequest) {
        /* No-Op */
    }

    override suspend fun onQueryChannelsResult(
        result: Result<List<Channel>>,
        request: QueryChannelsRequest,
    ) {
        /* No-Op */
    }

    override fun onTypingEventPrecondition(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    ): Result<Unit> = Result.Success(Unit)

    override fun onTypingEventRequest(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    ) {
        /* No-Op */
    }

    override fun onTypingEventResult(
        result: Result<ChatEvent>,
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    ) {
        /* No-Op */
    }

    override suspend fun onHideChannelPrecondition(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Result<Unit> = Result.Success(Unit)

    override suspend fun onHideChannelRequest(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ) {
        /* No-Op */
    }

    override suspend fun onHideChannelResult(
        result: Result<Unit>,
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ) {
        /* No-Op */
    }

    override suspend fun onMarkAllReadRequest() {
        /* No-Op */
    }

    override suspend fun onChannelMarkReadPrecondition(
        channelType: String,
        channelId: String,
    ): Result<Unit> = Result.Success(Unit)

    override suspend fun onCreateChannelRequest(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        extraData: Map<String, Any>,
        currentUser: User,
    ) {
        /* No-Op */
    }

    override suspend fun onCreateChannelResult(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        result: Result<Channel>,
    ) {
        /* No-Op */
    }

    override fun onCreateChannelPrecondition(
        currentUser: User?,
        channelId: String,
        memberIds: List<String>,
    ): Result<Unit> = Result.Success(Unit)

    override suspend fun onAttachmentSendRequest(channelType: String, channelId: String, message: Message) {
        /* No-Op */
    }

    public fun onUserSet(user: User)

    public fun onUserDisconnected()

    public override suspend fun onGetMessageResult(
        messageId: String,
        result: Result<Message>,
    ) {
        /* No-Op */
    }

    public override suspend fun onFetchCurrentUserResult(
        result: Result<User>,
    ) {
        /* No-Op */
    }
}
