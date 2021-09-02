package io.getstream.chat.android.compose.ui.attachments.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.MimeTypeIconProvider

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
