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

package io.getstream.chat.android.compose.util.extensions

import io.getstream.chat.android.common.model.messsagelist.DateSeparatorItem
import io.getstream.chat.android.common.model.messsagelist.MessageItem
import io.getstream.chat.android.common.model.messsagelist.MessageListItem
import io.getstream.chat.android.common.model.messsagelist.SystemMessageItem
import io.getstream.chat.android.common.model.messsagelist.ThreadSeparatorItem
import io.getstream.chat.android.common.model.messsagelist.TypingItem
import io.getstream.chat.android.compose.state.messages.list.DateSeparatorState
import io.getstream.chat.android.compose.state.messages.list.MessageItemGroupPosition
import io.getstream.chat.android.compose.state.messages.list.MessageItemState
import io.getstream.chat.android.compose.state.messages.list.MessageListItemState
import io.getstream.chat.android.compose.state.messages.list.SystemMessageState
import io.getstream.chat.android.compose.state.messages.list.ThreadSeparatorState
import io.getstream.chat.android.compose.state.messages.list.TypingItemState

/**
 * Converts common [MessageListItem] to compose [MessageListItemState].
 *
 * @return Compose [MessageListItemState] derived from common [MessageListItem].
 */
public fun MessageListItem.toMessageListItemState(): MessageListItemState {
    return when (this) {
        is DateSeparatorItem -> DateSeparatorState(this.date)
        is MessageItem -> MessageItemState(
            message = message,
            groupPosition = groupPosition.firstOrNull()?.toMessageItemGroupPosition() ?: MessageItemGroupPosition.None,
            parentMessageId = parentMessageId,
            isMine = isMine,
            isInThread = isInThread,
            currentUser = currentUser,
            isMessageRead = isMessageRead,
            shouldShowFooter = showMessageFooter,
            deletedMessageVisibility = deletedMessageVisibility,
            focusState = focusState?.toFocusState()
        )
        is SystemMessageItem -> SystemMessageState(this.message)
        is ThreadSeparatorItem -> ThreadSeparatorState(this.messageCount)
        is TypingItem -> TypingItemState(this.typingUsers)
    }
}
