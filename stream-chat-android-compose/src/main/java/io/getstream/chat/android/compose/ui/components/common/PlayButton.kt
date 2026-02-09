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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.ifNotNull

/**
 * Default play button component. Used, for example, as an overlay for video attachments.
 *
 * @param modifier The modifier used for styling.
 * @param contentDescription Text used by accessibility services to describe what this button represents.
 */
@Composable
internal fun PlayButton(
    size: PlayButtonSize,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val colors = ChatTheme.colors
    Box(
        modifier = modifier
            .size(size.componentSize)
            .background(colors.controlPlayControlBg, CircleShape)
            .ifNotNull(contentDescription) { description ->
                semantics { this.contentDescription = description }
            },
        contentAlignment = Alignment.Companion.Center,
    ) {
        Icon(
            modifier = Modifier.Companion.size(size.iconSize),
            painter = painterResource(id = R.drawable.stream_compose_ic_play),
            contentDescription = null,
            tint = colors.controlPlayControlIcon,
        )
    }
}

internal enum class PlayButtonSize(val componentSize: Dp, val iconSize: Dp) {
    Large(componentSize = 48.dp, iconSize = 20.dp),
    Medium(componentSize = 40.dp, iconSize = 16.dp),
    Small(componentSize = 20.dp, iconSize = 10.dp),
}

@Preview
@Composable
private fun PlayButtonPreview() {
    ChatTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayButtonSize.entries.forEach { size ->
                PlayButton(size)
            }
        }
    }
}
