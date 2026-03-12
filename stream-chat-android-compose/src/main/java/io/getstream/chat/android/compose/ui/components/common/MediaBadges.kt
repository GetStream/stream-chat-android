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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun VideoBadge(
    durationInSeconds: Long,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    MediaBadge(
        durationInSeconds = durationInSeconds,
        iconRes = R.drawable.stream_compose_ic_video,
        modifier = modifier,
        compact = compact,
    )
}

@Composable
internal fun AudioBadge(
    durationInSeconds: Long,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    MediaBadge(
        durationInSeconds = durationInSeconds,
        iconRes = R.drawable.stream_compose_ic_mic_solid,
        modifier = modifier,
        compact = compact,
    )
}

@Composable
private fun MediaBadge(
    durationInSeconds: Long,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Row(
        modifier = modifier
            .background(
                shape = MediaBadgeShape,
                color = ChatTheme.colors.accentBlack,
            )
            .padding(
                horizontal = StreamTokens.spacingXs,
                vertical = StreamTokens.spacing2xs,
            ),
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = ChatTheme.colors.textOnAccent,
        )
        Text(
            text = if (compact) {
                durationInSeconds.toCompactDuration()
            } else {
                durationInSeconds.toPreciseDuration()
            },
            style = ChatTheme.typography.numericMedium,
            color = ChatTheme.colors.textOnAccent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val MediaBadgeShape = RoundedCornerShape(StreamTokens.radiusFull)

private fun Long.toCompactDuration(): String = seconds.toComponents { hours, minutes, secs, _ ->
    when {
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "${secs}s"
    }
}

private fun Long.toPreciseDuration(): String = seconds.toComponents { hours, minutes, secs, _ ->
    if (hours > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format(Locale.ROOT, "%d:%02d", minutes, secs)
    }
}

@Preview
@Composable
private fun MediaBadgeCompactPreview() {
    ChatTheme {
        Row {
            VideoBadge(durationInSeconds = 8, compact = true)
            AudioBadge(durationInSeconds = 8, compact = true)
        }
    }
}

@Preview
@Composable
private fun MediaBadgePrecisePreview() {
    ChatTheme {
        Row {
            VideoBadge(durationInSeconds = 8)
            AudioBadge(durationInSeconds = 8)
        }
    }
}
