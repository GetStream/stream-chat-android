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

package io.getstream.chat.android.compose.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Displays an image icon if no image was loaded previously
 * or the request has failed, a circular progress indicator
 * if the image is loading or nothing if the image has successfully
 * loaded. Does not show the image background or loading indicator
 * if the media is a video attachment as it doesn't fit well along the
 * play button.
 *
 * @param asyncImagePainterState The painter state used to determine
 * which UI to show.
 * @param isImage If the attachment we are holding the place for is
 * a image or not.
 * @param progressIndicatorStrokeWidth The thickness of the progress indicator
 * used to indicate a loading thumbnail.
 * @param progressIndicatorFillMaxSizePercentage Dictates what percentage of
 * available parent size the progress indicator will fill.
 * @param placeholderIconTintColor The tint of the place holder icon.
 */
@Composable
internal fun MediaPreviewPlaceHolder(
    asyncImagePainterState: AsyncImagePainter.State,
    isImage: Boolean = false,
    progressIndicatorStrokeWidth: Dp,
    progressIndicatorFillMaxSizePercentage: Float,
    placeholderIconTintColor: Color = ChatTheme.colors.textLowEmphasis,

) {
    val painter = painterResource(
        id = R.drawable.stream_compose_ic_image_picker,
    )

    val imageModifier = Modifier.fillMaxSize(0.4f)

    when {
        asyncImagePainterState is AsyncImagePainter.State.Loading -> {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .fillMaxSize(progressIndicatorFillMaxSizePercentage),
                strokeWidth = progressIndicatorStrokeWidth,
                color = ChatTheme.colors.primaryAccent,
            )
        }
        asyncImagePainterState is AsyncImagePainter.State.Error && isImage -> Icon(
            tint = placeholderIconTintColor,
            modifier = imageModifier,
            painter = painter,
            contentDescription = null,
        )
        asyncImagePainterState is AsyncImagePainter.State.Success -> {}
        asyncImagePainterState is AsyncImagePainter.State.Empty && isImage -> {
            Icon(
                tint = placeholderIconTintColor,
                modifier = imageModifier,
                painter = painter,
                contentDescription = null,
            )
        }
    }
}
