/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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
 * Contains all the colors in our palette. Each color is used for various things and can be changed to
 * customize the app design style.
 * @param textHighEmphasis Used for main text and active icon status.
 * @param textHighEmphasisInverse Used for contrasting backgrounds or elements against the main text and active icon
 * status, providing better visibility and readability.
 * @param textLowEmphasis Used for secondary text, default icon state, deleted messages text and datestamp background.
 * @param disabled Used for disabled icons and empty states.
 * @param borders Used for borders, the background of self messages, selected items, pressed state, button dividers.
 * @param inputBackground Used for the input background, deleted messages, section headings.
 * @param barsBackground Used for button text, top and bottom bar background and other user messages.
 * @param overlay Used for general overlays and background when opening modals.
 * @param overlayDark Used for the date separator background color.
 * @param primaryAccent Used for selected icon state, call to actions, white buttons text and links.
 * @param errorAccent Used for error text labels, notification badges and disruptive action text and icons.
 * @param infoAccent Used for the online status.
 * @param highlight Used for message highlights.
 * @param giphyMessageBackground Used as a background for the ephemeral giphy messages.
 * @param threadSeparatorGradientStart Used as a start color for vertical gradient background in a thread separator.
 * @param threadSeparatorGradientEnd Used as an end color for vertical gradient background in a thread separator.
 * @param imageBackgroundMessageList Used to set the background colour of images inside the message list.
 * Most visible in placeholders before the images are loaded.
 * @param imageBackgroundMediaGalleryPicker Used to set the background colour of images inside the media gallery picker
 * in the media gallery preview screen. Most visible in placeholders before the images are loaded.
 * @param imageBackgroundMessageList Used to set the background colour of videos inside the message list.
 * Most visible in placeholders before the video previews are loaded.
 * @param videoBackgroundMediaGalleryPicker Used to set the background color of videos inside the media gallery picker
 * in the media gallery preview screen. Most visible in placeholders before the video previews are loaded.
 * @param showMoreOverlay The color of the overlay displaying how many more media attachments the message contains,
 * given it contains more than can be displayed in the message list media attachment preview.
 * @param showMoreCountText The color of the text displaying how many more media attachments the message contains,
 * given it contains more than can be displayed in the message list media attachment preview.
 * @param accentBlack Used for black accent elements.
 * @param accentError Used for destructive actions and error states.
 * @param accentNeutral Used for neutral accent for low-priority badges.
 * @param accentSuccess Used for success states and positive actions.
 * @param accentPrimary Used for main brand accent for interactive elements.
 * @param avatarPaletteBg1 Used for avatar background color.
 * @param avatarPaletteBg2 Used for avatar background color.
 * @param avatarPaletteBg3 Used for avatar background color.
 * @param avatarPaletteBg4 Used for avatar background color.
 * @param avatarPaletteBg5 Used for avatar background color.
 * @param avatarPaletteText1 Used for avatar text color.
 * @param avatarPaletteText2 Used for avatar text color.
 * @param avatarPaletteText3 Used for avatar text color.
 * @param avatarPaletteText4 Used for avatar text color.
 * @param avatarPaletteText5 Used for avatar text color.
 * @param backgroundCoreDisabled Used for disabled background in components like buttons.
 * @param backgroundCoreSurface Used for surface background in components like buttons.
 * @param backgroundCoreSurfaceSubtle Used for subtle surface backgrounds.
 * @param backgroundCoreInverse Used for elevated, transient, or high-attention UI surfaces that
 * sit on top of the default app background.
 * @param backgroundElevationElevation0 Used for base elevation surface backgrounds.
 * @param backgroundElevationElevation1 Slightly elevated surface backgrounds.
 * @param borderCoreImage Used for image frame border treatment.
 * @param borderCoreDefault Used for default border color.
 * @param borderCoreOnAccent Used for borders on accent backgrounds.
 * @param borderCoreOnDark Used for borders on dark backgrounds.
 * @param borderCoreSurfaceSubtle Used for very light separators.
 * @param borderCorePrimary Used for selected or active state border.
 * @param borderUtilityDisabled Used for disabled state borders.
 * @param brand50 Brand color at 50 intensity level.
 * @param brand100 Brand color at 100 intensity level.
 * @param brand150 Brand color at 150 intensity level.
 * @param brand200 Brand color at 200 intensity level.
 * @param brand300 Brand color at 300 intensity level.
 * @param brand400 Brand color at 400 intensity level.
 * @param brand500 Brand color at 500 intensity level.
 * @param brand600 Brand color at 600 intensity level.
 * @param brand700 Brand color at 700 intensity level.
 * @param brand800 Brand color at 800 intensity level.
 * @param brand900 Brand color at 900 intensity level.
 * @param textOnAccent Used for text displayed on accent/colored backgrounds.
 * @param textPrimary Used for main text color.
 * @param textSecondary Used for secondary text color with lower emphasis.
 * @param textTertiary Used for tertiary text color with lowest emphasis.
 * @param textDisabled Used for disabled text and icon color.
 * @param badgeBgInverse Used for badge background with inverse color scheme.
 * @param badgeBgOverlay Used for badge background when displayed as an overlay.
 * @param badgeText Used for badge text color.
 * @param badgeTextOnAccent Used for badge text color on accent backgrounds.
 * @param stateBgDisabled Used for disabled state background.
 * @param stateTextDisabled Used for disabled state text color.
 * @param appBackground Used for the default app background.
 * @param backgroundCoreSelected Used for selected state background.
 * @param backgroundElevationElevation2 Used for elevated surface backgrounds at elevation level 2.
 * @param buttonDestructiveBg Used for destructive button background.
 * @param buttonDestructiveBorder Used for destructive button border.
 * @param buttonDestructiveText Used for destructive button text.
 * @param buttonDestructiveTextOnAccent Used for destructive button text on accent backgrounds.
 * @param buttonPrimaryBg Used for primary button background.
 * @param buttonPrimaryBorder Used for primary button border.
 * @param buttonPrimaryText Used for primary button text.
 * @param buttonPrimaryTextOnAccent Used for primary button text on accent backgrounds.
 * @param buttonSecondaryBg Used for secondary button background.
 * @param buttonSecondaryBorder Used for secondary button border.
 * @param buttonSecondaryText Used for secondary button text.
 * @param buttonSecondaryTextOnAccent Used for secondary button text on accent backgrounds.
 * @param chatBgIncoming Used for incoming message bubble background.
 * @param chatBgOutgoing Used for outgoing message bubble background.
 * @param chatBgAttachmentIncoming Used for incoming message attachment background.
 * @param chatBgAttachmentOutgoing Used for outgoing message attachment background.
 * @param chatReplyIndicatorIncoming Used for the reply indicator color in incoming messages.
 * @param chatReplyIndicatorOutgoing Used for the reply indicator color in outgoing messages.
 * @param chatTextIncoming Used for incoming message text color in chat bubbles.
 * @param chatTextOutgoing Used for outgoing message text color in chat bubbles.
 * @param chatTextLink Used for link text color in chat messages.
 * @param chatTextMention Used for mention text color in chat messages.
 * @param chatTextTimestamp Used for timestamp text color in chat messages.
 * @param controlPlayControlBg Used for play control button background.
 * @param controlPlayControlIcon Used for play control button icon.
 * @param controlRemoveBg Used for remove control background.
 * @param controlRemoveBorder Used for remove control border.
 * @param controlRemoveIcon Used for remove control icon.
 * @param presenceBgOnline Used for online presence indicator.
 * @param presenceBgOffline Used for offline presence indicator.
 * @param presenceBorder Used for the outline around the presence dot.
 */
