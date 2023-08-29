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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import io.getstream.chat.android.compose.R

/**
 * Contains all the colors in our palette. Each color is used for various things an can be changed to
 * customize the app design style.
 * @param textHighEmphasis Used for main text and active icon status.
 * @param textLowEmphasis Used for secondary text, default icon state, deleted messages text and datestamp background.
 * @param disabled Used for disabled icons and empty states.
 * @param borders Used for borders, the background of self messages, selected items, pressed state, button dividers.
 * @param inputBackground Used for the input background, deleted messages, section headings.
 * @param appBackground Used for the default app background and channel list item background.
 * @param barsBackground Used for button text, top and bottom bar background and other user messages.
 * @param linkBackground Used for the message link card background.
 * @param overlay Used for general overlays and background when opening modals.
 * @param overlayDark Used for the date separator background color.
 * @param primaryAccent Used for selected icon state, call to actions, white buttons text and links.
 * @param errorAccent Used for error text labels, notification badges and disruptive action text and icons.
 * @param infoAccent Used for the online status.
 * @param highlight Used for message highlights.
 * @param ownMessagesBackground Used as a background color for the messages sent by the current user.
 * @param otherMessagesBackground Used as a background color for the messages sent by other users.
 * @param deletedMessagesBackground Used as a background for deleted messages.
 * @param giphyMessageBackground Used as a background for the ephemeral giphy messages.
 * @param threadSeparatorGradientStart Used as a start color for vertical gradient background in a thread separator.
 * @param threadSeparatorGradientEnd Used as an end color for vertical gradient background in a thread separator.
 * @param ownMessageText Used for message text color for the current user. [textHighEmphasis] by default.
 * @param otherMessageText Used for message text color for other users. [textHighEmphasis] by default.
 * @param imageBackgroundMessageList Used to set the background colour of images inside the message list.
 * Most visible in placeholders before the images are loaded.
 * @param imageBackgroundMediaGalleryPicker Used to set the background colour of images inside the media gallery picker
 * in the media gallery preview screen. Most visible in placeholders before the images are loaded.
 * @param imageBackgroundMessageList Used to set the background colour of videos inside the message list.
 * Most visible in placeholders before the video previews are loaded.
 * @param imageBackgroundMediaGalleryPicker Used to set the background colour of videos inside the media gallery picker
 * in the media gallery preview screen. Most visible in placeholders before the videos previews are loaded.
 * @param showMoreOverlay The color of the overlay displaying how many more media attachments the message contains,
 * given it contains more than can be displayed in the message list media attachment preview.
 * @param showMoreCountText The color of the text displaying how many more media attachments the message contains,
 * given it contains more than can be displayed in the message list media attachment preview.
 * @param ownMessageQuotedBackground Changes the background color of the quoted message contained in a reply sent by the current user.
 * @param otherMessageQuotedBackground Changes the background color of the quoted message contained in a reply sent by other users.
 * @param ownMessageQuotedText Changes the text color of the quoted message contained in a reply sent by the current user. [textHighEmphasis] by default.
 * @param otherMessageQuotedText Changes the text color of the quoted message contained in a reply sent by other users. [textHighEmphasis] by default.
 */
