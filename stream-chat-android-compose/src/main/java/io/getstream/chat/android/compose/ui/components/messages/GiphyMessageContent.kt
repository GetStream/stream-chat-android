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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyleDefaults
import io.getstream.chat.android.compose.ui.components.button.StreamTextButton
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.list.CancelGiphy
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.common.state.messages.list.SendGiphy
import io.getstream.chat.android.ui.common.state.messages.list.ShuffleGiphy

/**
 * Represents the content of an ephemeral giphy message.
 *
 * @param message The ephemeral giphy message.
 * @param currentUser The current user that's logged in.
 * @param modifier Modifier for styling.
 * @param onGiphyActionClick Handler when the user clicks on action button.
 */
@Suppress("LongMethod")
@Composable
public fun GiphyMessageContent(
    message: Message,
    currentUser: User?,
    modifier: Modifier = Modifier,
    onGiphyActionClick: (GiphyAction) -> Unit = {},
) {
    val colors = ChatTheme.colors

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = StreamTokens.spacingSm, vertical = StreamTokens.spacingXs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
        ) {
            Icon(
                painter = painterResource(R.drawable.stream_compose_ic_eye_open),
                contentDescription = null,
                tint = colors.chatTextOutgoing,
            )
            Text(
                text = stringResource(R.string.stream_compose_only_visible_to_you),
                style = ChatTheme.typography.captionEmphasis,
                color = colors.chatTextOutgoing,
            )
        }

        val attachmentState = AttachmentState(
            message = message,
            isMine = message.user.id == currentUser?.id,
            onLongItemClick = {},
            onMediaGalleryPreviewResult = {},
        )
        ChatTheme.componentFactory.GiphyAttachmentContent(
            modifier = Modifier.fillMaxWidth(),
            state = attachmentState,
        )

        Row(
            modifier = Modifier
                .padding(vertical = StreamTokens.spacingXs)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
        ) {
            StreamTextButton(
                onClick = { onGiphyActionClick(SendGiphy(message)) },
                text = stringResource(R.string.stream_compose_message_list_giphy_send),
                style = StreamButtonStyleDefaults.primaryGhost,
                modifier = Modifier.weight(1f),
            )

            StreamTextButton(
                onClick = { onGiphyActionClick(ShuffleGiphy(message)) },
                text = stringResource(R.string.stream_compose_message_list_giphy_shuffle),
                style = StreamButtonStyleDefaults.secondaryGhost,
                modifier = Modifier.weight(1f),
            )

            StreamTextButton(
                onClick = { onGiphyActionClick(CancelGiphy(message)) },
                text = stringResource(R.string.stream_compose_message_list_giphy_cancel),
                style = StreamButtonStyleDefaults.secondaryGhost,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview
@Composable
private fun GiphyMessageContentPreview() {
    val attachment = Attachment(type = AttachmentType.GIPHY, ogUrl = "")
    ChatTheme {
        Box(Modifier.background(MessageStyling.backgroundColor(true))) {
            GiphyMessageContent(
                message = Message(attachments = listOf(attachment)),
                currentUser = null,
                onGiphyActionClick = {},
            )
        }
    }
}
