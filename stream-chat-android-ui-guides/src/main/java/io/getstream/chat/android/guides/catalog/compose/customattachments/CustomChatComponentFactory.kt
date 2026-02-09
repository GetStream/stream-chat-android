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

package io.getstream.chat.android.guides.catalog.compose.customattachments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.components.ComposerCancelIcon
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.guides.R
import io.getstream.chat.android.models.Attachment

/**
 * A custom [ChatComponentFactory] that adds support for date attachments.
 */
object CustomChatComponentFactory : ChatComponentFactory {
    @Composable
    override fun CustomAttachmentContent(state: AttachmentState, modifier: Modifier) {
        if (state.message.attachments.any { it.type == "date" }) {
            DateAttachmentContent(state, modifier)
        }
    }
}

/**
 * A custom [AttachmentFactory] that adds support for date attachments.
 */
val dateAttachmentFactory: AttachmentFactory = AttachmentFactory(
    canHandle = { attachments -> attachments.any { it.type == "date" } },
    previewContent = { modifier, attachments, onAttachmentRemoved ->
        DateAttachmentPreviewContent(
            modifier = modifier,
            attachments = attachments,
            onAttachmentRemoved = onAttachmentRemoved,
        )
    },
    textFormatter = { attachment ->
        attachment.extraData["payload"].toString()
    },
)

/**
 * Represents the UI shown in the message input preview before sending.
 *
 * @param attachments Selected attachments.
 * @param onAttachmentRemoved Handler when the user removes an attachment.
 * @param modifier Modifier for styling.
 */
@Composable
fun DateAttachmentPreviewContent(
    attachments: List<Attachment>,
    onAttachmentRemoved: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
) {
    val attachment = attachments.first { it.type == "date" }
    val formattedDate = attachment.extraData["payload"].toString()

    Box(
        modifier = modifier
            .wrapContentHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(color = ChatTheme.colors.barsBackground),
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(16.dp)
                .fillMaxWidth(),
            text = formattedDate,
            style = ChatTheme.typography.body,
            maxLines = 1,
            color = ChatTheme.colors.textHighEmphasis,
        )

        ComposerCancelIcon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp),
            onClick = { onAttachmentRemoved(attachment) },
        )
    }
}

/**
 * Represents the UI shown in the message list for date attachments.
 *
 * @param attachmentState The state of the attachment.
 * @param modifier Modifier for styling.
 */
@Composable
fun DateAttachmentContent(
    attachmentState: AttachmentState,
    modifier: Modifier = Modifier,
) {
    val attachment = attachmentState.message.attachments.first { it.type == "date" }
    val formattedDate = attachment.extraData["payload"].toString()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(ChatTheme.shapes.attachment)
            .background(ChatTheme.colors.infoAccent)
            .padding(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(id = R.drawable.ic_calendar),
                contentDescription = null,
                tint = ChatTheme.colors.textHighEmphasis,
            )

            Text(
                text = formattedDate,
                style = ChatTheme.typography.body,
                maxLines = 1,
                color = ChatTheme.colors.textHighEmphasis,
            )
        }
    }
}
