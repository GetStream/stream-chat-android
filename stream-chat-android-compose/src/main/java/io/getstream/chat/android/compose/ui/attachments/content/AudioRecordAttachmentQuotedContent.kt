/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.extensions.durationInMs
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Attachment

/**
 * Builds an audio record attachment quoted message which shows a single audio in the attachments list.
 *
 * @param attachment The attachment we wish to show to users.
 */
@Composable
public fun AudioRecordAttachmentQuotedContent(
    attachment: Attachment,
    modifier: Modifier = Modifier,
) {
    val durationInMs = attachment.durationInMs ?: 0

    Row(
        modifier = modifier
            .padding(
                start = ChatTheme.dimens.quotedMessageAttachmentStartPadding,
                top = ChatTheme.dimens.quotedMessageAttachmentTopPadding,
                bottom = ChatTheme.dimens.quotedMessageAttachmentBottomPadding,
                end = ChatTheme.dimens.quotedMessageAttachmentEndPadding,
            )
            .clip(ChatTheme.shapes.quotedAttachment),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_file_aac),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp),
        )

        Spacer(modifier = Modifier.size(ChatTheme.dimens.quotedMessageAttachmentSpacerHorizontal))

        Column {
            Text(
                text = stringResource(id = R.string.stream_compose_audio_recording_preview),
                style = ChatTheme.typography.bodyBold,
                color = ChatTheme.colors.textHighEmphasis,
            )
            Spacer(modifier = Modifier.size(ChatTheme.dimens.quotedMessageAttachmentSpacerVertical))
            Text(
                text = ChatTheme.durationFormatter.format(durationInMs),
                color = ChatTheme.colors.textLowEmphasis,
                style = ChatTheme.typography.footnote,
            )
        }
    }
}
