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

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import com.skydoves.landscapist.coil.CoilImageState
import com.skydoves.landscapist.components.ImageComponent
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.plugins.ImagePlugin
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.helper.ImageAssetTransformer
import io.getstream.chat.android.ui.common.helper.ImageHeadersProvider
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
 * Displays an image asynchronously using `Coil` and the [LocalStreamImageLoader].
 * It transforms the image URL and provides headers before loading the image.
 *
 * @param data The data to load the image from. Can be a URL, URI, resource ID, etc.
 * @param modifier Modifier for styling.
 * @param contentScale The scale to be used for the content.
 * @param content A composable function that defines the content to be displayed based on the image loading state.
 *
 * @see ImageAssetTransformer
 * @see ImageHeadersProvider
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
 * It transforms the image URL and provides headers before loading the image.
 *
 * @see ImageAssetTransformer
 * @see ImageHeadersProvider
 *
 * @param imageRequest The request to load the image.
 * @param modifier Modifier for styling.
 * @param contentScale The scale to be used for the content.
 * @param content A composable function that defines the content to be displayed based on the image loading state.
 */
@Composable
internal fun StreamAsyncImage(
    imageRequest: ImageRequest,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    content: @Composable BoxScope.(state: AsyncImagePainter.State) -> Unit,
) {
    var imageSize by remember { mutableStateOf(Size.ORIGINAL) }
    Box(
        modifier = modifier.onSizeChanged { size ->
            if (size.width > 0 && size.height > 0) {
                imageSize = Size(size.width, size.height)
            }
        },
    ) {
        if (imageSize == Size.ORIGINAL) {
            content(AsyncImagePainter.State.Empty)
        } else {
            val context = LocalContext.current
            val imageAssetTransformer = ChatTheme.streamImageAssetTransformer
            val imageHeaderProvider = ChatTheme.streamImageHeadersProvider
            val asyncImagePainter = rememberAsyncImagePainter(
                model = imageRequest
                    .convertUrl(context, imageAssetTransformer)
                    .provideHeaders(context, imageHeaderProvider)
                    .size(context, imageSize),
                imageLoader = LocalStreamImageLoader.current,
                contentScale = contentScale,
            )
            content(asyncImagePainter.state)
        }
    }
}

/**
 * Wrapper around the [CoilImage] that plugs in our [LocalStreamImageLoader] singleton
 * that can be used to customize all image loading requests, like adding headers, interceptors and similar.
 *
 * @param data The data model to request image. See [ImageRequest.Builder.data] for types allowed.
 * @param modifier [Modifier] used to adjust the layout or drawing content.
 * @param component An image component that conjuncts pluggable [ImagePlugin]s.
 * @param requestListener A class for monitoring the status of a request while images load.
 * @param imageOptions Represents parameters to load generic [Image] Composable.
 * @param onImageStateChanged An image state change listener will be triggered whenever the image state is changed.
 * @param previewPlaceholder A painter that is specifically rendered when this function operates in preview mode.
 * @param loading Content to be displayed when the request is in progress.
 * @param success Content to be displayed when the request is succeeded.
 * @param failure Content to be displayed when the request is failed.
 */
@Composable
public fun StreamImage(
    data: () -> Any?,
    modifier: Modifier = Modifier,
    component: ImageComponent = rememberImageComponent {},
    requestListener: (() -> ImageRequest.Listener)? = null,
    imageOptions: ImageOptions = ImageOptions(),
    onImageStateChanged: (CoilImageState) -> Unit = {},
    previewPlaceholder: Painter? = null,
    loading: @Composable (BoxScope.(imageState: CoilImageState.Loading) -> Unit)? = null,
    success: @Composable (
        BoxScope.(
            imageState: CoilImageState.Success,
            painter: Painter,
        ) -> Unit
    )? = null,
    failure: @Composable (BoxScope.(imageState: CoilImageState.Failure) -> Unit)? = null,
) {
    StreamImage(
        imageRequest = data.asImageRequest(LocalContext.current) { listener(requestListener?.invoke()) },
        modifier = modifier,
        component = component,
        imageOptions = imageOptions,
        onImageStateChanged = onImageStateChanged,
        previewPlaceholder = previewPlaceholder,
        loading = loading,
        success = success,
        failure = failure,
    )
}

/**
 * Wrapper around the [CoilImage] that plugs in our [LocalStreamImageLoader] singleton
 * that can be used to customize all image loading requests, like adding headers, interceptors and similar.
 *
 * @param model The [ImageRequest] used to load the given image.
 * @param modifier [Modifier] used to adjust the layout or drawing content.
 * @param component An image component that conjuncts pluggable [ImagePlugin]s.
 * @param imageOptions Represents parameters to load generic [Image] Composable.
 * @param onImageStateChanged An image state change listener will be triggered whenever the image state is changed.
 * @param previewPlaceholder A painter that is specifically rendered when this function operates in preview mode.
 * @param loading Content to be displayed when the request is in progress.
 * @param success Content to be displayed when the request is succeeded.
 * @param failure Content to be displayed when the request is failed.
 */
