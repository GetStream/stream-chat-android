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

@file:Suppress("MagicNumber", "LongMethod")

package io.getstream.chat.android.compose.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import io.getstream.chat.android.compose.ui.theme.StreamDesign.Colors.Companion.default
import io.getstream.chat.android.compose.ui.theme.StreamDesign.Colors.Companion.defaultDark

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
     * Semantic color tokens for theming Chat SDK components.
     * Customize via [default] / [defaultDark] factory parameters or [copy].
     *
     * Parameters are organized by domain:
     * scales → accent → text → background → border → avatar → skeleton → component exceptions.
     *
     * @param brand The brand (accent) color scale. See [ColorScale].
     * @param chrome The chrome (neutral gray) color scale. See [ChromeScale].
     * @param accentPrimary Main brand accent for interactive elements.
     * @param accentError Destructive actions and error states.
     * @param accentSuccess Success states and positive actions.
     * @param accentWarning Warning or caution messages.
     * @param accentNeutral Neutral accent for low-priority badges.
     * @param textPrimary Main text color.
     * @param textSecondary Secondary metadata text.
     * @param textTertiary Lowest priority text.
     * @param textDisabled Disabled text.
     * @param textOnAccent Text on dark or accent backgrounds.
     * @param textInverse Text on inverse backgrounds.
     * @param textLink Hyperlinks and inline actions.
     * @param backgroundCoreElevation0 Flat surfaces.
     * @param backgroundCoreElevation1 Slightly elevated surfaces.
     * @param backgroundCoreElevation2 Card-like elements.
     * @param backgroundCoreElevation3 Popovers.
     * @param backgroundCoreElevation4 Dialogs, modals.
     * @param backgroundCoreSurface Standard section background.
     * @param backgroundCoreSurfaceSubtle Very light section background.
     * @param backgroundCoreSurfaceStrong Stronger section background.
     * @param backgroundCoreSurfaceCard Card surface backgrounds (e.g. link previews, attachments).
     * @param backgroundCoreInverse Inverse background for elevated, transient, or high-attention UI
     * surfaces that sit on top of the default app background.
     * @param backgroundCoreOnAccent Base surface for accent content (e.g., media controls over video).
     * Follows `chrome.s0` polarity: white in light, black in dark. Do not use for general UI surfaces.
     * @param backgroundCoreScrim Dimmed overlay for modals.
     * @param backgroundCoreOverlayDark Selected overlay (dark variant).
     * @param backgroundCoreOverlayLight Selected overlay (light variant).
     * @param backgroundCoreHighlight Highlight background (e.g. message focus/pin).
     * @param backgroundCoreApp Global application background.
     * @param backgroundUtilitySelected Selected overlay.
     * @param backgroundUtilityDisabled Disabled backgrounds.
     * @param borderCoreDefault Standard surface border.
     * @param borderCoreStrong Stronger surface border.
     * @param borderCoreSubtle Very light separators.
     * @param borderCoreOpacitySubtle Image frame border treatment (subtle).
     * @param borderCoreOpacityStrong Image frame border treatment (strong).
     * @param borderCoreOnAccent Borders on accent backgrounds.
     * @param borderCoreInverse Used on dark backgrounds.
     * @param borderUtilitySelected Selected overlay border.
     * @param borderUtilityActive Focus ring or focus border.
     * @param borderUtilityDisabled Optional disabled border for inputs, buttons, or chips.
     * @param borderUtilityError Error state border.
     * @param borderUtilityWarning Warning state border.
     * @param borderUtilitySuccess Success state border.
     * @param avatarBgPlaceholder Avatar placeholder background.
     * @param avatarPaletteBg1 Avatar background (slot 1).
     * @param avatarPaletteBg2 Avatar background (slot 2).
     * @param avatarPaletteBg3 Avatar background (slot 3).
     * @param avatarPaletteBg4 Avatar background (slot 4).
     * @param avatarPaletteBg5 Avatar background (slot 5).
     * @param avatarPaletteText1 Avatar text (slot 1).
     * @param avatarPaletteText2 Avatar text (slot 2).
     * @param avatarPaletteText3 Avatar text (slot 3).
     * @param avatarPaletteText4 Avatar text (slot 4).
     * @param avatarPaletteText5 Avatar text (slot 5).
     * @param avatarTextPlaceholder Avatar placeholder text.
     * @param avatarPresenceBorder The thin outline around the presence dot. Matches the local
     * surface behind the avatar; in high-contrast it uses the base surface.
     * @param skeletonLoadingBase Base color for the skeleton loading gradient (placeholder surfaces).
     * @param skeletonLoadingHighlight Highlight for the skeleton loading gradient (moving shimmer).
     *
     * The following component tokens alias **different** semantic tokens in light vs dark themes
     * and therefore cannot be expressed as a single derived expression:
     *
     * @param chatPollProgressFillOutgoing Outgoing poll progress fill.
     * @param chatReplyIndicatorIncoming Reply indicator shading for incoming messages.
     * @param chatReplyIndicatorOutgoing Reply indicator shading for outgoing messages.
     * @param controlPlaybackThumbBgDefault Playback thumb background in default state.
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
        public val backgroundCoreApp: Color,
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
        public val skeletonLoadingBase: Color,
        public val skeletonLoadingHighlight: Color,
        public val chatPollProgressFillOutgoing: Color,
        public val chatReplyIndicatorIncoming: Color,
        public val chatReplyIndicatorOutgoing: Color,
        public val controlPlaybackThumbBgDefault: Color,
    ) {

        /** Default badge background. */
        internal val badgeBgDefault: Color = backgroundCoreElevation3

        /** Badge background for error states. */
        internal val badgeBgError: Color = accentError

        /** Inverse badge background. */
        internal val badgeBgInverse: Color = chrome.s1000

        /** Badge background for neutral states. */
        internal val badgeBgNeutral: Color = accentNeutral

        /** Badge background when displayed as an overlay. */
        internal val badgeBgOverlay: Color = StreamPrimitiveColors.baseBlack.copy(alpha = .75f)

        /** Badge background for primary brand states. */
        internal val badgeBgPrimary: Color = accentPrimary

        /** Badge outer border. */
        internal val badgeBorder: Color = borderCoreInverse

        /** Badge text color. */
        internal val badgeText: Color = textPrimary

        /** Badge text color on accent backgrounds. */
        internal val badgeTextOnAccent: Color = textOnAccent

        /** Destructive button background. */
        internal val buttonDestructiveBg: Color = accentError

        /** Destructive button border. */
        internal val buttonDestructiveBorder: Color = accentError

        /** Destructive button text. */
        internal val buttonDestructiveText: Color = accentError

        /** Destructive button text on accent backgrounds. */
        internal val buttonDestructiveTextOnAccent: Color = textOnAccent

        /** Primary button background. */
        internal val buttonPrimaryBg: Color = accentPrimary

        /** Primary button border. */
        internal val buttonPrimaryBorder: Color = brand.s200

        /** Primary button text. */
        internal val buttonPrimaryText: Color = textLink

        /** Primary button text on accent backgrounds. */
        internal val buttonPrimaryTextOnAccent: Color = textOnAccent

        /** Secondary button background. */
        internal val buttonSecondaryBg: Color = backgroundCoreSurface

        /** Secondary button border. */
        internal val buttonSecondaryBorder: Color = borderCoreDefault

        /** Secondary button text. */
        internal val buttonSecondaryText: Color = textPrimary

        /** Secondary button text on accent backgrounds. */
        internal val buttonSecondaryTextOnAccent: Color = textPrimary

        /** Incoming bubble background. */
        internal val chatBgIncoming: Color = backgroundCoreSurface

        /** Outgoing message bubble background. */
        internal val chatBgOutgoing: Color = brand.s100

        /** Attachment card in incoming bubble. */
        internal val chatBgAttachmentIncoming: Color = backgroundCoreSurfaceStrong

        /** Attachment card in outgoing bubble. */
        internal val chatBgAttachmentOutgoing: Color = brand.s150

        /** Border for incoming message bubbles. */
        internal val chatBorderIncoming: Color = borderCoreSubtle

        /** Border for outgoing message bubbles. */
        internal val chatBorderOutgoing: Color = brand.s100

        /** Border on incoming message bubbles. */
        internal val chatBorderOnChatIncoming: Color = borderCoreStrong

        /** Border on outgoing message bubbles. */
        internal val chatBorderOnChatOutgoing: Color = brand.s300

        /** Incoming poll progress fill. */
        internal val chatPollProgressFillIncoming: Color = accentNeutral

        /** Incoming poll progress track. */
        internal val chatPollProgressTrackIncoming: Color = backgroundCoreSurfaceStrong

        /** Outgoing poll progress track. */
        internal val chatPollProgressTrackOutgoing: Color = brand.s200

        /** Incoming message text color. */
        internal val chatTextIncoming: Color = textPrimary

        /** Outgoing message text color. */
        internal val chatTextOutgoing: Color = brand.s900

        /** Links inside message bubbles. */
        internal val chatTextLink: Color = textLink

        /** Mention styling in chat messages. */
        internal val chatTextMention: Color = chatTextLink

        /** Reaction count text in chat. */
        internal val chatTextReaction: Color = textSecondary

        /** Read receipt text color. */
        internal val chatTextRead: Color = accentPrimary

        /** System messages like date separators. */
        internal val chatTextSystem: Color = textSecondary

        /** Time labels in chat messages. */
        internal val chatTextTimestamp: Color = textTertiary

        /** Typing indicator chip text. */
        internal val chatTextTypingIndicator: Color = textPrimary

        /** Username label in chat. */
        internal val chatTextUsername: Color = textSecondary

        /** Thread connector line for incoming messages. */
        internal val chatThreadConnectorIncoming: Color = borderCoreDefault

        /** Thread connector line for outgoing messages. */
        internal val chatThreadConnectorOutgoing: Color = brand.s150

        /** Audio waveform bar color. */
        internal val chatWaveformBar: Color = borderCoreOpacityStrong

        /** Audio waveform bar color when playing. */
        internal val chatWaveformBarPlaying: Color = accentPrimary

        /** Checkbox background (unselected). */
        internal val controlCheckboxBg: Color = Color.Transparent

        /** Checkbox background when selected. */
        internal val controlCheckboxBgSelected: Color = accentPrimary

        /** Checkbox border. */
        internal val controlCheckboxBorder: Color = borderCoreDefault

        /** Checkbox icon when selected. */
        internal val controlCheckboxIcon: Color = textOnAccent

        /** Chip border color. */
        internal val controlChipBorder: Color = borderCoreDefault

        /** Chip text color. */
        internal val controlChipText: Color = textPrimary

        /** Playback thumb background in active state. */
        internal val controlPlaybackThumbBgActive: Color = accentPrimary

        /** Playback thumb border in active state. */
        internal val controlPlaybackThumbBorderActive: Color = borderCoreOnAccent

        /** Playback thumb border in default state. */
        internal val controlPlaybackThumbBorderDefault: Color = borderCoreOpacityStrong

        /** Playback toggle border color. */
        internal val controlPlaybackToggleBorder: Color = borderCoreDefault

        /** Playback toggle text color. */
        internal val controlPlaybackToggleText: Color = textPrimary

        /** Play button background. */
        internal val controlPlayButtonBg: Color = chrome.s1000

        /** Play button icon. */
        internal val controlPlayButtonIcon: Color = textOnAccent

        /** Progress bar fill color. */
        internal val controlProgressBarFill: Color = accentNeutral

        /** Progress bar track color. */
        internal val controlProgressBarTrack: Color = backgroundCoreSurfaceStrong

        /** Indicator dot inside a selected radio button. */
        internal val controlRadioButtonIndicator: Color = textOnAccent

        /** Radio/check background (unselected). */
        internal val controlRadioCheckBg: Color = Color.Transparent

        /** Radio/check border. */
        internal val controlRadioCheckBorder: Color = borderCoreDefault

        /** Selected radio/check background. */
        internal val controlRadioCheckBgSelected: Color = accentPrimary

        /** Selected radio/check icon. */
        internal val controlRadioCheckIcon: Color = textOnAccent

        /** Remove control background. */
        internal val controlRemoveBg: Color = backgroundCoreInverse

        /** Remove control border. */
        internal val controlRemoveBorder: Color = borderCoreInverse

        /** Remove control icon. */
        internal val controlRemoveIcon: Color = textInverse

        /** Toggle switch track background. */
        internal val controlToggleSwitchBg: Color = accentNeutral

        /** Toggle switch track background when disabled. */
        internal val controlToggleSwitchBgDisabled: Color = backgroundUtilityDisabled

        /** Toggle switch track background when selected. */
        internal val controlToggleSwitchBgSelected: Color = accentPrimary

        /** Toggle switch knob color. */
        internal val controlToggleSwitchKnob: Color = backgroundCoreElevation4

        /** Default send icon color in the input. Uses the brand accent. */
        internal val inputSendIcon: Color = accentPrimary

        /** Send icon when disabled (e.g. empty input). */
        internal val inputSendIconDisabled: Color = textDisabled

        /** Main text inside the chat input. */
        internal val inputTextDefault: Color = textPrimary

        /** Input field disabled text color. */
        internal val inputTextDisabled: Color = textDisabled

        /** Icons inside the input area (attach, emoji, camera, send when idle). */
        internal val inputTextIcon: Color = textTertiary

        /** Placeholder text for the input. Lower emphasis than main text. */
        internal val inputTextPlaceholder: Color = textTertiary

        /** Reaction bar background. */
        internal val reactionBg: Color = backgroundCoreElevation3

        /** Border around unselected reaction chips. Subtle in normal modes, strong in high-contrast. */
        internal val reactionBorder: Color = borderCoreDefault

        /** Emoji color inside reaction chips. Uses primary text to stay clearly legible. */
        internal val reactionEmoji: Color = textPrimary

        /** Count label next to the emoji inside the reaction chip. */
        internal val reactionText: Color = textPrimary

        /** Offline presence indicator. */
        internal val avatarPresenceBgOffline: Color = accentNeutral

        /** Online presence indicator. */
        internal val avatarPresenceBgOnline: Color = accentSuccess

        public companion object {
            /**
             * Provides the default colors for the light mode of the app.
             *
             * @param brand The brand color scale. Defaults to [ColorScale.defaultLight].
             * @param chrome The chrome color scale. Defaults to [ChromeScale.defaultLight].
             * @return A [Colors] instance holding our color palette.
             */
            public fun default(
                brand: ColorScale = ColorScale.defaultLight(),
                chrome: ChromeScale = ChromeScale.defaultLight(),
            ): Colors {
                return Colors(
                    brand = brand,
                    chrome = chrome,
                    accentPrimary = brand.s500,
                    accentError = StreamPrimitiveColors.red500,
                    accentSuccess = StreamPrimitiveColors.green400,
                    accentWarning = StreamPrimitiveColors.yellow400,
                    accentNeutral = chrome.s500,
                    textPrimary = chrome.s900,
                    textSecondary = chrome.s700,
                    textTertiary = chrome.s500,
                    textDisabled = chrome.s300,
                    textOnAccent = chrome.s0,
                    textInverse = chrome.s0,
                    textLink = brand.s500,
                    backgroundCoreElevation0 = chrome.s0,
                    backgroundCoreElevation1 = chrome.s0,
                    backgroundCoreElevation2 = chrome.s0,
                    backgroundCoreElevation3 = chrome.s0,
                    backgroundCoreElevation4 = chrome.s0,
                    backgroundCoreSurface = chrome.s100,
                    backgroundCoreSurfaceSubtle = chrome.s50,
                    backgroundCoreSurfaceStrong = chrome.s150,
                    backgroundCoreSurfaceCard = chrome.s50,
                    backgroundCoreInverse = chrome.s900,
                    backgroundCoreOnAccent = chrome.s0,
                    backgroundCoreScrim = StreamPrimitiveColors.slate900.copy(alpha = 0.5f),
                    backgroundCoreOverlayDark = StreamPrimitiveColors.slate900.copy(alpha = 0.25f),
                    backgroundCoreOverlayLight = Color(0xBFFFFFFF),
                    backgroundCoreHighlight = StreamPrimitiveColors.yellow50,
                    backgroundCoreApp = chrome.s0,
                    backgroundUtilitySelected = StreamPrimitiveColors.slate900.copy(alpha = 0.2f),
                    backgroundUtilityDisabled = chrome.s100,
                    borderCoreDefault = chrome.s150,
                    borderCoreStrong = chrome.s300,
                    borderCoreSubtle = chrome.s100,
                    borderCoreOpacitySubtle = StreamPrimitiveColors.slate900.copy(alpha = 0.1f),
                    borderCoreOpacityStrong = StreamPrimitiveColors.slate900.copy(alpha = 0.25f),
                    borderCoreOnAccent = chrome.s0,
                    borderCoreInverse = chrome.s0,
                    borderUtilitySelected = brand.s500,
                    borderUtilityActive = brand.s500,
                    borderUtilityDisabled = chrome.s100,
                    borderUtilityError = StreamPrimitiveColors.red500,
                    borderUtilityWarning = StreamPrimitiveColors.yellow400,
                    borderUtilitySuccess = StreamPrimitiveColors.green400,
                    avatarBgPlaceholder = chrome.s150,
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
                    avatarTextPlaceholder = chrome.s500,
                    avatarPresenceBorder = chrome.s0,
                    skeletonLoadingBase = Color.Transparent,
                    skeletonLoadingHighlight = StreamPrimitiveColors.baseWhite,
                    chatPollProgressFillOutgoing = brand.s500,
                    chatReplyIndicatorIncoming = StreamPrimitiveColors.slate400,
                    chatReplyIndicatorOutgoing = brand.s400,
                    controlPlaybackThumbBgDefault = chrome.s0,
                )
            }

            /**
             * Provides the default colors for the dark mode of the app.
             *
             * @param brand The brand color scale. Defaults to [ColorScale.defaultDark].
             * @param chrome The chrome color scale. Defaults to [ChromeScale.defaultDark].
             * @return A [Colors] instance holding our color palette.
             */
            public fun defaultDark(
                brand: ColorScale = ColorScale.defaultDark(),
                chrome: ChromeScale = ChromeScale.defaultDark(),
            ): Colors {
                return Colors(
                    brand = brand,
                    chrome = chrome,
                    accentPrimary = brand.s400,
                    accentError = StreamPrimitiveColors.red400,
                    accentSuccess = StreamPrimitiveColors.green300,
                    accentWarning = StreamPrimitiveColors.yellow300,
                    accentNeutral = chrome.s500,
                    textPrimary = chrome.s900,
                    textSecondary = chrome.s700,
                    textTertiary = chrome.s500,
                    textDisabled = chrome.s300,
                    textOnAccent = chrome.s1000,
                    textInverse = chrome.s0,
                    textLink = brand.s600,
                    backgroundCoreElevation0 = chrome.s0,
                    backgroundCoreElevation1 = chrome.s50,
                    backgroundCoreElevation2 = chrome.s100,
                    backgroundCoreElevation3 = chrome.s200,
                    backgroundCoreElevation4 = chrome.s300,
                    backgroundCoreSurface = chrome.s100,
                    backgroundCoreSurfaceSubtle = chrome.s50,
                    backgroundCoreSurfaceStrong = chrome.s150,
                    backgroundCoreSurfaceCard = chrome.s100,
                    backgroundCoreInverse = chrome.s900,
                    backgroundCoreOnAccent = chrome.s0,
                    backgroundCoreScrim = StreamPrimitiveColors.baseBlack.copy(alpha = 0.75f),
                    backgroundCoreOverlayDark = StreamPrimitiveColors.baseBlack.copy(alpha = 0.5f),
                    backgroundCoreOverlayLight = Color(0xBF000000),
                    backgroundCoreHighlight = StreamPrimitiveColors.yellow800,
                    backgroundCoreApp = chrome.s0,
                    backgroundUtilitySelected = StreamPrimitiveColors.baseWhite.copy(alpha = 0.25f),
                    backgroundUtilityDisabled = chrome.s100,
                    borderCoreDefault = chrome.s200,
                    borderCoreStrong = chrome.s300,
                    borderCoreSubtle = chrome.s100,
                    borderCoreOpacitySubtle = StreamPrimitiveColors.baseWhite.copy(alpha = .2f),
                    borderCoreOpacityStrong = StreamPrimitiveColors.baseWhite.copy(alpha = 0.25f),
                    borderCoreOnAccent = chrome.s1000,
                    borderCoreInverse = chrome.s0,
                    borderUtilitySelected = brand.s400,
                    borderUtilityActive = brand.s400,
                    borderUtilityDisabled = chrome.s100,
                    borderUtilityError = StreamPrimitiveColors.red400,
                    borderUtilityWarning = StreamPrimitiveColors.yellow300,
                    borderUtilitySuccess = StreamPrimitiveColors.green300,
                    avatarBgPlaceholder = chrome.s150,
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
                    avatarTextPlaceholder = chrome.s500,
                    avatarPresenceBorder = chrome.s0,
                    skeletonLoadingBase = Color.Transparent,
                    skeletonLoadingHighlight = StreamPrimitiveColors.baseWhite.copy(alpha = 0.2f),
                    chatPollProgressFillOutgoing = StreamPrimitiveColors.baseWhite,
                    chatReplyIndicatorIncoming = StreamPrimitiveColors.neutral500,
                    chatReplyIndicatorOutgoing = brand.s700,
                    controlPlaybackThumbBgDefault = chrome.s900,
                )
            }
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
     * val purple = StreamDesign.ColorScale.from(brandColor = Color(0xFF6200EE))
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

        /**
         * Returns a new scale with the stops mirrored around the center.
         *
         * `s50` ↔ `s900`, `s100` ↔ `s800`, etc., while `s400` stays in place.
         * Use this to create a dark-theme counterpart from a light brand ramp.
         */
        public fun inverted(): ColorScale = ColorScale(
            s50 = s900,
            s100 = s800,
            s150 = s700,
            s200 = s600,
            s300 = s500,
            s400 = s400,
            s500 = s300,
            s600 = s200,
            s700 = s150,
            s800 = s100,
            s900 = s50,
        )

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
             * Use [inverted] to obtain the dark-theme counterpart:
             * ```
             * val purple = ColorScale.from(brandColor = Color(0xFF6200EE))
             * val light  = Colors.default(brand = purple)
             * val dark   = Colors.defaultDark(brand = purple.inverted())
             * ```
             *
             * @param brandColor The core brand color (maps to [s500]).
             */
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
