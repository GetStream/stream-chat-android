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

package io.getstream.chat.android.compose.ui.util

import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.compose.state.messages.list.MessageItemGroupPosition
import io.getstream.chat.android.compose.state.messages.list.MessageItemState
import io.getstream.chat.android.compose.state.messages.list.MessageListItemState

/**
 * @return If the current message failed to send.
 */
internal fun MessageItemState.isFailed(): Boolean = isMine && message.syncStatus == SyncStatus.FAILED_PERMANENTLY

/**
 * @param message message to check if it is grouped with the next message.
 *
 * @return If the current message is grouped with the next message.
 */
internal fun List<MessageListItemState>.isGroupedWithNextMessage(message: MessageItemState): Boolean {
    if (message.groupPosition == MessageItemGroupPosition.Bottom) return false
    if (message.message.isDeleted()) return true

    val messageIndex = indexOf(message)
    val nextMessage = take(messageIndex).findLast { it is MessageItemState } as? MessageItemState
        ?: return false
    if (message.isMine != nextMessage.isMine) return false
    if (nextMessage.message.isDeleted()) return false

    return (nextMessage.message.createdAt?.time ?: 0) -
        (message.message.createdAt?.time ?: 0) <
        DefaultMessageGroupingDelayMillis
}

private const val DefaultMessageGroupingDelayMillis: Long = 60 * 1000L
