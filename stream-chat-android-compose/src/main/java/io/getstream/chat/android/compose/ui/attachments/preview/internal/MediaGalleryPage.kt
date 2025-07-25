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

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.ShimmerProgressIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl
import kotlinx.coroutines.coroutineScope
import kotlin.math.abs

internal const val DefaultZoomScale = 1f
internal const val MidZoomScale = 2f
internal const val MaxZoomScale = 3f
internal const val DoubleTapTimeoutMs = 500L

/**
 * Composable for displaying and interacting with an image in the media gallery preview.
 * Note: It is assumed that this component is used within a pager.
 *
 * Renders an image with zoom and pan gesture support. The user can:
 * - Pinch to zoom in/out (up to a maximum scale of 3x)
 * - Double tap to cycle through zoom levels (1x → 2x → 3x → 1x)
 * - Pan the image when zoomed in, with boundaries to prevent excessive scrolling
 *
 * The component shows loading indicators while the image is being loaded and error
 * indicators if the image fails to load. It also resets zoom and pan when the user
 * navigates away from this page in the pager.
 *
 * @param attachment The attachment containing the image to display.
 * @param pagerState The state of the pager containing this image page.
 * @param page The page index of this image in the pager.
 */
@Suppress("LongMethod")
@Composable
internal fun MediaGalleryImagePage(
    attachment: Attachment,
    pagerState: PagerState,
    page: Int,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val data = attachment.imagePreviewUrl
        val context = LocalContext.current

        // Ensure we have a new imageRequest in case the data changes
        val imageRequest = remember(data) {
            ImageRequest.Builder(context)
                .data(data)
                .build()
        }

        var imageState by remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }

        val density = LocalDensity.current
        val parentSize = Size(density.run { maxWidth.toPx() }, density.run { maxHeight.toPx() })
        var imageSize by remember { mutableStateOf(Size(0f, 0f)) }

        var currentScale by remember { mutableFloatStateOf(DefaultZoomScale) }
        var translation by remember { mutableStateOf(Offset(0f, 0f)) }

        val scale by animateFloatAsState(targetValue = currentScale, label = "")

        val transformModifier = if (imageState is AsyncImagePainter.State.Success) {
            val state = imageState as AsyncImagePainter.State.Success
            val size = Size(
                width = state.result.image.width.toFloat(),
                height = state.result.image.height.toFloat(),
            )
            Modifier
                .aspectRatio(size.width / size.height, true)
                .background(color = ChatTheme.colors.overlay)
        } else {
            Modifier
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            StreamAsyncImage(
                imageRequest = imageRequest,
                modifier = transformModifier
                    .graphicsLayer {
                        scaleY = scale
                        scaleX = scale
                        translationX = translation.x
                        translationY = translation.y
                    }
                    .onGloballyPositioned {
                        imageSize = Size(it.size.width.toFloat(), it.size.height.toFloat())
                    }
                    .pointerInput(Unit) {
                        coroutineScope {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = true)
                                do {
                                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)

                                    val zoom = event.calculateZoom()
                                    currentScale = (zoom * currentScale).coerceAtMost(MaxZoomScale)

                                    val maxTranslation = calculateMaxOffset(
                                        imageSize = imageSize,
                                        scale = currentScale,
                                        parentSize = parentSize,
                                    )

                                    val offset = event.calculatePan()
                                    val newTranslationX = translation.x + offset.x * currentScale
                                    val newTranslationY = translation.y + offset.y * currentScale

                                    translation = Offset(
                                        newTranslationX.coerceIn(-maxTranslation.x, maxTranslation.x),
                                        newTranslationY.coerceIn(-maxTranslation.y, maxTranslation.y),
                                    )

                                    if (abs(newTranslationX) < calculateMaxOffsetPerAxis(
                                            imageSize.width,
                                            currentScale,
                                            parentSize.width,
                                        ) || zoom != DefaultZoomScale
                                    ) {
                                        event.changes.forEach { it.consume() }
                                    }
                                } while (event.changes.any { it.pressed })

                                if (currentScale < DefaultZoomScale) {
                                    currentScale = DefaultZoomScale
                                }
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        coroutineScope {
                            awaitEachGesture {
                                awaitFirstDown()
                                withTimeoutOrNull(DoubleTapTimeoutMs) {
                                    awaitFirstDown()
                                    currentScale = when {
                                        currentScale == MaxZoomScale -> DefaultZoomScale
                                        currentScale >= MidZoomScale -> MaxZoomScale
                                        else -> MidZoomScale
                                    }

                                    if (currentScale == DefaultZoomScale) {
                                        translation = Offset(0f, 0f)
                                    }
                                }
                            }
                        }
                    },
            ) { asyncImageState ->
                imageState = asyncImageState

                Crossfade(targetState = asyncImageState) { state ->
                    when (state) {
                        is AsyncImagePainter.State.Empty,
                        is AsyncImagePainter.State.Loading,
                        -> ShimmerProgressIndicator(
                            modifier = Modifier.fillMaxSize(),
                        )

                        is AsyncImagePainter.State.Success -> Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = state.painter,
                            contentDescription = null,
                        )

                        is AsyncImagePainter.State.Error -> ErrorIcon(modifier = Modifier.fillMaxSize())
                    }
                }
            }

            Log.d("isCurrentPage", "${page != pagerState.currentPage}")

            if (pagerState.currentPage != page) {
                currentScale = DefaultZoomScale
                translation = Offset(0f, 0f)
            }
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
 * Composable for displaying and interacting with a video in the media gallery preview.
 * Note: It is assumed that this component is used within a pager.
 *
 * Renders a video player with thumbnail preview, play button, and loading indicators.
 * The video playback begins when the user taps the play button. The video player includes
 * standard media controls for playback. Shows a loading indicator while the video is
 * buffering. The video is paused when the user navigates away from this page in the pager.
 *
 * This component handles various states:
 * - Initial state with thumbnail and play button
 * - Loading state while the video is buffering
 * - Playback state with media controls
 * - Error state when video playback fails
 *
 * The video playback is automatically paused and the preview is restored when
 * the user navigates away from this page in the pager.
 *
 * @param assetUrl The url of the video to display.
 * @param thumbnailUrl The url of the thumbnail to display before the video is played.
 * @param onPlaybackError Callback invoked when video playback encounters an error.
 * @param modifier The [Modifier] to be applied to the video player.
 */
