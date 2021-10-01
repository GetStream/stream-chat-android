package io.getstream.chat.android.compose.ui.attachments.content

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResult
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.imagepreview.ImagePreviewContract
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.hasLink
import io.getstream.chat.android.compose.ui.util.isMedia

/**
 * Builds an image attachment message, which can be composed of several images or will show an upload state if we're
 * currently uploading images.
 *
 * @param attachmentState The state of the attachment, holding the root modifier, the message
 * and the onLongItemClick handler.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun ImageAttachmentContent(
    attachmentState: AttachmentState,
    modifier: Modifier = Modifier,
) {
    val (message, onLongItemClick, onImagePreviewResult) = attachmentState

    Row(
        modifier
            .size(height = 200.dp, width = 250.dp)
            .clip(ChatTheme.shapes.attachment)
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {},
                onLongClick = { onLongItemClick(message) }
            )
    ) {
        val attachments =
            message.attachments.filter { !it.hasLink() && it.isMedia() }
        val imageCount = attachments.size

        if (imageCount == 1) {
            val attachment = attachments.first()

            ImageAttachmentContentItem(
                attachment = attachment,
                modifier = Modifier.weight(1f),
                message = message,
                attachmentPosition = 0,
                onImagePreviewResult = onImagePreviewResult,
                onLongItemClick = onLongItemClick
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxHeight()
            ) {
                for (imageIndex in 0..3 step 2) {
                    if (imageIndex < imageCount) {
                        ImageAttachmentContentItem(
                            attachment = attachments[imageIndex],
                            modifier = Modifier.weight(1f),
                            message = message,
                            attachmentPosition = imageIndex,
                            onImagePreviewResult = onImagePreviewResult,
                            onLongItemClick = onLongItemClick
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxHeight()
            ) {
                for (imageIndex in 1..4 step 2) {
                    if (imageIndex < imageCount) {
                        val attachment = attachments[imageIndex]
                        val isUploading = attachment.uploadState == Attachment.UploadState.InProgress

                        if (imageIndex == 3 && imageCount > 4) {
                            Box(modifier = Modifier.weight(1f)) {
                                ImageAttachmentContentItem(
                                    attachment = attachment,
                                    message = message,
                                    attachmentPosition = imageIndex,
                                    onImagePreviewResult = onImagePreviewResult,
                                    onLongItemClick = onLongItemClick
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
                                onLongItemClick = onLongItemClick
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
) {
    val painter = rememberImagePainter(attachment.imagePreviewUrl)

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
                    imagePreviewLauncher.launch(
                        ImagePreviewContract.Input(
                            messageId = message.id,
                            initialPosition = attachmentPosition
                        )
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

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = ChatTheme.colors.overlay
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