@Suppress("DEPRECATION_ERROR")
@Immutable
public data class StreamColors(
    public val textHighEmphasis: Color,
    public val textHighEmphasisInverse: Color,
    public val textLowEmphasis: Color,
    public val disabled: Color,
    public val borders: Color,
    public val inputBackground: Color,
    public val barsBackground: Color,
    public val overlay: Color,
    public val overlayDark: Color,
    public val primaryAccent: Color,
    public val errorAccent: Color,
    public val infoAccent: Color,
    public val highlight: Color,
    public val giphyMessageBackground: Color,
    public val threadSeparatorGradientStart: Color,
    public val threadSeparatorGradientEnd: Color,
    public val mediaShimmerBase: Color,
    public val mediaShimmerHighlights: Color,
    public val imageBackgroundMessageList: Color,
    public val imageBackgroundMediaGalleryPicker: Color,
    public val videoBackgroundMessageList: Color,
    public val videoBackgroundMediaGalleryPicker: Color,
    public val showMoreOverlay: Color,
    public val showMoreCountText: Color,

    // Design System semantic colors
    public val accentBlack: Color,
    public val accentError: Color,
    public val accentNeutral: Color,
    public val accentSuccess: Color,
    public val accentPrimary: Color,
    public val avatarBgPlaceholder: Color,
    public val avatarPaletteBg1: Color,
    public val avatarPaletteBg2: Color,
    public val avatarPaletteBg3: Color,
    public val avatarPaletteBg4: Color,
    public val avatarPaletteBg5: Color,
    public val avatarPaletteText1: Color,
    public val avatarPaletteText2: Color,
    public val avatarPaletteText3: Color,
    public val avatarPaletteText4: Color,
    public val avatarPaletteText5: Color,
    public val avatarTextPlaceholder: Color,
    public val backgroundCoreDisabled: Color,
    public val backgroundCoreSurface: Color,
    public val backgroundCoreSurfaceSubtle: Color,
    public val backgroundCoreInverse: Color,
    public val backgroundElevationElevation0: Color,
    public val backgroundElevationElevation1: Color,
    public val borderCoreImage: Color,
    public val borderCoreDefault: Color,
    public val borderCoreOnAccent: Color,
    public val borderCoreOnDark: Color,
    public val borderCoreOpacity25: Color,
    public val borderCoreSurfaceSubtle: Color,
    public val borderCorePrimary: Color,
    public val borderUtilityDisabled: Color,
    public val brand50: Color,
    public val brand100: Color,
    public val brand150: Color,
    public val brand200: Color,
    public val brand300: Color,
    public val brand400: Color,
    public val brand500: Color,
    public val brand600: Color,
    public val brand700: Color,
    public val brand800: Color,
    public val brand900: Color,
    public val textOnAccent: Color,
    public val textOnDark: Color,
    public val textPrimary: Color,
    public val textSecondary: Color,
    public val textTertiary: Color,
    public val textDisabled: Color,
    public val stateBgDisabled: Color,
    public val stateTextDisabled: Color,
    public val appBackground: Color = backgroundElevationElevation0,
    public val backgroundCoreSelected: Color = textPrimary.copy(alpha = .15f),
    public val backgroundElevationElevation2: Color,
    public val badgeBgDefault: Color = backgroundElevationElevation2,
    public val badgeBgInverse: Color = backgroundCoreInverse,
    public val badgeBgOverlay: Color,
    public val badgeText: Color = textPrimary,
    public val badgeTextOnAccent: Color = textOnAccent,
    public val badgeTextInverse: Color = textOnDark,
    public val buttonDestructiveBg: Color = accentError,
    public val buttonDestructiveBorder: Color = accentError,
    public val buttonDestructiveText: Color = accentError,
    public val buttonDestructiveTextOnAccent: Color = textOnAccent,
    public val buttonPrimaryBg: Color = accentPrimary,
    public val buttonPrimaryBorder: Color = brand200,
    public val buttonPrimaryText: Color = accentPrimary,
    public val buttonPrimaryTextOnAccent: Color = textOnAccent,
    public val buttonSecondaryBg: Color = backgroundCoreSurface,
    public val buttonSecondaryBorder: Color = borderCoreDefault,
    public val buttonSecondaryText: Color = textPrimary,
    public val buttonSecondaryTextOnAccent: Color = textPrimary,
    public val chatBgIncoming: Color,
    public val chatBgOutgoing: Color = brand100,
    public val chatBgAttachmentIncoming: Color,
    public val chatBgAttachmentOutgoing: Color,
    public val chatBorderOnChatIncoming: Color,
    public val chatBorderOnChatOutgoing: Color = brand300,
    public val chatPollProgressFillIncoming: Color,
    public val chatPollProgressTrackIncoming: Color,
    public val chatPollProgressFillOutgoing: Color,
    public val chatPollProgressTrackOutgoing: Color,
    public val chatReplyIndicatorIncoming: Color,
    public val chatReplyIndicatorOutgoing: Color,
    public val chatTextIncoming: Color = textPrimary,
    public val chatTextOutgoing: Color = textPrimary,
    public val chatTextLink: Color = accentPrimary,
    public val chatTextMention: Color = chatTextLink,
    public val chatTextTimestamp: Color = textTertiary,
    public val chatWaveformBar: Color = borderCoreOpacity25,
    public val chatWaveformBarPlaying: Color = accentPrimary,
    public val controlPlayControlBg: Color = accentBlack,
    public val controlPlayControlIcon: Color = textOnAccent,
    public val controlRadioCheckBgSelected: Color,
    public val controlRadioCheckBorder: Color = borderCoreDefault,
    public val controlRadioCheckIconSelected: Color,
    public val controlRemoveBg: Color,
    public val controlRemoveBorder: Color = borderCoreOnDark,
    public val controlRemoveIcon: Color,
    public val presenceBgOnline: Color = accentSuccess,
    public val presenceBgOffline: Color = accentNeutral,
    public val presenceBorder: Color,
) {

    public companion object {
        /**
         * Provides the default colors for the light mode of the app.
         *
         * @return A [StreamColors] instance holding our color palette.
         */
        @Suppress("LongMethod")
        @Composable
        public fun defaultColors(): StreamColors = StreamColors(
            textHighEmphasis = colorResource(R.color.stream_compose_text_high_emphasis),
            textHighEmphasisInverse = colorResource(R.color.stream_compose_text_high_emphasis_inverse),
            textLowEmphasis = colorResource(R.color.stream_compose_text_low_emphasis),
            disabled = colorResource(R.color.stream_compose_disabled),
            borders = colorResource(R.color.stream_compose_borders),
            inputBackground = colorResource(R.color.stream_compose_input_background),
            barsBackground = colorResource(R.color.stream_compose_bars_background),
            overlay = colorResource(R.color.stream_compose_overlay_regular),
            overlayDark = colorResource(R.color.stream_compose_overlay_dark),
            primaryAccent = colorResource(R.color.stream_compose_primary_accent),
            errorAccent = colorResource(R.color.stream_compose_error_accent),
            infoAccent = colorResource(R.color.stream_compose_info_accent),
            highlight = colorResource(R.color.stream_compose_highlight),
            giphyMessageBackground = colorResource(R.color.stream_compose_bars_background),
            threadSeparatorGradientStart = colorResource(R.color.stream_compose_input_background),
            threadSeparatorGradientEnd = colorResource(R.color.stream_compose_app_background),
            mediaShimmerBase = colorResource(R.color.stream_compose_input_background),
            mediaShimmerHighlights = colorResource(R.color.stream_compose_app_background),
            imageBackgroundMessageList = colorResource(R.color.stream_compose_input_background),
            imageBackgroundMediaGalleryPicker = colorResource(R.color.stream_compose_app_background),
            videoBackgroundMessageList = colorResource(R.color.stream_compose_input_background),
            videoBackgroundMediaGalleryPicker = colorResource(R.color.stream_compose_app_background),
            showMoreOverlay = colorResource(R.color.stream_compose_show_more_overlay),
            showMoreCountText = colorResource(R.color.stream_compose_show_more_text),

            accentBlack = StreamPrimitiveColors.baseBlack,
            accentError = StreamPrimitiveColors.red500,
            accentNeutral = StreamPrimitiveColors.slate500,
            accentPrimary = StreamPrimitiveColors.blue500,
            accentSuccess = StreamPrimitiveColors.green500,
            backgroundCoreDisabled = StreamPrimitiveColors.slate200,
            backgroundCoreSurface = StreamPrimitiveColors.slate100,
            backgroundCoreSurfaceSubtle = StreamPrimitiveColors.slate200,
            backgroundCoreInverse = StreamPrimitiveColors.slate900,
            backgroundElevationElevation0 = StreamPrimitiveColors.baseWhite,
            backgroundElevationElevation1 = StreamPrimitiveColors.baseWhite,
            backgroundElevationElevation2 = StreamPrimitiveColors.baseWhite,
            badgeBgOverlay = StreamPrimitiveColors.baseBlack.copy(alpha = .75f),
            borderCoreDefault = StreamPrimitiveColors.slate150,
            borderCoreImage = StreamPrimitiveColors.baseBlack.copy(alpha = .1f),
            borderCoreOnAccent = StreamPrimitiveColors.baseWhite,
            borderCoreOnDark = StreamPrimitiveColors.baseWhite,
            borderCoreOpacity25 = StreamPrimitiveColors.baseBlack.copy(alpha = 0.25f),
            borderCorePrimary = StreamPrimitiveColors.blue600,
            borderCoreSurfaceSubtle = StreamPrimitiveColors.slate200,
            borderUtilityDisabled = StreamPrimitiveColors.slate200,
            brand50 = StreamPrimitiveColors.blue50,
            brand100 = StreamPrimitiveColors.blue100,
            brand150 = StreamPrimitiveColors.blue150,
            brand200 = StreamPrimitiveColors.blue200,
            brand300 = StreamPrimitiveColors.blue300,
            brand400 = StreamPrimitiveColors.blue400,
            brand500 = StreamPrimitiveColors.blue500,
            brand600 = StreamPrimitiveColors.blue600,
            brand700 = StreamPrimitiveColors.blue700,
            brand800 = StreamPrimitiveColors.blue800,
            brand900 = StreamPrimitiveColors.blue900,
            textDisabled = StreamPrimitiveColors.slate400,
            textOnAccent = StreamPrimitiveColors.baseWhite,
            textOnDark = StreamPrimitiveColors.baseWhite,
            textPrimary = StreamPrimitiveColors.slate900,
            textSecondary = StreamPrimitiveColors.slate700,
            textTertiary = StreamPrimitiveColors.slate500,
            stateBgDisabled = StreamPrimitiveColors.slate200,
            stateTextDisabled = StreamPrimitiveColors.slate400,
            avatarBgPlaceholder = StreamPrimitiveColors.slate100,
            avatarPaletteBg1 = StreamPrimitiveColors.blue100,
            avatarPaletteBg2 = StreamPrimitiveColors.cyan100,
            avatarPaletteBg3 = StreamPrimitiveColors.green100,
            avatarPaletteBg4 = StreamPrimitiveColors.purple200,
            avatarPaletteBg5 = StreamPrimitiveColors.yellow200,
            avatarPaletteText1 = StreamPrimitiveColors.blue800,
            avatarPaletteText2 = StreamPrimitiveColors.cyan800,
            avatarPaletteText3 = StreamPrimitiveColors.green800,
            avatarPaletteText4 = StreamPrimitiveColors.purple800,
            avatarPaletteText5 = StreamPrimitiveColors.yellow800,
            avatarTextPlaceholder = StreamPrimitiveColors.slate500,
            chatBgIncoming = StreamPrimitiveColors.slate100,
            chatBgAttachmentIncoming = StreamPrimitiveColors.slate200,
            chatBgAttachmentOutgoing = StreamPrimitiveColors.blue200,
            chatBorderOnChatIncoming = StreamPrimitiveColors.slate300,
            chatPollProgressFillIncoming = StreamPrimitiveColors.slate500,
            chatPollProgressTrackIncoming = StreamPrimitiveColors.slate200,
            chatPollProgressFillOutgoing = StreamPrimitiveColors.blue500,
            chatPollProgressTrackOutgoing = StreamPrimitiveColors.blue200,
            chatReplyIndicatorIncoming = StreamPrimitiveColors.slate400,
            chatReplyIndicatorOutgoing = StreamPrimitiveColors.blue400,
            controlRadioCheckBgSelected = StreamPrimitiveColors.blue500,
            controlRadioCheckIconSelected = StreamPrimitiveColors.baseWhite,
            controlRemoveBg = StreamPrimitiveColors.slate900,
            controlRemoveBorder = StreamPrimitiveColors.baseWhite,
            controlRemoveIcon = StreamPrimitiveColors.baseWhite,
            presenceBorder = StreamPrimitiveColors.baseWhite,
        )

        /**
         * Provides the default colors for the dark mode of the app.
         *
         * @return A [StreamColors] instance holding our color palette.
         */
        @Suppress("LongMethod")
        @Composable
        public fun defaultDarkColors(): StreamColors = StreamColors(
            textHighEmphasis = colorResource(R.color.stream_compose_text_high_emphasis_dark),
            textHighEmphasisInverse = colorResource(R.color.stream_compose_text_high_emphasis_inverse_dark),
            textLowEmphasis = colorResource(R.color.stream_compose_text_low_emphasis_dark),
            disabled = colorResource(R.color.stream_compose_disabled_dark),
            borders = colorResource(R.color.stream_compose_borders_dark),
            inputBackground = colorResource(R.color.stream_compose_input_background_dark),
            appBackground = colorResource(R.color.stream_compose_app_background_dark),
            barsBackground = colorResource(R.color.stream_compose_bars_background_dark),
            overlay = colorResource(R.color.stream_compose_overlay_regular_dark),
            overlayDark = colorResource(R.color.stream_compose_overlay_dark_dark),
            primaryAccent = colorResource(R.color.stream_compose_primary_accent_dark),
            errorAccent = colorResource(R.color.stream_compose_error_accent_dark),
            infoAccent = colorResource(R.color.stream_compose_info_accent_dark),
            highlight = colorResource(R.color.stream_compose_highlight_dark),
            giphyMessageBackground = colorResource(R.color.stream_compose_bars_background_dark),
            threadSeparatorGradientStart = colorResource(R.color.stream_compose_input_background_dark),
            threadSeparatorGradientEnd = colorResource(R.color.stream_compose_app_background_dark),
            mediaShimmerBase = colorResource(R.color.stream_compose_input_background),
            mediaShimmerHighlights = colorResource(R.color.stream_compose_app_background),
            imageBackgroundMessageList = colorResource(R.color.stream_compose_input_background_dark),
            imageBackgroundMediaGalleryPicker = colorResource(R.color.stream_compose_app_background_dark),
            videoBackgroundMessageList = colorResource(R.color.stream_compose_input_background_dark),
            videoBackgroundMediaGalleryPicker = colorResource(R.color.stream_compose_app_background_dark),
            showMoreOverlay = colorResource(R.color.stream_compose_show_more_overlay_dark),
            showMoreCountText = colorResource(R.color.stream_compose_show_more_text_dark),

            accentBlack = StreamPrimitiveColors.baseBlack,
            accentError = StreamPrimitiveColors.red400,
            accentNeutral = StreamPrimitiveColors.neutral500,
            accentPrimary = StreamPrimitiveColors.blue400,
            accentSuccess = StreamPrimitiveColors.green400,
            backgroundCoreDisabled = StreamPrimitiveColors.slate800,
            backgroundCoreSurface = StreamPrimitiveColors.neutral700,
            backgroundCoreSurfaceSubtle = StreamPrimitiveColors.neutral800,
            backgroundCoreInverse = StreamPrimitiveColors.neutral50,
            backgroundElevationElevation0 = StreamPrimitiveColors.baseBlack,
            backgroundElevationElevation1 = StreamPrimitiveColors.neutral900,
            backgroundElevationElevation2 = StreamPrimitiveColors.neutral800,
            borderCoreDefault = StreamPrimitiveColors.neutral600,
            borderCoreImage = StreamPrimitiveColors.baseWhite.copy(alpha = .2f),
            borderCoreOnAccent = StreamPrimitiveColors.baseWhite,
            borderCoreOnDark = StreamPrimitiveColors.neutral900,
            borderCoreOpacity25 = StreamPrimitiveColors.baseWhite.copy(alpha = 0.25f),
            borderCorePrimary = StreamPrimitiveColors.blue300,
            borderCoreSurfaceSubtle = StreamPrimitiveColors.neutral700,
            borderUtilityDisabled = StreamPrimitiveColors.neutral800,
            brand50 = StreamPrimitiveColors.blue900,
            brand100 = StreamPrimitiveColors.blue800,
            brand150 = StreamPrimitiveColors.blue700,
            brand200 = StreamPrimitiveColors.blue700,
            brand300 = StreamPrimitiveColors.blue600,
            brand400 = StreamPrimitiveColors.blue500,
            brand500 = StreamPrimitiveColors.blue400,
            brand600 = StreamPrimitiveColors.blue300,
            brand700 = StreamPrimitiveColors.blue200,
            brand800 = StreamPrimitiveColors.blue150,
            brand900 = StreamPrimitiveColors.blue100,
            textDisabled = StreamPrimitiveColors.slate600,
            textOnAccent = StreamPrimitiveColors.baseWhite,
            textOnDark = StreamPrimitiveColors.neutral900,
            textPrimary = StreamPrimitiveColors.neutral50,
            textSecondary = StreamPrimitiveColors.neutral300,
            textTertiary = StreamPrimitiveColors.neutral400,
            badgeBgOverlay = StreamPrimitiveColors.baseBlack.copy(alpha = .75f),
            stateBgDisabled = StreamPrimitiveColors.slate800,
            stateTextDisabled = StreamPrimitiveColors.slate600,
            avatarBgPlaceholder = StreamPrimitiveColors.neutral700,
            avatarPaletteBg1 = StreamPrimitiveColors.blue800,
            avatarPaletteBg2 = StreamPrimitiveColors.cyan800,
            avatarPaletteBg3 = StreamPrimitiveColors.green800,
            avatarPaletteBg4 = StreamPrimitiveColors.purple800,
            avatarPaletteBg5 = StreamPrimitiveColors.yellow800,
            avatarPaletteText1 = StreamPrimitiveColors.blue100,
            avatarPaletteText2 = StreamPrimitiveColors.cyan100,
            avatarPaletteText3 = StreamPrimitiveColors.green100,
            avatarPaletteText4 = StreamPrimitiveColors.purple100,
            avatarPaletteText5 = StreamPrimitiveColors.yellow100,
            avatarTextPlaceholder = StreamPrimitiveColors.neutral400,
            chatBgIncoming = StreamPrimitiveColors.neutral800,
            chatBgAttachmentIncoming = StreamPrimitiveColors.neutral700,
            chatBgAttachmentOutgoing = StreamPrimitiveColors.blue700,
            chatBorderOnChatIncoming = StreamPrimitiveColors.slate600,
            chatPollProgressFillIncoming = StreamPrimitiveColors.baseWhite,
            chatPollProgressTrackIncoming = StreamPrimitiveColors.neutral600,
            chatPollProgressFillOutgoing = StreamPrimitiveColors.baseWhite,
            chatPollProgressTrackOutgoing = StreamPrimitiveColors.blue400,
            chatReplyIndicatorIncoming = StreamPrimitiveColors.neutral500,
            chatReplyIndicatorOutgoing = StreamPrimitiveColors.blue300,
            controlRadioCheckBgSelected = StreamPrimitiveColors.baseWhite,
            controlRadioCheckIconSelected = StreamPrimitiveColors.blue500,
            controlRemoveBg = StreamPrimitiveColors.neutral800,
            controlRemoveIcon = StreamPrimitiveColors.baseWhite,
            presenceBorder = StreamPrimitiveColors.baseBlack,
        )
    }
}

