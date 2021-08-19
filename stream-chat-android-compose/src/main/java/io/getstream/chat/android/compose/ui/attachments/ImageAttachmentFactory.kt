package io.getstream.chat.android.compose.ui.attachments

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.imagepreview.ImagePreviewActivity
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * An extension of the [AttachmentFactory] that validates attachments as images and uses [ImageAttachmentContent] to
 * build the UI for the message.
 * */
public class ImageAttachmentFactory : AttachmentFactory(
    predicate = { attachments -> attachments.all { it.type == "image" } },
    content = @Composable { ImageAttachmentContent(it) }
)

/**
 * Builds an image attachment message, which can be composed of several images.
 *
 * @param attachmentState - The state of the attachment, holding the root modifier, the message
 * and the onLongItemClick handler.
 * */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun ImageAttachmentContent(attachmentState: AttachmentState) {
    val (modifier, messageItem, onLongItemClick) = attachmentState
    val (message, _) = messageItem
    val context = LocalContext.current

    Row(
        modifier
            .size(height = 200.dp, width = 250.dp)
            .clip(ChatTheme.shapes.attachment)
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    context.startActivity(ImagePreviewActivity.getIntent(context, message.id))
                },
                onLongClick = { onLongItemClick(message) }
            )
    ) {
        val attachments = message.attachments.filter { it.ogUrl == null && it.titleLink == null }
        val imageCount = attachments.size

        if (imageCount == 1) {
            val painter = rememberImagePainter(attachments.first().imagePreviewUrl)

            Image(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxHeight()
            ) {
                for (imageIndex in 0..3 step 2) {
                    if (imageIndex < imageCount) {
                        val painter = rememberImagePainter(attachments[imageIndex].imagePreviewUrl)

                        Image(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            painter = painter,
                            contentDescription = null,
                            contentScale = ContentScale.Crop
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
                        val painter = rememberImagePainter(attachments[imageIndex].imagePreviewUrl)

                        if (imageIndex == 3 && imageCount > 4) {
                            Box(modifier = Modifier.weight(1f)) {
                                Image(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    painter = painter,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )

                                val remainingImagesCount = imageCount - (imageIndex + 1)

                                Surface(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    color = ChatTheme.colors.overlay
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .wrapContentSize()
                                            .align(Alignment.Center),
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
                        } else {
                            Image(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                painter = painter,
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}
