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

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.ImageRequest
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.attachments.preview.MediaGalleryInjector
import io.getstream.chat.android.compose.ui.attachments.preview.MediaGalleryPreviewContract
import io.getstream.chat.android.compose.ui.attachments.preview.MediaGalleryPreviewContract.Input
import io.getstream.chat.android.compose.ui.components.ShimmerProgressIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.AsyncImagePreviewHandler
import io.getstream.chat.android.compose.ui.util.LocalStreamImageLoader
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.compose.ui.util.applyIf
import io.getstream.chat.android.compose.ui.util.extensions.internal.imagePreviewData
import io.getstream.chat.android.compose.ui.util.ifNotNull
import io.getstream.chat.android.compose.ui.util.shouldBeDisplayedAsFullSizeAttachment
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.ui.common.helper.DownloadAttachmentUriGenerator
import io.getstream.chat.android.ui.common.helper.DownloadRequestInterceptor
import io.getstream.chat.android.ui.common.images.resizing.StreamCdnImageResizing

/**
 * Displays a preview of single or multiple video or attachments.
 *
 * @param attachmentState The state of the attachment, holding the root modifier, the message
 * and the onLongItemClick handler.
 * @param modifier The modifier used for styling.
 * @param maximumNumberOfPreviewedItems The maximum number of thumbnails that can be displayed
 * in a group when previewing Media attachments in the message list.
 * @param skipEnrichUrl Used by the media gallery. If set to true will skip enriching URLs when you update the message
 * by deleting an attachment contained within it. Set to false by default.
 * @param onItemClick Lambda called when an item gets clicked.
 * @param itemOverlayContent Represents the content overlaid above individual items.
 * By default it is used to display a play button over video previews.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
@Deprecated(
    message = "Use the overload that takes onItemClick as a single parameter of type MediaAttachmentClickData.",
    replaceWith = ReplaceWith(
        "MediaAttachmentContent(" +
            "state = attachmentState, " +
            "modifier = modifier, " +
            "maximumNumberOfPreviewedItems = maximumNumberOfPreviewedItems, " +
            "skipEnrichUrl = skipEnrichUrl, " +
            "onItemClick = onItemClick, " +
            "itemOverlayContent = itemOverlayContent" +
            ")",
    ),
    level = DeprecationLevel.WARNING,
)
public fun MediaAttachmentContent(
    attachmentState: AttachmentState,
    modifier: Modifier = Modifier,
    maximumNumberOfPreviewedItems: Int = 4,
    skipEnrichUrl: Boolean = false,
    onItemClick: (
        mediaGalleryPreviewLauncher: ManagedActivityResultLauncher<Input, MediaGalleryPreviewResult?>,
        message: Message,
        attachmentPosition: Int,
        videoThumbnailsEnabled: Boolean,
        downloadAttachmentUriGenerator: DownloadAttachmentUriGenerator,
        downloadRequestInterceptor: DownloadRequestInterceptor,
        streamCdnImageResizing: StreamCdnImageResizing,
        skipEnrichUrl: Boolean,
    ) -> Unit = ::onMediaAttachmentContentItemClick,
    itemOverlayContent: @Composable (attachmentType: String?) -> Unit = { attachmentType ->
        if (attachmentType == AttachmentType.VIDEO) {
            PlayButton()
        }
    },
) {
    MediaAttachmentContent(
        state = attachmentState,
        modifier = modifier,
        maximumNumberOfPreviewedItems = maximumNumberOfPreviewedItems,
        skipEnrichUrl = skipEnrichUrl,
        onItemClick = {
            onItemClick(
                it.mediaGalleryPreviewLauncher,
                it.message,
                it.attachmentPosition,
                it.videoThumbnailsEnabled,
                it.downloadAttachmentUriGenerator,
                it.downloadRequestInterceptor,
                it.streamCdnImageResizing,
                it.skipEnrichUrl,
            )
        },
        itemOverlayContent = itemOverlayContent,
    )
}

/**
 * Displays a preview of single or multiple video or attachments.
 *
 * @param state The state of the attachment, holding the root modifier, the message
 * and the onLongItemClick handler.
 * @param modifier The modifier used for styling.
 * @param maximumNumberOfPreviewedItems The maximum number of thumbnails that can be displayed
 * in a group when previewing Media attachments in the message list.
 * @param skipEnrichUrl Used by the media gallery. If set to true will skip enriching URLs when you update the message
 * by deleting an attachment contained within it. Set to false by default.
 * @param onItemClick Lambda called when an item gets clicked.
 * @param itemOverlayContent Represents the content overlaid above individual items.
 * By default it is used to display a play button over video previews.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun MediaAttachmentContent(
    state: AttachmentState,
    modifier: Modifier = Modifier,
    maximumNumberOfPreviewedItems: Int = 4,
    skipEnrichUrl: Boolean = false,
    onItemClick: (MediaAttachmentClickData) -> Unit = {
        onMediaAttachmentContentItemClick(
            it.mediaGalleryPreviewLauncher,
            it.message,
            it.attachmentPosition,
            it.videoThumbnailsEnabled,
            it.downloadAttachmentUriGenerator,
            it.downloadRequestInterceptor,
            it.streamCdnImageResizing,
            it.skipEnrichUrl,
        )
    },
    itemOverlayContent: @Composable (attachmentType: String?) -> Unit = { attachmentType ->
        if (attachmentType == AttachmentType.VIDEO) {
            PlayButtonOverlay()
        }
    },
) {
    val message = state.message

    // Prepare the image loader for the media gallery
    val imageLoader = LocalStreamImageLoader.current
    LaunchedEffect(imageLoader) {
        MediaGalleryInjector.install(imageLoader)
    }

    val attachments = remember(message.attachments) { message.attachments.filter { it.isImage() || it.isVideo() } }

    if (attachments.size == 1) {
        SingleMediaAttachment(
            attachment = attachments.first(),
            message = message,
            modifier = modifier,
            onMediaGalleryPreviewResult = state.onMediaGalleryPreviewResult,
            onLongItemClick = state.onLongItemClick,
            skipEnrichUrl = skipEnrichUrl,
            onContentItemClick = onItemClick,
            overlayContent = itemOverlayContent,
        )
    } else {
        val gridSpacing = StreamTokens.spacing2xs
        val description =
            stringResource(R.string.stream_ui_message_list_semantics_message_attachments, attachments.size)
        Row(
            modifier = modifier
                .semantics { this.contentDescription = description }
                .padding(MessageStyling.messageSectionPadding),
            horizontalArrangement = Arrangement.spacedBy(gridSpacing),
        ) {
            MultipleMediaAttachmentsColumns(
                attachments = attachments,
                gridSpacing = gridSpacing,
                maximumNumberOfPreviewedItems = maximumNumberOfPreviewedItems,
                message = message,
                skipEnrichUrl = skipEnrichUrl,
                onMediaGalleryPreviewResult = state.onMediaGalleryPreviewResult,
                onLongItemClick = state.onLongItemClick,
                onContentItemClick = onItemClick,
                itemOverlayContent = itemOverlayContent,
            )
        }
    }
}

/**
 * Displays a preview of a single image or video attachment.
 *
 * @param attachment The attachment that is previewed.
 * @param message The original message containing the attachment.
 * @param skipEnrichUrl Used by the media gallery. If set to true will skip enriching URLs when you update the message
 * by deleting an attachment contained within it. Set to false by default.
 * @param onMediaGalleryPreviewResult The result of the activity used for propagating
 * actions such as media attachment selection, deletion, etc.
 * @param onLongItemClick Lambda that gets called when an item is long clicked.
 * @param onContentItemClick Lambda called when an item gets clicked.
 * @param overlayContent Represents the content overlaid above attachment previews.
 * Usually used to display a play button over video previews.
 */
