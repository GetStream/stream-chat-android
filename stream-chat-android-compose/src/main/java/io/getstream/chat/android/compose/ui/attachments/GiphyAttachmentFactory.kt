package io.getstream.chat.android.compose.ui.attachments

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * An extensions of the [AttachmentFactory] that validates and shows Giphy attachments.
 */
public class GiphyAttachmentFactory : AttachmentFactory(
    canHandle = { attachments -> attachments.any { it.type == ModelType.attach_giphy } },
    content = @Composable { GiphyAttachmentContent(it) }
)

/**
 * Represents the content that [GiphyAttachmentFactory] produces for any given Giphy attachment.
 *
 * It shows the GIF, as well as a label for users to recognize it's sent from Giphy.
 *
 * @param attachmentState - The attachment to show.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun GiphyAttachmentContent(attachmentState: AttachmentState) {
    val context = LocalContext.current
    val (modifier, messageItem, onLongItemClick) = attachmentState
    val (message, _) = messageItem
    val attachment = message.attachments.firstOrNull { it.type == ModelType.attach_giphy }

    checkNotNull(attachment) {
        "Missing Giphy attachment."
    }

    val previewUrl = attachment.titleLink ?: attachment.ogUrl

    checkNotNull(previewUrl) {
        "Missing preview URL."
    }

    val painter = rememberImagePainter(attachment.imagePreviewUrl)

    Box(
        modifier = modifier
            .size(height = 200.dp, width = 250.dp)
            .clip(ChatTheme.shapes.attachment)
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
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        Image(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .width(64.dp)
                .wrapContentHeight(),
            painter = rememberImagePainter(R.drawable.stream_compose_giphy_label),
            contentDescription = null,
            contentScale = ContentScale.Inside
        )
    }
}
