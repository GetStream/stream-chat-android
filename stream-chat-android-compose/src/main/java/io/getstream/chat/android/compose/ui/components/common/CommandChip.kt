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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.previewdata.PreviewCommandData

/**
 * A pill-shaped chip displayed inside the composer input row when a command is active.
 */
@Composable
internal fun CommandChip(
    command: Command,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .background(
                color = ChatTheme.colors.backgroundCoreInverse,
                shape = RoundedCornerShape(StreamTokens.radiusFull),
            )
            .clickable(onClick = onDismiss)
            .padding(horizontal = StreamTokens.spacingXs, vertical = StreamTokens.spacing2xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
    ) {
        Icon(
            modifier = Modifier.size(StreamTokens.size12),
            painter = painterResource(id = R.drawable.stream_compose_ic_command_chip),
            contentDescription = null,
            tint = ChatTheme.colors.badgeTextInverse,
        )
        Text(
            text = command.name.uppercase(),
            style = ChatTheme.typography.metadataEmphasis,
            color = ChatTheme.colors.badgeTextInverse,
        )
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_command_chip_cancel),
            contentDescription = stringResource(id = R.string.stream_compose_cancel),
            tint = ChatTheme.colors.badgeTextInverse,
        )
    }
}

@Preview
@Composable
private fun CommandChipPreview() {
    ChatTheme {
        CommandChip(
            modifier = Modifier.padding(8.dp),
            command = PreviewCommandData.command1,
        )
    }
}
