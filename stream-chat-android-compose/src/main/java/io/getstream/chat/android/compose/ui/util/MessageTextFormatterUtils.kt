/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import io.getstream.chat.android.client.utils.message.isErrorOrFailed
import io.getstream.chat.android.compose.ui.theme.MessageTheme
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamShapes
import io.getstream.chat.android.compose.ui.theme.StreamTypography
import io.getstream.chat.android.models.Message

/**
 * Builds a function that returns text style, depending on whether the message is mine or not, and based on
 * whether the message is an error/failed message or not.
 *
 * @param ownTheme The theme for messages from the current user.
 * @param otherTheme The theme for messages from the other users.
 */
@Composable
internal fun defaultTextStyle(
    ownTheme: MessageTheme,
    otherTheme: MessageTheme,
): (Boolean, Message) -> TextStyle = { isMine, message ->
    val theme = if (isMine) ownTheme else otherTheme
    when {
        message.isErrorOrFailed() -> theme.errorTextStyle
        else -> theme.textStyle
    }
}

/**
 * Builds a function that returns text style, depending on whether the message is mine or not, and based on
 * whether the message is an error/failed message or not.
 *
 * @param isInDarkMode Indicator if the app is in dark mode.
 * @param typography The typography to use for styling.
 * @param colors The colors to use for styling.
 * @param shapes The shapes to use for styling.
 */
@Composable
internal fun defaultTextStyle(
    isInDarkMode: Boolean,
    typography: StreamTypography,
    colors: StreamColors,
    shapes: StreamShapes = StreamShapes.defaultShapes(),
): (Boolean, Message) -> TextStyle {
    val ownTheme = MessageTheme.defaultOwnTheme(isInDarkMode, typography, shapes, colors)
    val otherTheme = MessageTheme.defaultOtherTheme(isInDarkMode, typography, shapes, colors)
    return defaultTextStyle(ownTheme, otherTheme)
}

@Composable
internal fun defaultLinkStyle(colors: StreamColors): (isOwnMessage: Boolean) -> TextStyle = defaultLinkStyle(
    ownTheme = MessageTheme.defaultOwnTheme(colors = colors),
    otherTheme = MessageTheme.defaultOtherTheme(colors = colors),
)

internal fun defaultLinkStyle(
    ownTheme: MessageTheme,
    otherTheme: MessageTheme,
): (isOwnMessage: Boolean) -> TextStyle = { isOwnMessage ->
    if (isOwnMessage) ownTheme.linkStyle else otherTheme.linkStyle
}

/**
 * Function that returns the color of the mentions text, depending on whether the message is mine or not.
 *
 * @param ownTheme The theme for messages from the current user.
 * @param otherTheme The theme for messages from the other user.
 *
 * @return A function that returns the color of the mention text, based on whether the message is from the current user
 * or from another user.
 */
@Composable
internal fun defaultMentionColor(
    ownTheme: MessageTheme,
    otherTheme: MessageTheme,
): (Boolean) -> Color = { isMine ->
    if (isMine) ownTheme.mentionColor else otherTheme.mentionColor
}

/**
 * Function that returns the color of the mentions text, depending on whether the message is mine or not.
 *
 * @param isInDarkMode Indicator if the app is in dark mode.
 * @param typography The typography to use for styling.
 * @param colors The colors to use for styling.
 * @param shapes The shapes to use for styling.
 *
 * @return A function that returns the color of the mention text, based on whether the message is from the current user
 * or from another user.
 */
@Composable
internal fun defaultMentionColor(
    isInDarkMode: Boolean,
    typography: StreamTypography,
    colors: StreamColors,
    shapes: StreamShapes = StreamShapes.defaultShapes(),
): (Boolean) -> Color {
    val ownTheme = MessageTheme.defaultOwnTheme(isInDarkMode, typography, shapes, colors)
    val otherTheme = MessageTheme.defaultOtherTheme(isInDarkMode, typography, shapes, colors)
    return defaultMentionColor(ownTheme, otherTheme)
}
