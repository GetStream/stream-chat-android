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

package io.getstream.chat.android.ui.message.list

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.ChatClient

/**
 * Predicate class used to filter [MessageListItem.MessageItem] items which are deleted. Used by [MessageListView.setDeletedMessageListItemPredicate].
 */
public sealed class DeletedMessageListItemPredicate : MessageListView.MessageListItemPredicate {
    /**
     * Predicate object used to hide deleted [MessageListItem.MessageItem] items from everyone.
     */
    public object NotVisibleToAnyone : DeletedMessageListItemPredicate() {
        override fun predicate(item: MessageListItem): Boolean {
            return false
        }
    }

    /**
     * Predicate object used to show deleted [MessageListItem.MessageItem] items to everyone.
     */
    public object VisibleToEveryone : DeletedMessageListItemPredicate() {
        override fun predicate(item: MessageListItem): Boolean {
            return true
        }
    }

    /**
     * Predicate object used to hide deleted [MessageListItem.MessageItem] items from everyone except for the author of the message.
     */
    public object VisibleToAuthorOnly : DeletedMessageListItemPredicate() {
        override fun predicate(item: MessageListItem): Boolean {
            return ChatClient.instance().getCurrentUser()?.let { user ->
                item is MessageListItem.MessageItem && item.message.user.id == user.id
            } ?: false
        }
    }
}
