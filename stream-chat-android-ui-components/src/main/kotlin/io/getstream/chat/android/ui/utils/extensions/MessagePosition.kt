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
import io.getstream.chat.android.common.state.messagelist.MessagePosition

/**
 * Converts the common [MessagePosition] to ui-components [MessageListItem.Position].
 *
 * @return [MessageListItem.Position] derived from [MessagePosition].
 */
public fun MessagePosition.toUiPosition(): MessageListItem.Position? {
    return when (this) {
        MessagePosition.TOP -> MessageListItem.Position.TOP
        MessagePosition.MIDDLE -> MessageListItem.Position.MIDDLE
        MessagePosition.BOTTOM -> MessageListItem.Position.BOTTOM
        MessagePosition.NONE -> null
    }
}

/**
 * Converts the ui-components [MessageListItem.Position] to common [MessagePosition].
 *
 * @return [MessagePosition] derived from [MessageListItem.Position].
 */
public fun MessageListItem.Position.toCommonPosition(): MessagePosition {
    return when (this) {
        MessageListItem.Position.TOP -> MessagePosition.TOP
        MessageListItem.Position.MIDDLE -> MessagePosition.MIDDLE
        MessageListItem.Position.BOTTOM -> MessagePosition.BOTTOM
    }
}
