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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.message.isErrorOrFailed
import io.getstream.chat.android.client.utils.message.isGiphyEphemeral
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.models.Message

/**
 * Wraps the content of a message in a bubble.
 *
 * @param color The color of the bubble.
 * @param shape The shape of the bubble.
 * @param modifier Modifier for styling.
 * @param border The optional border of the bubble.
 * @param contentPadding Padding values to be applied to the content inside the bubble.
 * @param content The content of the message.
 */
@Composable
public fun MessageBubble(
    color: Color,
    shape: Shape,
    modifier: Modifier = Modifier,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        border = border,
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

/**
 * Determines the background color of the message bubble based on the message content and ownership.
 *
 * @param outgoing Whether the message is by the current user
 * @param message The message data.
 * @return A color for the message bubble.
 */
@Composable
internal fun getMessageBubbleColor(outgoing: Boolean, message: Message): Color {
    return when {
        message.isGiphyEphemeral() -> ChatTheme.colors.giphyMessageBackground
        message.isDeleted() -> MessageStyling.backgroundColor(outgoing)
        message.isErrorOrFailed() -> MessageStyling.backgroundColor(outgoing)
        else -> MessageStyling.backgroundColor(outgoing)
    }
}
