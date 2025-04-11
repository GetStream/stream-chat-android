/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.compose.ui.attachments.preview.internal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.attachments.content.PlayButton
import io.getstream.chat.android.compose.ui.components.ShimmerProgressIndicator
import io.getstream.chat.android.compose.ui.components.avatar.Avatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.compose.ui.util.isCompleted
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.images.resizing.applyStreamCdnImageResizingIfEnabled
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.ui.common.utils.extensions.initials

/**
 * Composable displaying a grid of media attachments in a bottom sheet layout.
 *
 * Shows a gallery view of all media attachments associated with a message, with
 * thumbnails arranged in a grid. The gallery appears as a bottom sheet with rounded
 * top corners and includes a header with a close button.
 *
 * The component overlays the entire screen with a semi-transparent background.
 * Tapping outside the gallery area dismisses it. Each media item displays a thumbnail
 * with the sender's avatar in the top-left corner, and video items show a play button.
 *
 * @param attachments List of attachments to display in the gallery grid.
 * @param user The sender of the message containing these attachments.
 * @param onClick Callback invoked when a media item is clicked, providing its index.
 * @param onDismiss Callback invoked when the gallery should be dismissed.
 * @param modifier Optional modifier applied to the gallery surface.
 */
@Composable
internal fun MediaGalleryPhotosMenu(
    attachments: List<Attachment>,
    user: User,
    onClick: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.overlay)
            .clickable(
                indication = null,
                interactionSource = null,
                onClick = onDismiss,
            ),
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            shadowElevation = 4.dp,
            color = ChatTheme.colors.barsBackground,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                MediaGalleryPhotosMenuHeader(onDismiss)
                LazyVerticalGrid(columns = GridCells.Fixed(ColumnCount)) {
                    itemsIndexed(attachments) { index, attachment ->
                        MediaGalleryPhotosMenuItem(
                            attachment = attachment,
                            user = user,
                            onClick = { onClick(index) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Composable that displays the header for the media gallery bottom sheet.
 *
 * Shows a title "Photos" centered in the header and a close button on the left side.
 * Both the title and close button use the high emphasis text color from the current theme.
 * The close button is clickable and will dismiss the gallery when pressed.
 *
 * @param onDismiss Callback invoked when the user clicks the close button.
 */
@Composable
private fun MediaGalleryPhotosMenuHeader(onDismiss: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Icon(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(8.dp)
                .clickable(
                    bounded = false,
                    onClick = onDismiss,
                ),
            painter = painterResource(id = R.drawable.stream_compose_ic_close),
            contentDescription = stringResource(id = R.string.stream_compose_cancel),
            tint = ChatTheme.colors.textHighEmphasis,
        )

        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(R.string.stream_compose_image_preview_photos),
            style = ChatTheme.typography.title3Bold,
            color = ChatTheme.colors.textHighEmphasis,
        )
    }
}

/**
 * Composable that displays a single media item in the gallery grid.
 *
 * Renders a thumbnail of the attachment with appropriate styling based on the media type
 * (image or video). For video attachments, a play button indicator is shown. Each media
 * item displays the user's avatar in the top-left corner to indicate the sender.
 *
 * The component handles various loading states of the media thumbnail:
 * - Loading: Shows a shimmer progress indicator
 * - Success: Displays the media thumbnail
 * - Error: Shows an error icon
 *
 * @param attachment The attachment to display in this grid item.
 * @param user The user who sent the message containing this attachment.
 * @param onClick Callback invoked when this grid item is clicked.
 */
@Suppress("LongMethod")
@Composable
private fun MediaGalleryPhotosMenuItem(
    attachment: Attachment,
    user: User,
    onClick: () -> Unit,
) {
    val isImage = attachment.isImage()
    val isVideo = attachment.isVideo()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        val data =
            if (isImage || (isVideo && ChatTheme.videoThumbnailsEnabled)) {
                attachment.imagePreviewUrl?.applyStreamCdnImageResizingIfEnabled(ChatTheme.streamCdnImageResizing)
            } else {
                null
            }

        val context = LocalContext.current
        val imageRequest = remember {
            ImageRequest.Builder(context)
                .data(data)
                .build()
        }

        var imageState by remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }

        val backgroundColor = if (isImage) {
            ChatTheme.colors.imageBackgroundMediaGalleryPicker
        } else {
            ChatTheme.colors.videoBackgroundMediaGalleryPicker
        }

        StreamAsyncImage(
            imageRequest = imageRequest,
            modifier = Modifier
                .padding(1.dp)
                .fillMaxSize()
                .background(color = backgroundColor),
            contentScale = ContentScale.Crop,
        ) { asyncImageState ->
            imageState = asyncImageState

            when (asyncImageState) {
                is AsyncImagePainter.State.Empty,
                is AsyncImagePainter.State.Loading,
                -> ShimmerProgressIndicator(modifier = Modifier.fillMaxSize())

                is AsyncImagePainter.State.Success -> Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = asyncImageState.painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )

                is AsyncImagePainter.State.Error -> ErrorIcon(Modifier.fillMaxSize())
            }
        }

        Avatar(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .size(24.dp)
                .border(width = 1.dp, color = Color.White, shape = ChatTheme.shapes.avatar)
                .shadow(elevation = 4.dp, shape = ChatTheme.shapes.avatar),
            imageUrl = user.image,
            initials = user.initials,
            textStyle = ChatTheme.typography.captionBold,
        )

        if (isVideo && imageState.isCompleted) {
            PlayButton(
                modifier = Modifier
                    .shadow(6.dp, shape = CircleShape)
                    .background(color = Color.White, shape = CircleShape)
                    .fillMaxSize(fraction = 0.2f),
                contentDescription = stringResource(R.string.stream_compose_cd_play_button),
            )
        }
    }
}

/**
 * Composable for displaying an error icon when the image fails to load.
 *
 * @param modifier The modifier to be applied to the icon.
 */
@Composable
private fun ErrorIcon(modifier: Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            tint = ChatTheme.colors.disabled,
            modifier = Modifier.fillMaxSize(fraction = 0.4f),
            painter = painterResource(R.drawable.stream_compose_ic_image_picker),
            contentDescription = stringResource(R.string.stream_ui_message_list_attachment_load_failed),
        )
    }
}

/**
 * Defines the number of columns in the media gallery grid.
 */
private const val ColumnCount = 3
