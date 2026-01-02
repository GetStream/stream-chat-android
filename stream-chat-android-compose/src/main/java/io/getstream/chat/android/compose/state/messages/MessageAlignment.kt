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

package io.getstream.chat.android.compose.state.messages

import androidx.compose.ui.Alignment

/**
 * Represents the horizontal alignment of messages in the message list.
 *
 * @param itemAlignment The alignment of the message item.
 * @param contentAlignment The alignment of the inner content.
 */
public enum class MessageAlignment(
    public val itemAlignment: Alignment,
    public val contentAlignment: Alignment.Horizontal,
) {
    /**
     * Represents the alignment at the start of the screen, by default for other people's messages.
     */
    Start(Alignment.CenterStart, Alignment.Start),

    /**
     * Represents the alignment at the end of the screen, by default for owned messages.
     */
    End(Alignment.CenterEnd, Alignment.End),
}
