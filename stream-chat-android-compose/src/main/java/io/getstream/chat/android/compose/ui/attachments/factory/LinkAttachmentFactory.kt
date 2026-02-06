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

package io.getstream.chat.android.compose.ui.attachments.factory

import android.content.Context
import io.getstream.chat.android.client.utils.attachment.isGiphy
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.LinkAttachmentClickData
import io.getstream.chat.android.compose.ui.attachments.content.LinkAttachmentContent
import io.getstream.chat.android.compose.ui.attachments.content.onLinkAttachmentContentClick
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.utils.extensions.hasLink

/**
 * An [AttachmentFactory] that validates attachments as images and uses [LinkAttachmentContent] to
 * build the UI for the message.
 *
 * Has no "preview content", given that this attachment only exists after being sent.
 *
 * @param linkDescriptionMaxLines - The limit of how many lines we show for the link description.
 * @param onLinkAttachmentContentClick Lambda called when an item gets clicked.
 * @param canHandle Lambda that checks if the factory can handle the given attachments.
 */
public class LinkAttachmentFactory(
    linkDescriptionMaxLines: Int,
    onItemClick: (LinkAttachmentClickData) -> Unit = {
        onLinkAttachmentContentClick(it.context, it.url)
    },
    canHandle: (attachments: List<Attachment>) -> Boolean = { links -> links.any { it.hasLink() && !it.isGiphy() } },
) : AttachmentFactory(
    type = Type.BuiltIn.LINK,
    canHandle = canHandle,
) {

    /**
     * Creates a new instance of [LinkAttachmentFactory] with the default parameters.
     */
    @Deprecated(
        message = "Use the constructor that does not take onContentItemClick parameter.",
        replaceWith = ReplaceWith("LinkAttachmentFactory(linkDescriptionMaxLines, onItemClick, canHandle)"),
        level = DeprecationLevel.WARNING,
    )
    public constructor(
        linkDescriptionMaxLines: Int,
        onContentItemClick: (context: Context, previewUrl: String) -> Unit,
        canHandle: (attachments: List<Attachment>) -> Boolean = { links ->
            links.any { it.hasLink() && !it.isGiphy() }
        },
    ) : this(
        linkDescriptionMaxLines = linkDescriptionMaxLines,
        onItemClick = {
            onContentItemClick(it.context, it.url)
        },
        canHandle = canHandle,
    )
}
