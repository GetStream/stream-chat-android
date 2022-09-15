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
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.querysort.QuerySorter
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.plugin.listeners.DeleteMessageListener
import io.getstream.chat.android.client.plugin.listeners.DeleteReactionListener
import io.getstream.chat.android.client.plugin.listeners.QueryMembersListener
import io.getstream.chat.android.client.plugin.listeners.SendGiphyListener
import io.getstream.chat.android.client.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.plugin.listeners.ShuffleGiphyListener
import io.getstream.chat.android.client.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.client.utils.Result

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
    SendMessageListener {

    override suspend fun onQueryMembersResult(
        result: Result<List<Member>>,
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member>,
    ) { /* No-Op */ }

    override suspend fun onDeleteReactionRequest(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
    ) { /* No-Op */ }

    override suspend fun onDeleteReactionResult(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
        result: Result<Message>,
    ) { /* No-Op */ }

    override fun onDeleteReactionPrecondition(currentUser: User?): Result<Unit> = Result.success(Unit)

    override suspend fun onSendReactionRequest(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
    ) { /* No-Op */ }

    override suspend fun onSendReactionResult(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
        result: Result<Reaction>,
    ) { /* No-Op */ }

    override fun onSendReactionPrecondition(
        currentUser: User?,
        reaction: Reaction,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun onGetRepliesPrecondition(
        messageId: String,
        limit: Int,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun onGetRepliesRequest(
        messageId: String,
        limit: Int,
    ) { /* No-Op */ }

    override suspend fun onGetRepliesResult(
        result: Result<List<Message>>,
        messageId: String,
        limit: Int,
    ) { /* No-Op */ }

    override suspend fun onGetRepliesMorePrecondition(
        messageId: String,
        firstId: String,
        limit: Int,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun onGetRepliesMoreRequest(
        messageId: String,
        firstId: String,
        limit: Int,
    ) { /* No-Op */ }

    override suspend fun onGetRepliesMoreResult(
        result: Result<List<Message>>,
        messageId: String,
        firstId: String,
        limit: Int,
    ) { /* No-Op */ }

    override fun onGiphySendResult(cid: String, result: Result<Message>) { /* No-Op */ }

    override suspend fun onShuffleGiphyResult(cid: String, result: Result<Message>) { /* No-Op */ }

    override suspend fun onMessageDeletePrecondition(messageId: String): Result<Unit> = Result.success(Unit)

    override suspend fun onMessageDeleteRequest(messageId: String) { /* No-Op */ }

    override suspend fun onMessageDeleteResult(
        originalMessageId: String,
        result: Result<Message>,
    ) { /* No-Op */ }

    override suspend fun onMessageSendResult(
        result: Result<Message>,
        channelType: String,
        channelId: String,
        message: Message,
    ) { /* No-Op */ }
}
