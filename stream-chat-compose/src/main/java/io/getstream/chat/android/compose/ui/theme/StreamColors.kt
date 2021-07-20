package io.getstream.chat.android.compose.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import io.getstream.chat.android.compose.R

/**
 * Contains all the colors in our palette. Each color is used for various things an can be changed to
 * customize the app design style.
 * // TODO - clearly define all these colors.
 * @param primaryAccent - Used for things like primary icon colors, buttons etc.
 * @param appCanvas - Used for the base app background.
 * @param textHighEmphasis - Color for text that's important, like message text.
 * @param textMidEmphasis - Color for text that's somewhat important, like labels.
 * @param textLowEmphasis - Color for text that's less important, like timestamps.
 * @param cardBackground - Color for card backgrounds, usually messages.
 * @param cardAltBackground - Alternate color for car background, like other people's messages.
 * @param stroke - Color of stroke accents, such as card borders.
 * @param strokeLowEmphasis - Color of stroke accents, for less emphasized borders and components.
 * */
class StreamColors(
    val primaryAccent: Color,
    val appCanvas: Color,
    val textHighEmphasis: Color,
    val textMidEmphasis: Color,
    val textLowEmphasis: Color,
    val textSelf: Color,
    val textPressed: Color,
    val cardBackground: Color,
    val cardAltBackground: Color,
    val cardSelfBackground: Color,
    val stroke: Color,
    val strokeLowEmphasis: Color
) {

    companion object {
        @Composable
        fun defaultColors(): StreamColors = StreamColors(
            primaryAccent = colorResource(id = R.color.primaryAccent),
            appCanvas = colorResource(R.color.appCanvas),
            textHighEmphasis = colorResource(R.color.textHighEmphasis),
            textMidEmphasis = colorResource(R.color.textMidEmphasis),
            textLowEmphasis = colorResource(R.color.textLowEmphasis),
            textSelf = colorResource(R.color.textSelf),
            textPressed = colorResource(R.color.textPressed),
            cardBackground = colorResource(R.color.cardBackground),
            cardAltBackground = colorResource(R.color.cardAltBackground),
            cardSelfBackground = colorResource(R.color.cardSelfBackground),
            stroke = colorResource(R.color.stroke),
            strokeLowEmphasis = colorResource(R.color.strokeLowEmphasis)
        )

        @Composable
        fun defaultDarkColors() = StreamColors(
            primaryAccent = colorResource(R.color.primaryAccentDark),
            appCanvas = colorResource(R.color.appCanvasDark),
            textHighEmphasis = colorResource(R.color.textHighEmphasisDark),
            textMidEmphasis = colorResource(R.color.textMidEmphasisDark),
            textLowEmphasis = colorResource(R.color.textLowEmphasisDark),
            textSelf = colorResource(R.color.textSelfDark),
            textPressed = colorResource(R.color.textPressedDark),
            cardBackground = colorResource(R.color.cardBackgroundDark),
            cardAltBackground = colorResource(R.color.cardAltBackgroundDark),
            cardSelfBackground = colorResource(R.color.cardSelfBackgroundDark),
            stroke = colorResource(R.color.strokeDark),
            strokeLowEmphasis = colorResource(R.color.strokeLowEmphasisDark)
        )
    }
}