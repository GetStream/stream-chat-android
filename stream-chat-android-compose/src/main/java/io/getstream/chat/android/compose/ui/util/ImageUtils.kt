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

package io.getstream.chat.android.compose.ui.util

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.LayoutDirection
import coil3.Image
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.asPainter
import coil3.compose.rememberAsyncImagePainter
import coil3.compose.rememberConstraintsSizeResolver
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Size
import coil3.size.SizeResolver
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.ShimmerProgressIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.helper.ImageAssetTransformer
import io.getstream.chat.android.ui.common.helper.ImageHeadersProvider
import io.getstream.chat.android.ui.common.images.internal.toNetworkHeaders
import io.getstream.chat.android.uiutils.util.adjustColorBrightness
import java.net.SocketTimeoutException
import kotlin.math.abs

private const val GradientDarkerColorFactor = 1.3f
private const val GradientLighterColorFactor = 0.7f
private const val MaxRetries = 3

/**
 * Generates a gradient for an initials avatar based on the user initials.
 *
 * @param initials The user initials to use for gradient colors.
 * @return The [Brush] that represents the gradient.
 */
@Composable
@ReadOnlyComposable
internal fun initialsGradient(initials: String): Brush {
    val gradientBaseColors = LocalContext.current.resources.getIntArray(R.array.stream_compose_avatar_gradient_colors)

    val baseColorIndex = abs(initials.hashCode()) % gradientBaseColors.size
    val baseColor = gradientBaseColors[baseColorIndex]

    return Brush.linearGradient(
        listOf(
            Color(adjustColorBrightness(baseColor, GradientDarkerColorFactor)),
            Color(adjustColorBrightness(baseColor, GradientLighterColorFactor)),
        ),
    )
}

/**
 * Applies the given mirroring scaleX based on the [layoutDirection] that's currently configured in the UI.
 *
 * Useful since the Painter from Compose doesn't know how to parse `autoMirrored` flags in SVGs.
 */
public fun Modifier.mirrorRtl(layoutDirection: LayoutDirection): Modifier {
    return this.scale(
        scaleX = if (layoutDirection == LayoutDirection.Ltr) 1f else -1f,
        scaleY = 1f,
    )
}

/**
 * It displays a shimmer effect while loading an image asynchronously using `Coil` and the [LocalStreamImageLoader].
 * The image URL is transformed and some extra headers are provided before loading the image.
 *
 * @see ImageAssetTransformer
 * @see ImageHeadersProvider
 *
 * @param data The data to load the image from. Can be a URL, URI, resource ID, etc.
 * @param contentDescription The description to use for the image.
 * @param modifier Modifier for styling.
 * @param contentScale The scale to be used for the content. Default is [ContentScale.Fit].
 */
@Composable
internal fun StreamAsyncImage(
    data: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    StreamAsyncImage(
        imageRequest = ImageRequest.Builder(LocalContext.current)
            .data(data)
            .build(),
        modifier = modifier,
        contentDescription = contentDescription,
        contentScale = contentScale,
    )
}

/**
 * It displays a shimmer effect while loading an image asynchronously using `Coil` and the [LocalStreamImageLoader].
 * The image URL is transformed and some extra headers are provided before loading the image.
 *
 * @see ImageAssetTransformer
 * @see ImageHeadersProvider
 *
 * @param imageRequest The request to load the image.
 * @param contentDescription The description to use for the image.
 * @param modifier Modifier for styling.
 * @param contentScale The scale to be used for the content. Default is [ContentScale.Fit].
 */
@Composable
internal fun StreamAsyncImage(
    imageRequest: ImageRequest,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    StreamAsyncImage(
        imageRequest = imageRequest,
        modifier = modifier,
        contentScale = contentScale,
    ) { state ->
        val painter = state.painter
        if (painter == null) {
            ShimmerProgressIndicator(
                modifier = Modifier.matchParentSize(),
            )
        } else {
            Image(
                modifier = Modifier.matchParentSize(),
                painter = painter,
                contentDescription = contentDescription,
                contentScale = contentScale,
            )
        }
    }
}

/**
 * Displays an image asynchronously using `Coil` and the [LocalStreamImageLoader].
 * The image URL is transformed and some extra headers are provided before loading the image.
 *
 * @see ImageAssetTransformer
 * @see ImageHeadersProvider
 *
 * @param data The data to load the image from. Can be a URL, URI, resource ID, etc.
 * @param modifier Modifier for styling.
 * @param contentScale The scale to be used for the content. Default is [ContentScale.Fit].
 * @param content A composable function that defines the content to be displayed based on the image loading state.
 */
