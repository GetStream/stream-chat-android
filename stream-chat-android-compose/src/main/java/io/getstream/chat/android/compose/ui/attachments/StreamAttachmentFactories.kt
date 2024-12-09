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
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.core.net.toUri
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.ui.attachments.content.onFileAttachmentContentItemClick
import io.getstream.chat.android.compose.ui.attachments.content.onFileUploadContentItemClick
import io.getstream.chat.android.compose.ui.attachments.content.onGiphyAttachmentContentClick
import io.getstream.chat.android.compose.ui.attachments.content.onLinkAttachmentContentClick
import io.getstream.chat.android.compose.ui.attachments.content.onMediaAttachmentContentItemClick
import io.getstream.chat.android.compose.ui.attachments.factory.AudioRecordAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.FileAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.GiphyAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.LinkAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.MediaAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.QuotedAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.UnsupportedAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.UploadAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.preview.MediaGalleryPreviewContract
import io.getstream.chat.android.compose.ui.attachments.preview.handler.AttachmentPreviewHandler
import io.getstream.chat.android.compose.ui.theme.StreamDimens
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModelFactory
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.images.resizing.StreamCdnImageResizing
import io.getstream.chat.android.ui.common.utils.GiphyInfoType
import io.getstream.chat.android.ui.common.utils.GiphySizingMode

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
     * @param getChatClient - A lambda that provides the ChatClient instance.
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
     * @param skipEnrichUrl Used by the media gallery. If set to true will skip enriching URLs when you update the
     * message by deleting an attachment contained within it. Set to false by default.
     * @param onUploadContentItemClick Lambda called when a uploading attachment content item gets clicked.
     * @param onLinkContentItemClick Lambda called when a link attachment content item gets clicked.
     * @param onGiphyContentItemClick Lambda called when a giphy attachment content item gets clicked.
     * @param onMediaContentItemClick Lambda called when a image or video attachment content item gets clicked.
     * @param onFileContentItemClick Lambda called when a file attachment content item gets clicked.
     * @param showFileSize Lambda called to determine if the file size should be shown for a given attachment.
     * @param skipTypes A list of [AttachmentFactory.Type] that should be skipped from the default factories.
     *
     * @return A [List] of various [AttachmentFactory] instances that provide different attachments support.
     */
    public fun defaultFactories(
        getChatClient: () -> ChatClient = { ChatClient.instance() },
        linkDescriptionMaxLines: Int = DEFAULT_LINK_DESCRIPTION_MAX_LINES,
        giphyInfoType: GiphyInfoType = GiphyInfoType.ORIGINAL,
        giphySizingMode: GiphySizingMode = GiphySizingMode.ADAPTIVE,
        contentScale: ContentScale = ContentScale.Crop,
        skipEnrichUrl: Boolean = false,
        onUploadContentItemClick: (
            Attachment,
            List<AttachmentPreviewHandler>,
        ) -> Unit = ::onFileUploadContentItemClick,
        onLinkContentItemClick: (context: Context, previewUrl: String) -> Unit = ::onLinkAttachmentContentClick,
        onGiphyContentItemClick: (context: Context, Url: String) -> Unit = ::onGiphyAttachmentContentClick,
        onMediaContentItemClick: (
            mediaGalleryPreviewLauncher: ManagedActivityResultLauncher<MediaGalleryPreviewContract.Input, MediaGalleryPreviewResult?>,
            message: Message,
            attachmentPosition: Int,
            videoThumbnailsEnabled: Boolean,
            streamCdnImageResizing: StreamCdnImageResizing,
            skipEnrichUrl: Boolean,
        ) -> Unit = ::onMediaAttachmentContentItemClick,
        showFileSize: (Attachment) -> Boolean = { true },
        onFileContentItemClick: (
            previewHandlers: List<AttachmentPreviewHandler>,
            attachment: Attachment,
        ) -> Unit = ::onFileAttachmentContentItemClick,
        skipTypes: List<AttachmentFactory.Type> = emptyList(),
    ): List<AttachmentFactory> = listOf(
        UploadAttachmentFactory(
            onContentItemClick = onUploadContentItemClick,
        ),
        AudioRecordAttachmentFactory(
            viewModelFactory = AudioPlayerViewModelFactory(
                getAudioPlayer = { getChatClient().audioPlayer },
                getRecordingUri = { it.assetUrl ?: it.upload?.toUri()?.toString() },
            ),
            getCurrentUserId = { getChatClient().getCurrentOrStoredUserId() },
        ),
        LinkAttachmentFactory(
            linkDescriptionMaxLines = linkDescriptionMaxLines,
            onContentItemClick = onLinkContentItemClick,
        ),
        GiphyAttachmentFactory(
            giphyInfoType = giphyInfoType,
            giphySizingMode = giphySizingMode,
            contentScale = contentScale,
            onContentItemClick = onGiphyContentItemClick,
        ),
        MediaAttachmentFactory(
            skipEnrichUrl = skipEnrichUrl,
            onContentItemClick = onMediaContentItemClick,
        ),
        FileAttachmentFactory(
            showFileSize = showFileSize,
            onContentItemClick = onFileContentItemClick,
        ),
        UnsupportedAttachmentFactory,
    ).filterNot { skipTypes.contains(it.type) }

    /**
     * Default quoted attachment factories we provide, which can transform image, file and link attachments.
     *
     * @return a [List] of various [AttachmentFactory] instances that provide different quoted attachments support.
     */
    public fun defaultQuotedFactories(): List<AttachmentFactory> = listOf(
        QuotedAttachmentFactory,
    )
}
