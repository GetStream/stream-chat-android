/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.components.attachments.images

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.request.ImageRequest
import coil3.video.VideoFrameDecoder
import coil3.video.videoFrameMillis
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState.Selection
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.common.utils.MediaStringUtil

private const val DefaultNumberOfPicturesPerRow = 3
private val ItemShape = RoundedCornerShape(2.dp)
private val SelectionIndicatorSize = 24.dp

/**
 * Shows the UI for images the user can pick for message attachments. Exposes the logic of selecting
 * items.
 *
 * @param images The images the user can pick, to be rendered in a list.
 * @param onImageSelected Handler when the user clicks on any image item.
 * @param modifier Modifier for styling.
 * @param itemContent Composable rendering an image/video item in the picker.
 * @param showAddMore Flag indicating the the "Add more" item should be shown at the beginning of the picker.
 * @param onAddMoreClick Action to be invoked when the user clicks on the "Add more" item.
 * @param addMoreContent Composable rendering the "Add more" item.
 */
@Composable
public fun ImagesPicker(
    images: List<AttachmentPickerItemState>,
    onImageSelected: (AttachmentPickerItemState) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (AttachmentPickerItemState) -> Unit = { imageItem ->
        DefaultImagesPickerItem(
            imageItem = imageItem,
            onImageSelected = onImageSelected,
        )
    },
    showAddMore: Boolean = false,
    onAddMoreClick: () -> Unit = {},
    addMoreContent: @Composable () -> Unit = {
        DefaultAddMoreItem(onAddMoreClick)
    },
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(DefaultNumberOfPicturesPerRow),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (showAddMore) {
            item { addMoreContent() }
        }
        items(images) { imageItem -> itemContent(imageItem) }
    }
}

/**
 * The default images picker item.
 *
 * @param imageItem The attachment item.
 * @param onImageSelected Handler when the user selects the image.
 */
@Composable
internal fun DefaultImagesPickerItem(
    imageItem: AttachmentPickerItemState,
    onImageSelected: (AttachmentPickerItemState) -> Unit,
) {
    val attachmentMetaData = imageItem.attachmentMetaData
    val isVideo = attachmentMetaData.type == AttachmentType.VIDEO

    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(attachmentMetaData.uri.toString())
        .apply {
            if (isVideo) {
                videoFrameMillis(VideoFrameMillis)
                decoderFactory(VideoFrameDecoder.Factory())
            }
        }
        .build()

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(ItemShape)
            .clickable { onImageSelected(imageItem) }
            .testTag("Stream_AttachmentPickerSampleImage"),
    ) {
        StreamAsyncImage(
            imageRequest = imageRequest,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        if (imageItem.selection is Selection.Selected) {
            SelectedIndicator(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(StreamTokens.spacingXs),
                selection = imageItem.selection,
            )
        } else {
            UnselectedIndicator(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(StreamTokens.spacingXs),
            )
        }

        if (isVideo) {
            VideoThumbnailOverlay(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(StreamTokens.spacingXs),
                videoLength = attachmentMetaData.videoLength,
            )
        }
    }
}

@Composable
private fun SelectedIndicator(
    selection: Selection.Selected,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(SelectionIndicatorSize)
            .background(
                shape = CircleShape,
                color = ChatTheme.colors.borderCoreOnDark,
            )
            .padding(2.dp)
            .background(
                shape = CircleShape,
                color = ChatTheme.colors.accentPrimary,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = selection.count.toString(),
            color = ChatTheme.colors.badgeText,
            style = ChatTheme.typography.numericXl,
        )
    }
}

@Composable
private fun UnselectedIndicator(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(SelectionIndicatorSize)
            .border(
                width = 2.dp,
                shape = CircleShape,
                color = ChatTheme.colors.borderCoreOnDark,
            ),
    )
}

@Composable
private fun VideoThumbnailOverlay(
    videoLength: Long,
    modifier: Modifier = Modifier,
) {
    val overlayShape = RoundedCornerShape(9.dp)

    Row(
        modifier = modifier
            .background(
                shape = overlayShape,
                color = ChatTheme.colors.badgeBgInverse,
            )
            .padding(
                horizontal = StreamTokens.spacingXs,
                vertical = StreamTokens.spacing2xs,
            ),
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_video),
            contentDescription = null,
            tint = ChatTheme.colors.badgeText,
        )
        Text(
            text = MediaStringUtil.convertVideoLength(videoLength),
            style = ChatTheme.typography.numericMd,
            color = ChatTheme.colors.badgeText,
        )
    }
}

/**
 * Default 'pick more' tile to be shown if the user can pick more images.
 *
 * @param onPickMoreClick Action invoked when the user clicks on the 'pick more' tile.
 */
@Composable
private fun DefaultAddMoreItem(onPickMoreClick: () -> Unit) {
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .background(
                color = ChatTheme.colors.backgroundCoreSurfaceSubtle,
                shape = ItemShape,
            )
            .clip(ItemShape)
            .clickable(onClick = onPickMoreClick)
            .testTag("Stream_AttachmentPickerPickMore"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs, Alignment.CenterVertically),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_add),
            contentDescription = null,
            tint = ChatTheme.colors.textPrimary,
        )
        Text(
            text = stringResource(R.string.stream_ui_message_composer_permissions_visual_media_add_more),
            style = ChatTheme.typography.captionEmphasis,
            color = ChatTheme.colors.textPrimary,
        )
    }
}

/**
 * The time code of the frame to extract from a video.
 */
private const val VideoFrameMillis: Long = 1000

@Preview(showBackground = true)
@Composable
private fun ImagesPickerItemsPreview() {
    ChatTheme {
        ImagesPicker(showAddMore = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun ImagesPickerAddMorePreview() {
    ChatTheme {
        ImagesPicker(showAddMore = true)
    }
}

@Composable
internal fun ImagesPicker(showAddMore: Boolean) {
    ImagesPicker(
        images = listOf(
            AttachmentPickerItemState(
                attachmentMetaData = AttachmentMetaData(),
            ),
            AttachmentPickerItemState(
                attachmentMetaData = AttachmentMetaData(),
                selection = Selection.Selected(count = 1),
            ),
            AttachmentPickerItemState(
                attachmentMetaData = AttachmentMetaData(type = AttachmentType.VIDEO).apply {
                    videoLength = VideoFrameMillis
                },
            ),
        ),
        onImageSelected = {},
        showAddMore = showAddMore,
    )
}
