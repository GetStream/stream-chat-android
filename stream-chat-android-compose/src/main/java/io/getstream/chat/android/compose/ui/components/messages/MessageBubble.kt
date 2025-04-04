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
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.message.isErrorOrFailed
import io.getstream.chat.android.client.utils.message.isGiphyEphemeral
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.core.internal.InternalStreamChatApi
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
    border: BorderStroke? = BorderStroke(1.dp, ChatTheme.colors.borders),
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
 * @param message The message data.
 * @param ownsMessage Indicates if the current user owns the message.
 * @return A color for the message bubble.
 */
@InternalStreamChatApi
@Composable
public fun getMessageBubbleColor(message: Message, ownsMessage: Boolean): Color {
    val theme = if (ownsMessage) ChatTheme.ownMessageTheme else ChatTheme.otherMessageTheme
    return when {
        message.isGiphyEphemeral() -> ChatTheme.colors.giphyMessageBackground
        message.isDeleted() -> theme.deletedBackgroundColor
        message.isErrorOrFailed() -> theme.errorBackgroundColor
        else -> theme.backgroundColor
    }
}
