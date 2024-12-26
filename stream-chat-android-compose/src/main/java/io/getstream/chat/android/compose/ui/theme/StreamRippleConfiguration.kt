/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Configuration for customizing the ripple effect on the composable components.
 *
 * @param color The color of the ripple effect.
 * @param rippleAlpha The alpha of the ripple effect.
 */
public class StreamRippleConfiguration(
    public val color: Color,
    public val rippleAlpha: RippleAlpha,
) {

    public companion object {

        private const val LUMINANCE_THRESHOLD = 0.5

        // Note: Values taken from material:RippleDefaults to keep it backwards compatible
        private val LightThemeHighContrastRippleAlpha = RippleAlpha(
            pressedAlpha = 0.24f,
            focusedAlpha = 0.24f,
            draggedAlpha = 0.16f,
            hoveredAlpha = 0.08f,
        )

        // Note: Values taken from material:RippleDefaults to keep it backwards compatible
        private val LightThemeLowContrastRippleAlpha = RippleAlpha(
            pressedAlpha = 0.12f,
            focusedAlpha = 0.12f,
            draggedAlpha = 0.08f,
            hoveredAlpha = 0.04f,
        )

        // Note: Values taken from material:RippleDefaults to keep it backwards compatible
        private val DarkThemeRippleAlpha = RippleAlpha(
            pressedAlpha = 0.10f,
            focusedAlpha = 0.12f,
            draggedAlpha = 0.08f,
            hoveredAlpha = 0.04f,
        )

        /**
         * Creates the default [StreamRippleConfiguration].
         *
         * @param contentColor The current content color.
         * @param lightTheme Indicator if the system is in light theme.
         */
        @Composable
        public fun defaultRippleConfiguration(contentColor: Color, lightTheme: Boolean): StreamRippleConfiguration =
            StreamRippleConfiguration(
                color = rippleColor(contentColor, lightTheme),
                rippleAlpha = rippleAlpha(contentColor, lightTheme),
            )

        private fun rippleColor(contentColor: Color, lightTheme: Boolean): Color {
            val contentLuminance = contentColor.luminance()
            return if (!lightTheme && contentLuminance < LUMINANCE_THRESHOLD) {
                Color.White
            } else {
                contentColor
            }
        }

        private fun rippleAlpha(contentColor: Color, lightTheme: Boolean): RippleAlpha {
            return when {
                lightTheme -> {
                    if (contentColor.luminance() > LUMINANCE_THRESHOLD) {
                        LightThemeHighContrastRippleAlpha
                    } else {
                        LightThemeLowContrastRippleAlpha
                    }
                }
                else -> {
                    DarkThemeRippleAlpha
                }
            }
        }
    }
}

/**
 * Maps a [StreamRippleConfiguration] to the android [RippleConfiguration].
 * Used to hide the internal implementation of the ripple configuration, and not expose it outside of [ChatTheme].
 */
@OptIn(ExperimentalMaterial3Api::class)
internal fun StreamRippleConfiguration.toRippleConfiguration(): RippleConfiguration =
    RippleConfiguration(color = color, rippleAlpha = rippleAlpha)
