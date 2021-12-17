package io.getstream.chat.android.compose.ui.components.attachments.images

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState

/**
 * Shows the UI for images the user can pick for message attachments. Exposes the logic of selecting
 * items.
 *
 * @param images The images the user can pick, to be rendered in a list.
 * @param onImageSelected Handler when the user clicks on any image item.
 * @param modifier Modifier for styling.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ImagesPicker(
    images: List<AttachmentPickerItemState>,
    onImageSelected: (AttachmentPickerItemState) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        modifier = modifier,
        cells = GridCells.Fixed(3),
        contentPadding = PaddingValues(1.dp)
    ) {
        items(images) { imageItem -> ImagesPickerGridItem(imageItem = imageItem, onImageSelected) }
    }
}
