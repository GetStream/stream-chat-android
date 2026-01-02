/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.ui.common.state.messages.list.MessageListState
import io.getstream.chat.android.ui.common.state.messages.list.NewMessageState
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.model.MessageListItemWrapper

/**
 * Converts the common [MessageListState] to ui-components [MessageListItemWrapper].
 *
 * @param isInThread Whether the message list is currently in thread mode or not.
 * @param prevNewMessageState The previous [NewMessageState] to compare with the current one.
 *
 * @return [MessageListItemWrapper] derived from [MessageListState].
 */
public fun MessageListState.toMessageListItemWrapper(
    isInThread: Boolean,
    prevNewMessageState: NewMessageState? = null,
): MessageListItemWrapper {
    var messagesList: List<MessageListItem> = messageItems.map { it.toUiMessageListItem() }

    if (isLoadingOlderMessages) messagesList = messagesList + listOf(MessageListItem.LoadingMoreIndicatorItem)
    if (isLoadingNewerMessages) messagesList = listOf(MessageListItem.LoadingMoreIndicatorItem) + messagesList

    return MessageListItemWrapper(
        items = messagesList,
        hasNewMessages = newMessageState != prevNewMessageState,
        newMessageState = newMessageState,
        isTyping = messagesList.firstOrNull { it is MessageListItem.TypingItem } != null,
        areNewestMessagesLoaded = endOfNewMessagesReached,
        isThread = isInThread,
    )
}
