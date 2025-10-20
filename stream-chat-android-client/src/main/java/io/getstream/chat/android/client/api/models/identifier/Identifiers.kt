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
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySorter

/**
 * Identifier for a [ChatClient.queryChannel] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun QueryChannelIdentifier(
    channelType: String,
    channelId: String,
    request: QueryChannelRequest,
): Int {
    var result = "QueryChannel".hashCode()
    result = 31 * result + channelType.hashCode()
    result = 31 * result + channelId.hashCode()
    result = 31 * result + request.hashCode()
    return result
}

/**
 * Identifier for a [ChatClient.queryChannels] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun QueryChannelsIdentifier(
    request: QueryChannelsRequest,
): Int {
    var result = "QueryChannels".hashCode()
    result = 31 * result + request.hashCode()
    return result
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
    members: List<Member> = emptyList(),
): Int {
    var result = "QueryMembers".hashCode()
    result = 31 * result + channelType.hashCode()
    result = 31 * result + channelId.hashCode()
    result = 31 * result + offset.hashCode()
    result = 31 * result + limit.hashCode()
    result = 31 * result + filter.hashCode()
    result = 31 * result + sort.hashCode()
    result = 31 * result + members.hashCode()
    return result
}

/**
 * Identifier for a [ChatClient.deleteReaction] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun DeleteReactionIdentifier(
    messageId: String,
    reactionType: String,
    cid: String?,
): Int {
    var result = "DeleteReaction".hashCode()
    result = 31 * result + messageId.hashCode()
    result = 31 * result + reactionType.hashCode()
    result = 31 * result + (cid?.hashCode() ?: 0)
    return result
}

/**
 * Identifier for a [ChatClient.sendReaction] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun SendReactionIdentifier(
    reaction: Reaction,
    enforceUnique: Boolean,
    cid: String?,
): Int {
    var result = "SendReaction".hashCode()
    result = 31 * result + reaction.hashCode()
    result = 31 * result + enforceUnique.hashCode()
    result = 31 * result + cid.hashCode()
    return result
}

/**
 * Identifier for a [ChatClient.getReplies] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun GetRepliesIdentifier(
    messageId: String,
    limit: Int,
): Int {
    var result = "GetReplies".hashCode()
    result = 31 * result + messageId.hashCode()
    result = 31 * result + limit.hashCode()
    return result
}

/**
 * Identifier for a [ChatClient.getNewerReplies] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun getNewerRepliesIdentifier(
    parentId: String,
    limit: Int,
    lastId: String? = null,
): Int {
    var result = "GetOlderReplies".hashCode()
    result = 31 * result + parentId.hashCode()
    result = 31 * result + limit.hashCode()
    result = 31 * result + (lastId?.hashCode() ?: 0)
    return result
}

/**
 * Identifier for a [ChatClient.getRepliesMore] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun GetRepliesMoreIdentifier(
    messageId: String,
    firstId: String,
    limit: Int,
): Int {
    var result = "GetRepliesMore".hashCode()
    result = 31 * result + messageId.hashCode()
    result = 31 * result + firstId.hashCode()
    result = 31 * result + limit.hashCode()
    return result
}

/**
 * Identifier for a [ChatClient.sendGiphy] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun SendGiphyIdentifier(
    request: SendActionRequest,
): Int {
    var result = "SendGiphy".hashCode()
    result = 31 * result + request.hashCode()
    return result
}

/**
 * Identifier for a [ChatClient.shuffleGiphy] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun ShuffleGiphyIdentifier(
    request: SendActionRequest,
): Int {
    var result = "ShuffleGiphy".hashCode()
    result = 31 * result + request.hashCode()
    return result
}

/**
 * Identifier for a [ChatClient.deleteMessage] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun DeleteMessageIdentifier(
    messageId: String,
    hard: Boolean,
): Int {
    var result = "DeleteMessage".hashCode()
    result = 31 * result + messageId.hashCode()
    result = 31 * result + hard.hashCode()
    return result
}

/**
 * Identifier for a [ChatClient.deleteMessageForMe] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun DeleteMessageForMeIdentifier(
    messageId: String,
): Int {
    var result = "DeleteMessageForMe".hashCode()
    result = 31 * result + messageId.hashCode()
    return result
}

/**
 * Identifier for [ChatClient.keystroke] and [ChatClient.stopTyping] calls.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun SendEventIdentifier(
    eventType: String,
    channelType: String,
    channelId: String,
    parentId: String?,
): Int {
    var result = "SendEvent".hashCode()
    result = 31 * result + eventType.hashCode()
    result = 31 * result + channelType.hashCode()
    result = 31 * result + channelId.hashCode()
    result = 31 * result + parentId.hashCode()
    return result
}

/**
 * Identifier for a [ChatClient.updateMessage] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun UpdateMessageIdentifier(
    message: Message,
): Int {
    var result = "UpdateMessage".hashCode()
    result = 31 * result + message.hashCode()
    return result
}

/**
 * Identifier for a [ChatClient.hideChannel] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun HideChannelIdentifier(
    channelType: String,
    channelId: String,
    clearHistory: Boolean,
): Int {
    var result = "HideChannel".hashCode()
    result = 31 * result + channelType.hashCode()
    result = 31 * result + channelId.hashCode()
    result = 31 * result + clearHistory.hashCode()
    return result
}

/**
 * Identifier for a [ChatClient.getMessage] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun GetMessageIdentifier(
    messageId: String,
): Int {
    var result = "GetMessage".hashCode()
    result = 31 * result + messageId.hashCode()

    return result
}

/**
 * Identifier for a [ChatClient.markAllRead] call.
 */
@Suppress("FunctionName", "FunctionOnlyReturningConstant")
internal fun MarkAllReadIdentifier(): Int {
    return "MarkAllRead".hashCode()
}

/**
 * Identifier for a [ChatClient.hideChannel] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun MarkReadIdentifier(
    channelType: String,
    channelId: String,
): Int {
    var result = "MarkRead".hashCode()
    result = 31 * result + channelType.hashCode()
    result = 31 * result + channelId.hashCode()
    return result
}

/**
 * Identifier for a [ChatClient.sendMessage] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun SendMessageIdentifier(
    channelType: String,
    channelId: String,
    messageId: String,
): Int {
    var result = "SendMessage".hashCode()
    result = 31 * result + channelType.hashCode()
    result = 31 * result + channelId.hashCode()
    result = 31 * result + messageId.hashCode()
    return result
}

/**
 * Identifier for a [ChatClient.getDevices] call.
 */
@Suppress("FunctionName", "FunctionOnlyReturningConstant")
internal fun GetDevicesIdentifier(): Int {
    return "GetDevices".hashCode()
}

/**
 * Identifier for a [ChatClient.addDevice] call.
 */
@Suppress("FunctionName", "FunctionOnlyReturningConstant")
internal fun AddDeviceIdentifier(
    device: Device,
): Int {
    var result = "AddDevice".hashCode()
    result = 31 * result + device.hashCode()
    return result
}

/**
 * Identifier for a [ChatClient.deleteDevice] call.
 */
@Suppress("FunctionName", "FunctionOnlyReturningConstant")
internal fun DeleteDeviceIdentifier(
    device: Device,
): Int {
    var result = "DeleteDevice".hashCode()
    result = 31 * result + device.hashCode()
    return result
}

/**
 * Identifier for a [ChatClient.connectUser] call.
 */
@Suppress("FunctionName", "MagicNumber")
internal fun ConnectUserIdentifier(
    user: User,
): Int {
    var result = "ConnectUser".hashCode()
    result = 31 * result + user.id.hashCode()
    return result
}
