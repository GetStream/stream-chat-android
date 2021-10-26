package io.getstream.chat.android.compose.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Contains all the typography we provide for our components.
 *
 * @param title1 Used for big titles, like the image attachment overlay text.
 * @param title3Bold Used for titles of app bars and bottom bars.
 * @param body Used for body content, such as messages.
 * @param bodyItalic Used for body content, italicized, like deleted message components.
 * @param bodyBold Used for emphasized body content, like small titles.
 * @param footnote Used for footnote information, like timestamps.
 * @param footnoteItalic Used for footnote information that's less important, like the deleted message text.
 * @param footnoteBold Used for footnote information in certain important items, like the thread reply text, or user
 * info components.
 */
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
                fontFamily = fontFamily
            ),
            title3 = TextStyle(
                fontSize = 18.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.W400,
                fontFamily = fontFamily
            ),
            title3Bold = TextStyle(
                fontSize = 18.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.W500,
                fontFamily = fontFamily
            ),
            body = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                fontFamily = fontFamily
            ),
            bodyItalic = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                fontStyle = FontStyle.Italic,
                fontFamily = fontFamily
            ),
            bodyBold = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.W500,
                fontFamily = fontFamily
            ),
            footnote = TextStyle(
                fontSize = 12.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W400,
                fontFamily = fontFamily
            ),
            footnoteItalic = TextStyle(
                fontSize = 12.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W400,
                fontStyle = FontStyle.Italic,
                fontFamily = fontFamily
            ),
            footnoteBold = TextStyle(
                fontSize = 12.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W500,
                fontFamily = fontFamily
            ),
            captionBold = TextStyle(
                fontSize = 10.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.W700,
                fontFamily = fontFamily
            ),
            tabBar = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.W400,
                fontFamily = fontFamily
            )
        )
    }
}