@Suppress("LongParameterList")
@Composable
internal fun SingleMediaAttachment(
    attachment: Attachment,
    message: Message,
    modifier: Modifier,
    skipEnrichUrl: Boolean,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    onLongItemClick: (Message) -> Unit,
    onContentItemClick: (MediaAttachmentClickData) -> Unit,
    overlayContent: @Composable (attachmentType: String?) -> Unit,
) {
    val isVideo = attachment.isVideo()
    // Depending on the CDN, images might not contain their original dimensions
    val ratio: Float? by remember(key1 = attachment.originalWidth, key2 = attachment.originalHeight) {
        derivedStateOf {
            val width = attachment.originalWidth?.toFloat()
            val height = attachment.originalHeight?.toFloat()

            if (width != null && height != null) {
                width / height
            } else {
                null
            }
        }
    }

    val shouldBeFullSize = message.shouldBeDisplayedAsFullSizeAttachment()
    MediaAttachmentContentItem(
        attachment = attachment,
        modifier = modifier
            .applyIf(!shouldBeFullSize) { padding(MessageStyling.messageSectionPadding) }
            .heightIn(
                max = if (isVideo) {
                    ChatTheme.dimens.attachmentsContentVideoMaxHeight
                } else {
                    ChatTheme.dimens.attachmentsContentImageMaxHeight
                },
            )
            .width(
                if (isVideo) {
                    ChatTheme.dimens.attachmentsContentVideoWidth
                } else {
                    ChatTheme.dimens.attachmentsContentImageWidth
                },
            )
            .aspectRatio(ratio ?: EqualDimensionsRatio),
        shape = if (shouldBeFullSize) null else ChatTheme.shapes.attachment,
        message = message,
        attachmentPosition = 0,
        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
        onLongItemClick = onLongItemClick,
        skipEnrichUrl = skipEnrichUrl,
        onItemClick = onContentItemClick,
        overlayContent = overlayContent,
    )
}

