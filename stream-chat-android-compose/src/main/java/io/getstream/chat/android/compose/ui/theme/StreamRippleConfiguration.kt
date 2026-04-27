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

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RippleConfiguration

@OptIn(ExperimentalMaterial3Api::class)
internal fun streamRippleConfiguration(
    colors: StreamDesign.Colors,
    lightTheme: Boolean,
): RippleConfiguration = RippleConfiguration(
    color = if (lightTheme) colors.chrome.s900 else colors.chrome.s1000,
    rippleAlpha = if (lightTheme) LightRippleAlpha else DarkRippleAlpha,
)

private val LightRippleAlpha = RippleAlpha(
    pressedAlpha = 0.15f,
    focusedAlpha = 0.15f,
    draggedAlpha = 0.10f,
    hoveredAlpha = 0.10f,
)

private val DarkRippleAlpha = RippleAlpha(
    pressedAlpha = 0.20f,
    focusedAlpha = 0.20f,
    draggedAlpha = 0.15f,
    hoveredAlpha = 0.15f,
)
