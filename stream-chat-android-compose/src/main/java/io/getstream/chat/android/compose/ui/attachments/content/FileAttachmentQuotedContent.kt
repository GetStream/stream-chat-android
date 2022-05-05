package io.getstream.chat.android.compose.ui.attachments.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.rememberImagePainter
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.ui.util.MimeTypeIconProvider

/**
 * TODO
 */
@Composable
public fun FileAttachmentQuotedContent(
    attachment: Attachment,
    modifier: Modifier = Modifier,
) {
    val isImage = attachment.type == "image"

    val painter = if (isImage) {
        val dataToLoad = attachment.imageUrl ?: attachment.upload

        rememberImagePainter(dataToLoad)
    } else {
        painterResource(id = MimeTypeIconProvider.getIconRes(attachment.mimeType))
    }

    Box(modifier = modifier) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = if (isImage) ContentScale.Crop else ContentScale.Fit
        )
    }
}