@Suppress("MagicNumber")
internal object StreamPrimitiveColors {
    val baseBlack = Color(0xFF000000)
    val baseTransparent = Color(0x00000000)
    val baseWhite = Color(0xFFFFFFFF)
    val blue50 = Color(0xFFF3F7FF)
    val blue100 = Color(0xFFD2E3FF)
    val blue150 = Color(0xFFC3D9FF)
    val blue200 = Color(0xFFA6C4FF)
    val blue300 = Color(0xFF7AA7FF)
    val blue400 = Color(0xFF4E8BFF)
    val blue500 = Color(0xFF005FFF)
    val blue600 = Color(0xFF0052CE)
    val blue700 = Color(0xFF0042A3)
    val blue800 = Color(0xFF003179)
    val blue900 = Color(0xFF091A3B)
    val cyan100 = Color(0xFFD7F7FB)
    val cyan800 = Color(0xFF1C8791)
    val green100 = Color(0xFFC9FCE7)
    val green400 = Color(0xFF59E9B5)
    val green500 = Color(0xFF00E2A1)
    val green800 = Color(0xFF006548)
    val neutral50 = Color(0xFFF7F7F7)
    val neutral100 = Color(0xFFEFEFEF)
    val neutral300 = Color(0xFFABABAB)
    val neutral400 = Color(0xFF8F8F8F)
    val neutral500 = Color(0xFF7F7F7F)
    val neutral600 = Color(0xFF565656)
    val neutral700 = Color(0xFF4A4A4A)
    val neutral900 = Color(0xFF1C1C1C)
    val neutral800 = Color(0xFF323232)
    val neutral900 = Color(0xFF1C1C1C)
    val purple100 = Color(0xFFEBDEFD)
    val purple200 = Color(0xFFD8BFFC)
    val purple800 = Color(0xFF6640AB)
    val red400 = Color(0xFFE6756C)
    val red500 = Color(0xFFD92F26)
    val slate50 = Color(0xFFF6F8FA)
    val slate100 = Color(0xFFF2F4F6)
    val slate150 = Color(0xFFD5DBE1)
    val slate200 = Color(0xFFE2E6EA)
    val slate300 = Color(0xFFA3ACBA)
    val slate400 = Color(0xFFB8BEC4)
    val slate500 = Color(0xFF687385)
    val slate600 = Color(0xFF838990)
    val slate700 = Color(0xFF4A4A4A)
    val slate800 = Color(0xFF50565D)
    val slate900 = Color(0xFF1E252B)
    val yellow100 = Color(0xFFFFF1C2)
    val yellow200 = Color(0xFFFFE8A0)
    val yellow800 = Color(0xFF9F7700)
}
