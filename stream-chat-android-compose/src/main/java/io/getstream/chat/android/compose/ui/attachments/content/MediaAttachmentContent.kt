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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.attachments.preview.MediaGalleryPreviewContract
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.rememberStreamImagePainter
import io.getstream.chat.android.uiutils.constant.AttachmentType
import io.getstream.chat.android.uiutils.extension.hasLink

/**
 * Displays a preview of single or multiple video or attachments.
 *
 * @param attachmentState The state of the attachment, holding the root modifier, the message
 * and the onLongItemClick handler.
 * @param modifier The modifier used for styling.
 * @param maximumNumberOfPreviewedItems The maximum number of thumbnails that can be displayed
 * in a group when previewing Media attachments in the message list.
 * @param playButton Represents the play button that is overlaid above video attachment
 * previews.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun MediaAttachmentContent(
    attachmentState: AttachmentState,
    modifier: Modifier = Modifier,
    maximumNumberOfPreviewedItems: Int = 4,
    playButton: @Composable () -> Unit = { PlayButton() },
) {
    val (message, onLongItemClick, _, onMediaGalleryPreviewResult) = attachmentState
    val gridSpacing = ChatTheme.dimens.attachmentsContentMediaGridSpacing

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
            message.attachments.filter {
                !it.hasLink() && it.type == AttachmentType.IMAGE || it.type == AttachmentType.VIDEO
            }
        val attachmentCount = attachments.size

        if (attachmentCount == 1) {
            val attachment = attachments.first()

            ShowSingleMediaAttachment(
                attachment = attachment,
                message = message,
                onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                onLongItemClick = onLongItemClick,
                playButton = playButton
            )
        } else {
            ShowMultipleMediaAttachments(
                attachments = attachments,
                attachmentCount = attachmentCount,
                gridSpacing = gridSpacing,
                maximumNumberOfPreviewedItems = maximumNumberOfPreviewedItems,
                message = message,
                onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                onLongItemClick = onLongItemClick,
                playButton = playButton
            )
        }
    }
}

/**
 * Displays a preview of a single image or video attachment.
 *
 * @param attachment The attachment that is previewed.
 * @param message The original message containing the attachment.
 * @param onMediaGalleryPreviewResult The result of the activity used for propagating
 * actions such as media attachment selection, deletion, etc.
 * @param onLongItemClick Lambda that gets called when an item is long clicked.
 * @param playButton Represents the play button that is overlaid above video attachment
 * previews.
 */
@Composable
internal fun ShowSingleMediaAttachment(
    attachment: Attachment,
    message: Message,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    onLongItemClick: (Message) -> Unit,
    playButton: @Composable () -> Unit,
) {
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

    MediaAttachmentContentItem(
        attachment = attachment,
        modifier = Modifier
            .heightIn(
                max = if (attachment.type == AttachmentType.VIDEO) {
                    ChatTheme.dimens.attachmentsContentVideoMaxHeight
                } else {
                    ChatTheme.dimens.attachmentsContentImageMaxHeight
                }
            )
            .fillMaxWidth()
            .aspectRatio(ratio ?: EqualDimensionsRatio),
        message = message,
        attachmentPosition = 0,
        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
        onLongItemClick = onLongItemClick,
        playButton = playButton
    )
}

/**
 * Displays previews of multiple image and video attachment laid out in a grid.
 *
 * @param attachments The list of attachments that are to be previewed.
 * @param attachmentCount The number of attachments that are to be previewed.
 * @param gridSpacing Determines the spacing strategy between items.
 * @param maximumNumberOfPreviewedItems The maximum number of thumbnails that can be displayed
 * in a group when previewing Media attachments in the message list.
 * @param message The original message containing the attachments.
 * @param onMediaGalleryPreviewResult The result of the activity used for propagating
 * actions such as media attachment selection, deletion, etc.
 * @param onLongItemClick Lambda that gets called when an item is long clicked.
 * @param playButton Represents the play button that is overlaid above video attachment
 * previews.
 */
