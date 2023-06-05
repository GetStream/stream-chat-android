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

package io.getstream.chat.android.ui.common.extensions.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.getMemberExcludingCurrent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListPayloadDiff
import io.getstream.chat.android.ui.common.extensions.getCreatedAtOrThrow
import io.getstream.chat.android.ui.common.extensions.getLastMessage

internal fun Channel.diff(other: Channel): ChannelListPayloadDiff {
    val usersChanged = getMemberExcludingCurrent() != other.getMemberExcludingCurrent()
    return ChannelListPayloadDiff(
        nameChanged = name != other.name,
        avatarViewChanged = usersChanged,
        usersChanged = usersChanged,
        readStateChanged = read != other.read,
        lastMessageChanged = getLastMessage() != other.getLastMessage(),
        unreadCountChanged = unreadCount != other.unreadCount && other.unreadCount != null,
        extraDataChanged = extraData != other.extraData
    )
}

internal fun Channel.isMessageRead(message: Message): Boolean {
    val currentUser = ChatClient.instance().getCurrentUser()
    return read.filter { it.user.id != currentUser?.id }
        .mapNotNull { it.lastRead }
        .any { it.time >= message.getCreatedAtOrThrow().time }
}

internal const val EXTRA_DATA_MUTED: String = "mutedChannel"

internal var Channel.isMuted: Boolean
    get() = extraData[EXTRA_DATA_MUTED] as Boolean? ?: false
    set(value) {
        extraData[EXTRA_DATA_MUTED] = value
    }
