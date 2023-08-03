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

package io.getstream.chat.android.compose.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Contains all the typography we provide for our components.
 *
 * @param title1 Used for big titles, like the image attachment overlay text.
 * @param title3 Used for empty content text.
 * @param title3Bold Used for titles of app bars and bottom bars.
 * @param body Used for body content, such as messages.
 * @param bodyItalic Used for body content, italicized, like deleted message components.
 * @param bodyBold Used for emphasized body content, like small titles.
 * @param footnote Used for footnote information, like timestamps.
 * @param footnoteItalic Used for footnote information that's less important, like the deleted message text.
 * @param footnoteBold Used for footnote information in certain important items, like the thread reply text,
 * or user info components.
 * @param captionBold Used for unread count indicator.
 * @param singleEmoji Used for messages whose content consists only of a single emoji.
 * @param emojiOnly Used for messages whose content consists only if emojis.
 */
@Immutable
public data class StreamTypography(
    public val title1: TextStyle,
    public val title3: TextStyle,
    public val title3Bold: TextStyle,
    public val body: TextStyle,
    public val bodyItalic: TextStyle,
    public val bodyBold: TextStyle,
    public val footnote: TextStyle,
    public val footnoteItalic: TextStyle,
    public val footnoteBold: TextStyle,
    public val captionBold: TextStyle,
    public val tabBar: TextStyle,
    public val singleEmoji: TextStyle,
    public val emojiOnly: TextStyle,
) {

    public companion object {
        /**
         * Builds the default typography set for our theme, with the ability to customize the font family.
         *
         * @param fontFamily The font that the users want to use for the app.
         * @return [StreamTypography] that holds all the default text styles that we support.
         */
        public fun defaultTypography(fontFamily: FontFamily? = null): StreamTypography = StreamTypography(
            title1 = TextStyle(
                fontSize = 24.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.W500,
                fontFamily = fontFamily,
            ),
            title3 = TextStyle(
                fontSize = 18.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.W400,
                fontFamily = fontFamily,
            ),
            title3Bold = TextStyle(
                fontSize = 18.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.W500,
                fontFamily = fontFamily,
            ),
            body = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                fontFamily = fontFamily,
            ),
            bodyItalic = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                fontStyle = FontStyle.Italic,
                fontFamily = fontFamily,
            ),
            bodyBold = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.W500,
                fontFamily = fontFamily,
            ),
            footnote = TextStyle(
                fontSize = 12.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W400,
                fontFamily = fontFamily,
            ),
            footnoteItalic = TextStyle(
                fontSize = 12.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W400,
                fontStyle = FontStyle.Italic,
                fontFamily = fontFamily,
            ),
            footnoteBold = TextStyle(
                fontSize = 12.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W500,
                fontFamily = fontFamily,
            ),
            captionBold = TextStyle(
                fontSize = 10.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.W700,
                fontFamily = fontFamily,
            ),
            tabBar = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.W400,
                fontFamily = fontFamily,
            ),
            singleEmoji = TextStyle(
                fontSize = 50.sp,
                fontFamily = fontFamily,
            ),
            emojiOnly = TextStyle(
                fontSize = 50.sp,
                fontFamily = fontFamily,
            ),
        )
    }
}
