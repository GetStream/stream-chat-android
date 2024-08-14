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

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.MediaAttachmentContent
import io.getstream.chat.android.compose.ui.attachments.content.MediaAttachmentPreviewContent
import io.getstream.chat.android.compose.ui.attachments.content.PlayButton
import io.getstream.chat.android.compose.ui.attachments.content.onMediaAttachmentContentItemClick
import io.getstream.chat.android.compose.ui.attachments.preview.MediaGalleryPreviewContract
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.images.resizing.StreamCdnImageResizing

/**
 * An [AttachmentFactory] that is able to handle Image and Video attachments.
 *
 * @param maximumNumberOfPreviewedItems The maximum number of thumbnails that can be displayed
 * in a group when previewing Media attachments in the message list. Values between 4 and 8 are optimal.
 * @param skipEnrichUrl Used by the media gallery. If set to true will skip enriching URLs when you update the message
 * by deleting an attachment contained within it. Set to false by default.
 * @param onContentItemClick Lambda called when an item gets clicked.
 * @param itemOverlayContent Represents the content overlaid above individual items.
 * By default it is used to display a play button over video previews.
 * @param previewItemOverlayContent Represents the content overlaid above individual preview items.
 * By default it is used to display a play button over video previews.
 */
@Suppress("FunctionName")
public fun MediaAttachmentFactory(
    maximumNumberOfPreviewedItems: Int = 4,
    skipEnrichUrl: Boolean = false,
    onContentItemClick: (
        mediaGalleryPreviewLauncher: ManagedActivityResultLauncher<MediaGalleryPreviewContract.Input, MediaGalleryPreviewResult?>,
        message: Message,
        attachmentPosition: Int,
        videoThumbnailsEnabled: Boolean,
        streamCdnImageResizing: StreamCdnImageResizing,
        skipEnrichUrl: Boolean,
    ) -> Unit = ::onMediaAttachmentContentItemClick,
    itemOverlayContent: @Composable (attachmentType: String?) -> Unit = { attachmentType ->
        if (attachmentType == AttachmentType.VIDEO) {
            DefaultItemOverlayContent()
        }
    },
    previewItemOverlayContent: @Composable (attachmentType: String?) -> Unit = { attachmentType ->
        if (attachmentType == AttachmentType.VIDEO) {
            DefaultPreviewItemOverlayContent()
        }
    },
): AttachmentFactory =
    AttachmentFactory(
        canHandle = {
            it.none { attachment ->
                !attachment.isImage() && !attachment.isVideo()
            }
        },
        previewContent = { modifier, attachments, onAttachmentRemoved ->
            MediaAttachmentPreviewContent(
                attachments = attachments,
                onAttachmentRemoved = onAttachmentRemoved,
                modifier = modifier,
                previewItemOverlayContent = previewItemOverlayContent,
            )
        },
        content = @Composable { modifier, state ->
            MediaAttachmentContent(
                modifier = modifier,
                attachmentState = state,
                maximumNumberOfPreviewedItems = maximumNumberOfPreviewedItems,
                itemOverlayContent = itemOverlayContent,
                skipEnrichUrl = skipEnrichUrl,
                onItemClick = onContentItemClick,
            )
        },
    )

/**
 * Represents the default play button that is
 * overlaid above video attachment previews inside
 * the messages list.
 */
@Preview(name = "DefaultItemOverlayContent Preview")
@Composable
private fun DefaultItemOverlayContent() {
    PlayButton(
        modifier = Modifier
            .padding(2.dp)
            .shadow(6.dp, shape = CircleShape)
            .background(color = Color.White, shape = CircleShape)
            .fillMaxWidth(0.25f)
            .aspectRatio(1f),
    )
}

/**
 * Represents the default play button that is
 * overlaid above video attachment previews inside
 * the message input.
 */
@Preview(name = "DefaultPreviewItemOverlayContent Preview")
@Composable
internal fun DefaultPreviewItemOverlayContent() {
    PlayButton(
        modifier = Modifier
            .shadow(6.dp, shape = CircleShape)
            .background(color = Color.White, shape = CircleShape)
            .fillMaxSize(0.25f),
    )
}
