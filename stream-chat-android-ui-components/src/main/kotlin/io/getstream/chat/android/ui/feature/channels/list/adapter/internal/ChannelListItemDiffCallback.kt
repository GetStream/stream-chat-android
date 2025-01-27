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

package io.getstream.chat.android.ui.feature.channels.list.adapter.internal

import androidx.recyclerview.widget.DiffUtil
import io.getstream.chat.android.client.extensions.currentUserUnreadCount
import io.getstream.chat.android.client.extensions.getMembersExcludingCurrent
import io.getstream.chat.android.ui.common.extensions.internal.cast
import io.getstream.chat.android.ui.common.extensions.internal.safeCast
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListPayloadDiff
import io.getstream.chat.android.ui.utils.extensions.getLastMessage

internal object ChannelListItemDiffCallback : DiffUtil.ItemCallback<ChannelListItem>() {
    override fun areItemsTheSame(oldItem: ChannelListItem, newItem: ChannelListItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }

        return when (oldItem) {
            is ChannelListItem.ChannelItem -> {
                oldItem.channel.cid == newItem.safeCast<ChannelListItem.ChannelItem>()?.channel?.cid
            }

            else -> true
        }
    }

    override fun areContentsTheSame(oldItem: ChannelListItem, newItem: ChannelListItem): Boolean {
        // this is only called if areItemsTheSame returns true, so they must be the same class
        return when (oldItem) {
            is ChannelListItem.ChannelItem -> {
                oldItem
                    .diff(newItem.cast())
                    .hasDifference()
                    .not()
            }

            else -> true
        }
    }

    override fun getChangePayload(oldItem: ChannelListItem, newItem: ChannelListItem): Any {
        // only called if their contents aren't the same, so they must be channel items and not loading items
        return oldItem
            .cast<ChannelListItem.ChannelItem>()
            .diff(newItem.cast())
    }
    private fun ChannelListItem.ChannelItem.diff(other: ChannelListItem.ChannelItem): ChannelListPayloadDiff {
        val usersChanged = channel.getMembersExcludingCurrent() != other.channel.getMembersExcludingCurrent()
        return ChannelListPayloadDiff(
            nameChanged = channel.name != other.channel.name,
            avatarViewChanged = usersChanged,
            usersChanged = usersChanged,
            readStateChanged = channel.read != other.channel.read,
            lastMessageChanged = channel.getLastMessage() != other.channel.getLastMessage(),
            unreadCountChanged = channel.currentUserUnreadCount() != other.channel.currentUserUnreadCount(),
            extraDataChanged = channel.extraData != other.channel.extraData,
            typingUsersChanged = typingUsers != other.typingUsers,
        )
    }
}
