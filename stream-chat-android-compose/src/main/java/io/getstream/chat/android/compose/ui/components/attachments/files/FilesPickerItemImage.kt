package io.getstream.chat.android.compose.ui.components.attachments.files

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.rememberImagePainter
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.MimeTypeIconProvider

/**
 * Represents the image that's shown in file picker items. This can be either an image/icon that represents the file type
 * or a thumbnail in case the file type is an image.
 *
 * @param fileItem - The item we use to show the image.
 */
@Composable
public fun FilesPickerItemImage(
    fileItem: AttachmentPickerItemState,
    modifier: Modifier = Modifier,
) {
    val attachment = fileItem.attachmentMetaData
    val isImage = fileItem.attachmentMetaData.type == "image"

    val painter = if (isImage) {
        val dataToLoad = attachment.uri ?: attachment.file

        rememberImagePainter(dataToLoad)
    } else {
        painterResource(id = MimeTypeIconProvider.getIconRes(attachment.mimeType))
    }

    val shape = if (isImage) ChatTheme.shapes.imageThumbnail else null

    val imageModifier = modifier.let { baseModifier ->
        if (shape != null) baseModifier.clip(shape) else baseModifier
    }

    Image(
        modifier = imageModifier,
        painter = painter,
        contentDescription = null,
        contentScale = if (isImage) ContentScale.Crop else ContentScale.Fit
    )
}
