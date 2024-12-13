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

package io.getstream.chat.android.ui.feature.messages.list.adapter.internal

import androidx.recyclerview.widget.DiffUtil
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemPayloadDiff

internal object MessageListItemDiffCallback : DiffUtil.ItemCallback<MessageListItem>() {
    override fun areItemsTheSame(oldItem: MessageListItem, newItem: MessageListItem): Boolean {
        return oldItem.getStableId() == newItem.getStableId()
    }

    override fun areContentsTheSame(oldItem: MessageListItem, newItem: MessageListItem): Boolean {
        return when (oldItem) {
            is MessageListItem.MessageItem ->
                getMessageListItemChangePayload(oldItem, newItem)?.anyChanged()?.not() ?: false
            is MessageListItem.DateSeparatorItem ->
                oldItem.date == (newItem as? MessageListItem.DateSeparatorItem)?.date
            is MessageListItem.ThreadSeparatorItem -> oldItem == (newItem as? MessageListItem.ThreadSeparatorItem)
            is MessageListItem.LoadingMoreIndicatorItem -> true
            is MessageListItem.TypingItem ->
                oldItem.users.map(User::id) == ((newItem) as? MessageListItem.TypingItem)?.users?.map(User::id)
            is MessageListItem.ThreadPlaceholderItem -> true
            is MessageListItem.UnreadSeparatorItem ->
                oldItem.unreadCount == (newItem as? MessageListItem.UnreadSeparatorItem)?.unreadCount

            is MessageListItem.StartOfTheChannelItem ->
                oldItem.channel == (newItem as? MessageListItem.StartOfTheChannelItem)?.channel
        }
    }

    private fun getMessageListItemChangePayload(
        oldItem: MessageListItem,
        newItem: MessageListItem,
    ): MessageListItemPayloadDiff? {
        return (oldItem as? MessageListItem.MessageItem)?.let { oldMessageItem ->
            (newItem as? MessageListItem.MessageItem)?.let { newMessageItem ->
                getMessageChangePayload(oldMessageItem, newMessageItem)
            }
        }
    }

    private fun getMessageChangePayload(
        oldItem: MessageListItem.MessageItem,
        newItem: MessageListItem.MessageItem,
    ): MessageListItemPayloadDiff {
        val oldMessage = oldItem.message
        val newMessage = newItem.message

        return MessageListItemPayloadDiff(
            text = oldMessage.text != newMessage.text,
            replyText = oldMessage.replyTo?.text != newMessage.replyTo?.text,
            reactions = (oldMessage.reactionGroups != newMessage.reactionGroups),
            attachments = oldMessage.attachments != newMessage.attachments,
            replies = oldMessage.replyCount != newMessage.replyCount,
            syncStatus = oldMessage.syncStatus != newMessage.syncStatus,
            deleted = oldMessage.deletedAt != newMessage.deletedAt,
            positions = oldItem.positions != newItem.positions,
            pinned = oldMessage.pinned != newMessage.pinned,
            user = oldMessage.user != newMessage.user,
            mentions = oldMessage.mentionedUsers != newMessage.mentionedUsers,
            footer = oldItem.showMessageFooter != newItem.showMessageFooter,
            poll = oldMessage.poll != newMessage.poll,
            threadMode = oldItem.isThreadMode != newItem.isThreadMode,
        )
    }

    override fun getChangePayload(oldItem: MessageListItem, newItem: MessageListItem): Any? =
        getMessageListItemChangePayload(oldItem, newItem)
}
