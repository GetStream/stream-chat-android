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

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The Stream Chat Design System namespace.
 *
 * Provides all design tokens for theming Chat SDK components:
 * - [Colors] -- semantic color tokens
 * - [Typography] -- text styles
 * - [Dimens] -- component dimensions
 *
 * Use via [ChatTheme]:
 * ```
 * ChatTheme(
 *     colors = StreamDesign.Colors.default().copy(accentPrimary = ...),
 *     typography = StreamDesign.Typography.default(fontFamily = ...),
 * ) { content() }
 * ```
 */
public object StreamDesign {

    /**
     * Contains all the colors in our palette. Each color is used for various things and can be
     * changed to customize the app design style.
     *
     * @param accentBlack Used for black accent elements.
     * @param accentError Used for destructive actions and error states.
     * @param accentNeutral Used for neutral accent for low-priority badges.
     * @param accentSuccess Used for success states and positive actions.
     * @param accentPrimary Used for main brand accent for interactive elements.
     * @param avatarBgPlaceholder Used for avatar placeholder background color.
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
     * @param avatarTextPlaceholder Used for avatar placeholder text color.
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
     * @param borderCoreOpacity25 Used for borders with 25% opacity.
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
     * @param textOnDark Used for text displayed on dark backgrounds.
     * @param textPrimary Used for main text color.
     * @param textSecondary Used for secondary text color with lower emphasis.
     * @param textTertiary Used for tertiary text color with lowest emphasis.
     * @param textDisabled Used for disabled text and icon color.
     * @param stateBgDisabled Used for disabled state background.
     * @param stateTextDisabled Used for disabled state text color.
     * @param appBackground Used for the default app background.
     * @param backgroundCoreSelected Used for selected state background.
     * @param backgroundElevationElevation2 Used for elevated surface backgrounds at elevation level 2.
     * @param badgeBgDefault Used for badge background.
     * @param badgeBgInverse Used for badge background with inverse color scheme.
     * @param badgeBgOverlay Used for badge background when displayed as an overlay.
     * @param badgeText Used for badge text color.
     * @param badgeTextOnAccent Used for badge text color on accent backgrounds.
     * @param badgeTextInverse Used for badge text color on inverse backgrounds.
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
     * @param chatBorderOnChatIncoming Used for border on incoming message bubbles.
     * @param chatBorderOnChatOutgoing Used for border on outgoing message bubbles.
     * @param chatPollProgressFillIncoming Used for incoming poll progress fill color.
     * @param chatPollProgressTrackIncoming Used for incoming poll progress track color.
     * @param chatPollProgressFillOutgoing Used for outgoing poll progress fill color.
     * @param chatPollProgressTrackOutgoing Used for outgoing poll progress track color.
     * @param chatReplyIndicatorIncoming Used for the reply indicator color in incoming messages.
     * @param chatReplyIndicatorOutgoing Used for the reply indicator color in outgoing messages.
     * @param chatTextIncoming Used for incoming message text color in chat bubbles.
     * @param chatTextOutgoing Used for outgoing message text color in chat bubbles.
     * @param chatTextLink Used for link text color in chat messages.
     * @param chatTextMention Used for mention text color in chat messages.
     * @param chatTextTimestamp Used for timestamp text color in chat messages.
     * @param chatWaveformBar Used for audio waveform bar color.
     * @param chatWaveformBarPlaying Used for audio waveform bar color when playing.
     * @param chipText Used for chip text color.
     * @param controlPlayControlBg Used for play control button background.
     * @param controlPlayControlIcon Used for play control button icon.
     * @param controlRadioCheckBgSelected Used for selected radio/check background.
     * @param controlRadioCheckBorder Used for radio/check border.
     * @param controlRadioCheckIconSelected Used for selected radio/check icon.
     * @param controlRemoveBg Used for remove control background.
     * @param controlRemoveBorder Used for remove control border.
     * @param controlRemoveIcon Used for remove control icon.
     * @param presenceBgOnline Used for online presence indicator.
     * @param presenceBgOffline Used for offline presence indicator.
     * @param presenceBorder Used for the outline around the presence dot.
     * @param overlayBackground Used for regular overlay/scrim backgrounds.
     * @param overlayBackgroundDark Used for darker overlay/scrim backgrounds.
     * @param highlightBackground Used for highlight backgrounds such as message highlights.
     */
    @Immutable
    public data class Colors(
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
        public val chipText: Color = brand900,
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
        public val overlayBackground: Color,
        public val overlayBackgroundDark: Color,
        public val highlightBackground: Color,
    ) {

        public companion object {
            /**
             * Provides the default colors for the light mode of the app.
             *
             * @return A [Colors] instance holding our color palette.
             */
            @Suppress("LongMethod")
            public fun default(): Colors = Colors(
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
                overlayBackground = StreamPrimitiveColors.baseBlack.copy(alpha = 0.5f),
                overlayBackgroundDark = StreamPrimitiveColors.baseBlack.copy(alpha = 0.6f),
                highlightBackground = StreamPrimitiveColors.highlightLight,
            )

            /**
             * Provides the default colors for the dark mode of the app.
             *
             * @return A [Colors] instance holding our color palette.
             */
            @Suppress("LongMethod")
            public fun defaultDark(): Colors = Colors(
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
                overlayBackground = StreamPrimitiveColors.baseBlack.copy(alpha = 0.2f),
                overlayBackgroundDark = StreamPrimitiveColors.baseWhite.copy(alpha = 0.6f),
                highlightBackground = StreamPrimitiveColors.highlightDark,
            )
        }
    }

