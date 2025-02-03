/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.messages.list

import androidx.compose.foundation.layout.Arrangement

/**
 * Defines the vertical arrangement of the messages list.
 * Exposes two predefined vertical arrangements: [Top] and [Bottom].
 *
 * You can also create your own custom vertical arrangement for the message list by implementing this interface.
 */
public interface MessagesVerticalArrangement {

    /**
     * The vertical arrangement of the messages list.
     */
    public val arrangement: Arrangement.Vertical

    /**
     * Defines a Top-aligned vertical arrangement of the message list.
     */
    public data object Top : MessagesVerticalArrangement {
        override val arrangement: Arrangement.Vertical = Arrangement.Top
    }

    /**
     * Defines a Bottom-aligned vertical arrangement of the message list.
     */
    public data object Bottom : MessagesVerticalArrangement {
        override val arrangement: Arrangement.Vertical = Arrangement.Bottom
    }
}
