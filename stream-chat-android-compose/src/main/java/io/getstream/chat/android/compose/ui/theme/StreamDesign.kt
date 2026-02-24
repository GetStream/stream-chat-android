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

/**
 * The Stream Chat Design System namespace.
 *
 * Provides all design tokens for theming Chat SDK components:
 * - [Colors] -- semantic color tokens
 * - [Typography] -- text styles
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
     * @param backgroundCoreSurfaceStrong Stronger section background for prominent surface areas.
     * @param backgroundCoreInverse Used for elevated, transient, or high-attention UI surfaces that
     * sit on top of the default app background.
     * @param backgroundElevationElevation0 Used for base elevation surface backgrounds.
     * @param backgroundElevationElevation1 Slightly elevated surface backgrounds.
     * @param borderCoreOpacity10 Used for 10% opacity border treatment (e.g. image frames).
     * @param borderCoreDefault Used for default border color.
     * @param borderCoreStrong Stronger surface border with higher contrast.
     * @param borderCoreOnAccent Used for borders on accent backgrounds.
     * @param borderCoreOnDark Used for borders on dark backgrounds.
     * @param borderCoreOpacity25 Used for borders with 25% opacity.
     * @param borderCoreSubtle Used for subtle/very light separators.
     * @param borderUtilitySelected Used for selected or active state border (focus ring).
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
     * @param backgroundCoreSelected Used for selected state background.
     * @param backgroundElevationElevation2 Used for elevated surface backgrounds at elevation level 2.
     * @param backgroundElevationElevation3 Popover surface backgrounds.
     * @param backgroundElevationElevation4 Dialog and modal surface backgrounds.
     * @param badgeBgOverlay Used for badge background when displayed as an overlay.
     * @param chatBgIncoming Used for incoming message bubble background.
     * @param chatBgAttachmentIncoming Used for incoming message attachment background.
     * @param chatBgAttachmentOutgoing Used for outgoing message attachment background.
     * @param chatBorderOnChatIncoming Used for border on incoming message bubbles.
     * @param chatPollProgressFillIncoming Used for incoming poll progress fill color.
     * @param chatPollProgressTrackIncoming Used for incoming poll progress track color.
     * @param chatPollProgressFillOutgoing Used for outgoing poll progress fill color.
     * @param chatPollProgressTrackOutgoing Used for outgoing poll progress track color.
     * @param chatReplyIndicatorIncoming Used for the reply indicator color in incoming messages.
     * @param chatReplyIndicatorOutgoing Used for the reply indicator color in outgoing messages.
     * @param controlRadioCheckBgSelected Used for selected radio/check background.
     * @param controlRadioCheckIconSelected Used for selected radio/check icon.
     * @param controlRemoveBg Used for remove control background.
     * @param controlRemoveIcon Used for remove control icon.
     * @param presenceBorder Used for the outline around the presence dot.
     * @param backgroundCoreScrim Used for dimmed scrim backgrounds (e.g. behind modals).
     * @param backgroundCoreOverlayDark Used for dark overlay backgrounds on media/badges.
     * @param backgroundCoreHighlight Used for highlight backgrounds (e.g. message focus/pin).
     * @param skeletonLoadingHighlight Shimmer highlight color for skeleton loading gradients.
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
        public val backgroundCoreSurfaceStrong: Color,
        public val backgroundCoreInverse: Color,
        public val backgroundElevationElevation0: Color,
        public val backgroundElevationElevation1: Color,
        public val borderCoreOpacity10: Color,
        public val borderCoreDefault: Color,
        public val borderCoreStrong: Color,
        public val borderCoreOnAccent: Color,
        public val borderCoreOnDark: Color,
        public val borderCoreOpacity25: Color,
        public val borderCoreSubtle: Color,
        public val borderUtilitySelected: Color,
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
        public val backgroundCoreSelected: Color = textPrimary.copy(alpha = .15f),
        public val backgroundElevationElevation2: Color,
        public val backgroundElevationElevation3: Color,
        public val backgroundElevationElevation4: Color,
        public val badgeBgOverlay: Color,
        public val chatBgIncoming: Color,
        public val chatBgAttachmentIncoming: Color,
        public val chatBgAttachmentOutgoing: Color,
        public val chatBorderOnChatIncoming: Color,
        public val chatPollProgressFillIncoming: Color,
        public val chatPollProgressTrackIncoming: Color,
        public val chatPollProgressFillOutgoing: Color,
        public val chatPollProgressTrackOutgoing: Color,
        public val chatReplyIndicatorIncoming: Color,
        public val chatReplyIndicatorOutgoing: Color,
        public val controlRadioCheckBgSelected: Color,
        public val controlRadioCheckIconSelected: Color,
        public val controlRemoveBg: Color,
        public val controlRemoveIcon: Color,
        public val presenceBorder: Color,
        public val backgroundCoreScrim: Color,
        public val backgroundCoreOverlayDark: Color,
        public val backgroundCoreHighlight: Color,
        public val skeletonLoadingHighlight: Color,
    ) {

        /** Badge background for error states. */
        public val badgeBgError: Color = accentError

        /** Badge background for neutral states. */
        public val badgeBgNeutral: Color = accentNeutral

        /** Badge background for primary brand states. */
        public val badgeBgPrimary: Color = accentPrimary

        /** Badge outer border. */
        public val badgeBorder: Color = borderCoreOnDark

        /** Typing indicator background. */
        public val chatBgTypingIndicator: Color = accentNeutral

        /** Border for incoming message bubbles. */
        public val chatBorderIncoming: Color = borderCoreSubtle

        /** Border for outgoing message bubbles. */
        public val chatBorderOutgoing: Color = brand100

        /** Reaction text color in chat. */
        public val chatTextReaction: Color = textSecondary

        /** Read receipt text color. */
        public val chatTextRead: Color = accentPrimary

        /** Username text color in chat. */
        public val chatTextUsername: Color = textSecondary

        /** Thread connector line for incoming messages. */
        public val chatThreadConnectorIncoming: Color = borderCoreDefault

        /** Thread connector line for outgoing messages. */
        public val chatThreadConnectorOutgoing: Color = brand150

        /** Chip background color. */
        public val chipBg: Color = brand100

        /** Composer background color. */
        public val composerBg: Color = backgroundElevationElevation1

        /** Playback toggle border color. */
        public val controlPlaybackToggleBorder: Color = borderCoreDefault

        /** Playback toggle text color. */
        public val controlPlaybackToggleText: Color = textPrimary

        /** Progress bar fill color. */
        public val controlProgressBarFill: Color = accentNeutral

        /** Progress bar track color. */
        public val controlProgressBarTrack: Color = backgroundCoreSurfaceStrong

        /** Radio/check background (unselected). */
        public val controlRadioCheckBg: Color = Color.Transparent

        /** Toggle switch track background. */
        public val controlToggleSwitchBg: Color = backgroundCoreSurfaceStrong

        /** Toggle switch track background when disabled. */
        public val controlToggleSwitchBgDisabled: Color = backgroundCoreDisabled

        /** Toggle switch track background when selected. */
        public val controlToggleSwitchBgSelected: Color = accentPrimary

        /** Toggle switch knob color. */
        public val controlToggleSwitchKnob: Color = backgroundElevationElevation4

        /** Input field default border. */
        public val inputBorderDefault: Color = borderCoreDefault

        /** Input field border on hover. */
        public val inputBorderHover: Color = borderCoreStrong

        /** Input field border when selected/focused. */
        public val inputBorderSelected: Color = borderUtilitySelected

        /** Send icon color. */
        public val inputSendIcon: Color = accentPrimary

        /** Send icon color when disabled. */
        public val inputSendIconDisabled: Color = textDisabled

        /** Input field default text color. */
        public val inputTextDefault: Color = textPrimary

        /** Input field disabled text color. */
        public val inputTextDisabled: Color = textDisabled

        /** Input field icon color. */
        public val inputTextIcon: Color = textTertiary

        /** Input field placeholder text color. */
        public val inputTextPlaceholder: Color = textTertiary

        /** Reaction background color. */
        public val reactionBg: Color = backgroundElevationElevation3

        /** Reaction border color. */
        public val reactionBorder: Color = borderCoreDefault

        /** Reaction emoji color. */
        public val reactionEmoji: Color = textPrimary

        /** Reaction text color. */
        public val reactionText: Color = textPrimary

        /** Skeleton loading gradient base color. */
        public val skeletonLoadingBase: Color = Color.Transparent

        /** Default app background color. */
        public val backgroundCoreApp: Color = backgroundElevationElevation0

        /** Default badge background. */
        public val badgeBgDefault: Color = backgroundElevationElevation2

        /** Badge background with inverse color scheme. */
        public val badgeBgInverse: Color = backgroundCoreInverse

        /** Badge text color. */
        public val badgeText: Color = textPrimary

        /** Badge text color on accent backgrounds. */
        public val badgeTextOnAccent: Color = textOnAccent

        /** Badge text color on inverse backgrounds. */
        public val badgeTextInverse: Color = textOnDark

        /** Destructive button background. */
        public val buttonDestructiveBg: Color = accentError

        /** Destructive button border. */
        public val buttonDestructiveBorder: Color = accentError

        /** Destructive button text. */
        public val buttonDestructiveText: Color = accentError

        /** Destructive button text on accent backgrounds. */
        public val buttonDestructiveTextOnAccent: Color = textOnAccent

        /** Primary button background. */
        public val buttonPrimaryBg: Color = accentPrimary

        /** Primary button border. */
        public val buttonPrimaryBorder: Color = brand200

        /** Primary button text. */
        public val buttonPrimaryText: Color = accentPrimary

        /** Primary button text on accent backgrounds. */
        public val buttonPrimaryTextOnAccent: Color = textOnAccent

        /** Secondary button background. */
        public val buttonSecondaryBg: Color = backgroundCoreSurface

        /** Secondary button border. */
        public val buttonSecondaryBorder: Color = borderCoreDefault

        /** Secondary button text. */
        public val buttonSecondaryText: Color = textPrimary

        /** Secondary button text on accent backgrounds. */
        public val buttonSecondaryTextOnAccent: Color = textPrimary

        /** Outgoing message bubble background. */
        public val chatBgOutgoing: Color = brand100

        /** Border on outgoing message bubbles. */
        public val chatBorderOnChatOutgoing: Color = brand300

        /** Incoming message text color. */
        public val chatTextIncoming: Color = textPrimary

        /** Outgoing message text color. */
        public val chatTextOutgoing: Color = brand900

        /** Link text color in chat messages. */
        public val chatTextLink: Color = accentPrimary

        /** Mention text color in chat messages. */
        public val chatTextMention: Color = chatTextLink

        /** System messages like date separators and unread dividers. */
        public val chatTextSystem: Color = textSecondary

        /** Timestamp text color in chat messages. */
        public val chatTextTimestamp: Color = textTertiary

        /** Audio waveform bar color. */
        public val chatWaveformBar: Color = borderCoreOpacity25

        /** Audio waveform bar color when playing. */
        public val chatWaveformBarPlaying: Color = accentPrimary

        /** Chip text color. */
        public val chipText: Color = brand900

        /** Play control button background. */
        public val controlPlayControlBg: Color = accentBlack

        /** Play control button icon. */
        public val controlPlayControlIcon: Color = textOnAccent

        /** Radio/check border. */
        public val controlRadioCheckBorder: Color = borderCoreDefault

        /** Remove control border. */
        public val controlRemoveBorder: Color = borderCoreOnDark

        /** Online presence indicator. */
        public val presenceBgOnline: Color = accentSuccess

        /** Offline presence indicator. */
        public val presenceBgOffline: Color = accentNeutral

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
                accentSuccess = StreamPrimitiveColors.green400,
                backgroundCoreDisabled = StreamPrimitiveColors.slate100,
                backgroundCoreSurface = StreamPrimitiveColors.slate100,
                backgroundCoreSurfaceSubtle = StreamPrimitiveColors.slate50,
                backgroundCoreSurfaceStrong = StreamPrimitiveColors.slate150,
                backgroundCoreInverse = StreamPrimitiveColors.slate900,
                backgroundElevationElevation0 = StreamPrimitiveColors.baseWhite,
                backgroundElevationElevation1 = StreamPrimitiveColors.baseWhite,
                backgroundElevationElevation2 = StreamPrimitiveColors.baseWhite,
                backgroundElevationElevation3 = StreamPrimitiveColors.baseWhite,
                backgroundElevationElevation4 = StreamPrimitiveColors.baseWhite,
                badgeBgOverlay = StreamPrimitiveColors.baseBlack.copy(alpha = .75f),
                borderCoreDefault = StreamPrimitiveColors.slate150,
                borderCoreStrong = StreamPrimitiveColors.slate300,
                borderCoreOpacity10 = StreamPrimitiveColors.baseBlack.copy(alpha = .1f),
                borderCoreOnAccent = StreamPrimitiveColors.baseWhite,
                borderCoreOnDark = StreamPrimitiveColors.baseWhite,
                borderCoreOpacity25 = StreamPrimitiveColors.baseBlack.copy(alpha = 0.25f),
                borderUtilitySelected = StreamPrimitiveColors.blue500,
                borderCoreSubtle = StreamPrimitiveColors.slate100,
                borderUtilityDisabled = StreamPrimitiveColors.slate100,
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
                textDisabled = StreamPrimitiveColors.slate300,
                textOnAccent = StreamPrimitiveColors.baseWhite,
                textOnDark = StreamPrimitiveColors.baseWhite,
                textPrimary = StreamPrimitiveColors.slate900,
                textSecondary = StreamPrimitiveColors.slate700,
                textTertiary = StreamPrimitiveColors.slate500,
                avatarBgPlaceholder = StreamPrimitiveColors.slate100,
                avatarPaletteBg1 = StreamPrimitiveColors.blue150,
                avatarPaletteBg2 = StreamPrimitiveColors.cyan150,
                avatarPaletteBg3 = StreamPrimitiveColors.green150,
                avatarPaletteBg4 = StreamPrimitiveColors.purple150,
                avatarPaletteBg5 = StreamPrimitiveColors.yellow150,
                avatarPaletteText1 = StreamPrimitiveColors.blue900,
                avatarPaletteText2 = StreamPrimitiveColors.cyan900,
                avatarPaletteText3 = StreamPrimitiveColors.green900,
                avatarPaletteText4 = StreamPrimitiveColors.purple900,
                avatarPaletteText5 = StreamPrimitiveColors.yellow900,
                avatarTextPlaceholder = StreamPrimitiveColors.slate500,
                chatBgIncoming = StreamPrimitiveColors.slate100,
                chatBgAttachmentIncoming = StreamPrimitiveColors.slate150,
                chatBgAttachmentOutgoing = StreamPrimitiveColors.blue150,
                chatBorderOnChatIncoming = StreamPrimitiveColors.slate300,
                chatPollProgressFillIncoming = StreamPrimitiveColors.slate500,
                chatPollProgressTrackIncoming = StreamPrimitiveColors.slate150,
                chatPollProgressFillOutgoing = StreamPrimitiveColors.blue500,
                chatPollProgressTrackOutgoing = StreamPrimitiveColors.blue200,
                chatReplyIndicatorIncoming = StreamPrimitiveColors.slate400,
                chatReplyIndicatorOutgoing = StreamPrimitiveColors.blue400,
                controlRadioCheckBgSelected = StreamPrimitiveColors.blue500,
                controlRadioCheckIconSelected = StreamPrimitiveColors.baseWhite,
                controlRemoveBg = StreamPrimitiveColors.slate900,
                controlRemoveIcon = StreamPrimitiveColors.baseWhite,
                presenceBorder = StreamPrimitiveColors.baseWhite,
                backgroundCoreScrim = StreamPrimitiveColors.baseBlack.copy(alpha = 0.25f),
                backgroundCoreOverlayDark = StreamPrimitiveColors.baseBlack.copy(alpha = 0.25f),
                backgroundCoreHighlight = StreamPrimitiveColors.yellow50,
                skeletonLoadingHighlight = StreamPrimitiveColors.baseWhite,
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
                accentNeutral = StreamPrimitiveColors.neutral300,
                accentPrimary = StreamPrimitiveColors.blue300,
                accentSuccess = StreamPrimitiveColors.green300,
                backgroundCoreDisabled = StreamPrimitiveColors.neutral800,
                backgroundCoreSurface = StreamPrimitiveColors.neutral800,
                backgroundCoreSurfaceSubtle = StreamPrimitiveColors.neutral900,
                backgroundCoreSurfaceStrong = StreamPrimitiveColors.neutral700,
                backgroundCoreInverse = StreamPrimitiveColors.neutral50,
                backgroundCoreSelected = StreamPrimitiveColors.baseWhite.copy(alpha = 0.25f),
                backgroundElevationElevation0 = StreamPrimitiveColors.baseBlack,
                backgroundElevationElevation1 = StreamPrimitiveColors.neutral900,
                backgroundElevationElevation2 = StreamPrimitiveColors.neutral800,
                backgroundElevationElevation3 = StreamPrimitiveColors.neutral700,
                backgroundElevationElevation4 = StreamPrimitiveColors.neutral600,
                borderCoreDefault = StreamPrimitiveColors.neutral600,
                borderCoreStrong = StreamPrimitiveColors.neutral400,
                borderCoreOpacity10 = StreamPrimitiveColors.baseWhite.copy(alpha = .2f),
                borderCoreOnAccent = StreamPrimitiveColors.baseWhite,
                borderCoreOnDark = StreamPrimitiveColors.neutral900,
                borderCoreOpacity25 = StreamPrimitiveColors.baseWhite.copy(alpha = 0.25f),
                borderUtilitySelected = StreamPrimitiveColors.blue300,
                borderCoreSubtle = StreamPrimitiveColors.neutral800,
                borderUtilityDisabled = StreamPrimitiveColors.neutral700,
                brand50 = StreamPrimitiveColors.blue900,
                brand100 = StreamPrimitiveColors.blue800,
                brand150 = StreamPrimitiveColors.blue700,
                brand200 = StreamPrimitiveColors.blue600,
                brand300 = StreamPrimitiveColors.blue500,
                brand400 = StreamPrimitiveColors.blue400,
                brand500 = StreamPrimitiveColors.blue300,
                brand600 = StreamPrimitiveColors.blue200,
                brand700 = StreamPrimitiveColors.blue150,
                brand800 = StreamPrimitiveColors.blue100,
                brand900 = StreamPrimitiveColors.blue50,
                textDisabled = StreamPrimitiveColors.neutral500,
                textOnAccent = StreamPrimitiveColors.baseWhite,
                textOnDark = StreamPrimitiveColors.neutral900,
                textPrimary = StreamPrimitiveColors.baseWhite,
                textSecondary = StreamPrimitiveColors.neutral100,
                textTertiary = StreamPrimitiveColors.neutral300,
                badgeBgOverlay = StreamPrimitiveColors.baseBlack.copy(alpha = .75f),
                avatarBgPlaceholder = StreamPrimitiveColors.neutral700,
                avatarPaletteBg1 = StreamPrimitiveColors.blue600,
                avatarPaletteBg2 = StreamPrimitiveColors.cyan600,
                avatarPaletteBg3 = StreamPrimitiveColors.green600,
                avatarPaletteBg4 = StreamPrimitiveColors.purple600,
                avatarPaletteBg5 = StreamPrimitiveColors.yellow600,
                avatarPaletteText1 = StreamPrimitiveColors.blue100,
                avatarPaletteText2 = StreamPrimitiveColors.cyan100,
                avatarPaletteText3 = StreamPrimitiveColors.green100,
                avatarPaletteText4 = StreamPrimitiveColors.purple100,
                avatarPaletteText5 = StreamPrimitiveColors.yellow100,
                avatarTextPlaceholder = StreamPrimitiveColors.neutral400,
                chatBgIncoming = StreamPrimitiveColors.neutral800,
                chatBgAttachmentIncoming = StreamPrimitiveColors.neutral700,
                chatBgAttachmentOutgoing = StreamPrimitiveColors.blue700,
                chatBorderOnChatIncoming = StreamPrimitiveColors.neutral400,
                chatPollProgressFillIncoming = StreamPrimitiveColors.neutral300,
                chatPollProgressTrackIncoming = StreamPrimitiveColors.neutral700,
                chatPollProgressFillOutgoing = StreamPrimitiveColors.baseWhite,
                chatPollProgressTrackOutgoing = StreamPrimitiveColors.blue600,
                chatReplyIndicatorIncoming = StreamPrimitiveColors.neutral500,
                chatReplyIndicatorOutgoing = StreamPrimitiveColors.blue150,
                controlRadioCheckBgSelected = StreamPrimitiveColors.baseWhite,
                controlRadioCheckIconSelected = StreamPrimitiveColors.neutral900,
                controlRemoveBg = StreamPrimitiveColors.neutral50,
                controlRemoveIcon = StreamPrimitiveColors.neutral900,
                presenceBorder = StreamPrimitiveColors.neutral900,
                backgroundCoreScrim = StreamPrimitiveColors.baseBlack.copy(alpha = 0.75f),
                backgroundCoreOverlayDark = StreamPrimitiveColors.baseBlack.copy(alpha = 0.5f),
                backgroundCoreHighlight = StreamPrimitiveColors.yellow900,
                skeletonLoadingHighlight = StreamPrimitiveColors.baseWhite.copy(alpha = 0.2f),
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
}
