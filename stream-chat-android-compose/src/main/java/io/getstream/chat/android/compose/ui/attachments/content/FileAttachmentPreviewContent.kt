/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.components.CancelIcon
import io.getstream.chat.android.compose.ui.components.composer.MessageInput
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.utils.MediaStringUtil

/**
 * UI for currently selected file attachments, within the [MessageInput].
 *
 * @param attachments Selected attachments.
 * @param onAttachmentRemoved Handler when the user removes an attachment from the list.
 * @param modifier Modifier for styling.
 */
@Composable
public fun FileAttachmentPreviewContent(
    attachments: List<Attachment>,
    onAttachmentRemoved: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
    ) {
        items(attachments) { attachment ->
            Surface(
                modifier = Modifier.padding(1.dp),
                color = ChatTheme.colors.appBackground,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ChatTheme.colors.borders),
            ) {
                Row(
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp)
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FileAttachmentImage(attachment = attachment)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = attachment.title ?: attachment.name ?: "",
                            style = ChatTheme.typography.bodyBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = ChatTheme.colors.textHighEmphasis,
                        )

                        val fileSize = attachment.upload?.length()?.let { length ->
                            MediaStringUtil.convertFileSizeByteCount(length)
                        }
                        if (fileSize != null) {
                            Text(
                                text = fileSize,
                                style = ChatTheme.typography.footnote,
                                color = ChatTheme.colors.textLowEmphasis,
                            )
                        }
                    }

                    CancelIcon(
                        modifier = Modifier.padding(4.dp),
                        onClick = { onAttachmentRemoved(attachment) },
                    )
                }
            }
        }
    }
}
