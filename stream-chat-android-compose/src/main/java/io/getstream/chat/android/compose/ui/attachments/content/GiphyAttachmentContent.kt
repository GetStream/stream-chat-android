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

package io.getstream.chat.android.compose.ui.attachments.content

import android.content.Context
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.compose.LocalAsyncImagePreviewHandler
import io.getstream.chat.android.client.utils.attachment.isGiphy
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.theme.StreamDimens
import io.getstream.chat.android.compose.ui.util.AsyncImagePreviewHandler
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.compose.ui.util.applyIf
import io.getstream.chat.android.compose.ui.util.shouldBeDisplayedAsFullSizeAttachment
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.utils.GiphyInfoType
import io.getstream.chat.android.ui.common.utils.GiphySizingMode
import io.getstream.chat.android.ui.common.utils.giphyInfo

/**
 * Builds a Giphy attachment message.
 *
 * It shows the GIF, as well as a label for users to recognize it's sent from Giphy.
 *
 * @param attachmentState - The attachment to show.
 * @param modifier Modifier for styling.
 * @param giphyInfoType Used to modify the quality and dimensions of the rendered
 * Giphy attachments.
 * @param giphySizingMode Sets the Giphy container sizing strategy. Setting it to automatic
 * makes the container capable of adaptive resizing and ignore
 * [StreamDimens.attachmentsContentGiphyWidth] and [StreamDimens.attachmentsContentGiphyHeight]
 * dimensions, however you can still clip maximum dimensions using [StreamDimens.attachmentsContentGiphyMaxWidth]
 * and [StreamDimens.attachmentsContentGiphyMaxHeight].
 * Setting it to fixed size mode will make it respect all given dimensions.
 * @param contentScale Used to determine the way Giphys are scaled inside the [Image] composable.
 * @param onItemClick Lambda called when an item gets clicked.
 */
@OptIn(ExperimentalFoundationApi::class)
@Suppress("LongMethod")
@Composable
@Deprecated(
    message = "Use GiphyAttachmentContent version with the `onItemClick` parameter that accepts a " +
        "GiphyAttachmentClickData.",
    replaceWith = ReplaceWith(
        "GiphyAttachmentContent(" +
            "state = state, " +
            "modifier = modifier, " +
            "giphyInfoType = giphyInfoType, " +
            "giphySizingMode = giphySizingMode, " +
            "contentScale = contentScale, " +
            "onItemClick = onItemClick" +
            ")",
    ),
    level = DeprecationLevel.WARNING,
)
public fun GiphyAttachmentContent(
    attachmentState: AttachmentState,
    modifier: Modifier = Modifier,
    giphyInfoType: GiphyInfoType = GiphyInfoType.ORIGINAL,
    giphySizingMode: GiphySizingMode = GiphySizingMode.ADAPTIVE,
    contentScale: ContentScale = ContentScale.Crop,
    onItemClick: (context: Context, previewUrl: String) -> Unit = ::onGiphyAttachmentContentClick,
) {
    GiphyAttachmentContent(
        state = attachmentState,
        modifier = modifier,
        giphyInfoType = giphyInfoType,
        giphySizingMode = giphySizingMode,
        contentScale = contentScale,
        onItemClick = {
            onItemClick(
                it.context,
                it.url,
            )
        },
    )
}

/**
 * Builds a Giphy attachment message.
 *
 * It shows the GIF, as well as a label for users to recognize it's sent from Giphy.
 *
 * @param state - The attachment to show.
 * @param modifier Modifier for styling.
 * @param giphyInfoType Used to modify the quality and dimensions of the rendered
 * Giphy attachments.
 * @param giphySizingMode Sets the Giphy container sizing strategy. Setting it to automatic
 * makes the container capable of adaptive resizing and ignore
 * [StreamDimens.attachmentsContentGiphyWidth] and [StreamDimens.attachmentsContentGiphyHeight]
 * dimensions, however you can still clip maximum dimensions using [StreamDimens.attachmentsContentGiphyMaxWidth]
 * and [StreamDimens.attachmentsContentGiphyMaxHeight].
 * Setting it to fixed size mode will make it respect all given dimensions.
 * @param contentScale Used to determine the way Giphys are scaled inside the [Image] composable.
 * @param onItemClick Lambda called when an item gets clicked.
 */
