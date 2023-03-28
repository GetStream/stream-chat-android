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

package io.getstream.chat.android.compose.ui.attachments.factory

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import io.getstream.chat.android.client.utils.attachment.isGiphy
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.LinkAttachmentContent
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.uiutils.extension.hasLink

/**
 * An [AttachmentFactory] that validates attachments as images and uses [LinkAttachmentContent] to
 * build the UI for the message.
 *
 * Has no "preview content", given that this attachment only exists after being sent.
 *
 * @param linkDescriptionMaxLines - The limit of how many lines we show for the link description.
 * @param onContentItemClicked Lambda called when an item gets clicked.
 */
@Suppress("FunctionName")
public fun LinkAttachmentFactory(
    linkDescriptionMaxLines: Int,
    onContentItemClicked: (context: Context, previewUrl: String) -> Unit = { context, previewUrl ->
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(previewUrl)
            )
        )
    },
): AttachmentFactory = AttachmentFactory(
    canHandle = { links -> links.any { it.hasLink() && !it.isGiphy() } },
    content = @Composable { modifier, state ->
        LinkAttachmentContent(
            modifier = modifier
                .width(ChatTheme.dimens.attachmentsContentLinkWidth)
                .wrapContentHeight(),
            attachmentState = state,
            linkDescriptionMaxLines = linkDescriptionMaxLines,
            onItemClick = onContentItemClicked,
        )
    },
)
