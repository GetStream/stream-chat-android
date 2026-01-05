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

import io.getstream.chat.android.client.utils.message.isErrorOrFailed
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem

/**
 * Checks if the message is the last in a group, meaning it has no messages below it.
 * A message with [MessagePosition.NONE] is also considered the last in the group,
 * as it represents a single isolated message.
 */
@InternalStreamChatApi
public fun MessageListItem.MessageItem.isBottomPosition(): Boolean {
    return MessagePosition.BOTTOM in positions || MessagePosition.NONE in positions
}

@InternalStreamChatApi
public fun MessageListItem.MessageItem.isNotBottomPosition(): Boolean {
    return !isBottomPosition()
}

/**
 * @return If the mine message is the type of error or failed to send.
 */
@InternalStreamChatApi
internal fun MessageListItem.MessageItem.isErrorOrFailed(): Boolean = isMine && message.isErrorOrFailed()
