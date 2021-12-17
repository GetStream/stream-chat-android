package io.getstream.chat.android.compose.ui.components.attachments.images

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents each image item in the grid.
 *
 * @param imageItem The image to render.
 * @param onImageSelected Handler when the user selects an image.
 */
@Composable
internal fun ImagesPickerGridItem(
    imageItem: AttachmentPickerItemState,
    onImageSelected: (AttachmentPickerItemState) -> Unit,
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
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .background(shape = CircleShape, color = ChatTheme.colors.overlayDark)
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.Center),
                    painter = painterResource(id = R.drawable.stream_compose_ic_checkmark),
                    contentDescription = null,
                    tint = ChatTheme.colors.appBackground
                )
            }
        }
    }
}
