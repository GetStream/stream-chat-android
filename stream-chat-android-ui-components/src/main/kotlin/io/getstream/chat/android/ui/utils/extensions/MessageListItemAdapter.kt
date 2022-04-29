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
import com.getstream.sdk.chat.utils.extensions.isBottomPosition
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.message.list.adapter.internal.MessageListItemAdapter

@InternalStreamChatApi
internal fun MessageListItemAdapter.isGroupedWithNextMessage(messageItem: MessageListItem.MessageItem): Boolean {
    if (messageItem.isBottomPosition()) return false

    val messageIndex = currentList.indexOf(
        currentList.find {
            (it as? MessageListItem.MessageItem)?.message?.id == messageItem.message.id
        }
    )
    if (messageIndex == -1) return false

    val nextMessage = currentList.takeLast(currentList.size - messageIndex - 1)
        .find { it is MessageListItem.MessageItem } as? MessageListItem.MessageItem
        ?: return false
    if (messageItem.isMine != nextMessage.isMine) return false

    return (nextMessage.message.createdAt?.time ?: 0) - (
        messageItem.message.createdAt?.time
            ?: 0
        ) < DEFAULT_MESSAGE_GROUPING_DELAY
}

private const val DEFAULT_MESSAGE_GROUPING_DELAY: Long = 60 * 1000L
