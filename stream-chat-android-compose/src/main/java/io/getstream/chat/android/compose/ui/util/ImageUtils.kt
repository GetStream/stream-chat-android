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

package io.getstream.chat.android.compose.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.uiutils.util.adjustColorBrightness
import kotlin.math.abs

private const val GradientDarkerColorFactor = 1.3f
private const val GradientLighterColorFactor = 0.7f

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
 * Wrapper around the [coil.compose.rememberAsyncImagePainter] that plugs in our [LocalStreamImageLoader] singleton
 * that can be used to customize all image loading requests, like adding headers, interceptors and similar.
 *
 * @param data The data to load as a painter.
 * @param placeholderPainter The painter used as a placeholder, while loading.
 * @param errorPainter The painter used when the request fails.
 * @param fallbackPainter The painter used as a fallback, in case the data is null.
 * @param onLoading Handler when the loading starts.
 * @param onSuccess Handler when the request is successful.
 * @param onError Handler when the request fails.
 * @param contentScale The scaling model to use for the image.
 * @param filterQuality The quality algorithm used when scaling the image.
 *
 * @return The [AsyncImagePainter] that remembers the request and the image that we want to show.
 */
@Composable
public fun rememberStreamImagePainter(
    data: Any?,
    placeholderPainter: Painter? = null,
    errorPainter: Painter? = null,
    fallbackPainter: Painter? = errorPainter,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
    contentScale: ContentScale = ContentScale.Fit,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
): AsyncImagePainter {
    return rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(data)
            .build(),
        imageLoader = LocalStreamImageLoader.current,
        placeholder = placeholderPainter,
        error = errorPainter,
        fallback = fallbackPainter,
        contentScale = contentScale,
        onSuccess = onSuccess,
        onError = onError,
        onLoading = onLoading,
        filterQuality = filterQuality,
    )
}

/**
 * Wrapper around the [coil.compose.rememberAsyncImagePainter] that plugs in our [LocalStreamImageLoader] singleton
 * that can be used to customize all image loading requests, like adding headers, interceptors and similar.
 *
 * @param model The [ImageRequest] used to load the given image.
 * @param placeholderPainter The painter used as a placeholder, while loading.
 * @param errorPainter The painter used when the request fails.
 * @param fallbackPainter The painter used as a fallback, in case the data is null.
 * @param onLoading Handler when the loading starts.
 * @param onSuccess Handler when the request is successful.
 * @param onError Handler when the request fails.
 * @param contentScale The scaling model to use for the image.
 * @param filterQuality The quality algorithm used when scaling the image.
 *
 * @return The [AsyncImagePainter] that remembers the request and the image that we want to show.
 */
@Composable
public fun rememberStreamImagePainter(
    model: ImageRequest,
    placeholderPainter: Painter? = null,
    errorPainter: Painter? = null,
    fallbackPainter: Painter? = errorPainter,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
    contentScale: ContentScale = ContentScale.Fit,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
): AsyncImagePainter {
    return rememberAsyncImagePainter(
        model = model,
        imageLoader = LocalStreamImageLoader.current,
        placeholder = placeholderPainter,
        error = errorPainter,
        fallback = fallbackPainter,
        contentScale = contentScale,
        onSuccess = onSuccess,
        onError = onError,
        onLoading = onLoading,
        filterQuality = filterQuality,
    )
}

/**
 * Used to change a parameter set on Coil requests in order
 * to force Coil into retrying a request.
 *
 * See: https://github.com/coil-kt/coil/issues/884#issuecomment-975932886
 */
internal const val RetryHash: String = "retry_hash"
