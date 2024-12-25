/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.LocalStreamImageLoader
import io.getstream.chat.android.compose.ui.util.mirrorRtl
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.ui.common.utils.MediaStringUtil

private const val DefaultNumberOfPicturesPerRow = 3

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
        contentPadding = PaddingValues(1.dp),
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
            .height(125.dp)
            .padding(2.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = { onImageSelected(imageItem) },
            )
            .testTag("Stream_AttachmentPickerSampleImage"),
    ) {
        CoilImage(
            imageRequest = { imageRequest },
            imageLoader = { LocalStreamImageLoader.current },
            modifier = Modifier.fillMaxSize(),
            imageOptions = ImageOptions(contentScale = ContentScale.Crop),
        )

        if (imageItem.isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .background(shape = CircleShape, color = ChatTheme.colors.overlayDark),
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.Center),
                    painter = painterResource(id = R.drawable.stream_compose_ic_checkmark),
                    contentDescription = null,
                    tint = ChatTheme.colors.appBackground,
                )
            }
        }

        if (isVideo) {
            VideoThumbnailOverlay(attachmentMetaData.videoLength)
        }
    }
}

/**
 * Represents an overlay that is shown over videos in the picker.
 *
 * @param videoLength The duration of video in seconds.
 * @param modifier Modifier for styling.
 */
@Composable
private fun BoxScope.VideoThumbnailOverlay(
    videoLength: Long,
    modifier: Modifier = Modifier,
) {
    val overlayShape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .wrapContentSize()
            .padding(horizontal = 4.dp, vertical = 5.dp)
            .border(
                width = 1.dp,
                color = ChatTheme.colors.borders,
                shape = overlayShape,
            )
            .background(
                shape = overlayShape,
                color = ChatTheme.colors.barsBackground,
            )
            .align(Alignment.BottomCenter)
            .padding(vertical = 2.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            modifier = Modifier
                .size(16.dp)
                .aspectRatio(1f)
                .mirrorRtl(LocalLayoutDirection.current)
                .align(Alignment.CenterVertically),
            painter = painterResource(id = R.drawable.stream_compose_ic_video),
            contentDescription = null,
            tint = ChatTheme.colors.textHighEmphasis,
        )

        Text(
            modifier = Modifier
                .padding(start = 4.dp, end = 2.dp)
                .align(Alignment.CenterVertically),
            text = MediaStringUtil.convertVideoLength(videoLength),
            style = ChatTheme.typography.footnote,
            color = ChatTheme.colors.textHighEmphasis,
        )
    }
}

/**
 * Default 'pick more' tile to be shown if the user can pick more images.
 *
 * @param onPickMoreClick Action invoked when the user clicks on the 'pick more' tile.
 */
@Composable
internal fun DefaultAddMoreItem(onPickMoreClick: () -> Unit) {
    Column(
        modifier = Modifier
            .height(125.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = ChatTheme.colors.borders,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onPickMoreClick,
            )
            .testTag("Stream_AttachmentPickerPickMore"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_add),
            contentDescription = null,
            tint = ChatTheme.colors.textLowEmphasis,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.stream_ui_message_composer_permissions_visual_media_add_more),
            style = ChatTheme.typography.body,
            color = ChatTheme.colors.textLowEmphasis,
        )
    }
}

/**
 * The time code of the frame to extract from a video.
 */
private const val VideoFrameMillis: Long = 1000
