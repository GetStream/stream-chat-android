package io.getstream.chat.android.compose.ui.attachments.content

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.getstream.sdk.chat.utils.MediaStringUtil
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.MimeTypeIconProvider
import io.getstream.chat.android.offline.ChatDomain

/**
 * Width of file attachments.
 */
internal val FILE_ATTACHMENT_WIDTH = 250.dp

/**
 * Builds a file attachment message which shows a list of files.
 *
 * @param attachmentState - The state of the attachment, holding the root modifier, the message
 * and the onLongItemClick handler.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun FileAttachmentContent(
    attachmentState: AttachmentState,
    modifier: Modifier = Modifier,
) {
    val (message, onLongItemClick) = attachmentState

    Column(
        modifier = modifier
            .wrapContentHeight()
            .width(FILE_ATTACHMENT_WIDTH)
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {},
                onLongClick = { onLongItemClick(message) }
            )
    ) {
        for (attachment in message.attachments) {
            FileAttachmentItem(attachment = attachment)
        }
    }
}

/**
 * Represents each file item in the list of file attachments.
 *
 * @param attachment The file attachment to show.
 */
@Composable
public fun FileAttachmentItem(attachment: Attachment) {
    Surface(
        modifier = Modifier
            .padding(2.dp)
            .fillMaxWidth(),
        color = ChatTheme.colors.appBackground, shape = ChatTheme.shapes.attachment
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FileAttachmentImage(attachment = attachment)

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(start = 16.dp, end = 8.dp),
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
                    .align(Alignment.Top)
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
                ),
                tint = ChatTheme.colors.textHighEmphasis
            )
        }
    }
}

/**
 * Represents the image that's shown in file attachments. This can be either an image/icon that represents the file type
 * or a thumbnail in case the file type is an image.
 *
 * @param attachment - The attachment we use to show the image.
 */
@Composable
public fun FileAttachmentImage(attachment: Attachment) {
    val isImage = attachment.type == "image"

    val painter = if (isImage) {
        val dataToLoad = attachment.imageUrl ?: attachment.upload

        rememberImagePainter(dataToLoad)
    } else {
        painterResource(id = MimeTypeIconProvider.getIconRes(attachment.mimeType))
    }

    val shape = if (isImage) ChatTheme.shapes.imageThumbnail else null

    val imageModifier = Modifier.size(height = 40.dp, width = 35.dp).let { baseModifier ->
        if (shape != null) baseModifier.clip(shape) else baseModifier
    }

    Image(
        modifier = imageModifier,
        painter = painter,
        contentDescription = null,
        contentScale = if (isImage) ContentScale.Crop else ContentScale.Fit
    )
}