@OptIn(ExperimentalFoundationApi::class)
@Suppress("LongMethod")
@Composable
public fun GiphyAttachmentContent(
    state: AttachmentState,
    modifier: Modifier = Modifier,
    giphyInfoType: GiphyInfoType = GiphyInfoType.FIXED_HEIGHT_DOWNSAMPLED,
    giphySizingMode: GiphySizingMode = GiphySizingMode.ADAPTIVE,
    contentScale: ContentScale = ContentScale.Crop,
    onItemClick: (GiphyAttachmentClickData) -> Unit = {
        onGiphyAttachmentContentClick(it.context, it.url)
    },
) {
    val context = LocalContext.current
    val message = state.message
    val attachment = message.attachments.firstOrNull(Attachment::isGiphy)

    checkNotNull(attachment) {
        "Missing Giphy attachment."
    }

    val previewUrl = attachment.titleLink ?: attachment.ogUrl

    checkNotNull(previewUrl) {
        "Missing preview URL."
    }

    val density = LocalDensity.current

    val giphyInfo = attachment.giphyInfo(giphyInfoType)

    val maxWidth = ChatTheme.dimens.attachmentsContentGiphyMaxWidth
    val maxHeight = ChatTheme.dimens.attachmentsContentGiphyMaxHeight

    val width = ChatTheme.dimens.attachmentsContentGiphyWidth
    val height = ChatTheme.dimens.attachmentsContentGiphyHeight

    val giphyDimensions: DpSize by remember(key1 = giphyInfo) {
        derivedStateOf {
            if (giphyInfo != null) {
                with(density) {
                    val giphyWidth = giphyInfo.width.toDp()
                    val giphyHeight = giphyInfo.height.toDp()

                    when {
                        giphySizingMode == GiphySizingMode.FIXED_SIZE -> {
                            DpSize(
                                width = width.coerceIn(
                                    minimumValue = null,
                                    maximumValue = maxWidth,
                                ),
                                height = height.coerceIn(
                                    minimumValue = null,
                                    maximumValue = maxHeight,
                                ),
                            )
                        }

                        else -> calculateResultingDimensions(
                            maxWidth = maxWidth,
                            maxHeight = maxHeight,
                            giphyWidth = giphyWidth,
                            giphyHeight = giphyHeight,
                        )
                    }
                }
            } else {
                DpSize(maxWidth, maxHeight)
            }
        }
    }

    val shouldBeFullSize = message.shouldBeDisplayedAsFullSizeAttachment()
    Box(
        modifier = modifier
            .size(giphyDimensions)
            .applyIf(!shouldBeFullSize) {
                padding(MessageStyling.messageSectionPadding)
                    .clip(ChatTheme.shapes.attachment)
            }
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    onItemClick(
                        GiphyAttachmentClickData(
                            context = context,
                            url = previewUrl,
                            attachment = attachment,
                            message = message,
                        ),
                    )
                },
                onLongClick = { state.onLongItemClick(message) },
            ),
    ) {
        StreamAsyncImage(
            data = giphyInfo?.url,
            modifier = Modifier.fillMaxSize(),
            contentDescription = null,
            contentScale = contentScale,
        )

        Image(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .width(64.dp)
                .wrapContentHeight(),
            painter = painterResource(R.drawable.stream_compose_giphy_label),
            contentDescription = null,
            contentScale = ContentScale.Inside,
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

/**
 * Handles clicks on Giphy attachment content.
 *
 * @param context Context needed to start the Activity.
 * @param previewUrl The url of the Giphy attachment being clicked.
 */
internal fun onGiphyAttachmentContentClick(context: Context, previewUrl: String) {
    context.startActivity(
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse(previewUrl),
        ),
    )
}

/**
 * Data class that holds information about a giphy attachment click event.
 *
 * @param context The context in which the click event occurred.
 * @param url The URL of the giphy attachment that was clicked.
 * @param message The message containing the giphy attachment.
 */
@ConsistentCopyVisibility
public data class GiphyAttachmentClickData internal constructor(
    val context: Context,
    val url: String,
    val attachment: Attachment,
    val message: Message,
)

@Composable
@Preview(showBackground = true)
private fun GiphyAttachmentContentPreview() {
    ChatTheme {
        GiphyAttachmentContent()
    }
}

@Composable
internal fun GiphyAttachmentContent() {
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(color = Color.Red.toArgb(), width = 200, height = 150)
    }
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        GiphyAttachmentContent(
            attachmentState = AttachmentState(
                message = Message(
                    text = "Hello",
                    attachments = listOf(
                        Attachment(
                            titleLink = "https://giphy.com/gifs/funny-cat-3oEjI6SIIHBdRxXI40",
                            type = AttachmentType.GIPHY,
                        ),
                    ),
                ),
            ),
        )
    }
}
