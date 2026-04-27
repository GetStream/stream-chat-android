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

package io.getstream.chat.android.compose.ui.components.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.applyIf
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.extensions.isInt

@Composable
internal fun PlaybackSpeedToggle(
    speed: Float,
    outlineColor: Color = ChatTheme.colors.controlPlaybackToggleBorder,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    val colors = ChatTheme.colors
    val textColor = if (enabled) colors.controlPlaybackToggleText else colors.textDisabled
    val borderColor = if (enabled) outlineColor else colors.borderUtilityDisabled

    val textMeasurer = rememberTextMeasurer()
    val style = ChatTheme.typography.metadataEmphasis
    val minTextWidth = remember(textMeasurer, style) {
        textMeasurer.measure("x1.5", style).size.width
    }

    Text(
        text = if (speed.isInt()) "x${speed.toInt()}" else "x$speed",
        style = style,
        color = textColor,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .border(1.dp, borderColor, SpeedToggleShape)
            .clip(SpeedToggleShape)
            .applyIf(enabled) { clickable(onClick = onClick) }
            .padding(horizontal = StreamTokens.spacingXs, vertical = StreamTokens.spacing2xs)
            .widthIn(min = with(LocalDensity.current) { minTextWidth.toDp() }),
    )
}

private val SpeedToggleShape = RoundedCornerShape(StreamTokens.radiusLg)

@Preview
@Composable
private fun PlaybackSpeedTogglePreview() {
    ChatTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            PlaybackSpeedToggle(
                speed = 1.5f,
            )
            PlaybackSpeedToggle(
                speed = 1.5f,
                enabled = false,
            )
        }
    }
}
