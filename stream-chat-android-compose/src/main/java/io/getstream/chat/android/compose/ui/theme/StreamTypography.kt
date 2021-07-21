package io.getstream.chat.android.compose.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Contains all the typography we provide for our components.
 *
 * @param title3Bold - Used for titles of app bars and bottom bars.
 * @param body - Used for body content, such as messages.
 * @param bodyItalic - Used for body content, italicized, like deleted message components.
 * @param bodyBold - Used for emphasized body content, like small titles.
 * @param footnote - Used for footnote information, like timestamps.
 * @param tabBar - Used for items on top/bottom bars.
 * */
class StreamTypography(
    val title1: TextStyle,
    val title3: TextStyle,
    val title3Bold: TextStyle,
    val body: TextStyle,
    val bodyItalic: TextStyle,
    val bodyBold: TextStyle,
    val footnote: TextStyle,
    val footnoteItalic: TextStyle,
    val footnoteBold: TextStyle,
    val captionBold: TextStyle,
    val tabBar: TextStyle
) {

    companion object {
        // TODO - We might need to provide the custom font family
        /**
         * Builds the default typography set for our theme.
         * */
        val default = StreamTypography(
            title1 = TextStyle(
                fontSize = 28.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.W500
            ),
            title3 = TextStyle(
                fontSize = 20.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.W400
            ),
            title3Bold = TextStyle(
                fontSize = 20.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.W500
            ),
            body = TextStyle(
                fontSize = 17.sp,
                fontWeight = FontWeight.W400
            ),
            bodyItalic = TextStyle(
                fontSize = 17.sp,
                fontWeight = FontWeight.W400,
                fontStyle = FontStyle.Italic
            ),
            bodyBold = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.W500
            ),
            footnote = TextStyle(
                fontSize = 13.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W400
            ),
            footnoteItalic = TextStyle(
                fontSize = 13.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W400,
                fontStyle = FontStyle.Italic
            ),
            footnoteBold = TextStyle(
                fontSize = 13.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W500
            ),
            captionBold = TextStyle(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.W700
            ),
            tabBar = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.W400
            )
        )
    }
}