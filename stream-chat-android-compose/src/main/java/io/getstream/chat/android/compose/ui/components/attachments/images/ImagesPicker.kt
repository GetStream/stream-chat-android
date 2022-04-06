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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import coil.fetch.VideoFrameUriFetcher
import coil.request.videoFrameMillis
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.MediaStringUtil
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.mirrorRtl

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
public fun ImagesPicker(
    images: List<AttachmentPickerItemState>,
    onImageSelected: (AttachmentPickerItemState) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (AttachmentPickerItemState) -> Unit = { imageItem ->
        DefaultImagesPickerItem(
            imageItem = imageItem,
            onImageSelected = onImageSelected
        )
    },
) {
    LazyVerticalGrid(
        modifier = modifier,
        cells = GridCells.Fixed(3),
        contentPadding = PaddingValues(1.dp)
    ) {
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
    val isVideo = attachmentMetaData.type == ModelType.attach_video

    val painter = rememberImagePainter(
        data = attachmentMetaData.uri.toString(),
        builder = {
            if (isVideo) {
                videoFrameMillis(VideoFrameMillis)
                fetcher(VideoFrameUriFetcher(LocalContext.current))
            }
        }
    )

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

        if (isVideo) {
            Icon(
                modifier = Modifier
                    .padding(4.dp)
                    .size(24.dp)
                    .mirrorRtl(LocalLayoutDirection.current)
                    .align(Alignment.BottomStart),
                painter = painterResource(id = R.drawable.stream_compose_ic_video),
                contentDescription = null,
                tint = Color.White
            )

            val videoLength = attachmentMetaData.videoLength
            if (videoLength != 0L) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .align(Alignment.BottomEnd),
                    text = MediaStringUtil.convertVideoLength(videoLength),
                    style = ChatTheme.typography.bodyBold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * The time code of the frame to extract from a video.
 */
private const val VideoFrameMillis: Long = 1000
