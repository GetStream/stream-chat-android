/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

@file:JvmName("ChannelInfoOptionKt")

package io.getstream.chat.android.compose.ui.channel.info

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.clickable

@Composable
internal fun ChannelInfoOption(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)?,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .run { onClick?.let { clickable(onClick = it) } ?: this }
            .minimumInteractiveComponentSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

@Composable
internal fun ChannelInfoOptionButton(
    @DrawableRes icon: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChannelInfoOption(
        modifier = modifier,
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = ChatTheme.typography.bodyBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun ChannelInfoOptionNavigationButton(
    @DrawableRes icon: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChannelInfoOption(
        modifier = modifier,
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(icon),
            contentDescription = null,
            tint = ChatTheme.colors.textLowEmphasis,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = ChatTheme.typography.bodyBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = ChatTheme.colors.textLowEmphasis,
        )
    }
}

@Composable
internal fun ChannelInfoOptionSwitch(
    @DrawableRes icon: Int,
    text: String,
    checked: Boolean,
    onCheckedChange: (checked: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ChannelInfoOption(
        modifier = modifier,
        onClick = { onCheckedChange(!checked) },
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = ChatTheme.colors.textLowEmphasis,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = ChatTheme.typography.bodyBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Switch(
            checked = checked,
            onCheckedChange = null, // Switch should not be interactable
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChannelInfoOptionButtonPreview() {
    ChatTheme {
        ChannelInfoOptionButton(
            icon = R.drawable.stream_compose_ic_delete,
            text = "Delete",
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChannelInfoOptionNavigationButtonPreview() {
    ChatTheme {
        ChannelInfoOptionNavigationButton(
            icon = R.drawable.stream_compose_ic_file_picker,
            text = "Files",
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChannelInfoOptionSwitchPreview() {
    ChatTheme {
        ChannelInfoOptionSwitch(
            icon = R.drawable.stream_compose_ic_mute,
            text = "Mute",
            checked = true,
            onCheckedChange = {},
        )
    }
}
