package io.getstream.chat.android.compose.ui.attachments.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.ui.components.CancelIcon
import io.getstream.chat.android.compose.ui.components.composer.MessageInput

/**
 * UI for currently selected image attachments, within the [MessageInput].
 *
 * @param attachments Selected attachments.
 * @param onAttachmentRemoved Handler when the user removes an attachment from the list.
 * @param modifier Modifier for styling.
 */
@Composable
public fun ImageAttachmentPreviewContent(
    attachments: List<Attachment>,
    onAttachmentRemoved: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start)
    ) {
        items(attachments) { image ->
            val painter = rememberImagePainter(data = image.upload ?: image.imagePreviewUrl)

            Box(
                modifier = Modifier
                    .size(95.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )

                CancelIcon(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    onClick = { onAttachmentRemoved(image) }
                )
            }
        }
    }
}
