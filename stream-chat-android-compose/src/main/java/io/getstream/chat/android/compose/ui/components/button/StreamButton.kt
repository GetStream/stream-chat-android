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
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.ifNotNull

@Composable
internal fun StreamTextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: StreamButtonStyle = StreamButtonStyleDefaults.primarySolid,
    size: StreamButtonSize = StreamButtonSize.Medium,
    leadingIcon: Painter? = null,
    trailingIcon: Painter? = null,
) {
    StreamButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        style = style,
        size = size,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = StreamTokens.spacingSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingSm, Alignment.CenterHorizontally),
        ) {
            leadingIcon?.let { Icon(painter = it, contentDescription = null) }
            Text(text)
            trailingIcon?.let { Icon(painter = it, contentDescription = null) }
        }
    }
}

@Composable
internal fun StreamButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: StreamButtonStyle = StreamButtonStyleDefaults.primarySolid,
    size: StreamButtonSize = StreamButtonSize.Medium,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .defaultMinSize(size.minimumSize, size.minimumSize)
            .clip(CircleShape)
            .ifNotNull(style.containerColor(enabled), Modifier::background)
            .ifNotNull(style.borderColor(enabled)) { border(1.dp, it, CircleShape) }
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

        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            LocalTextStyle provides ChatTheme.typography.bodyEmphasis,
            content = content,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StreamButtonPreview() {
    ChatTheme {
        val styles = listOf(
            StreamButtonStyleDefaults.primarySolid,
            StreamButtonStyleDefaults.primaryOutline,
            StreamButtonStyleDefaults.primaryGhost,
            StreamButtonStyleDefaults.secondarySolid,
            StreamButtonStyleDefaults.secondaryOutline,
            StreamButtonStyleDefaults.secondaryGhost,
            StreamButtonStyleDefaults.destructiveSolid,
            StreamButtonStyleDefaults.destructiveOutline,
            StreamButtonStyleDefaults.destructiveGhost,
        )

        Column(
            modifier = Modifier.padding(StreamTokens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
        ) {
            styles.forEach { style ->
                Row(horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs)) {
                    val painter = painterResource(R.drawable.stream_compose_ic_checkmark)
                    StreamButton(onClick = {}, style = style) {
                        Icon(painter, null)
                    }
                    StreamTextButton(
                        onClick = {},
                        style = style,
                        leadingIcon = painter,
                        text = "{{ label }}",
                        trailingIcon = painter,
                    )
                }
            }
        }
    }
}
