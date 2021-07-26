package io.getstream.chat.android.compose.ui.messages.attachments

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentItem
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Shows the UI for images the user can pick for message attachments. Exposes the logic of selecting
 * items.
 *
 * @param images - The images the user can pick, to be rendered in a list.
 * @param onImageSelected - Handler when the user clicks on any image item.
 * @param modifier - Modifier for styling.
 * */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ImagesPicker(
    images: List<AttachmentItem>,
    onImageSelected: (AttachmentItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        modifier = modifier,
        cells = GridCells.Fixed(3),
        contentPadding = PaddingValues(1.dp)
    ) {
        items(images) { imageItem -> ImageGridItem(imageItem = imageItem, onImageSelected) }
    }
}

/**
 * Represents each image item in the grid.
 *
 * @param imageItem - The image to render.
 * @param onImageSelected - Handler when the user selects an image.
 * */
@Composable
internal fun ImageGridItem(
    imageItem: AttachmentItem,
    onImageSelected: (AttachmentItem) -> Unit,
) {
    val painter = rememberImagePainter(data = imageItem.attachmentMetaData.uri.toString())

    Box(
        modifier = Modifier
            .height(125.dp)
            .padding(2.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                onClick = { onImageSelected(imageItem) }
            )
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        if (imageItem.isSelected) {
            Icon(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = ChatTheme.colors.primaryAccent
            )
        }
    }
}
