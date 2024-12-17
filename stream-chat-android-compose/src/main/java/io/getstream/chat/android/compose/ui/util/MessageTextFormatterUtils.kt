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
import io.getstream.chat.android.compose.ui.theme.MessageTheme
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamShapes
import io.getstream.chat.android.compose.ui.theme.StreamTypography

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
): (Boolean) -> Color {
    return { isMine ->
        if (isMine) ownTheme.mentionColor else otherTheme.mentionColor
    }
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
