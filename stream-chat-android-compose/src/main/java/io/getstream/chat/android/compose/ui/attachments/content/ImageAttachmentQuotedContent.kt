package io.getstream.chat.android.compose.ui.attachments.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberImagePainter
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * TODO
 */
@Composable
public fun ImageAttachmentQuotedContent(
    attachment: Attachment,
    modifier: Modifier = Modifier,
) {
    val imagePainter = rememberImagePainter(attachment.imagePreviewUrl)

    Box(modifier = modifier
        .clip(ChatTheme.shapes.quotedAttachment)
    ) {
        Image(
            painter = imagePainter,
            contentDescription = "",
            contentScale = ContentScale.Crop
        )
    }
}