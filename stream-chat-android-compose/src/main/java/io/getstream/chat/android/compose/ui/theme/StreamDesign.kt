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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

/**
 * The Stream Chat Design System namespace.
 *
 * Provides all design tokens for theming Chat SDK components:
 * - [ColorScale] -- the brand (accent) color ramp
 * - [ChromeScale] -- the chrome (neutral gray) color ramp
 * - [Colors] -- semantic color tokens derived from the scales above
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
     * Parameters are organized by domain:
     * scales → accent → text → background → border → badge → chat → control → avatar → skeleton.
     *
     * @param brand The brand (accent) color scale. See [ColorScale].
     * @param chrome The chrome (neutral gray) color scale. See [ChromeScale].
     * @param accentPrimary Used for main brand accent for interactive elements.
     * @param accentError Used for destructive actions and error states.
     * @param accentSuccess Used for success states and positive actions.
     * @param accentWarning Used for warning or caution states.
     * @param accentNeutral Used for neutral accent for low-priority badges.
     * @param textPrimary Used for main text color.
     * @param textSecondary Used for secondary text color with lower emphasis.
     * @param textTertiary Used for tertiary text color with lowest emphasis.
     * @param textDisabled Used for disabled text and icon color.
     * @param textOnAccent Used for text displayed on accent/colored backgrounds.
     * @param textInverse Used for text displayed on dark/inverse backgrounds.
     * @param textLink Used for hyperlinks and inline action text.
     * @param backgroundCoreElevation0 Used for base elevation surface backgrounds.
     * @param backgroundCoreElevation1 Slightly elevated surface backgrounds.
     * @param backgroundCoreElevation2 Used for elevated surface backgrounds at elevation level 2.
     * @param backgroundCoreElevation3 Popover surface backgrounds.
     * @param backgroundCoreElevation4 Dialog and modal surface backgrounds.
     * @param backgroundCoreSurface Used for surface background in components like buttons.
     * @param backgroundCoreSurfaceSubtle Used for subtle surface backgrounds.
     * @param backgroundCoreSurfaceStrong Stronger section background for prominent surface areas.
     * @param backgroundCoreSurfaceCard Card surface backgrounds (e.g. link previews, attachments).
     * @param backgroundCoreInverse Elevated, transient, or high-attention UI surfaces that sit on
     * top of the default app background.
     * @param backgroundCoreOnAccent Surfaces that must remain white across themes
     * (e.g., media controls over video).
     * @param backgroundCoreScrim Used for dimmed scrim backgrounds (e.g. behind modals).
     * @param backgroundCoreOverlayDark Used for dark overlay backgrounds on media/badges.
     * @param backgroundCoreOverlayLight Used for light overlay backgrounds.
     * @param backgroundCoreHighlight Used for highlight backgrounds (e.g. message focus/pin).
     * @param backgroundUtilitySelected Selected state overlay background.
     * @param backgroundUtilityDisabled Used for disabled utility backgrounds.
     * @param borderCoreDefault Used for default border color.
     * @param borderCoreStrong Stronger surface border with higher contrast.
     * @param borderCoreSubtle Used for subtle/very light separators.
     * @param borderCoreOpacitySubtle Subtle translucent border (e.g. image frames, avatars).
     * @param borderCoreOpacityStrong Stronger translucent border (e.g. waveforms, playback thumbs).
     * @param borderCoreOnAccent Used for borders on accent backgrounds.
     * @param borderCoreInverse Used for borders on dark/inverse backgrounds.
     * @param borderUtilitySelected Used for selected or active state border (focus ring).
     * @param borderUtilityActive Used for active/selected state border.
     * @param borderUtilityDisabled Used for disabled state borders.
     * @param borderUtilityError Used for error state borders.
     * @param borderUtilityWarning Used for warning state borders.
     * @param borderUtilitySuccess Used for success state borders.
     * @param badgeBgOverlay Used for badge background when displayed as an overlay.
     * @param badgeBgInverse Used for inverse badge background.
     * @param chatBgIncoming Used for incoming message bubble background.
     * @param chatBgAttachmentIncoming Used for incoming message attachment background.
     * @param chatBgAttachmentOutgoing Used for outgoing message attachment background.
     * @param chatBorderOnChatIncoming Used for border on incoming message bubbles.
     * @param chatPollProgressFillIncoming Used for incoming poll progress fill color.
     * @param chatPollProgressTrackIncoming Used for incoming poll progress track color.
     * @param chatPollProgressFillOutgoing Used for outgoing poll progress fill color.
     * @param chatPollProgressTrackOutgoing Used for outgoing poll progress track color.
     * @param chatReplyIndicatorIncoming Reply indicator color in incoming messages.
     * @param chatReplyIndicatorOutgoing Reply indicator color in outgoing messages.
     * @param controlRemoveBg Used for remove control background.
     * @param controlRemoveIcon Used for remove control icon.
     * @param controlPlaybackThumbBgDefault Default background for the playback thumb control.
     * @param avatarBgPlaceholder Used for avatar placeholder background color.
     * @param avatarPaletteBg1 Used for avatar background color (slot 1).
     * @param avatarPaletteBg2 Used for avatar background color (slot 2).
     * @param avatarPaletteBg3 Used for avatar background color (slot 3).
     * @param avatarPaletteBg4 Used for avatar background color (slot 4).
     * @param avatarPaletteBg5 Used for avatar background color (slot 5).
     * @param avatarPaletteText1 Used for avatar text color (slot 1).
     * @param avatarPaletteText2 Used for avatar text color (slot 2).
     * @param avatarPaletteText3 Used for avatar text color (slot 3).
     * @param avatarPaletteText4 Used for avatar text color (slot 4).
     * @param avatarPaletteText5 Used for avatar text color (slot 5).
     * @param avatarTextPlaceholder Used for avatar placeholder text color.
     * @param avatarPresenceBorder Used for the outline around the presence dot.
     * @param skeletonLoadingHighlight Shimmer highlight color for skeleton loading gradients.
     */
    @Immutable
    public data class Colors(
        public val brand: ColorScale,
        public val chrome: ChromeScale,
        public val accentPrimary: Color,
        public val accentError: Color,
        public val accentSuccess: Color,
        public val accentWarning: Color,
        public val accentNeutral: Color,
        public val textPrimary: Color,
        public val textSecondary: Color,
        public val textTertiary: Color,
        public val textDisabled: Color,
        public val textOnAccent: Color,
        public val textInverse: Color,
        public val textLink: Color,
        public val backgroundCoreElevation0: Color,
        public val backgroundCoreElevation1: Color,
        public val backgroundCoreElevation2: Color,
        public val backgroundCoreElevation3: Color,
        public val backgroundCoreElevation4: Color,
        public val backgroundCoreSurface: Color,
        public val backgroundCoreSurfaceSubtle: Color,
        public val backgroundCoreSurfaceStrong: Color,
        public val backgroundCoreSurfaceCard: Color,
        public val backgroundCoreInverse: Color,
        public val backgroundCoreOnAccent: Color,
        public val backgroundCoreScrim: Color,
        public val backgroundCoreOverlayDark: Color,
        public val backgroundCoreOverlayLight: Color,
        public val backgroundCoreHighlight: Color,
        public val backgroundUtilitySelected: Color,
        public val backgroundUtilityDisabled: Color,
        public val borderCoreDefault: Color,
        public val borderCoreStrong: Color,
        public val borderCoreSubtle: Color,
        public val borderCoreOpacitySubtle: Color,
        public val borderCoreOpacityStrong: Color,
        public val borderCoreOnAccent: Color,
        public val borderCoreInverse: Color,
        public val borderUtilitySelected: Color,
        public val borderUtilityActive: Color,
        public val borderUtilityDisabled: Color,
        public val borderUtilityError: Color,
        public val borderUtilityWarning: Color,
        public val borderUtilitySuccess: Color,
        public val badgeBgOverlay: Color,
        public val badgeBgInverse: Color,
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
        public val controlRemoveBg: Color,
        public val controlRemoveIcon: Color,
        public val controlPlaybackThumbBgDefault: Color,
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
        public val avatarPresenceBorder: Color,
        public val skeletonLoadingHighlight: Color,
    ) {

        /** Badge background for error states. */
        public val badgeBgError: Color = accentError

        /** Badge background for neutral states. */
        public val badgeBgNeutral: Color = accentNeutral

        /** Badge background for primary brand states. */
        public val badgeBgPrimary: Color = accentPrimary

        /** Badge outer border. */
        public val badgeBorder: Color = borderCoreInverse

        /** Typing indicator text color. */
        public val chatTextTypingIndicator: Color = textPrimary

        /** Border for incoming message bubbles. */
        public val chatBorderIncoming: Color = borderCoreSubtle

        /** Border for outgoing message bubbles. */
        public val chatBorderOutgoing: Color = brand.s100

        /** Reaction text color in chat. */
        public val chatTextReaction: Color = textSecondary

        /** Read receipt text color. */
        public val chatTextRead: Color = accentPrimary

        /** Username text color in chat. */
        public val chatTextUsername: Color = textSecondary

        /** Thread connector line for incoming messages. */
        public val chatThreadConnectorIncoming: Color = borderCoreDefault

        /** Thread connector line for outgoing messages. */
        public val chatThreadConnectorOutgoing: Color = brand.s150

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

        /** Selected radio/check background. */
        public val controlRadioCheckBgSelected: Color = accentPrimary

        /** Selected radio/check icon. */
        public val controlRadioCheckIcon: Color = textOnAccent

        /** Indicator dot inside a selected radio button. */
        public val controlRadioButtonIndicator: Color = textOnAccent

        /** Toggle switch track background. */
        public val controlToggleSwitchBg: Color = accentNeutral

        /** Toggle switch track background when disabled. */
        public val controlToggleSwitchBgDisabled: Color = backgroundUtilityDisabled

        /** Toggle switch track background when selected. */
        public val controlToggleSwitchBgSelected: Color = accentPrimary

        /** Toggle switch knob color. */
        public val controlToggleSwitchKnob: Color = backgroundCoreElevation4

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
        public val reactionBg: Color = backgroundCoreElevation3

        /** Reaction border color. */
        public val reactionBorder: Color = borderCoreDefault

        /** Reaction emoji color. */
        public val reactionEmoji: Color = textPrimary

        /** Reaction text color. */
        public val reactionText: Color = textPrimary

        /** Skeleton loading gradient base color. */
        public val skeletonLoadingBase: Color = Color.Transparent

        /** Default app background color. */
        public val backgroundCoreApp: Color = backgroundCoreElevation0

        /** Default badge background. */
        public val badgeBgDefault: Color = backgroundCoreElevation3

        /** Badge text color. */
        public val badgeText: Color = textPrimary

        /** Badge text color on accent backgrounds. */
        public val badgeTextOnAccent: Color = textOnAccent

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
        public val buttonPrimaryBorder: Color = brand.s200

        /** Primary button text. */
        public val buttonPrimaryText: Color = textLink

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
        public val chatBgOutgoing: Color = brand.s100

        /** Border on outgoing message bubbles. */
        public val chatBorderOnChatOutgoing: Color = brand.s300

        /** Incoming message text color. */
        public val chatTextIncoming: Color = textPrimary

        /** Outgoing message text color. */
        public val chatTextOutgoing: Color = brand.s900

        /** Link text color in chat messages. */
        public val chatTextLink: Color = textLink

        /** Mention text color in chat messages. */
        public val chatTextMention: Color = chatTextLink

        /** System messages like date separators and unread dividers. */
        public val chatTextSystem: Color = textSecondary

        /** Timestamp text color in chat messages. */
        public val chatTextTimestamp: Color = textTertiary

        /** Audio waveform bar color. */
        public val chatWaveformBar: Color = borderCoreOpacityStrong

        /** Audio waveform bar color when playing. */
        public val chatWaveformBarPlaying: Color = accentPrimary

        /** Chip text color. */
        public val controlChipText: Color = textPrimary

        /** Chip border color. */
        public val controlChipBorder: Color = borderCoreDefault

        /** Play button background. */
        public val controlPlayButtonBg: Color = chrome.s1000

        /** Play button icon. */
        public val controlPlayButtonIcon: Color = textOnAccent

        /** Checkbox background (unselected). */
        public val controlCheckboxBg: Color = Color.Transparent

        /** Checkbox border. */
        public val controlCheckboxBorder: Color = borderCoreDefault

        /** Checkbox background when selected. */
        public val controlCheckboxBgSelected: Color = accentPrimary

        /** Checkbox icon when selected. */
        public val controlCheckboxIcon: Color = textOnAccent

        /** Playback thumb border in default state. */
        public val controlPlaybackThumbBorderDefault: Color = borderCoreOpacityStrong

        /** Playback thumb background in active state. */
        public val controlPlaybackThumbBgActive: Color = accentPrimary

        /** Playback thumb border in active state. */
        public val controlPlaybackThumbBorderActive: Color = borderCoreOnAccent

        /** Radio/check border. */
        public val controlRadioCheckBorder: Color = borderCoreDefault

        /** Remove control border. */
        public val controlRemoveBorder: Color = borderCoreInverse

        /** Online presence indicator. */
        public val avatarPresenceBgOnline: Color = accentSuccess

        /** Offline presence indicator. */
        public val avatarPresenceBgOffline: Color = accentNeutral

        public companion object {
            /**
             * Provides the default colors for the light mode of the app.
             *
             * @return A [Colors] instance holding our color palette.
             */
            @Suppress("LongMethod", "MagicNumber")
            public fun default(): Colors = Colors(
                brand = ColorScale.defaultLight(),
                chrome = ChromeScale.defaultLight(),
                accentPrimary = StreamPrimitiveColors.blue500,
                accentError = StreamPrimitiveColors.red500,
                accentSuccess = StreamPrimitiveColors.green400,
                accentWarning = StreamPrimitiveColors.yellow400,
                accentNeutral = StreamPrimitiveColors.slate500,
                textPrimary = StreamPrimitiveColors.slate900,
                textSecondary = StreamPrimitiveColors.slate700,
                textTertiary = StreamPrimitiveColors.slate500,
                textDisabled = StreamPrimitiveColors.slate300,
                textOnAccent = StreamPrimitiveColors.baseWhite,
                textInverse = StreamPrimitiveColors.baseWhite,
                textLink = StreamPrimitiveColors.blue500,
                backgroundCoreElevation0 = StreamPrimitiveColors.baseWhite,
                backgroundCoreElevation1 = StreamPrimitiveColors.baseWhite,
                backgroundCoreElevation2 = StreamPrimitiveColors.baseWhite,
                backgroundCoreElevation3 = StreamPrimitiveColors.baseWhite,
                backgroundCoreElevation4 = StreamPrimitiveColors.baseWhite,
                backgroundCoreSurface = StreamPrimitiveColors.slate100,
                backgroundCoreSurfaceSubtle = StreamPrimitiveColors.slate50,
                backgroundCoreSurfaceStrong = StreamPrimitiveColors.slate150,
                backgroundCoreSurfaceCard = StreamPrimitiveColors.slate50,
                backgroundCoreInverse = StreamPrimitiveColors.slate900,
                backgroundCoreOnAccent = StreamPrimitiveColors.baseWhite,
                backgroundCoreScrim = StreamPrimitiveColors.slate900.copy(alpha = 0.5f),
                backgroundCoreOverlayDark = StreamPrimitiveColors.slate900.copy(alpha = 0.25f),
                backgroundCoreOverlayLight = Color(0xBFFFFFFF),
                backgroundCoreHighlight = StreamPrimitiveColors.yellow50,
                backgroundUtilitySelected = StreamPrimitiveColors.slate900.copy(alpha = 0.2f),
                backgroundUtilityDisabled = StreamPrimitiveColors.slate100,
                borderCoreDefault = StreamPrimitiveColors.slate150,
                borderCoreStrong = StreamPrimitiveColors.slate300,
                borderCoreSubtle = StreamPrimitiveColors.slate100,
                borderCoreOpacitySubtle = StreamPrimitiveColors.slate900.copy(alpha = 0.1f),
                borderCoreOpacityStrong = StreamPrimitiveColors.slate900.copy(alpha = 0.25f),
                borderCoreOnAccent = StreamPrimitiveColors.baseWhite,
                borderCoreInverse = StreamPrimitiveColors.baseWhite,
                borderUtilitySelected = StreamPrimitiveColors.blue500,
                borderUtilityActive = StreamPrimitiveColors.blue500,
                borderUtilityDisabled = StreamPrimitiveColors.slate100,
                borderUtilityError = StreamPrimitiveColors.red500,
                borderUtilityWarning = StreamPrimitiveColors.yellow400,
                borderUtilitySuccess = StreamPrimitiveColors.green400,
                badgeBgOverlay = StreamPrimitiveColors.baseBlack.copy(alpha = .75f),
                badgeBgInverse = StreamPrimitiveColors.baseBlack,
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
                controlRemoveBg = StreamPrimitiveColors.slate900,
                controlRemoveIcon = StreamPrimitiveColors.baseWhite,
                controlPlaybackThumbBgDefault = StreamPrimitiveColors.baseWhite,
                avatarBgPlaceholder = StreamPrimitiveColors.slate150,
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
                avatarPresenceBorder = StreamPrimitiveColors.baseWhite,
                skeletonLoadingHighlight = StreamPrimitiveColors.baseWhite,
            )

            /**
             * Provides the default colors for the dark mode of the app.
             *
             * @return A [Colors] instance holding our color palette.
             */
            @Suppress("LongMethod", "MagicNumber")
            public fun defaultDark(): Colors = Colors(
                brand = ColorScale.defaultDark(),
                chrome = ChromeScale.defaultDark(),
                accentPrimary = StreamPrimitiveColors.blue400,
                accentError = StreamPrimitiveColors.red400,
                accentSuccess = StreamPrimitiveColors.green300,
                accentWarning = StreamPrimitiveColors.yellow300,
                accentNeutral = StreamPrimitiveColors.neutral300,
                textPrimary = StreamPrimitiveColors.neutral50,
                textSecondary = StreamPrimitiveColors.neutral150,
                textTertiary = StreamPrimitiveColors.neutral300,
                textDisabled = StreamPrimitiveColors.neutral500,
                textOnAccent = StreamPrimitiveColors.baseWhite,
                textInverse = StreamPrimitiveColors.baseBlack,
                textLink = StreamPrimitiveColors.blue200,
                backgroundCoreElevation0 = StreamPrimitiveColors.baseBlack,
                backgroundCoreElevation1 = StreamPrimitiveColors.neutral900,
                backgroundCoreElevation2 = StreamPrimitiveColors.neutral800,
                backgroundCoreElevation3 = StreamPrimitiveColors.neutral600,
                backgroundCoreElevation4 = StreamPrimitiveColors.neutral500,
                backgroundCoreSurface = StreamPrimitiveColors.neutral800,
                backgroundCoreSurfaceSubtle = StreamPrimitiveColors.neutral900,
                backgroundCoreSurfaceStrong = StreamPrimitiveColors.neutral700,
                backgroundCoreSurfaceCard = StreamPrimitiveColors.neutral800,
                backgroundCoreInverse = StreamPrimitiveColors.neutral50,
                backgroundCoreOnAccent = StreamPrimitiveColors.baseBlack,
                backgroundCoreScrim = StreamPrimitiveColors.baseBlack.copy(alpha = 0.75f),
                backgroundCoreOverlayDark = StreamPrimitiveColors.baseBlack.copy(alpha = 0.5f),
                backgroundCoreOverlayLight = Color(0xBF000000),
                backgroundCoreHighlight = StreamPrimitiveColors.yellow800,
                backgroundUtilitySelected = StreamPrimitiveColors.baseWhite.copy(alpha = 0.25f),
                backgroundUtilityDisabled = StreamPrimitiveColors.neutral800,
                borderCoreDefault = StreamPrimitiveColors.neutral600,
                borderCoreStrong = StreamPrimitiveColors.neutral500,
                borderCoreSubtle = StreamPrimitiveColors.neutral800,
                borderCoreOpacitySubtle = StreamPrimitiveColors.baseWhite.copy(alpha = .2f),
                borderCoreOpacityStrong = StreamPrimitiveColors.baseWhite.copy(alpha = 0.25f),
                borderCoreOnAccent = StreamPrimitiveColors.baseWhite,
                borderCoreInverse = StreamPrimitiveColors.baseBlack,
                borderUtilitySelected = StreamPrimitiveColors.blue400,
                borderUtilityActive = StreamPrimitiveColors.blue400,
                borderUtilityDisabled = StreamPrimitiveColors.neutral800,
                borderUtilityError = StreamPrimitiveColors.red400,
                borderUtilityWarning = StreamPrimitiveColors.yellow300,
                borderUtilitySuccess = StreamPrimitiveColors.green300,
                badgeBgOverlay = StreamPrimitiveColors.baseBlack.copy(alpha = .75f),
                badgeBgInverse = StreamPrimitiveColors.baseWhite,
                chatBgIncoming = StreamPrimitiveColors.neutral800,
                chatBgAttachmentIncoming = StreamPrimitiveColors.neutral700,
                chatBgAttachmentOutgoing = StreamPrimitiveColors.blue700,
                chatBorderOnChatIncoming = StreamPrimitiveColors.neutral500,
                chatPollProgressFillIncoming = StreamPrimitiveColors.neutral300,
                chatPollProgressTrackIncoming = StreamPrimitiveColors.neutral700,
                chatPollProgressFillOutgoing = StreamPrimitiveColors.baseWhite,
                chatPollProgressTrackOutgoing = StreamPrimitiveColors.blue600,
                chatReplyIndicatorIncoming = StreamPrimitiveColors.neutral500,
                chatReplyIndicatorOutgoing = StreamPrimitiveColors.blue150,
                controlRemoveBg = StreamPrimitiveColors.neutral50,
                controlRemoveIcon = StreamPrimitiveColors.baseBlack,
                controlPlaybackThumbBgDefault = StreamPrimitiveColors.neutral50,
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
                avatarTextPlaceholder = StreamPrimitiveColors.neutral300,
                avatarPresenceBorder = StreamPrimitiveColors.baseBlack,
                skeletonLoadingHighlight = StreamPrimitiveColors.baseWhite.copy(alpha = 0.2f),
            )
        }
    }

    /**
     * An 11-stop color ramp representing the **brand** (accent) palette.
     *
     * In the Stream design system the brand scale maps to `blue` by default.
     * Light themes use the natural order (s50 = lightest, s900 = darkest);
     * dark themes invert the mapping so that perceptual intensity stays
     * consistent (s50 is still the faintest tint suitable for backgrounds).
     *
     * Re-brand the entire Chat UI from a single color:
     * ```
     * val purple = StreamDesign.ColorScale.from(Color(0xFF6200EE))
     * ChatTheme(colors = StreamDesign.Colors.default(brand = purple))
     * ```
     *
     * For pixel-perfect branding, provide an explicit scale:
     * ```
     * ChatTheme(
     *     colors = StreamDesign.Colors.default(
     *         brand = StreamDesign.ColorScale(
     *             s50 = Color(0xFFF3E8FF),
     *             // …
     *             s900 = Color(0xFF3B0764),
     *         ),
     *     ),
     * )
     * ```
     *
     * @param s50  Faintest tint — backgrounds, subtle fills.
     * @param s100 Light tint — outgoing bubble background.
     * @param s150 Light-mid tint — attachment backgrounds, thread connectors.
     * @param s200 Mid-light tint — primary button border, poll track.
     * @param s300 Mid-tint — outgoing bubble on-chat border.
     * @param s400 Mid-strong — interactive accent (dark theme default).
     * @param s500 Core brand — interactive accent (light theme default).
     * @param s600 Strong — link text (dark theme).
     * @param s700 Darker — reply indicator (dark theme).
     * @param s800 Deep — brand depth.
     * @param s900 Deepest — outgoing text.
     */
    @Immutable
    public data class ColorScale(
        public val s50: Color,
        public val s100: Color,
        public val s150: Color,
        public val s200: Color,
        public val s300: Color,
        public val s400: Color,
        public val s500: Color,
        public val s600: Color,
        public val s700: Color,
        public val s800: Color,
        public val s900: Color,
    ) {
        public companion object {
            /**
             * Default brand scale for light themes.
             *
             * Maps brand stops 50..900 → blue 50..900 (natural order).
             */
            public fun defaultLight(): ColorScale = ColorScale(
                s50 = StreamPrimitiveColors.blue50,
                s100 = StreamPrimitiveColors.blue100,
                s150 = StreamPrimitiveColors.blue150,
                s200 = StreamPrimitiveColors.blue200,
                s300 = StreamPrimitiveColors.blue300,
                s400 = StreamPrimitiveColors.blue400,
                s500 = StreamPrimitiveColors.blue500,
                s600 = StreamPrimitiveColors.blue600,
                s700 = StreamPrimitiveColors.blue700,
                s800 = StreamPrimitiveColors.blue800,
                s900 = StreamPrimitiveColors.blue900,
            )

            /**
             * Default brand scale for dark themes.
             *
             * Inverts the mapping: brand 50 → blue 900, brand 900 → blue 50,
             * so that perceptual intensity is preserved on dark backgrounds.
             */
            public fun defaultDark(): ColorScale = ColorScale(
                s50 = StreamPrimitiveColors.blue900,
                s100 = StreamPrimitiveColors.blue800,
                s150 = StreamPrimitiveColors.blue700,
                s200 = StreamPrimitiveColors.blue600,
                s300 = StreamPrimitiveColors.blue500,
                s400 = StreamPrimitiveColors.blue400,
                s500 = StreamPrimitiveColors.blue300,
                s600 = StreamPrimitiveColors.blue200,
                s700 = StreamPrimitiveColors.blue150,
                s800 = StreamPrimitiveColors.blue100,
                s900 = StreamPrimitiveColors.blue50,
            )

            /**
             * Generates a brand scale from a single [brandColor].
             *
             * The input is placed at [s500] (the core brand stop). Lighter
             * stops are interpolated toward white and darker stops toward
             * black using Oklab perceptual color space.
             *
             * This is an approximation intended for quick re-branding.
             * For pixel-perfect results, provide an explicit [ColorScale].
             *
             * @param brandColor The core brand color (maps to [s500]).
             */
            @Suppress("MagicNumber")
            public fun from(brandColor: Color): ColorScale = ColorScale(
                s50 = lerp(Color.White, brandColor, 0.04f),
                s100 = lerp(Color.White, brandColor, 0.08f),
                s150 = lerp(Color.White, brandColor, 0.16f),
                s200 = lerp(Color.White, brandColor, 0.26f),
                s300 = lerp(Color.White, brandColor, 0.42f),
                s400 = lerp(Color.White, brandColor, 0.65f),
                s500 = brandColor,
                s600 = lerp(brandColor, Color.Black, 0.25f),
                s700 = lerp(brandColor, Color.Black, 0.42f),
                s800 = lerp(brandColor, Color.Black, 0.58f),
                s900 = lerp(brandColor, Color.Black, 0.75f),
            )
        }
    }

    /**
     * A 13-stop neutral ramp representing the **chrome** (structural gray) palette.
     *
     * Chrome defines the visual canvas: text, backgrounds, borders, surfaces.
     * Light themes map to the **slate** foundation (cool grays);
     * dark themes switch to **neutral** (warm grays) and invert the direction,
     * so that [s0] is always the base surface and [s900] is always the
     * strongest foreground.
     *
     * [s0] and [s1000] are the absolute endpoints (white/black in light,
     * black/white in dark). They absorb the light↔dark polarity, allowing
     * all downstream tokens to reference chrome stops with identical
     * expressions regardless of theme.
     *
     * @param s0    Base surface — white in light, black in dark.
     * @param s50   Faintest tint — subtle surfaces, elevation-1 (dark).
     * @param s100  Light — standard surface, disabled states.
     * @param s150  Light-mid — surface-strong, default border, avatar placeholder bg.
     * @param s200  Mid-light — elevation-3 border (dark), border default (dark).
     * @param s300  Mid — border strong, disabled text, elevation-4 (dark).
     * @param s400  Mid-neutral — neutral accent.
     * @param s500  Core neutral — tertiary text, accent neutral.
     * @param s600  Strong — secondary metadata (not used in default themes).
     * @param s700  Darker — secondary text.
     * @param s800  Deep — not used in default themes.
     * @param s900  Deepest foreground — primary text, inverse background.
     * @param s1000 Absolute endpoint — black in light, white in dark.
     */
    @Immutable
    public data class ChromeScale(
        public val s0: Color,
        public val s50: Color,
        public val s100: Color,
        public val s150: Color,
        public val s200: Color,
        public val s300: Color,
        public val s400: Color,
        public val s500: Color,
        public val s600: Color,
        public val s700: Color,
        public val s800: Color,
        public val s900: Color,
        public val s1000: Color,
    ) {
        public companion object {
            /**
             * Default chrome scale for light themes.
             *
             * Maps chrome 0 → white, 50..900 → slate 50..900, 1000 → black.
             */
            public fun defaultLight(): ChromeScale = ChromeScale(
                s0 = StreamPrimitiveColors.baseWhite,
                s50 = StreamPrimitiveColors.slate50,
                s100 = StreamPrimitiveColors.slate100,
                s150 = StreamPrimitiveColors.slate150,
                s200 = StreamPrimitiveColors.slate200,
                s300 = StreamPrimitiveColors.slate300,
                s400 = StreamPrimitiveColors.slate400,
                s500 = StreamPrimitiveColors.slate500,
                s600 = StreamPrimitiveColors.slate600,
                s700 = StreamPrimitiveColors.slate700,
                s800 = StreamPrimitiveColors.slate800,
                s900 = StreamPrimitiveColors.slate900,
                s1000 = StreamPrimitiveColors.baseBlack,
            )

            /**
             * Default chrome scale for dark themes.
             *
             * Switches to the **neutral** palette and inverts the direction:
             * chrome 0 → black, 50 → neutral 900, …, 900 → neutral 50, 1000 → white.
             */
            public fun defaultDark(): ChromeScale = ChromeScale(
                s0 = StreamPrimitiveColors.baseBlack,
                s50 = StreamPrimitiveColors.neutral900,
                s100 = StreamPrimitiveColors.neutral800,
                s150 = StreamPrimitiveColors.neutral700,
                s200 = StreamPrimitiveColors.neutral600,
                s300 = StreamPrimitiveColors.neutral500,
                s400 = StreamPrimitiveColors.neutral400,
                s500 = StreamPrimitiveColors.neutral300,
                s600 = StreamPrimitiveColors.neutral200,
                s700 = StreamPrimitiveColors.neutral150,
                s800 = StreamPrimitiveColors.neutral100,
                s900 = StreamPrimitiveColors.neutral50,
                s1000 = StreamPrimitiveColors.baseWhite,
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
     * @param headingExtraSmall Style for extra-small headings and section labels.
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
        public val headingExtraSmall: TextStyle,
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
                headingExtraSmall = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = StreamTokens.fontWeightMedium,
                    fontSize = StreamTokens.fontSizeSm,
                    lineHeight = StreamTokens.lineHeightNormal,
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
