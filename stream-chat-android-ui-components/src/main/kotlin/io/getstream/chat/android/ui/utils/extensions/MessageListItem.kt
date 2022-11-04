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
import io.getstream.chat.android.common.model.messsagelist.DateSeparatorItemState
import io.getstream.chat.android.common.model.messsagelist.MessageItemState
import io.getstream.chat.android.common.model.messsagelist.SystemMessageItemState
import io.getstream.chat.android.common.model.messsagelist.ThreadDateSeparatorItemState
import io.getstream.chat.android.common.model.messsagelist.TypingItemState
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.common.model.messsagelist.MessageListItemState as MessageListItemCommon

/**
 * Converts [MessageListItemCommon] to [MessageListItem] to be shown inside [MessageListView].
 *
 * @return [MessageListItem] derived from [MessageListItemCommon].
 */
public fun MessageListItemCommon.toUiMessageListItem(): MessageListItem {
    return when (this) {
        is DateSeparatorItemState -> MessageListItem.DateSeparatorItem(date = date)
        is SystemMessageItemState -> MessageListItem.MessageItem(message = message)
        is ThreadDateSeparatorItemState -> MessageListItem.ThreadSeparatorItem(date = date, messageCount = replyCount)
        is TypingItemState -> MessageListItem.TypingItem(users = typingUsers)
        is MessageItemState -> MessageListItem.MessageItem(
            message = message,
            positions = groupPosition,
            isMine = isMine,
            messageReadBy = messageReadBy,
            isThreadMode = isInThread,
            isMessageRead = isMessageRead,
            showMessageFooter = showMessageFooter
        )
    }
}
