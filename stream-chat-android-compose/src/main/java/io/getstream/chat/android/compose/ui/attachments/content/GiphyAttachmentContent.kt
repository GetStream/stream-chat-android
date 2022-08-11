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

package io.getstream.chat.android.compose.ui.attachments.content

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.attachments.StreamAttachmentFactories
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.rememberStreamImagePainter
import io.getstream.chat.android.ui.utils.GiphyInfoType
import io.getstream.chat.android.ui.utils.giphyInfo
import kotlin.math.abs

/**
 * Builds a Giphy attachment message.
 *
 * It shows the GIF, as well as a label for users to recognize it's sent from Giphy.
 *
 * @param attachmentState - The attachment to show.
 * @param modifier Modifier for styling.
 * @param giphyInfoType Used to modify the quality of the rendered Giphy attachments.
 * @param upscaleFactor The amount the Giphy will upscaled. By default this is set to 1,
 * meaning that the each Giphy pixel will take exactly 1 screen pixel.
 */
@OptIn(ExperimentalFoundationApi::class)
@Suppress("LongMethod")
@Composable
public fun GiphyAttachmentContent(
    attachmentState: AttachmentState,
    modifier: Modifier = Modifier,
    giphyInfoType: GiphyInfoType = GiphyInfoType.ORIGINAL,
    upscaleFactor: Float = 1f,
) {
    val context = LocalContext.current
    val (message, onLongItemClick) = attachmentState
    val attachment = message.attachments.firstOrNull { it.type == ModelType.attach_giphy }

    checkNotNull(attachment) {
        "Missing Giphy attachment."
    }

    val previewUrl = attachment.titleLink ?: attachment.ogUrl

    checkNotNull(previewUrl) {
        "Missing preview URL."
    }

    val density = LocalDensity.current

    val painter = rememberStreamImagePainter(attachment.imagePreviewUrl)

    val giphyInfo = attachment.giphyInfo(giphyInfoType)

    val maxWidth = ChatTheme.dimens.attachmentsContentGiphyMaxWidth
    val maxHeight = ChatTheme.dimens.attachmentsContentGiphyMaxHeight

    val giphyDimensions: DpSize by remember(key1 = giphyInfo, key2 = upscaleFactor) {
        derivedStateOf {
            if (giphyInfo != null) {
                with(density) {
                    val giphyWidth = (giphyInfo.width * abs(upscaleFactor)).toDp()
                    val giphyHeight = (giphyInfo.height * abs(upscaleFactor)).toDp()

                    when {
                        upscaleFactor == StreamAttachmentFactories.GIPHY_FILL_MAX_SPACE -> calculateResultingDimensions(
                            maxWidth = maxWidth,
                            maxHeight = maxHeight,
                            giphyWidth = giphyWidth,
                            giphyHeight = giphyHeight
                        )
                        giphyWidth <= maxWidth && giphyHeight <= maxHeight -> DpSize(giphyWidth, giphyHeight)
                        else -> calculateResultingDimensions(
                            maxWidth = maxWidth,
                            maxHeight = maxHeight,
                            giphyWidth = giphyWidth,
                            giphyHeight = giphyHeight
                        )
                    }
                }
            } else {
                DpSize(maxWidth, maxHeight)
            }
        }
    }

    Box(
        modifier = modifier
            .size(giphyDimensions)
            .clip(ChatTheme.shapes.attachment)
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(previewUrl)
                        )
                    )
                },
                onLongClick = { onLongItemClick(message) }
            )
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        Image(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .width(64.dp)
                .wrapContentHeight(),
            painter = painterResource(R.drawable.stream_compose_giphy_label),
            contentDescription = null,
            contentScale = ContentScale.Inside
        )
    }
}

/**
 * Calculates the resulting dimension of a resized Giphy that
 * need to be constrained inside an upper limit of height and width.
 *
 * The resulting resized dimensions will retain the original aspect ratio
 *
 * @param maxWidth The maximum width dimension the resulting Giphy
 * will be constrained to.
 * @param maxHeight The maximum height dimension the resulting Giphy
 * will be constrained to.
 * @param giphyWidth The original width of the Giphy expressed in Dp.
 * @param giphyHeight The original height of the giphy expressed in Dp.
 *
 * @return The resulting resized dimensions.
 */
private fun calculateResultingDimensions(
    maxWidth: Dp,
    maxHeight: Dp,
    giphyWidth: Dp,
    giphyHeight: Dp,
): DpSize {
    val widthRatio = maxWidth / giphyWidth
    val heightRatio = maxHeight / giphyHeight

    val resultingRatio = minOf(widthRatio, heightRatio)

    return if (widthRatio < heightRatio) {
        DpSize(maxWidth, (giphyHeight.value * resultingRatio).dp)
    } else {
        DpSize((giphyWidth.value * resultingRatio).dp, maxHeight)
    }
}