@Composable
internal fun MediaGalleryVideoPage(
    assetUrl: String?,
    thumbnailUrl: String?,
    onPlaybackError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StreamMediaPlayerContent(
        modifier = modifier,
        assetUrl = assetUrl,
        thumbnailUrl = if (ChatTheme.videoThumbnailsEnabled) thumbnailUrl else null,
        playWhenReady = false,
        onPlaybackError = onPlaybackError,
    )
}

/**
 * Calculates max offset that an image can have before reaching the edges.
 *
 * @param imageSize The size of the image that is being viewed.
 * @param scale The current scale of the image that is being viewed.
 * @param parentSize The size of the view containing the image being viewed.
 */
private fun calculateMaxOffset(imageSize: Size, scale: Float, parentSize: Size): Offset {
    val maxTranslationY = calculateMaxOffsetPerAxis(imageSize.height, scale, parentSize.height)
    val maxTranslationX = calculateMaxOffsetPerAxis(imageSize.width, scale, parentSize.width)
    return Offset(maxTranslationX, maxTranslationY)
}

/**
 * Calculates max offset an image can have on a single axis.
 *
 * @param axisSize The size of the image on a given axis.
 * @param scale The current scale of of the image.
 * @param parentAxisSize The size of the parent view on a given axis.
 */
private fun calculateMaxOffsetPerAxis(axisSize: Float, scale: Float, parentAxisSize: Float): Float {
    return (axisSize * scale - parentAxisSize).coerceAtLeast(0f) / 2
}
