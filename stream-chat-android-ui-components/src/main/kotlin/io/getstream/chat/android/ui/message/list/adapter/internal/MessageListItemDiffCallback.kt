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

package io.getstream.chat.android.ui.message.list.adapter.internal

import androidx.recyclerview.widget.DiffUtil
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff

internal object MessageListItemDiffCallback : DiffUtil.ItemCallback<MessageListItem>() {
    override fun areItemsTheSame(oldItem: MessageListItem, newItem: MessageListItem): Boolean {
        return oldItem.getStableId() == newItem.getStableId()
    }

    override fun areContentsTheSame(oldItem: MessageListItem, newItem: MessageListItem): Boolean {
        return when (oldItem) {
            is MessageListItem.MessageItem -> {
                newItem as MessageListItem.MessageItem
                val oldMessage = oldItem.message
                val newMessage = newItem.message

                oldMessage.text == newItem.message.text &&
                    oldMessage.reactionScores == newMessage.reactionScores &&
                    oldMessage.reactionCounts == newMessage.reactionCounts &&
                    oldMessage.attachments == newMessage.attachments &&
                    oldMessage.replyCount == newMessage.replyCount &&
                    oldMessage.syncStatus == newMessage.syncStatus &&
                    oldMessage.deletedAt == newMessage.deletedAt &&
                    oldItem.positions == newItem.positions &&
                    oldItem.isMessageRead == newItem.isMessageRead &&
                    oldItem.isThreadMode == newItem.isThreadMode &&
                    oldMessage.extraData == newMessage.extraData &&
                    oldMessage.pinned == newMessage.pinned &&
                    oldMessage.user == newMessage.user &&
                    oldMessage.mentionedUsers == newMessage.mentionedUsers
            }
            is MessageListItem.DateSeparatorItem -> oldItem.date == (newItem as? MessageListItem.DateSeparatorItem)?.date
            is MessageListItem.ThreadSeparatorItem -> oldItem == (newItem as? MessageListItem.ThreadSeparatorItem)
            is MessageListItem.LoadingMoreIndicatorItem -> true
            is MessageListItem.TypingItem -> oldItem.users.map(User::id) == ((newItem) as? MessageListItem.TypingItem)?.users?.map(
                User::id
            )
            is MessageListItem.ThreadPlaceholderItem -> true
        }
    }

    override fun getChangePayload(oldItem: MessageListItem, newItem: MessageListItem): Any? {
        return if (oldItem is MessageListItem.MessageItem) {
            newItem as MessageListItem.MessageItem
            val oldMessage = oldItem.message
            val newMessage = newItem.message

            MessageListItemPayloadDiff(
                text = oldMessage.text != newMessage.text,
                reactions = (oldMessage.reactionCounts != newMessage.reactionCounts) || (oldMessage.reactionScores != newMessage.reactionScores),
                attachments = oldMessage.attachments != newMessage.attachments,
                replies = oldMessage.replyCount != newMessage.replyCount,
                syncStatus = oldMessage.syncStatus != newMessage.syncStatus,
                deleted = oldMessage.deletedAt != newMessage.deletedAt,
                positions = oldItem.positions != newItem.positions,
                pinned = oldMessage.pinned != newMessage.pinned,
                user = oldMessage.user != newMessage.user,
                mentions = oldMessage.mentionedUsers != newMessage.mentionedUsers
            )
        } else {
            null
        }
    }
}
