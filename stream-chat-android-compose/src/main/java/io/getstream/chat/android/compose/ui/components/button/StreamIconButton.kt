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

package io.getstream.chat.android.compose.ui.components.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamSpacings

@Composable
internal fun StreamIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier.Companion,
    enabled: Boolean = true,
    style: StreamButtonStyle = StreamButtonStyleDefaults.primarySolid,
    size: StreamButtonSize = StreamButtonSize.Medium,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
        modifier
            .defaultMinSize(size.minimumSize, size.minimumSize)
            .clip(CircleShape)
            .background(color = style.containerColor(enabled))
            .run { style.border(enabled)?.let { border(it, CircleShape) } ?: this }
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = remember(::MutableInteractionSource),
                indication = ripple(),
            ),
        contentAlignment = Alignment.Center,
    ) {
        val contentColor = style.contentColor(enabled)
        CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
    }
}

@Preview(showBackground = true)
@Composable
private fun StreamIconButtonPreview() {
    ChatTheme {
        val styles = listOf(
            StreamButtonStyleDefaults.primarySolid,
            StreamButtonStyleDefaults.primaryGhost,
            StreamButtonStyleDefaults.secondaryOutline,
            StreamButtonStyleDefaults.secondaryGhost,
            StreamButtonStyleDefaults.destructiveSolid,
            StreamButtonStyleDefaults.destructiveGhost,
        )

        Column(
            modifier = Modifier.padding(StreamSpacings.md),
            verticalArrangement = Arrangement.spacedBy(StreamSpacings.xs),
        ) {
            styles.forEach { style ->
                Row(horizontalArrangement = Arrangement.spacedBy(StreamSpacings.md)) {
                    StreamIconButton(onClick = {}, style = style) {
                        Icon(Icons.Default.Add, null)
                    }
                    StreamIconButton(onClick = {}, style = style, enabled = false) {
                        Icon(Icons.Default.Add, null)
                    }
                }
            }
        }
    }
}
