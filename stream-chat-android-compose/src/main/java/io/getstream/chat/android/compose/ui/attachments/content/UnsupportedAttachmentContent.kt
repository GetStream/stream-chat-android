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

package io.getstream.chat.android.compose.ui.attachments.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.components.attachments.files.FileTypeIcon
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.MimeTypeIconProvider
import io.getstream.chat.android.compose.ui.util.applyIf
import io.getstream.chat.android.compose.ui.util.shouldBeDisplayedAsFullSizeAttachment
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Message

/**
 * Represents fallback content for unsupported attachments.
 *
 * @param state The state of the attachment to show.
 * @param modifier Modifier for styling.
 */
@Composable
public fun UnsupportedAttachmentContent(
    state: AttachmentState,
    modifier: Modifier = Modifier,
) {
    val shouldBeFullSize = state.message.shouldBeDisplayedAsFullSizeAttachment()

    Column(modifier = modifier) {
        for (attachment in state.message.attachments) {
            if (attachment.type !in supportedAttachmentTypes) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .applyIf(!shouldBeFullSize) {
                            val color = MessageStyling.attachmentBackgroundColor(state.isMine)
                            padding(MessageStyling.messageSectionPadding)
                                .background(color, RoundedCornerShape(StreamTokens.radiusLg))
                        }
                        .padding(StreamTokens.spacingSm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FileTypeIcon(
                        data = MimeTypeIconProvider.getIcon(attachment.mimeType),
                        modifier = Modifier.size(height = 40.dp, width = 35.dp),
                    )

                    Text(
                        text = stringResource(id = R.string.stream_compose_message_list_unsupported_attachment),
                        style = ChatTheme.typography.captionEmphasis,
                        color = MessageStyling.textColor(outgoing = state.isMine),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = StreamTokens.spacingSm),
                    )
                }
            }
        }
    }
}

private val supportedAttachmentTypes = setOf(
    AttachmentType.IMAGE,
    AttachmentType.GIPHY,
    AttachmentType.VIDEO,
    AttachmentType.AUDIO,
    AttachmentType.FILE,
    AttachmentType.AUDIO_RECORDING,
)

@Preview(showBackground = true)
@Composable
private fun OwnUnsupportedAttachmentContentPreview() {
    ChatTheme {
        UnsupportedAttachmentContent(
            state = AttachmentState(
                message = Message(
                    attachments = mutableListOf(
                        Attachment(type = "unknown"),
                        Attachment(type = "custom_type"),
                    ),
                ),
                isMine = true,
            ),
        )
    }
}
