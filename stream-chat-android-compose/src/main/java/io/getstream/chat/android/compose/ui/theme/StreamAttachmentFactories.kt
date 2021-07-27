package io.getstream.chat.android.compose.ui.theme

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.Top
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.getstream.sdk.chat.utils.MediaStringUtil
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.imagepreview.ImagePreviewActivity
import io.getstream.chat.android.compose.ui.util.MimeTypeIconProvider
import io.getstream.chat.android.offline.ChatDomain

public object StreamAttachmentFactories {

    /**
     * Default attachment factories we provide, which can transform image and file attachments.
     *
     * Uses [ImageAttachmentFactory] and [FileAttachmentFactory] to build UI.
     * */
    public val defaultFactories: List<AttachmentFactory> = listOf(
        AttachmentFactory(
            { state -> LinkAttachmentFactory(state) },
            { message -> message.attachments.any { it.ogUrl != null } }
        ),
        AttachmentFactory(
            { state -> ImageAttachmentFactory(state) },
            { message -> message.attachments.any { it.type == "image" } }
        ),
        AttachmentFactory(
            { state -> FileAttachmentFactory(state) },
            { message -> message.attachments.any { it.type != "image" } }
        )
    )

    /**
     * Builds a link attachment message, which shows the link image preview, the title of the link
     * as well as its description.
     *
     * When clicking it, we open the preview link.
     *
     * @param attachmentState - The state of the attachment, holding the root modifier, the message
     * and the onLongItemClick handler.
     * */
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun LinkAttachmentFactory(
        attachmentState: AttachmentState,
    ) {
        val (modifier, messageItem, onLongItemClick) = attachmentState
        val (message, _) = messageItem

        val context = LocalContext.current
        val attachment = message.attachments.firstOrNull { it.ogUrl != null }
        val previewUrl = attachment?.ogUrl

        requireNotNull(previewUrl) {
            IllegalStateException("Missing preview URL.")
        }

        val painter = rememberImagePainter(data = attachment.imagePreviewUrl)

        Column(
            modifier = modifier
                .width(250.dp)
                .wrapContentHeight()
                .clip(ChatTheme.shapes.attachment)
                .background(ChatTheme.colors.borders)
                .combinedClickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(previewUrl)
                            )
                        )
                    },
                    onLongClick = { onLongItemClick(message) }
                )
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp)
                    .padding(4.dp)
                    .clip(ChatTheme.shapes.attachment),
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            val title = attachment.title

            if (title != null) {
                Text(
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp),
                    text = title,
                    style = ChatTheme.typography.bodyBold,
                    color = ChatTheme.colors.textHighEmphasis,
                )
            }

            val description = attachment.text

            if (description != null) {
                Text(
                    modifier = Modifier.padding(
                        start = 4.dp,
                        end = 4.dp,
                        bottom = 4.dp,
                        top = 2.dp
                    ),
                    text = description,
                    style = ChatTheme.typography.footnote,
                    color = ChatTheme.colors.textHighEmphasis,
                )
            }
        }
    }

    /**
     * Builds an image attachment message, which can be composed of several images.
     *
     * @param attachmentState - The state of the attachment, holding the root modifier, the message
     * and the onLongItemClick handler.
     * */
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun ImageAttachmentFactory(
        attachmentState: AttachmentState,
    ) {
        val (modifier, messageItem, onLongItemClick) = attachmentState
        val (message, _) = messageItem
        val context = LocalContext.current

        Row(
            modifier
                .size(height = 200.dp, width = 250.dp)
                .clip(RoundedCornerShape(16.dp))
                .combinedClickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        context.startActivity(ImagePreviewActivity.getIntent(context, message.id))
                    },
                    onLongClick = { onLongItemClick(message) }
                )
        ) {
            val attachments = message.attachments
            val imageCount = attachments.size

            if (imageCount == 1) {
                val painter = rememberImagePainter(attachments.first().imagePreviewUrl)

                Image(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            } else {
                // TODO - See if there's a better way to do this
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
                                                .align(Center),
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

    /**
     * Builds a file attachment message.
     *
     * @param attachmentState - The state of the attachment, holding the root modifier, the message
     * and the onLongItemClick handler.
     * */
    @Composable
    private fun FileAttachmentFactory(
        attachmentState: AttachmentState,
    ) {
        val (modifier, messageItem, _) = attachmentState
        val (message, _) = messageItem

        Column(
            modifier = modifier
                .wrapContentHeight()
                .width(200.dp)
        ) {
            for (attachment in message.attachments) {
                Surface(
                    modifier = Modifier.padding(2.dp),
                    color = ChatTheme.colors.appBackground, shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            modifier = Modifier
                                .size(height = 40.dp, width = 35.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            painter = painterResource(id = MimeTypeIconProvider.getIconRes(attachment.mimeType)),
                            contentDescription = null
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .padding(start = 16.dp),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = attachment.title ?: attachment.name ?: "",
                                style = ChatTheme.typography.bodyBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = ChatTheme.colors.textHighEmphasis
                            )

                            Text(
                                text = MediaStringUtil.convertFileSizeByteCount(attachment.fileSize.toLong()),
                                style = ChatTheme.typography.footnote,
                                color = ChatTheme.colors.textLowEmphasis
                            )
                        }

                        Icon(
                            modifier = Modifier
                                .align(Top)
                                .padding(end = 2.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = rememberRipple(bounded = false)
                                ) {
                                    ChatDomain
                                        .instance()
                                        .downloadAttachment(attachment)
                                        .enqueue()
                                },
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = stringResource(
                                id = R.string.stream_compose_download
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Holds the information required to build an attachment message.
 *
 * @param factory - Function that provides a modifier and a message, to show the attachment.
 * @param predicate - Function that checks the message and returns if the factory can consume it or
 * not.
 * */
public class AttachmentFactory(
    public val factory: @Composable (AttachmentState) -> Unit,
    private val predicate: (Message) -> Boolean,
) {
    /**
     * Returns if this specific factory can handle a specific message.
     *
     * @param message - The message to check.
     * @return a boolean value, if we can consume the message and render UI.
     * */
    public fun canHandle(message: Message): Boolean {
        return predicate(message)
    }
}
