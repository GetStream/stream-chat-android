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

package io.getstream.chat.android.client.api.models.identifier

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.api.models.querysort.QuerySorter
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction

/**
 * Identifier for a [ChatClient.queryChannel] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun QueryChannelIdentifier(
    channelType: String,
    channelId: String,
    request: QueryChannelRequest,
): String {
    var result = channelType.hashCode()
    result = 31 * result + channelId.hashCode()
    result = 31 * result + request.hashCode()
    return "QueryChannel($result)"
}

/**
 * Identifier for a [ChatClient.queryChannels] call.
 */
@Suppress("FunctionName")
internal fun QueryChannelsIdentifier(
    request: QueryChannelsRequest,
): String {
    return "QueryChannels(${request.hashCode()})"
}

/**
 * Identifier for a [ChatClient.queryMembers] call.
 */
@Suppress("FunctionName", "MagicNumber", "LongParameterList")
internal fun QueryMembersIdentifier(
    channelType: String,
    channelId: String,
    offset: Int,
    limit: Int,
    filter: FilterObject,
    sort: QuerySorter<Member>,
    members: List<Member> = emptyList()
): String {
    var result = channelType.hashCode()
    result = 31 * result + channelId.hashCode()
    result = 31 * result + offset.hashCode()
    result = 31 * result + limit.hashCode()
    result = 31 * result + filter.hashCode()
    result = 31 * result + sort.hashCode()
    result = 31 * result + members.hashCode()
    return "QueryMembers($result)"
}

/**
 * Identifier for a [ChatClient.deleteReaction] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun DeleteReactionIdentifier(
    messageId: String,
    reactionType: String,
    cid: String?
): String {
    var result = messageId.hashCode()
    result = 31 * result + reactionType.hashCode()
    result = 31 * result + (cid?.hashCode() ?: 0)
    return "DeleteReaction($result)"
}

/**
 * Identifier for a [ChatClient.sendReaction] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun SendReactionIdentifier(
    reaction: Reaction,
    enforceUnique: Boolean,
    cid: String?
): String {
    var result = reaction.hashCode()
    result = 31 * result + enforceUnique.hashCode()
    result = 31 * result + cid.hashCode()
    return "SendReaction($result)"
}

/**
 * Identifier for a [ChatClient.getReplies] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun GetRepliesIdentifier(
    messageId: String,
    limit: Int
): String {
    var result = messageId.hashCode()
    result = 31 * result + limit.hashCode()
    return "GetReplies($result)"
}

/**
 * Identifier for a [ChatClient.getRepliesMore] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun GetRepliesMoreIdentifier(
    messageId: String,
    firstId: String,
    limit: Int
): String {
    var result = messageId.hashCode()
    result = 31 * result + firstId.hashCode()
    result = 31 * result + limit.hashCode()
    return "GetRepliesMore($result)"
}

/**
 * Identifier for a [ChatClient.sendGiphy] call.
 */
@Suppress("FunctionName")
internal fun SendGiphyIdentifier(
    request: SendActionRequest
): String {
    return "SendGiphy(${request.hashCode()})"
}

/**
 * Identifier for a [ChatClient.shuffleGiphy] call.
 */
@Suppress("FunctionName")
internal fun ShuffleGiphyIdentifier(
    request: SendActionRequest
): String {
    return "ShuffleGiphy(${request.hashCode()})"
}

/**
 * Identifier for a [ChatClient.deleteMessage] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun DeleteMessageIdentifier(
    messageId: String,
    hard: Boolean
): String {
    var result = messageId.hashCode()
    result = 31 * result + hard.hashCode()
    return "DeleteMessage($result)"
}

/**
 * Identifier for [ChatClient.keystroke] and [ChatClient.stopTyping] calls.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun SendEventIdentifier(
    eventType: String,
    channelType: String,
    channelId: String,
    parentId: String?
): String {
    var result = eventType.hashCode()
    result = 31 * result + channelType.hashCode()
    result = 31 * result + channelId.hashCode()
    result = 31 * result + parentId.hashCode()
    return "SendEvent($result)"
}

/**
 * Identifier for a [ChatClient.updateMessage] call.
 */
@Suppress("FunctionName")
internal fun UpdateMessageIdentifier(
    message: Message
): String {
    return "UpdateMessage(${message.hashCode()})"
}

/**
 * Identifier for a [ChatClient.hideChannel] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun HideChannelIdentifier(
    channelType: String,
    channelId: String,
    clearHistory: Boolean
): String {
    var result = channelType.hashCode()
    result = 31 * result + channelId.hashCode()
    result = 31 * result + clearHistory.hashCode()
    return "HideChannel($result)"
}

/**
 * Identifier for a [ChatClient.markAllRead] call.
 */
@Suppress("FunctionName", "FunctionOnlyReturningConstant")
internal fun MarkAllReadIdentifier(): String {
    return "MarkAllRead"
}
