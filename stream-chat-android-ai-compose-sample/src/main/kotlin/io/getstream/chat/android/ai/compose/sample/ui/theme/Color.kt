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

@file:Suppress("MagicNumber") // A palette of color literals.

package io.getstream.chat.android.ai.compose.sample.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val Blue600 = Color(0xFF0169CC)
private val White = Color(0xFFFFFFFF)
private val Grey200 = Color(0xFFECECEC)
private val Grey900 = Color(0xFF0D0D0D)
private val Grey500 = Color(0xFF5D5D5D)

private val Blue400 = Color(0xFF339CFF)
private val Grey800 = Color(0xFF212121)
private val Grey700 = Color(0xFF2F2F2F)
private val Grey600 = Color(0xFF424242)
private val Grey100 = Color(0xFFF3F3F3)

private val Red500 = Color(0xFFE02E2A)
private val Red900 = Color(0xFF4D100E)
private val Red50 = Color(0xFFFFF0F0)

private val BlueGrey40 = Color(0xFF6B7280)
private val BlueGrey80 = Color(0xFF9CA3AF)

internal val DarkColorScheme =
    darkColorScheme(
        primary = Blue400,
        onPrimary = White,
        primaryContainer = Blue400.copy(alpha = 0.2f),
        onPrimaryContainer = Blue400,
        secondary = BlueGrey80,
        onSecondary = White,
        secondaryContainer = BlueGrey80.copy(alpha = 0.2f),
        onSecondaryContainer = BlueGrey80,
        tertiary = Blue400,
        onTertiary = White,
        background = Grey800,
        onBackground = White,
        surface = Grey700,
        onSurface = White,
        surfaceVariant = Grey600,
        onSurfaceVariant = Grey100,
        outline = White.copy(alpha = 0.15f),
        outlineVariant = White.copy(alpha = 0.15f),
        error = Red500,
        onError = White,
        errorContainer = Red900.copy(alpha = 0.2f),
        onErrorContainer = Red500,
    )

internal val LightColorScheme =
    lightColorScheme(
        primary = Blue600,
        onPrimary = White,
        primaryContainer = Blue600.copy(alpha = 0.1f),
        onPrimaryContainer = Blue600,
        secondary = BlueGrey40,
        onSecondary = White,
        secondaryContainer = BlueGrey40.copy(alpha = 0.1f),
        onSecondaryContainer = BlueGrey40,
        tertiary = Blue600,
        onTertiary = White,
        background = White,
        onBackground = Grey900,
        surface = Grey100,
        onSurface = Grey900,
        surfaceVariant = Grey200,
        onSurfaceVariant = Grey500,
        outline = Grey900.copy(alpha = 0.1f),
        outlineVariant = Grey900.copy(alpha = 0.1f),
        error = Red500,
        onError = White,
        errorContainer = Red50.copy(alpha = 0.1f),
        onErrorContainer = Red500,
    )
