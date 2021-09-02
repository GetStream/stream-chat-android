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
public data class StreamColors(
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
        /**
         * Provides the default colors for the light mode of the app.
         *
         * @return A [StreamColors] instance holding our color palette.
         */
        @Composable
        public fun defaultColors(): StreamColors = StreamColors(
            textHighEmphasis = colorResource(R.color.stream_compose_text_high_emphasis),
            textLowEmphasis = colorResource(R.color.stream_compose_text_low_emphasis),
            disabled = colorResource(R.color.stream_compose_disabled),
            borders = colorResource(R.color.stream_compose_borders),
            inputBackground = colorResource(R.color.stream_compose_input_background),
            appBackground = colorResource(R.color.stream_compose_app_background),
            barsBackground = colorResource(R.color.stream_compose_bars_background),
            linkBackground = colorResource(R.color.stream_compose_link_background),
            overlay = colorResource(R.color.stream_compose_overlay),
            primaryAccent = colorResource(id = R.color.stream_compose_primary_accent),
            errorAccent = colorResource(R.color.stream_compose_error_accent),
            infoAccent = colorResource(R.color.stream_compose_info_accent),
        )

        /**
         * Provides the default colors for the dark mode of the app.
         *
         * @return A [StreamColors] instance holding our color palette.
         */
        @Composable
        public fun defaultDarkColors(): StreamColors = StreamColors(
            textHighEmphasis = colorResource(R.color.stream_compose_text_high_emphasis_dark),
            textLowEmphasis = colorResource(R.color.stream_compose_text_low_emphasis_dark),
            disabled = colorResource(R.color.stream_compose_disabled_dark),
            borders = colorResource(R.color.stream_compose_borders_dark),
            inputBackground = colorResource(R.color.stream_compose_input_background_dark),
            appBackground = colorResource(R.color.stream_compose_app_background_dark),
            barsBackground = colorResource(R.color.stream_compose_bars_background_dark),
            linkBackground = colorResource(R.color.stream_compose_link_background_dark),
            overlay = colorResource(R.color.stream_compose_overlay_dark),
            primaryAccent = colorResource(id = R.color.stream_compose_primary_accent_dark),
            errorAccent = colorResource(R.color.stream_compose_error_accent_dark),
            infoAccent = colorResource(R.color.stream_compose_info_accent_dark),
        )
    }
}