@Suppress("DEPRECATION_ERROR")
@Immutable
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
    public val overlayDark: Color,
    public val primaryAccent: Color,
    public val errorAccent: Color,
    public val infoAccent: Color,
    public val highlight: Color,
    @Deprecated("Use MessageTheme.backgroundColor instead", level = DeprecationLevel.ERROR)
    public val ownMessagesBackground: Color,
    @Deprecated("Use MessageTheme.backgroundColor instead", level = DeprecationLevel.ERROR)
    public val otherMessagesBackground: Color,
    @Deprecated("Use MessageTheme.deletedBackgroundColor instead", level = DeprecationLevel.ERROR)
    public val deletedMessagesBackground: Color,
    public val giphyMessageBackground: Color,
    public val threadSeparatorGradientStart: Color,
    public val threadSeparatorGradientEnd: Color,
    @Deprecated("Use MessageTheme.textStyle.color instead", level = DeprecationLevel.ERROR)
    public val ownMessageText: Color = textHighEmphasis,
    @Deprecated("Use MessageTheme.textStyle.color instead", level = DeprecationLevel.ERROR)
    public val otherMessageText: Color = textHighEmphasis,
    public val imageBackgroundMessageList: Color,
    public val imageBackgroundMediaGalleryPicker: Color,
    public val videoBackgroundMessageList: Color,
    public val videoBackgroundMediaGalleryPicker: Color,
    public val showMoreOverlay: Color,
    public val showMoreCountText: Color,
    @Deprecated("Use MessageTheme.quotedBackgroundColor instead", level = DeprecationLevel.ERROR)
    public val ownMessageQuotedBackground: Color = otherMessagesBackground,
    @Deprecated("Use MessageTheme.quotedBackgroundColor instead", level = DeprecationLevel.ERROR)
    public val otherMessageQuotedBackground: Color = ownMessagesBackground,
    @Deprecated("Use MessageTheme.quotedTextStyle.color instead", level = DeprecationLevel.ERROR)
    public val ownMessageQuotedText: Color = textHighEmphasis,
    @Deprecated("Use MessageTheme.quotedTextStyle.color instead", level = DeprecationLevel.ERROR)
    public val otherMessageQuotedText: Color = textHighEmphasis,
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
            overlay = colorResource(R.color.stream_compose_overlay_regular),
            overlayDark = colorResource(R.color.stream_compose_overlay_dark),
            primaryAccent = colorResource(R.color.stream_compose_primary_accent),
            errorAccent = colorResource(R.color.stream_compose_error_accent),
            infoAccent = colorResource(R.color.stream_compose_info_accent),
            highlight = colorResource(R.color.stream_compose_highlight),
            ownMessagesBackground = colorResource(R.color.stream_compose_borders),
            otherMessagesBackground = colorResource(R.color.stream_compose_bars_background),
            deletedMessagesBackground = colorResource(R.color.stream_compose_input_background),
            giphyMessageBackground = colorResource(R.color.stream_compose_bars_background),
            threadSeparatorGradientStart = colorResource(R.color.stream_compose_input_background),
            threadSeparatorGradientEnd = colorResource(R.color.stream_compose_app_background),
            ownMessageText = colorResource(R.color.stream_compose_text_high_emphasis),
            otherMessageText = colorResource(R.color.stream_compose_text_high_emphasis),
            imageBackgroundMessageList = colorResource(R.color.stream_compose_input_background),
            imageBackgroundMediaGalleryPicker = colorResource(R.color.stream_compose_app_background),
            videoBackgroundMessageList = colorResource(R.color.stream_compose_input_background),
            videoBackgroundMediaGalleryPicker = colorResource(R.color.stream_compose_app_background),
            showMoreOverlay = colorResource(R.color.stream_compose_show_more_overlay),
            showMoreCountText = colorResource(R.color.stream_compose_show_more_text),
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
            overlay = colorResource(R.color.stream_compose_overlay_regular_dark),
            overlayDark = colorResource(R.color.stream_compose_overlay_dark_dark),
            primaryAccent = colorResource(R.color.stream_compose_primary_accent_dark),
            errorAccent = colorResource(R.color.stream_compose_error_accent_dark),
            infoAccent = colorResource(R.color.stream_compose_info_accent_dark),
            highlight = colorResource(R.color.stream_compose_highlight_dark),
            ownMessagesBackground = colorResource(R.color.stream_compose_borders_dark),
            otherMessagesBackground = colorResource(R.color.stream_compose_bars_background_dark),
            deletedMessagesBackground = colorResource(R.color.stream_compose_input_background_dark),
            giphyMessageBackground = colorResource(R.color.stream_compose_bars_background_dark),
            threadSeparatorGradientStart = colorResource(R.color.stream_compose_input_background_dark),
            threadSeparatorGradientEnd = colorResource(R.color.stream_compose_app_background_dark),
            ownMessageText = colorResource(R.color.stream_compose_text_high_emphasis_dark),
            otherMessageText = colorResource(R.color.stream_compose_text_high_emphasis_dark),
            imageBackgroundMessageList = colorResource(R.color.stream_compose_input_background_dark),
            imageBackgroundMediaGalleryPicker = colorResource(R.color.stream_compose_app_background_dark),
            videoBackgroundMessageList = colorResource(R.color.stream_compose_input_background_dark),
            videoBackgroundMediaGalleryPicker = colorResource(R.color.stream_compose_app_background_dark),
            showMoreOverlay = colorResource(R.color.stream_compose_show_more_overlay_dark),
            showMoreCountText = colorResource(R.color.stream_compose_show_more_text_dark),
        )
    }
}