/**
 * Displays previews of multiple image and video attachment laid out in a grid.
 *
 * @param attachments The list of attachments that are to be previewed.
 * @param gridSpacing Determines the spacing strategy between items.
 * @param maximumNumberOfPreviewedItems The maximum number of thumbnails that can be displayed
 * in a group when previewing Media attachments in the message list.
 * @param message The original message containing the attachments.
 * @param skipEnrichUrl Used by the media gallery. If set to true will skip enriching URLs when you update the message
 * by deleting an attachment contained within it. Set to false by default.
 * @param onMediaGalleryPreviewResult The result of the activity used for propagating
 * actions such as media attachment selection, deletion, etc.
 * @param onLongItemClick Lambda that gets called when an item is long clicked.
 * @param onContentItemClick Lambda called when an item gets clicked.
 * @param itemOverlayContent Represents the content overlaid above individual items.
 * Usually used to display a play button over video previews.
 */
@Suppress("LongParameterList", "LongMethod")
@Composable
internal fun RowScope.MultipleMediaAttachmentsColumns(
    attachments: List<Attachment>,
    gridSpacing: Dp,
    maximumNumberOfPreviewedItems: Int = 4,
    message: Message,
    skipEnrichUrl: Boolean,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    onLongItemClick: (Message) -> Unit,
    onContentItemClick: (MediaAttachmentClickData) -> Unit,
    itemOverlayContent: @Composable (attachmentType: String?) -> Unit,
) {
    val totalPreviewCount = attachments.size.coerceAtMost(maximumNumberOfPreviewedItems)
    val col1Count = totalPreviewCount / 2
    val col2Count = totalPreviewCount - col1Count

    val columnSizingModifier = Modifier
        .weight(1f, fill = false)
        .size(
            width = ChatTheme.dimens.attachmentsContentGroupPreviewWidth / 2,
            height = ChatTheme.dimens.attachmentsContentGroupPreviewHeight,
        )

    Column(
        modifier = columnSizingModifier.testTag("Stream_MultipleMediaAttachmentsColumn"),
        verticalArrangement = Arrangement.spacedBy(gridSpacing),
    ) {
        for (positionInColumn in 0 until col1Count) {
            val shape = attachmentShape(
                positionInColumn = positionInColumn,
                isFirstColumn = true,
                itemsInColumn = col1Count,
            )
            MediaAttachmentContentItem(
                attachment = attachments[positionInColumn],
                modifier = Modifier.weight(1f),
                shape = shape,
                message = message,
                skipEnrichUrl = skipEnrichUrl,
                attachmentPosition = positionInColumn,
                onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                onLongItemClick = onLongItemClick,
                onItemClick = onContentItemClick,
                overlayContent = itemOverlayContent,
            )
        }
    }

    Column(
        modifier = columnSizingModifier,
        verticalArrangement = Arrangement.spacedBy(gridSpacing),
    ) {
        for (positionInColumn in 0 until col2Count) {
            val attachmentIndex = col1Count + positionInColumn
            val attachment = attachments[attachmentIndex]
            val isUploading = attachment.uploadState is Attachment.UploadState.InProgress

            val isLastVisibleSlot = attachmentIndex == maximumNumberOfPreviewedItems - 1
            val hasHiddenItems = attachments.size > maximumNumberOfPreviewedItems

            val shape = attachmentShape(
                positionInColumn = positionInColumn,
                isFirstColumn = false,
                itemsInColumn = col2Count,
            )

            if (isLastVisibleSlot && hasHiddenItems) {
                Box(modifier = Modifier.weight(1f)) {
                    MediaAttachmentContentItem(
                        attachment = attachment,
                        shape = shape,
                        message = message,
                        skipEnrichUrl = skipEnrichUrl,
                        attachmentPosition = attachmentIndex,
                        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                        onLongItemClick = onLongItemClick,
                        onItemClick = onContentItemClick,
                        overlayContent = itemOverlayContent,
                    )

                    if (!isUploading) {
                        MediaAttachmentShowMoreOverlay(
                            mediaCount = attachments.size,
                            maximumNumberOfPreviewedItems = maximumNumberOfPreviewedItems,
                            shape = shape,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            } else {
                MediaAttachmentContentItem(
                    attachment = attachment,
                    modifier = Modifier.weight(1f),
                    shape = shape,
                    message = message,
                    skipEnrichUrl = skipEnrichUrl,
                    attachmentPosition = attachmentIndex,
                    onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                    onLongItemClick = onLongItemClick,
                    onItemClick = onContentItemClick,
                    overlayContent = itemOverlayContent,
                )
            }
        }
    }
}

private fun attachmentShape(
    positionInColumn: Int,
    isFirstColumn: Boolean,
    itemsInColumn: Int,
): RoundedCornerShape {
    val outer = StreamTokens.radiusLg
    val inner = StreamTokens.radiusMd

    val isFirstInColumn = positionInColumn == 0
    val isLastInColumn = positionInColumn == itemsInColumn - 1

    return RoundedCornerShape(
        topStart = if (isFirstColumn && isFirstInColumn) outer else inner,
        topEnd = if (!isFirstColumn && isFirstInColumn) outer else inner,
        bottomEnd = if (!isFirstColumn && isLastInColumn) outer else inner,
        bottomStart = if (isFirstColumn && isLastInColumn) outer else inner,
    )
}

/**
 * Displays previews of image and video attachments.
 *
 * @param message The original message containing the attachments.
 * @param attachmentPosition The position of the attachment in the list
 * of attachments. Used to remember the item position when viewing it in a separate
 * activity.
 * @param attachment The attachment that is previewed.
 * @param skipEnrichUrl Used by the media gallery. If set to true will skip enriching URLs when you update the message
 * by deleting an attachment contained within it. Set to false by default.
 * @param shape Optional shape of the item.
 * @param onMediaGalleryPreviewResult The result of the activity used for propagating
 * actions such as media attachment selection, deletion, etc.
 * @param onLongItemClick Lambda that gets called when the item is long clicked.
 * @param onItemClick Lambda called when an item gets clicked.
 * @param modifier Modifier used for styling.
 * @param overlayContent Represents the content overlaid above attachment previews.
 * Usually used to display a play button over video previews.
 */
@Suppress("LongParameterList", "LongMethod")
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MediaAttachmentContentItem(
    message: Message,
    attachmentPosition: Int,
    attachment: Attachment,
    skipEnrichUrl: Boolean,
    shape: Shape?,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit,
    onLongItemClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    onItemClick: (MediaAttachmentClickData) -> Unit,
    overlayContent: @Composable (attachmentType: String?) -> Unit,
) {
    val isImage = attachment.isImage()
    val isVideo = attachment.isVideo()

    val data = attachment.imagePreviewData

    val context = LocalContext.current
    val imageRequest = remember(data) {
        ImageRequest.Builder(context)
            .data(data)
            .build()
    }

    val config = ChatTheme.mediaGalleryConfig
    val mixedMediaPreviewLauncher = rememberLauncherForActivityResult(
        contract = MediaGalleryPreviewContract(config),
        onResult = { result -> onMediaGalleryPreviewResult(result) },
    )

    val areVideosEnabled = ChatTheme.videoThumbnailsEnabled
    val streamCdnImageResizing = ChatTheme.streamCdnImageResizing

    val downloadAttachmentUriGenerator = ChatTheme.streamDownloadAttachmentUriGenerator
    val downloadRequestInterceptor = ChatTheme.streamDownloadRequestInterceptor

    val description = if (isImage) {
        stringResource(R.string.stream_ui_message_list_semantics_message_attachment_image)
    } else if (isVideo) {
        stringResource(R.string.stream_ui_message_list_semantics_message_attachment_video)
    } else {
        null
    }

    Box(
        modifier = modifier
            .semantics {
                testTag = "Stream_MediaContent_${if (isVideo) "Video" else "Image"}"
                if (description != null) {
                    contentDescription = description
                }
            }
            .ifNotNull(shape, Modifier::clip)
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = {
                    if (message.syncStatus == SyncStatus.COMPLETED) {
                        onItemClick(
                            MediaAttachmentClickData(
                                mixedMediaPreviewLauncher,
                                message,
                                attachmentPosition,
                                areVideosEnabled,
                                downloadAttachmentUriGenerator,
                                downloadRequestInterceptor,
                                streamCdnImageResizing,
                                skipEnrichUrl,
                            ),
                        )
                    } else {
                        onLongItemClick(message)
                    }
                },
                onLongClick = { onLongItemClick(message) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        val backgroundColor =
            if (isImage) {
                ChatTheme.colors.imageBackgroundMessageList
            } else {
                ChatTheme.colors.videoBackgroundMessageList
            }

        StreamAsyncImage(
            imageRequest = imageRequest,
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentScale = ContentScale.Crop,
        ) { asyncImageState ->
            Crossfade(targetState = asyncImageState) { state ->
                when (state) {
                    is AsyncImagePainter.State.Empty,
                    is AsyncImagePainter.State.Loading,
                    -> ShimmerProgressIndicator(
                        modifier = Modifier.fillMaxSize(),
                    )

                    is AsyncImagePainter.State.Success -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                modifier = Modifier.fillMaxSize(),
                                painter = state.painter,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                            )
                            overlayContent(attachment.type)
                        }
                    }

                    is AsyncImagePainter.State.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                tint = ChatTheme.colors.disabled,
                                modifier = Modifier.fillMaxSize(0.4f),
                                painter = painterResource(R.drawable.stream_compose_ic_image_picker),
                                contentDescription = stringResource(
                                    id = R.string.stream_ui_message_list_attachment_load_failed,
                                ),
                            )
                            overlayContent(attachment.type)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayButtonOverlay() {
    val colors = ChatTheme.colors
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(colors.controlPlayControlBg, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.stream_compose_ic_play),
            contentDescription = null,
            tint = colors.controlPlayControlIcon,
            modifier = Modifier.size(16.dp),
        )
    }
}

/**
 * A simple play button that is overlaid above
 * video attachments.
 *
 * @param modifier The modifier used for styling.
 * @param contentDescription Used to describe the content represented by this composable.
 */
@Suppress("MagicNumber")
@Composable
internal fun PlayButton(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.fillMaxSize(0.6f),
            painter = painterResource(id = R.drawable.stream_compose_ic_play),
            contentDescription = contentDescription,
            tint = ChatTheme.colors.controlPlayControlIcon,
        )
    }
}

/**
 * Represents an overlay that's shown on the last media attachment preview in the media attachment
 * item gallery.
 *
 * @param mediaCount The number of total media attachments.
 * @param maximumNumberOfPreviewedItems The maximum number of thumbnails that can be displayed
 * in a group when previewing Media attachments in the message list.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun MediaAttachmentShowMoreOverlay(
    mediaCount: Int,
    maximumNumberOfPreviewedItems: Int,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    val remainingMediaCount = mediaCount - maximumNumberOfPreviewedItems

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = ChatTheme.colors.showMoreOverlay, shape = shape),
    ) {
        Text(
            modifier = modifier
                .wrapContentSize(),
            text = stringResource(
                id = R.string.stream_compose_remaining_media_attachments_count,
                remainingMediaCount,
            ),
            color = ChatTheme.colors.showMoreCountText,
            style = ChatTheme.typography.title1,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Data class that holds all the information required to handle
 * media attachment clicks.
 *
 * @param mediaGalleryPreviewLauncher The [ManagedActivityResultLauncher] to launch the media gallery.
 * @param message The message that holds the clicked attachment.
 * @param attachmentPosition The position of the clicked attachment in the message's attachments list.
 * @param videoThumbnailsEnabled Whether video thumbnails are enabled.
 * @param downloadAttachmentUriGenerator The [DownloadAttachmentUriGenerator] used to generate URIs for downloading
 * attachments.
 * @param downloadRequestInterceptor The [DownloadRequestInterceptor] used to intercept download requests.
 * @param streamCdnImageResizing The [StreamCdnImageResizing] used to resize images.
 * @param skipEnrichUrl Used by the media gallery. If set to true will skip enriching URLs when you update the message
 * by deleting an attachment contained within it. Set to false by default.
 */
@ConsistentCopyVisibility
public data class MediaAttachmentClickData internal constructor(
    val mediaGalleryPreviewLauncher: ManagedActivityResultLauncher<Input, MediaGalleryPreviewResult?>,
    val message: Message,
    val attachmentPosition: Int,
    val videoThumbnailsEnabled: Boolean,
    val downloadAttachmentUriGenerator: DownloadAttachmentUriGenerator,
    val downloadRequestInterceptor: DownloadRequestInterceptor,
    val streamCdnImageResizing: StreamCdnImageResizing,
    val skipEnrichUrl: Boolean,
)

/**
 * Produces the same height as the width of the
 * Composable when calling [Modifier.aspectRatio].
 */
private const val EqualDimensionsRatio = 1f

/**
 * Handles click on individual image or video attachment content items.
 *
 * @param mediaGalleryPreviewLauncher The launcher used for launching the media gallery after
 * clicking on an attachment.
 * @param message The message which contains the attachment.
 * @param attachmentPosition The position (inside the message) of the attachment being clicked on.
 * @param skipEnrichUrl Whether the URL should skip being enriched, i.e. rendered as
 * a link attachment. Used when updating the message from the gallery by doing actions
 * such as deleting an attachment.
 */
@Suppress("LongParameterList")
internal fun onMediaAttachmentContentItemClick(
    mediaGalleryPreviewLauncher: ManagedActivityResultLauncher<Input, MediaGalleryPreviewResult?>,
    message: Message,
    attachmentPosition: Int,
    videoThumbnailsEnabled: Boolean,
    downloadAttachmentUriGenerator: DownloadAttachmentUriGenerator,
    downloadRequestInterceptor: DownloadRequestInterceptor,
    streamCdnImageResizing: StreamCdnImageResizing,
    skipEnrichUrl: Boolean,
) {
    mediaGalleryPreviewLauncher.launch(
        Input(
            message = message,
            initialPosition = attachmentPosition,
            videoThumbnailsEnabled = videoThumbnailsEnabled,
            downloadAttachmentUriGenerator = downloadAttachmentUriGenerator,
            downloadRequestInterceptor = downloadRequestInterceptor,
            streamCdnImageResizing = streamCdnImageResizing,
            skipEnrichUrl = skipEnrichUrl,
        ),
    )
}

@Composable
@Preview(showBackground = true)
private fun SingleMediaAttachmentContentPreview() {
    ChatTheme {
        SingleMediaAttachmentContent()
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
internal fun SingleMediaAttachmentContent() {
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(color = Color.Yellow.toArgb(), width = 200, height = 150)
    }
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        MediaAttachmentContent(
            attachmentState = AttachmentState(
                message = Message(
                    text = "Hello",
                    attachments = listOf(
                        Attachment(
                            type = AttachmentType.IMAGE,
                            imageUrl = "https://placekitten.com/200/300",
                        ),
                    ),
                ),
            ),
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun MultipleMediaAttachmentContentPreview() {
    ChatTheme {
        MultipleMediaAttachmentContent()
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
internal fun MultipleMediaAttachmentContent() {
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(color = Color.Blue.toArgb(), width = 200, height = 150)
    }
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        val attachments = listOf(
            Attachment(
                type = AttachmentType.VIDEO,
                thumbUrl = "https://placekitten.com/100/100",
            ),
            Attachment(
                type = AttachmentType.IMAGE,
                imageUrl = "https://placekitten.com/200/200",
            ),
            Attachment(
                type = AttachmentType.VIDEO,
                thumbUrl = "https://placekitten.com/300/300",
            ),
            Attachment(
                type = AttachmentType.IMAGE,
                imageUrl = "https://placekitten.com/400/400",
            ),
        )
        MediaAttachmentContent(
            attachmentState = AttachmentState(
                message = Message(attachments = attachments),
            ),
        )
    }
}