@Suppress("LongParameterList", "LongMethod")
@Composable
internal fun RowScope.ShowMultipleMediaAttachments(
    attachments: List<Attachment>,
    attachmentCount: Int,
    gridSpacing: Dp,
    maximumNumberOfPreviewedItems: Int = 4,
    message: Message,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    onLongItemClick: (Message) -> Unit,
    playButton: @Composable () -> Unit,
) {

    Column(
        modifier = Modifier
            .weight(1f, fill = false)
            .aspectRatio(TwiceAsTallAsIsWideRatio),
        verticalArrangement = Arrangement.spacedBy(gridSpacing)
    ) {
        for (attachmentIndex in 0 until maximumNumberOfPreviewedItems step 2) {
            if (attachmentIndex < attachmentCount) {
                MediaAttachmentContentItem(
                    attachment = attachments[attachmentIndex],
                    modifier = Modifier.weight(1f),
                    message = message,
                    attachmentPosition = attachmentIndex,
                    onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                    onLongItemClick = onLongItemClick,
                    playButton = playButton
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
        for (attachmentIndex in 1 until maximumNumberOfPreviewedItems step 2) {
            if (attachmentIndex < attachmentCount) {
                val attachment = attachments[attachmentIndex]
                val isUploading = attachment.uploadState is Attachment.UploadState.InProgress
                val lastItemInColumnIndex = (maximumNumberOfPreviewedItems - 1) - (maximumNumberOfPreviewedItems % 2)

                if (attachmentIndex == lastItemInColumnIndex && attachmentCount > maximumNumberOfPreviewedItems) {
                    Box(modifier = Modifier.weight(1f)) {
                        MediaAttachmentContentItem(
                            attachment = attachment,
                            message = message,
                            attachmentPosition = attachmentIndex,
                            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                            onLongItemClick = onLongItemClick,
                            playButton = playButton
                        )

                        if (!isUploading) {
                            MediaAttachmentViewMoreOverlay(
                                mediaCount = attachmentCount,
                                maximumNumberOfPreviewedItems = maximumNumberOfPreviewedItems,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                } else {
                    MediaAttachmentContentItem(
                        attachment = attachment,
                        modifier = Modifier.weight(1f),
                        message = message,
                        attachmentPosition = attachmentIndex,
                        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                        onLongItemClick = onLongItemClick,
                        playButton = playButton
                    )
                }
            }
        }
    }
}

/**
 * Displays previews of image and video attachments.
 *
 * @param message The original message containing the attachments.
 * @param attachmentPosition The position of the attachment in the list
 * of attachments. Used to remember the item position when viewing it in a separate
 * activity.
 * @param attachment The attachment that is previewed.
 * @param onMediaGalleryPreviewResult The result of the activity used for propagating
 * actions such as media attachment selection, deletion, etc.
 * @param onLongItemClick Lambda that gets called when the item is long clicked.
 * @param modifier Modifier used for styling.
 * @param playButton Represents the play button that is overlaid above video attachment
 * previews.
 */
@Suppress("LongParameterList")
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MediaAttachmentContentItem(
    message: Message,
    attachmentPosition: Int,
    attachment: Attachment,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit,
    onLongItemClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    playButton: @Composable () -> Unit,
) {
    val painter = rememberStreamImagePainter(attachment.imagePreviewUrl)

    val mixedMediaPreviewLauncher = rememberLauncherForActivityResult(
        contract = MediaGalleryPreviewContract(),
        onResult = { result -> onMediaGalleryPreviewResult(result) }
    )

    Box(
        modifier = modifier
            .background(Color.Black)
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = MutableInteractionSource(),
                indication = rememberRipple(),
                onClick = {
                    mixedMediaPreviewLauncher.launch(
                        MediaGalleryPreviewContract.Input(
                            messageId = message.id,
                            initialPosition = attachmentPosition
                        )
                    )
                },
                onLongClick = { onLongItemClick(message) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = modifier
                .fillMaxSize(),
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        if (attachment.type == AttachmentType.VIDEO) {
            playButton()
        }
    }
}

/**
 * A simple play button that is overlaid above
 * video attachments.
 *
 * @param modifier The modifier used for styling.
 */
@Composable
internal fun PlayButton(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column {
            Image(
                modifier = Modifier
                    .fillMaxSize(0.85f)
                    .alignBy { measured ->
                        // emulated offset as seen in the design specs,
                        // otherwise the button is visibly off to the start of the screen
                        -(measured.measuredWidth * 1 / 8)
                    },
                painter = painterResource(id = R.drawable.stream_compose_ic_play),
                contentDescription = null,
            )
        }
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
internal fun MediaAttachmentViewMoreOverlay(
    mediaCount: Int,
    maximumNumberOfPreviewedItems: Int,
    modifier: Modifier = Modifier,
) {
    val remainingMediaCount = mediaCount - maximumNumberOfPreviewedItems

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = ChatTheme.colors.overlay),
    ) {
        Text(
            modifier = modifier
                .wrapContentSize(),
            text = stringResource(
                id = R.string.stream_compose_remaining_media_attachments_count,
                remainingMediaCount
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
