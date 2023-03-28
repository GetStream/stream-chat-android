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

package io.getstream.chat.android.compose.ui.attachments

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResult
import io.getstream.chat.android.compose.ui.attachments.factory.FileAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.GiphyAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.ImageAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.LinkAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.QuotedAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.UploadAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.preview.ImagePreviewContract
import io.getstream.chat.android.compose.ui.attachments.preview.handler.AttachmentPreviewHandler
import io.getstream.chat.android.compose.ui.theme.StreamDimens
import io.getstream.chat.android.ui.utils.GiphyInfoType
import io.getstream.chat.android.ui.utils.GiphySizingMode
import io.getstream.chat.android.uiutils.extension.isUploading

/**
 * Provides different attachment factories that build custom message content based on a given attachment.
 */
public object StreamAttachmentFactories {

    /**
     * The default max length of the link attachments description. We limit this, because for some links the description
     * can be too long.
     */
    private const val DEFAULT_LINK_DESCRIPTION_MAX_LINES = 5

    /**
     * Default attachment factories we provide, which can transform image, file and link attachments.
     *
     * @param linkDescriptionMaxLines - The limit of how long the link attachment descriptions can be.
     * @param giphyInfoType Used to modify the quality and dimensions of the rendered
     * Giphy attachments.
     * @param giphySizingMode Sets the Giphy container sizing strategy. Setting it to automatic
     * makes the container capable of adaptive resizing and ignore
     * [StreamDimens.attachmentsContentGiphyWidth] and [StreamDimens.attachmentsContentGiphyHeight]
     * dimensions, however you can still clip maximum dimensions using
     * [StreamDimens.attachmentsContentGiphyMaxWidth] and [StreamDimens.attachmentsContentGiphyMaxHeight].
     * Setting it to fixed size mode will make it respect all given dimensions.
     * @param contentScale Used to determine the way Giphys are scaled inside the [Image] composable.
     * @param skipEnrichUrl Used by the image gallery. If set to true will skip enriching URLs when you update the
     * message by deleting an attachment contained within it. Set to false by default.
     * @param onUploadContentItemClicked Lambda called when a uploading attachment content item gets clicked.
     * @param onLinkContentItemClicked Lambda called when a link attachment content item gets clicked.
     * @param onGiphyContentItemClick Lambda called when a giphy attachment content item gets clicked.
     * @param onImageContentItemClicked Lambda called when an image attachment content item gets clicked.
     * @param onFileContentItemClick Lambda called when a file attachment content item gets clicked.

     *
     * @return A [List] of various [AttachmentFactory] instances that provide different attachments support.
     */
    public fun defaultFactories(
        linkDescriptionMaxLines: Int = DEFAULT_LINK_DESCRIPTION_MAX_LINES,
        giphyInfoType: GiphyInfoType = GiphyInfoType.ORIGINAL,
        giphySizingMode: GiphySizingMode = GiphySizingMode.ADAPTIVE,
        contentScale: ContentScale = ContentScale.Crop,
        skipEnrichUrl: Boolean = false,
        onUploadContentItemClicked: (Attachment, List<AttachmentPreviewHandler>) -> Unit = { attachment, previewHandlers ->
            if (!attachment.isUploading()) {
                previewHandlers
                    .firstOrNull { it.canHandle(attachment) }
                    ?.handleAttachmentPreview(attachment)
            }
        },
        onLinkContentItemClicked: (context: Context, previewUrl: String) -> Unit = { context, previewUrl ->
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(previewUrl)
                )
            )
        },
        onGiphyContentItemClick: (context: Context, Url: String) -> Unit = { context, url ->
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )
        },
        onImageContentItemClicked: (
            imagePreviewLauncher: ManagedActivityResultLauncher<ImagePreviewContract.Input, ImagePreviewResult?>,
            message: Message,
            attachmentPosition: Int,
            skipEnrichUrl: Boolean,
        ) -> Unit = { imagePreviewLauncher, messageClicked, clickedAttachmentPosition, skipEnrichUrl ->
            imagePreviewLauncher.launch(
                ImagePreviewContract.Input(
                    messageId = messageClicked.id,
                    initialPosition = clickedAttachmentPosition,
                    skipEnrichUrl = skipEnrichUrl,
                )
            )
        },
        onFileContentItemClick: (
            previewHandler: List<AttachmentPreviewHandler>,
            attachment: Attachment,
        ) -> Unit = { previewHandlers, attachment ->
            previewHandlers
                .firstOrNull { it.canHandle(attachment) }
                ?.handleAttachmentPreview(attachment)
        },
    ): List<AttachmentFactory> = listOf(
        UploadAttachmentFactory(
            onContentItemClicked = onUploadContentItemClicked
        ),
        LinkAttachmentFactory(
            linkDescriptionMaxLines = linkDescriptionMaxLines,
            onContentItemClicked = onLinkContentItemClicked,
        ),
        GiphyAttachmentFactory(
            giphyInfoType = giphyInfoType,
            giphySizingMode = giphySizingMode,
            contentScale = contentScale,
            onContentItemClick = onGiphyContentItemClick,
        ),
        ImageAttachmentFactory(
            skipEnrichUrl = skipEnrichUrl,
            onContentItemClick = onImageContentItemClicked,
        ),
        FileAttachmentFactory(
            onContentItemClick = onFileContentItemClick,
        ),
    )

    /**
     * Default quoted attachment factories we provide, which can transform image, file and link attachments.
     *
     * @return a [List] of various [AttachmentFactory] instances that provide different quoted attachments support.
     */
    public fun defaultQuotedFactories(): List<AttachmentFactory> = listOf(
        QuotedAttachmentFactory()
    )
}
