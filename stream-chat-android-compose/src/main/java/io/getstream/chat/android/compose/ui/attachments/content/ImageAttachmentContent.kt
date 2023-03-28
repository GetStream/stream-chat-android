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

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResult
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.attachments.preview.ImagePreviewContract
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.isMedia
import io.getstream.chat.android.compose.ui.util.rememberStreamImagePainter
import io.getstream.chat.android.uiutils.extension.hasLink

/**
 * Builds an image attachment message, which can be composed of several images or will show an upload state if we're
 * currently uploading images.
 *
 * @param attachmentState The state of the attachment, holding the root modifier, the message
 * @param skipEnrichUrl Used by the image gallery. If set to true will skip enriching URLs when you update the message
 * by deleting an attachment contained within it. Set to false by default.
 * @param onContentItemClicked Lambda called when an item gets clicked.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun ImageAttachmentContent(
    attachmentState: AttachmentState,
    modifier: Modifier = Modifier,
    skipEnrichUrl: Boolean = false,
    onContentItemClicked: (
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
) {
    val (message, onLongItemClick, onImagePreviewResult) = attachmentState
    val gridSpacing = ChatTheme.dimens.attachmentsContentImageGridSpacing

    Row(
        modifier
            .clip(ChatTheme.shapes.attachment)
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {},
                onLongClick = { onLongItemClick(message) }
            ),
        horizontalArrangement = Arrangement.spacedBy(gridSpacing)
    ) {
        val attachments =
            message.attachments.filter { !it.hasLink() && it.isMedia() }
        val imageCount = attachments.size

        if (imageCount == 1) {
            val attachment = attachments.first()

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

            ImageAttachmentContentItem(
                attachment = attachment,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio ?: EqualDimensionsRatio),
                message = message,
                attachmentPosition = 0,
                onImagePreviewResult = onImagePreviewResult,
                onLongItemClick = onLongItemClick,
                skipEnrichUrl = skipEnrichUrl,
                onContentItemClicked = onContentItemClicked,
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .aspectRatio(TwiceAsTallAsIsWideRatio),
                verticalArrangement = Arrangement.spacedBy(gridSpacing)
            ) {
                for (imageIndex in 0..3 step 2) {
                    if (imageIndex < imageCount) {
                        ImageAttachmentContentItem(
                            attachment = attachments[imageIndex],
                            modifier = Modifier.weight(1f),
                            message = message,
                            attachmentPosition = imageIndex,
                            onImagePreviewResult = onImagePreviewResult,
                            onLongItemClick = onLongItemClick,
                            onContentItemClicked = onContentItemClicked,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .aspectRatio(TwiceAsTallAsIsWideRatio),
                verticalArrangement = Arrangement.spacedBy(gridSpacing)
            ) {
                for (imageIndex in 1..4 step 2) {
                    if (imageIndex < imageCount) {
                        val attachment = attachments[imageIndex]
                        val isUploading = attachment.uploadState is Attachment.UploadState.InProgress

                        if (imageIndex == 3 && imageCount > 4) {
                            Box(modifier = Modifier.weight(1f)) {
                                ImageAttachmentContentItem(
                                    attachment = attachment,
                                    message = message,
                                    attachmentPosition = imageIndex,
                                    onImagePreviewResult = onImagePreviewResult,
                                    onLongItemClick = onLongItemClick,
                                    onContentItemClicked = onContentItemClicked,
                                )

                                if (!isUploading) {
                                    ImageAttachmentViewMoreOverlay(
                                        imageCount = imageCount,
                                        imageIndex = imageIndex,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        } else {
                            ImageAttachmentContentItem(
                                attachment = attachment,
                                modifier = Modifier.weight(1f),
                                message = message,
                                attachmentPosition = imageIndex,
                                onImagePreviewResult = onImagePreviewResult,
                                onLongItemClick = onLongItemClick,
                                onContentItemClicked = onContentItemClicked,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Represents each image item in the attachment gallery.
 *
 * @param attachment Image attachment data to show.
 * @param modifier Modifier for styling.
 * @param skipEnrichUrl Used by the image gallery. If set to true will skip enriching URLs when you update the message
 * by deleting an attachment contained within it. Set to false by default.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ImageAttachmentContentItem(
    message: Message,
    attachmentPosition: Int,
    attachment: Attachment,
    onImagePreviewResult: (ImagePreviewResult?) -> Unit,
    onLongItemClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    skipEnrichUrl: Boolean = false,
    onContentItemClicked: (
        imagePreviewLauncher: ManagedActivityResultLauncher<ImagePreviewContract.Input, ImagePreviewResult?>,
        message: Message,
        attachmentPosition: Int,
        skipEnrichUrl: Boolean,
    ) -> Unit,
) {
    val painter = rememberStreamImagePainter(attachment.imagePreviewUrl)

    val imagePreviewLauncher = rememberLauncherForActivityResult(
        contract = ImagePreviewContract(),
        onResult = { result -> onImagePreviewResult(result) }
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = MutableInteractionSource(),
                indication = rememberRipple(),
                onClick = {
                    onContentItemClicked(
                        imagePreviewLauncher,
                        message,
                        attachmentPosition,
                        skipEnrichUrl
                    )
                },
                onLongClick = { onLongItemClick(message) }
            )
    ) {
        Image(
            modifier = modifier
                .fillMaxSize(),
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * Represents an overlay that's shown on the last image in the image attachment item gallery.
 *
 * @param imageCount The number of total images.
 * @param imageIndex The current image index.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun ImageAttachmentViewMoreOverlay(
    imageCount: Int,
    imageIndex: Int,
    modifier: Modifier = Modifier,
) {
    val remainingImagesCount = imageCount - (imageIndex + 1)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = ChatTheme.colors.overlay),
    ) {
        Text(
            modifier = modifier
                .wrapContentSize(),
            text = stringResource(
                id = R.string.stream_compose_remaining_images_count,
                remainingImagesCount
            ),
            color = ChatTheme.colors.barsBackground,
            style = ChatTheme.typography.title1,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Produces the same height as the width of the
 * Composable when calling [Modifier.aspectRatio].
 */
private const val EqualDimensionsRatio = 1f

/**
 * Produces a height value that is twice the width of the
 * Composable when calling [Modifier.aspectRatio].
 */
private const val TwiceAsTallAsIsWideRatio = 0.5f
