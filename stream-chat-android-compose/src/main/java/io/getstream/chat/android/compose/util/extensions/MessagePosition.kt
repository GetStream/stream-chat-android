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

import io.getstream.chat.android.common.state.messagelist.MessagePosition
import io.getstream.chat.android.compose.state.messages.list.MessageItemGroupPosition

/**
 * Converts common [MessagePosition] to compose [MessageItemGroupPosition].
 *
 * @return Derived [MessageItemGroupPosition] from [MessagePosition].
 */
public fun MessagePosition.toMessageItemGroupPosition(): MessageItemGroupPosition {
    return when (this) {
        MessagePosition.TOP -> MessageItemGroupPosition.Top
        MessagePosition.MIDDLE -> MessageItemGroupPosition.Middle
        MessagePosition.BOTTOM -> MessageItemGroupPosition.Bottom
        MessagePosition.NONE -> MessageItemGroupPosition.None
    }
}