@Composable
internal fun StreamAsyncImage(
    data: Any?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    content: @Composable BoxScope.(state: AsyncImagePainter.State) -> Unit,
) {
    StreamAsyncImage(
        imageRequest = ImageRequest.Builder(LocalContext.current)
            .data(data)
            .build(),
        modifier = modifier,
        contentScale = contentScale,
        content = content,
    )
}

/**
 * Displays an image asynchronously using `Coil` and the [LocalStreamImageLoader].
 * The image URL is transformed and some extra headers are provided before loading the image.
 *
 * @see ImageAssetTransformer
 * @see ImageHeadersProvider
 *
 * @param imageRequest The request to load the image.
 * @param modifier Modifier for styling.
 * @param contentScale The scale to be used for the content. Default is [ContentScale.Fit].
 * @param content A composable function that defines the content to be displayed based on the image loading state.
 */
@Composable
internal fun StreamAsyncImage(
    imageRequest: ImageRequest,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    content: @Composable BoxScope.(state: AsyncImagePainter.State) -> Unit,
) {
    val sizeResolver = rememberConstraintsSizeResolver()
    Box(
        modifier = modifier.then(sizeResolver),
    ) {
        val context = LocalContext.current
        val imageAssetTransformer = ChatTheme.streamImageAssetTransformer
        val imageHeaderProvider = ChatTheme.streamImageHeadersProvider
        var fetchRetries by remember { mutableIntStateOf(0) }
        val asyncImagePainter = rememberAsyncImagePainter(
            model = imageRequest
                .convertUrl(context, imageAssetTransformer)
                .provideHeaders(context, imageHeaderProvider)
                .size(sizeResolver),
            imageLoader = LocalStreamImageLoader.current,
            contentScale = contentScale,
        )
        val state by asyncImagePainter.state.collectAsState()
        LaunchedEffect(state) {
            if ((state as? AsyncImagePainter.State.Error)?.result?.throwable is SocketTimeoutException) {
                if (fetchRetries++ < MaxRetries) {
                    asyncImagePainter.restart()
                }
            }
        }
        // Skip empty state of first rememberAsyncImagePainter emission when not in preview mode.
        if (LocalInspectionMode.current || state !is AsyncImagePainter.State.Empty) {
            content(state)
        }
    }
}

private fun ImageRequest.convertUrl(
    context: Context,
    imageAssetTransformer: ImageAssetTransformer,
): ImageRequest {
    return this.newBuilder(context)
        .data(imageAssetTransformer.transform(data))
        .build()
}

/**
 * Provides headers to the given [ImageRequest] based on the [ImageHeadersProvider] implementation.
 *
 * @param context The current context.
 * @param imageHeaderProvider The [ImageHeadersProvider] implementation to use.
 * @return The [ImageRequest] with the headers applied.
 */
private fun ImageRequest.provideHeaders(
    context: Context,
    imageHeaderProvider: ImageHeadersProvider,
): ImageRequest =
    this.newBuilder(context).apply {
        httpHeaders(
            imageHeaderProvider.getImageRequestHeaders(data.toString())
                .toNetworkHeaders(),
        )
    }.build()

/**
 * Set the [SizeResolver] as a new build of the [ImageRequest].
 * Otherwise, set the size to [Size.ORIGINAL] in preview mode to fix loading local images.
 */
@Composable
private fun ImageRequest.size(sizeResolver: SizeResolver): ImageRequest = run {
    newBuilder().run {
        if (LocalInspectionMode.current) {
            size(Size.ORIGINAL)
        } else {
            size(sizeResolver)
        }
    }.build()
}

/**
 * A completed state is when the image is either successfully loaded or failed to load.
 */
internal val AsyncImagePainter.State.isCompleted: Boolean
    get() = this is AsyncImagePainter.State.Success || this is AsyncImagePainter.State.Error

/**
 * Controls what [AsyncImage], [SubcomposeAsyncImage], and [AsyncImagePainter] render
 * when in preview mode ([LocalInspectionMode] is true).
 */
@OptIn(ExperimentalCoilApi::class)
internal inline fun AsyncImagePreviewHandler(
    crossinline handle: suspend (request: ImageRequest) -> Image,
) = AsyncImagePreviewHandler { _, request ->
    handle(request).let { image ->
        AsyncImagePainter.State.Success(
            painter = image.asPainter(request.context),
            result = SuccessResult(image, request),
        )
    }
}
