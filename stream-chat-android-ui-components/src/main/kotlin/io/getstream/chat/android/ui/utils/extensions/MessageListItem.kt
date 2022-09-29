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

package io.getstream.chat.android.ui.utils.extensions

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.common.model.messsagelist.DateSeparatorItem
import io.getstream.chat.android.common.model.messsagelist.MessageItem
import io.getstream.chat.android.common.model.messsagelist.SystemMessageItem
import io.getstream.chat.android.common.model.messsagelist.ThreadSeparatorItem
import io.getstream.chat.android.common.model.messsagelist.TypingItem
import io.getstream.chat.android.common.model.messsagelist.MessageListItem as MessageListItemCommon

/**
 * Converts [MessageListItemCommon] to [MessageListItem] to be shown inside
 * [io.getstream.chat.android.ui.message.list.MessageListView].
 *
 * @return [MessageListItem] derived from [MessageListItemCommon].
 */
public fun MessageListItemCommon.toUiMessageListItem(): MessageListItem {
    return when (this) {
        is DateSeparatorItem -> MessageListItem.DateSeparatorItem(date = date)
        is SystemMessageItem -> MessageListItem.ThreadPlaceholderItem
        is ThreadSeparatorItem -> MessageListItem.ThreadSeparatorItem(date = date, messageCount = messageCount)
        is TypingItem -> MessageListItem.TypingItem(users = typingUsers)
        is MessageItem -> MessageListItem.MessageItem(
            message = message,
            positions = groupPosition.mapNotNull { it.toUiPosition() },
            isMine = isMine,
            messageReadBy = messageReadBy,
            isThreadMode = isInThread,
            isMessageRead = isMessageRead,
            showMessageFooter = showMessageFooter
        )
    }
}