    /**
     * Contains all the typography we provide for our components.
     *
     * @param bodyDefault Used for body text, like message text.
     * @param bodyEmphasis Used for emphasized body text requiring visual prominence.
     * @param captionDefault Style for captions and supplementary information.
     * @param captionEmphasis Style for emphasized captions that require attention.
     * @param headingSmall Style for small headings.
     * @param headingMedium Style for medium headings.
     * @param headingLarge Style for large, prominent headings.
     * @param metadataDefault Style for metadata and secondary information.
     * @param metadataEmphasis Style for emphasized metadata in secondary content areas.
     * @param numericMedium Style for medium-sized numeric indicators, like the unread count.
     * @param numericLarge Style for large numeric indicators.
     * @param numericExtraLarge Style for extra-large numeric indicators.
     */
    @Immutable
    public data class Typography(
        public val bodyDefault: TextStyle,
        public val bodyEmphasis: TextStyle,
        public val captionDefault: TextStyle,
        public val captionEmphasis: TextStyle,
        public val headingSmall: TextStyle,
        public val headingMedium: TextStyle,
        public val headingLarge: TextStyle,
        public val metadataDefault: TextStyle,
        public val metadataEmphasis: TextStyle,
        public val numericMedium: TextStyle,
        public val numericLarge: TextStyle,
        public val numericExtraLarge: TextStyle,
    ) {

        public companion object {
            /**
             * Builds the default typography set for our theme, with the ability to customize the
             * font family.
             *
             * @param fontFamily The font that the users want to use for the app.
             * @return [Typography] that holds all the default text styles that we support.
             */
            @Suppress("LongMethod")
            public fun default(fontFamily: FontFamily? = null): Typography = Typography(
                bodyDefault = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = StreamTokens.fontWeightRegular,
                    fontSize = StreamTokens.fontSizeMd,
                    lineHeight = StreamTokens.lineHeightNormal,
                ),
                bodyEmphasis = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = StreamTokens.fontWeightSemiBold,
                    fontSize = StreamTokens.fontSizeMd,
                    lineHeight = StreamTokens.lineHeightNormal,
                ),
                captionDefault = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = StreamTokens.fontWeightRegular,
                    fontSize = StreamTokens.fontSizeSm,
                    lineHeight = StreamTokens.lineHeightTight,
                ),
                captionEmphasis = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = StreamTokens.fontWeightSemiBold,
                    fontSize = StreamTokens.fontSizeSm,
                    lineHeight = StreamTokens.lineHeightTight,
                ),
                headingSmall = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = StreamTokens.fontWeightSemiBold,
                    fontSize = StreamTokens.fontSizeMd,
                    lineHeight = StreamTokens.lineHeightNormal,
                ),
                headingMedium = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = StreamTokens.fontWeightSemiBold,
                    fontSize = StreamTokens.fontSizeLg,
                    lineHeight = StreamTokens.lineHeightRelaxed,
                ),
                headingLarge = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = StreamTokens.fontWeightSemiBold,
                    fontSize = StreamTokens.fontSizeXl,
                    lineHeight = StreamTokens.lineHeightRelaxed,
                ),
                metadataDefault = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = StreamTokens.fontWeightRegular,
                    fontSize = StreamTokens.fontSizeXs,
                    lineHeight = StreamTokens.lineHeightTight,
                ),
                metadataEmphasis = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = StreamTokens.fontWeightSemiBold,
                    fontSize = StreamTokens.fontSizeXs,
                    lineHeight = StreamTokens.lineHeightTight,
                ),
                numericMedium = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = StreamTokens.fontWeightBold,
                    fontSize = StreamTokens.fontSize2xs,
                    lineHeight = StreamTokens.lineHeightTighter,
                ),
                numericLarge = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = StreamTokens.fontWeightBold,
                    fontSize = StreamTokens.fontSizeXs,
                ),
                numericExtraLarge = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = StreamTokens.fontWeightBold,
                    fontSize = StreamTokens.fontSizeSm,
                    lineHeight = StreamTokens.lineHeightTighter,
                ),
            )
        }
    }

    /**
     * Contains all the dimens we provide for our components.
     *
     * @param channelItemVerticalPadding The vertical content padding inside channel list item.
     * @param channelItemHorizontalPadding The horizontal content padding inside channel list item.
     * @param channelAvatarSize The size of channel avatar.
     * @param selectedChannelMenuUserItemWidth The width of a member tile in the selected channel menu.
     * @param selectedChannelMenuUserItemHorizontalPadding The padding inside a member tile in the
     * selected channel menu.
     * @param selectedChannelMenuUserItemAvatarSize The size of a member avatar in the selected
     * channel menu.
     * @param attachmentsContentImageWidth The width of image attachments in the message list.
     * @param attachmentsContentGiphyWidth The width of Giphy attachments in the message list.
     * @param attachmentsContentGiphyHeight The height of Giphy attachments in the message list.
     * @param attachmentsContentLinkWidth The width of link attachments in the message list.
     * @param attachmentsContentFileWidth The width of file attachments in the message list.
     * @param attachmentsContentFileUploadWidth The width of uploading file attachments in the
     * message list.
     * @param attachmentsContentUnsupportedWidth The width of unsupported attachments in the
     * message list.
     * @param threadSeparatorVerticalPadding The vertical content padding inside thread separator.
     * @param threadSeparatorTextVerticalPadding The vertical padding inside thread separator text.
     * @param suggestionListMaxHeight The maximum height of the suggestion list popup.
     * @param suggestionListPadding The outer padding of the suggestion list popup.
     * @param suggestionListElevation The elevation of the suggestion list popup.
     * @param mentionSuggestionItemHorizontalPadding The horizontal content padding inside mention
     * list item.
     * @param mentionSuggestionItemVerticalPadding The vertical content padding inside mention
     * list item.
     * @param mentionSuggestionItemAvatarSize The size of a channel avatar in the suggestion list
     * popup.
     * @param commandSuggestionItemHorizontalPadding The horizontal content padding inside command
     * list item.
     * @param commandSuggestionItemVerticalPadding The vertical content padding inside command
     * list item.
     * @param commandSuggestionItemIconSize The size of a command icon in the suggestion list popup.
     * @param threadParticipantItemSize The size of thread participant avatar items.
     * @param userReactionsMaxHeight The max height of the message reactions section when we click
     * on message reactions.
     * @param userReactionItemWidth The width of user reaction item.
     * @param userReactionItemAvatarSize The size of a user avatar in the user reaction item.
     * @param userReactionItemIconSize The size of a reaction icon in the user reaction item.
     * @param headerElevation The elevation of the headers.
     * @param messageItemMaxWidth The max width of message items inside message list.
     * @param quotedMessageTextVerticalPadding The vertical padding of text inside quoted message.
     * @param quotedMessageTextHorizontalPadding The horizontal padding of text inside quoted
     * message.
     * @param quotedMessageAttachmentPreviewSize The size of the quoted message attachment preview.
     * @param quotedMessageAttachmentTopPadding The top padding of the quoted message attachment.
     * @param quotedMessageAttachmentBottomPadding The bottom padding of the quoted message
     * attachment.
     * @param quotedMessageAttachmentStartPadding The start padding of the quoted message
     * attachment.
     * @param quotedMessageAttachmentEndPadding The end padding of the quoted message attachment.
     * @param quotedMessageAttachmentSpacerHorizontal The horizontal spacing between quoted message
     * attachment components.
     * @param quotedMessageAttachmentSpacerVertical The vertical spacing between quoted message
     * attachment components.
     * @param groupAvatarInitialsXOffset The x offset of the user initials inside avatar when there
     * are more than two users.
     * @param groupAvatarInitialsYOffset The y offset of the user initials inside avatar when there
     * are more than two users.
     * @param attachmentsPickerHeight The height of the attachments picker.
     * @param attachmentsSystemPickerHeight The height of the system attachments picker.
     * @param attachmentsContentImageMaxHeight The maximum height an image attachment will expand to.
     * @param attachmentsContentGiphyMaxWidth The maximum width a Giphy attachment will expand to.
     * @param attachmentsContentGiphyMaxHeight The maximum height a Giphy attachment will expand to.
     * @param attachmentsContentVideoMaxHeight The maximum height video attachment will expand to.
     * @param attachmentsContentMediaGridSpacing The spacing between media preview tiles in the
     * message list.
     * @param attachmentsContentVideoWidth The width of media attachment previews in the message
     * list.
     * @param attachmentsContentGroupPreviewWidth The width of the container displaying media
     * previews tiled in a group.
     * @param attachmentsContentGroupPreviewHeight The height of the container displaying media
     * previews tiled in a group.
     * @param pollOptionInputHeight The height of the poll option input field.
     * @param messageComposerShadowElevation The elevation of the message composer shadow.
     */
    @Immutable
    public data class Dimens(
        public val channelItemVerticalPadding: Dp,
        public val channelItemHorizontalPadding: Dp,
        public val channelAvatarSize: Dp,
        public val selectedChannelMenuUserItemWidth: Dp,
        public val selectedChannelMenuUserItemHorizontalPadding: Dp,
        public val selectedChannelMenuUserItemAvatarSize: Dp,
        public val attachmentsContentImageWidth: Dp,
        public val attachmentsContentGiphyWidth: Dp,
        public val attachmentsContentGiphyHeight: Dp,
        public val attachmentsContentLinkWidth: Dp,
        public val attachmentsContentFileWidth: Dp,
        public val attachmentsContentFileUploadWidth: Dp,
        public val attachmentsContentUnsupportedWidth: Dp,
        public val threadSeparatorVerticalPadding: Dp,
        public val threadSeparatorTextVerticalPadding: Dp,
        public val suggestionListMaxHeight: Dp,
        public val suggestionListPadding: Dp,
        public val suggestionListElevation: Dp,
        public val mentionSuggestionItemHorizontalPadding: Dp,
        public val mentionSuggestionItemVerticalPadding: Dp,
        public val mentionSuggestionItemAvatarSize: Dp,
        public val commandSuggestionItemHorizontalPadding: Dp,
        public val commandSuggestionItemVerticalPadding: Dp,
        public val commandSuggestionItemIconSize: Dp,
        public val threadParticipantItemSize: Dp,
        public val userReactionsMaxHeight: Dp,
        public val userReactionItemWidth: Dp,
        public val userReactionItemAvatarSize: Dp,
        public val userReactionItemIconSize: Dp,
        public val headerElevation: Dp,
        public val messageItemMaxWidth: Dp,
        public val quotedMessageTextVerticalPadding: Dp,
        public val quotedMessageTextHorizontalPadding: Dp,
        public val quotedMessageAttachmentPreviewSize: Dp,
        public val quotedMessageAttachmentTopPadding: Dp,
        public val quotedMessageAttachmentBottomPadding: Dp,
        public val quotedMessageAttachmentStartPadding: Dp,
        public val quotedMessageAttachmentEndPadding: Dp,
        public val quotedMessageAttachmentSpacerHorizontal: Dp,
        public val quotedMessageAttachmentSpacerVertical: Dp,
        public val groupAvatarInitialsXOffset: Dp,
        public val groupAvatarInitialsYOffset: Dp,
        public val attachmentsPickerHeight: Dp,
        public val attachmentsSystemPickerHeight: Dp,
        public val attachmentsContentImageMaxHeight: Dp,
        public val attachmentsContentGiphyMaxWidth: Dp = attachmentsContentGiphyWidth,
        public val attachmentsContentGiphyMaxHeight: Dp = attachmentsContentGiphyHeight,
        public val attachmentsContentVideoMaxHeight: Dp,
        public val attachmentsContentMediaGridSpacing: Dp,
        public val attachmentsContentVideoWidth: Dp,
        public val attachmentsContentGroupPreviewWidth: Dp,
        public val attachmentsContentGroupPreviewHeight: Dp,
        public val pollOptionInputHeight: Dp,
        public val messageComposerShadowElevation: Dp,
    ) {

        public companion object {
            /**
             * Builds the default dimensions for our theme.
             *
             * @return A [Dimens] instance holding our default dimensions.
             */
            public fun default(): Dimens = Dimens(
                channelItemVerticalPadding = 12.dp,
                channelItemHorizontalPadding = 8.dp,
                channelAvatarSize = 40.dp,
                selectedChannelMenuUserItemWidth = 80.dp,
                selectedChannelMenuUserItemHorizontalPadding = 8.dp,
                selectedChannelMenuUserItemAvatarSize = 64.dp,
                attachmentsContentImageWidth = 250.dp,
                attachmentsContentGiphyWidth = 250.dp,
                attachmentsContentGiphyHeight = 200.dp,
                attachmentsContentLinkWidth = 250.dp,
                attachmentsContentFileWidth = 250.dp,
                attachmentsContentFileUploadWidth = 250.dp,
                attachmentsContentUnsupportedWidth = 250.dp,
                threadSeparatorVerticalPadding = 8.dp,
                threadSeparatorTextVerticalPadding = 2.dp,
                suggestionListMaxHeight = 256.dp,
                suggestionListPadding = 8.dp,
                suggestionListElevation = 4.dp,
                mentionSuggestionItemHorizontalPadding = 16.dp,
                mentionSuggestionItemVerticalPadding = 8.dp,
                mentionSuggestionItemAvatarSize = 40.dp,
                commandSuggestionItemHorizontalPadding = 8.dp,
                commandSuggestionItemVerticalPadding = 8.dp,
                commandSuggestionItemIconSize = 24.dp,
                threadParticipantItemSize = 16.dp,
                userReactionsMaxHeight = 256.dp,
                userReactionItemWidth = 80.dp,
                userReactionItemIconSize = 24.dp,
                userReactionItemAvatarSize = 64.dp,
                headerElevation = 4.dp,
                messageItemMaxWidth = 250.dp,
                quotedMessageTextHorizontalPadding = 8.dp,
                quotedMessageTextVerticalPadding = 6.dp,
                quotedMessageAttachmentPreviewSize = 36.dp,
                quotedMessageAttachmentBottomPadding = 6.dp,
                quotedMessageAttachmentTopPadding = 6.dp,
                quotedMessageAttachmentStartPadding = 8.dp,
                quotedMessageAttachmentEndPadding = 0.dp,
                quotedMessageAttachmentSpacerHorizontal = 8.dp,
                quotedMessageAttachmentSpacerVertical = 2.dp,
                groupAvatarInitialsXOffset = 1.5.dp,
                groupAvatarInitialsYOffset = 2.5.dp,
                attachmentsPickerHeight = 350.dp,
                attachmentsSystemPickerHeight = 72.dp,
                attachmentsContentImageMaxHeight = 600.dp,
                attachmentsContentVideoMaxHeight = 400.dp,
                attachmentsContentMediaGridSpacing = 2.dp,
                attachmentsContentVideoWidth = 250.dp,
                attachmentsContentGroupPreviewWidth = 250.dp,
                attachmentsContentGroupPreviewHeight = 196.dp,
                pollOptionInputHeight = 56.dp,
                messageComposerShadowElevation = 24.dp,
            )
        }
    }
}
