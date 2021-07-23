package io.getstream.chat.android.compose.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import io.getstream.chat.android.compose.R

/**
 * Contains all the colors in our palette. Each color is used for various things an can be changed to
 * customize the app design style.
 * @param textHighEmphasis - Used for main text and active icon status.
 * @param textLowEmphasis - Used for secondary text, default icon state, deleted messages text and datestamp background.
 * @param disabled - Used for disabled icons and empty states.
 * @param borders - Used for borders, the background of self messages, selected items, pressed state, button dividers.
 * @param inputBackground - Used for the input background, deleted messages, section headings.
 * @param appBackground - Used for the default app background and channel list item background.
 * @param barsBackground - Used for button text, top and bottom bar background and other user messages.
 * @param linkBackground - Used for the message link card background.
 * @param overlay - Used for general overlays and background when opening modals.
 * @param primaryAccent - Used for selected icon state, call to actions, white buttons text and links.
 * @param errorAccent - Used for error text labels, notification badges and disruptive action text and icons.
 * @param infoAccent - Used for the online status.
 * */
public class StreamColors(
    public val textHighEmphasis: Color,
    public val textLowEmphasis: Color,
    public val disabled: Color,
    public val borders: Color,
    public val inputBackground: Color,
    public val appBackground: Color,
    public val barsBackground: Color,
    public val linkBackground: Color,
    public val overlay: Color,
    public val primaryAccent: Color,
    public val errorAccent: Color,
    public val infoAccent: Color,
) {

    public companion object {
        @Composable
        public fun defaultColors(): StreamColors = StreamColors(
            textHighEmphasis = colorResource(R.color.textHighEmphasis),
            textLowEmphasis = colorResource(R.color.textLowEmphasis),
            disabled = colorResource(R.color.disabled),
            borders = colorResource(R.color.borders),
            inputBackground = colorResource(R.color.inputBackground),
            appBackground = colorResource(R.color.appBackground),
            barsBackground = colorResource(R.color.barsBackground),
            linkBackground = colorResource(R.color.linkBackground),
            overlay = colorResource(R.color.overlay),
            primaryAccent = colorResource(id = R.color.primaryAccent),
            errorAccent = colorResource(R.color.errorAccent),
            infoAccent = colorResource(R.color.infoAccent),
        )

        @Composable
        public fun defaultDarkColors(): StreamColors = StreamColors(
            textHighEmphasis = colorResource(R.color.textHighEmphasisDark),
            textLowEmphasis = colorResource(R.color.textLowEmphasisDark),
            disabled = colorResource(R.color.disabledDark),
            borders = colorResource(R.color.bordersDark),
            inputBackground = colorResource(R.color.inputBackgroundDark),
            appBackground = colorResource(R.color.appBackgroundDark),
            barsBackground = colorResource(R.color.barsBackgroundDark),
            linkBackground = colorResource(R.color.linkBackgroundDark),
            overlay = colorResource(R.color.overlayDark),
            primaryAccent = colorResource(id = R.color.primaryAccentDark),
            errorAccent = colorResource(R.color.errorAccentDark),
            infoAccent = colorResource(R.color.infoAccentDark),
        )
    }
}