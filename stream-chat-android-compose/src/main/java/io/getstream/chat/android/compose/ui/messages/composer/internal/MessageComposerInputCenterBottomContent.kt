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

package io.getstream.chat.android.compose.ui.messages.composer.internal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.common.Checkbox
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens

@Composable
internal fun MessageComposerInputCenterBottomContent(
    alsoSendToChannel: Boolean,
    onAlsoSendToChannelChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onAlsoSendToChannelChange(!alsoSendToChannel) },
            )
            .padding(
                start = StreamTokens.spacingMd,
                end = StreamTokens.spacingMd,
                bottom = StreamTokens.spacingMd,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        Checkbox(
            modifier = Modifier.testTag("Stream_AlsoSendToChannel"),
            checked = alsoSendToChannel,
            onCheckedChange = onAlsoSendToChannelChange,
            interactionSource = interactionSource,
        )
        Text(
            text = stringResource(R.string.stream_compose_message_composer_show_in_channel),
            color = if (alsoSendToChannel) {
                ChatTheme.colors.textPrimary
            } else {
                ChatTheme.colors.textTertiary
            },
            style = ChatTheme.typography.metadataDefault,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectedPreview() {
    ChatPreviewTheme {
        MessageComposerInputCenterBottomContent(
            alsoSendToChannel = true,
            onAlsoSendToChannelChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UnselectedPreview() {
    ChatPreviewTheme {
        MessageComposerInputCenterBottomContent(
            alsoSendToChannel = false,
            onAlsoSendToChannelChange = {},
        )
    }
}
