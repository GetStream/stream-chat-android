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

package io.getstream.chat.android.ai.openui.rendering

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Visual tokens used by the built-in components. By default they derive from the surrounding
 * `MaterialTheme`; wrap content in [ProvideOpenUITheme] to match your design system.
 */
@Immutable
public data class OpenUITheme(
    val colors: Colors,
    val spacing: Spacing = Spacing(),
    val radius: Radius = Radius(),
    val typography: Typography,
) {
    @Immutable
    /** The color palette. */
    public data class Colors(
        val accent: Color,
        val onAccent: Color,
        val textPrimary: Color,
        val textSecondary: Color,
        val textTertiary: Color,
        /** Elevated card surfaces. */
        val surface: Color,
        /** Recessed ("sunk") surfaces and input backgrounds. */
        val sunkenSurface: Color,
        /** Subtle fills (chips, tags, table headers). */
        val subtleFill: Color,
        val border: Color,
        val success: Color = Color(0xFF219E5C),
        val warning: Color = Color(0xFFDB8C14),
        val danger: Color = Color(0xFFDB363D),
        val info: Color = Color(0xFF2973E8),
        val neutral: Color,
        /** Categorical chart colors. */
        val chartPalette: List<Color> = DefaultChartPalette,
    ) {
        /** The semantic color of a variant (`success`, `warning`, `danger`/`error`, `info`, `neutral`). */
        public fun variant(name: String?): Color = when (name) {
            "success" -> success
            "warning" -> warning
            "danger", "error" -> danger
            "info" -> info
            else -> neutral
        }

        /** The chart color for series [index], cycling through [chartPalette]. */
        public fun chartColor(index: Int): Color = chartPalette[Math.floorMod(index, chartPalette.size)]
    }

    /** OpenUI's spacing scale (`none`, `xs`, `s`, `m`, `l`, `xl`, `2xl`). */
    @Immutable
    public data class Spacing(
        val xs: Dp = 4.dp,
        val s: Dp = 8.dp,
        val m: Dp = 12.dp,
        val l: Dp = 16.dp,
        val xl: Dp = 24.dp,
        val xxl: Dp = 32.dp,
    ) {
        /** The size of spacing [token], or [default] for an unknown token. */
        public fun value(token: String?, default: Dp = m): Dp = when (token) {
            "none" -> 0.dp
            "xs" -> xs
            "s" -> s
            "m" -> m
            "l" -> l
            "xl" -> xl
            "2xl" -> xxl
            else -> default
        }
    }

    @Immutable
    /** Corner radii. */
    public data class Radius(val small: Dp = 8.dp, val medium: Dp = 12.dp, val large: Dp = 16.dp, val pill: Dp = 999.dp)

    @Immutable
    /** Text styles. */
    public data class Typography(
        val title: TextStyle,
        val heading: TextStyle,
        val body: TextStyle,
        val bodyLarge: TextStyle,
        val bodySmall: TextStyle,
        val caption: TextStyle,
        val code: TextStyle,
        val metric: TextStyle,
    )

    /** Defaults. */
    public companion object {
        /** The default categorical chart colors. */
        public val DefaultChartPalette: List<Color> = listOf(
            Color(0xFF4078F5),
            Color(0xFF1CB59E),
            Color(0xFFFA9E29),
            Color(0xFFE04A75),
            Color(0xFF8C61ED),
            Color(0xFF29ADE0),
            Color(0xFF9EB833),
            Color(0xFFED6945),
        )

        /** A theme derived from the current `MaterialTheme`. */
        @Composable
        @ReadOnlyComposable
        public fun fromMaterial(): OpenUITheme {
            val scheme = MaterialTheme.colorScheme
            val type = MaterialTheme.typography
            return OpenUITheme(
                colors = Colors(
                    accent = scheme.primary,
                    onAccent = scheme.onPrimary,
                    textPrimary = scheme.onSurface,
                    textSecondary = scheme.onSurfaceVariant,
                    textTertiary = scheme.onSurfaceVariant.copy(alpha = 0.6f),
                    surface = scheme.surfaceContainerLow,
                    sunkenSurface = scheme.surfaceContainerHighest,
                    subtleFill = scheme.surfaceContainerHigh,
                    border = scheme.outlineVariant,
                    neutral = scheme.onSurfaceVariant,
                ),
                typography = Typography(
                    title = type.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    heading = type.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    body = type.bodyLarge,
                    bodyLarge = type.titleLarge,
                    bodySmall = type.bodyMedium,
                    caption = type.bodySmall,
                    code = type.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    metric = type.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                ),
            )
        }

        /** The theme in effect: the one provided with [ProvideOpenUITheme], or [fromMaterial]. */
        public val current: OpenUITheme
            @Composable
            @ReadOnlyComposable
            get() = LocalOpenUITheme.current ?: fromMaterial()
    }
}

internal val LocalOpenUITheme = staticCompositionLocalOf<OpenUITheme?> { null }

/** Applies [theme] to the OpenUI components rendered in [content]. */
@Composable
public fun ProvideOpenUITheme(theme: OpenUITheme, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalOpenUITheme provides theme, content = content)
}
