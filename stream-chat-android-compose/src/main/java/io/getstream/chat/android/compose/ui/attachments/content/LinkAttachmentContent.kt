package io.getstream.chat.android.compose.ui.attachments.content

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.hasLink

/**
 * Builds a link attachment message, which shows the link image preview, the title of the link
 * as well as its description.
 *
 * When clicking it, we open the preview link.
 *
 * @param attachmentState - The state of the attachment, holding the root modifier, the message
 * and the onLongItemClick handler.
 *
 * @param linkDescriptionMaxLines - The limit of how many lines we show for the link description.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun LinkAttachmentContent(
    attachmentState: AttachmentState,
    linkDescriptionMaxLines: Int,
    modifier: Modifier = Modifier,
) {
    val (message, onLongItemClick) = attachmentState

    val context = LocalContext.current
    val attachment = message.attachments.firstOrNull { it.hasLink() && it.type != ModelType.attach_giphy }

    checkNotNull(attachment) {
        "Missing link attachment."
    }

    val previewUrl = attachment.titleLink ?: attachment.ogUrl

    checkNotNull(previewUrl) {
        "Missing preview URL."
    }

    val hasImage = attachment.imagePreviewUrl != null
    val painter = rememberImagePainter(data = attachment.imagePreviewUrl)

    Column(
        modifier = modifier
            .clip(ChatTheme.shapes.attachment)
            .background(ChatTheme.colors.linkBackground)
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
        if (hasImage) {
            BoxWithConstraints(modifier = Modifier.wrapContentSize()) {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp)
                        .clip(ChatTheme.shapes.attachment),
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )

                val authorName = attachment.authorName

                if (authorName != null) {
                    Text(
                        text = authorName,
                        color = ChatTheme.colors.primaryAccent,
                        maxLines = 1,
                        style = ChatTheme.typography.bodyBold,
                        fontSize = 16.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .wrapContentWidth()
                            .widthIn(max = maxWidth / 2)
                            .background(
                                color = ChatTheme.colors.linkBackground,
                                shape = ChatTheme.shapes.attachmentSiteLabel
                            )
                            .padding(vertical = 6.dp, horizontal = 12.dp)
                            .align(Alignment.BottomStart)
                    )
                }
            }
        }

        val title = attachment.title

        if (title != null) {
            Text(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                text = title,
                style = ChatTheme.typography.bodyBold,
                color = ChatTheme.colors.textHighEmphasis,
            )
        }

        val description = attachment.text

        if (description != null) {
            Text(
                modifier = Modifier.padding(
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 4.dp,
                    top = 2.dp
                ),
                text = description,
                style = ChatTheme.typography.footnote,
                color = ChatTheme.colors.textHighEmphasis,
                maxLines = linkDescriptionMaxLines,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