@Composable
public fun StreamImage(
    imageRequest: () -> ImageRequest,
    modifier: Modifier = Modifier,
    component: ImageComponent = rememberImageComponent {},
    imageOptions: ImageOptions = ImageOptions(),
    onImageStateChanged: (CoilImageState) -> Unit = {},
    previewPlaceholder: Painter? = null,
    loading: @Composable (BoxScope.(imageState: CoilImageState.Loading) -> Unit)? = null,
    success: @Composable (
        BoxScope.(
            imageState: CoilImageState.Success,
            painter: Painter,
        ) -> Unit
    )? = null,
    failure: @Composable (BoxScope.(imageState: CoilImageState.Failure) -> Unit)? = null,
) {
    CoilImage(
        imageRequest = imageRequest
            .convertUrl(LocalContext.current, ChatTheme.streamImageAssetTransformer)
            .provideHeaders(LocalContext.current, ChatTheme.streamImageHeadersProvider),
        imageLoader = { LocalStreamImageLoader.current },
        modifier = modifier,
        component = component,
        imageOptions = imageOptions,
        onImageStateChanged = onImageStateChanged,
        previewPlaceholder = previewPlaceholder,
        loading = loading,
        success = success,
        failure = failure,
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
@Deprecated(
    message = "Use StreamImage instead",
    replaceWith = ReplaceWith(
        expression = "StreamImage(data = { data })",
        imports = arrayOf("io.getstream.chat.android.compose.ui.util.StreamImage"),
    ),
)
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
    return rememberStreamImagePainter(
        model = data.toImageRequest(LocalContext.current),
        placeholderPainter = placeholderPainter,
        errorPainter = errorPainter,
        fallbackPainter = fallbackPainter,
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
@Deprecated(
    message = "Use StreamImage instead",
    replaceWith = ReplaceWith(
        expression = "StreamImage(imageRequest = { imageRequest })",
        imports = arrayOf("io.getstream.chat.android.compose.ui.util.StreamImage"),
    ),
)
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
        model = model
            .convertUrl(LocalContext.current, ChatTheme.streamImageAssetTransformer)
            .provideHeaders(LocalContext.current, ChatTheme.streamImageHeadersProvider),
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

private fun (() -> ImageRequest).convertUrl(
    context: Context,
    imageAssetTransformer: ImageAssetTransformer,
): () -> ImageRequest = { this().convertUrl(context, imageAssetTransformer) }

private fun ImageRequest.convertUrl(
    context: Context,
    imageAssetTransformer: ImageAssetTransformer,
): ImageRequest {
    return this.newBuilder(context)
        .data(imageAssetTransformer.transform(data))
        .build()
}

/**
 * Converts the current lambda to another one that returns an [ImageRequest] that can be used to load the image.
 *
 * @param context The current context.
 * @param block The block to apply to the [ImageRequest.Builder].
 * @return The lambda that returns the [ImageRequest] that can be used to load the image.
 */
private fun (() -> Any?).asImageRequest(
    context: Context,
    block: ImageRequest.Builder.() -> Unit = { },
): () -> ImageRequest =
    { this().toImageRequest(context, block) }

/**
 * Converts the current data to an [ImageRequest] that can be used to load the image.
 *
 * @param context The current context.
 * @param block The block to apply to the [ImageRequest.Builder].
 * @return The [ImageRequest] that can be used to load the image.
 */
private fun Any?.toImageRequest(
    context: Context,
    block: ImageRequest.Builder.() -> Unit = { },
): ImageRequest =
    ImageRequest.Builder(context)
        .data(this)
        .apply(block)
        .build()

/**
 * Converts the current lambda to another one that provides headers to the [ImageRequest] based on
 * the [ImageHeadersProvider] implementation.
 *
 * @param context The current context.
 * @param imageHeaderProvider The [ImageHeadersProvider] implementation to use.
 * @return The lambda that returns the [ImageRequest] with the headers applied.
 */
private fun (() -> ImageRequest).provideHeaders(
    context: Context,
    imageHeaderProvider: ImageHeadersProvider,
): () -> ImageRequest = { this().provideHeaders(context, imageHeaderProvider) }

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
    this.newBuilder(context)
        .apply {
            imageHeaderProvider.getImageRequestHeaders(this@provideHeaders.data.toString())
                .entries
                .forEach { addHeader(it.key, it.value) }
        }
        .build()

/**
 * Set the [Size] as a new build of the [ImageRequest].
 */
private fun ImageRequest.size(context: Context, size: Size): ImageRequest = run {
    newBuilder(context)
        .size(size)
        .build()
}

/**
 * Used to change a parameter set on Coil requests in order
 * to force Coil into retrying a request.
 *
 * See: https://github.com/coil-kt/coil/issues/884#issuecomment-975932886
 */
internal const val RetryHash: String = "retry_hash"
