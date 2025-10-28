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

package io.getstream.chat.android.ui.common.images.internal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import coil3.ImageLoader
import coil3.asDrawable
import coil3.gif.MovieDrawable
import coil3.imageLoader
import coil3.load
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import coil3.request.transformations
import coil3.size.ScaleDrawable
import coil3.transform.CircleCropTransformation
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.common.disposable.CoilDisposable
import io.getstream.chat.android.ui.common.disposable.Disposable
import io.getstream.chat.android.ui.common.helper.DefaultImageAssetTransformer
import io.getstream.chat.android.ui.common.helper.DefaultImageHeadersProvider
import io.getstream.chat.android.ui.common.helper.ImageAssetTransformer
import io.getstream.chat.android.ui.common.helper.ImageHeadersProvider
import io.getstream.chat.android.ui.common.images.internal.StreamCoil.streamImageLoader
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal object CoilStreamImageLoader : StreamImageLoader {

    override var imageHeadersProvider: ImageHeadersProvider = DefaultImageHeadersProvider
    override var imageAssetTransformer: ImageAssetTransformer = DefaultImageAssetTransformer

    override suspend fun loadAsBitmap(
        context: Context,
        url: String,
        transformation: StreamImageLoader.ImageTransformation,
    ): Bitmap? = withContext(DispatcherProvider.IO) {
        url.takeUnless(String::isBlank)
            .transformedAsset()
            ?.let { asset ->
                val imageResult = context.streamImageLoader.execute(
                    ImageRequest.Builder(context)
                        .httpHeaders(imageHeadersProvider.getImageRequestHeaders(asset.toString()).toNetworkHeaders())
                        .data(url)
                        .applyTransformation(transformation)
                        .build(),
                )
                (imageResult.image?.asDrawable(context.resources) as? BitmapDrawable)?.bitmap
            }
    }

    override fun load(
        target: ImageView,
        data: Any?,
        placeholderResId: Int?,
        transformation: StreamImageLoader.ImageTransformation,
        onStart: () -> Unit,
        onComplete: () -> Unit,
    ): Disposable {
        val context = target.context
        val disposable = target.loadDisposables(data, context.streamImageLoader) {
            if (placeholderResId != null) {
                placeholder(placeholderResId)
                fallback(placeholderResId)
                error(placeholderResId)
            }

            listener(
                onStart = { onStart() },
                onCancel = { onComplete() },
                onError = { _, _ -> onComplete() },
                onSuccess = { _, _ -> onComplete() },
            )
            applyTransformation(transformation)
        }

        return CoilDisposable(disposable)
    }

    override fun load(
        target: ImageView,
        data: Any?,
        placeholderDrawable: Drawable?,
        transformation: StreamImageLoader.ImageTransformation,
        onStart: () -> Unit,
        onComplete: () -> Unit,
    ): Disposable {
        val context = target.context
        val disposable = target.loadDisposables(data, context.streamImageLoader) {
            if (placeholderDrawable != null) {
                placeholder(placeholderDrawable)
                fallback(placeholderDrawable)
                error(placeholderDrawable)
            }

            listener(
                onStart = { onStart() },
                onCancel = { onComplete() },
                onError = { _, _ -> onComplete() },
                onSuccess = { _, _ -> onComplete() },
            )
            applyTransformation(transformation)
        }

        return CoilDisposable(disposable)
    }

    /**
     * Loads an image into a drawable and then applies the drawable to the container, resizing it based on the scale
     * types and the given configuration.
     *
     * @param target The target to load the image into.
     * @param data The data to load.
     * @param placeholderDrawable Drawable that's shown while the image is loading.
     * @param transformation The transformation for the image before applying to the target.
     * @param onStart The callback when the load has started.
     * @param onComplete The callback when the load has finished.
     */
    override suspend fun loadAndResize(
        target: ImageView,
        data: Any?,
        placeholderDrawable: Drawable?,
        transformation: StreamImageLoader.ImageTransformation,
        onStart: () -> Unit,
        onComplete: () -> Unit,
    ): Unit = coroutineScope {
        val context = target.context

        val calledContext = coroutineContext

        withContext(DispatcherProvider.IO) {
            val asset = data.transformedAsset()
            val headers = asset?.toString()?.let { url ->
                imageHeadersProvider.getImageRequestHeaders(url).toNetworkHeaders()
            } ?: NetworkHeaders.EMPTY
            val result = context.streamImageLoader.execute(
                ImageRequest.Builder(context)
                    .httpHeaders(headers)
                    .placeholder(placeholderDrawable)
                    .fallback(placeholderDrawable)
                    .error(placeholderDrawable)
                    .data(asset)
                    .listener(
                        onStart = { launch(calledContext) { onStart() } },
                        onCancel = { launch(calledContext) { onComplete() } },
                        onError = { _, _ -> launch(calledContext) { onComplete() } },
                        onSuccess = { _, _ -> launch(calledContext) { onComplete() } },
                    )
                    .applyTransformation(transformation)
                    .build(),
            )

            result.image?.asDrawable(context.resources)
        }?.let { drawable ->
            launch(calledContext) {
                if (drawable is ScaleDrawable &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable.child is AnimatedImageDrawable
                ) {
                    (drawable.child as AnimatedImageDrawable).start()
                } else if (drawable is MovieDrawable) {
                    drawable.start()
                }
                target.setImageDrawable(drawable)
            }
        }
    }

    override fun loadVideoThumbnail(
        target: ImageView,
        uri: Uri?,
        placeholderResId: Int?,
        transformation: StreamImageLoader.ImageTransformation,
        onStart: () -> Unit,
        onComplete: () -> Unit,
    ): Disposable {
        val context = target.context
        val disposable = target.loadDisposables(uri, context.streamImageLoader) {
            if (placeholderResId != null) {
                placeholder(placeholderResId)
                fallback(placeholderResId)
                error(placeholderResId)
            }

            listener(
                onStart = { onStart() },
                onCancel = { onComplete() },
                onError = { _, _ -> onComplete() },
                onSuccess = { _, _ -> onComplete() },
            )
            applyTransformation(transformation)
        }

        return CoilDisposable(disposable)
    }

    private fun ImageView.loadDisposables(
        data: Any?,
        imageLoader: ImageLoader = context.imageLoader,
        builder: ImageRequest.Builder.() -> Unit = {},
    ): coil3.request.Disposable {
        val asset = data.transformedAsset()
        return this.load(
            asset,
            imageLoader,
        ) {
            asset?.toString()?.let { url ->
                httpHeaders(imageHeadersProvider.getImageRequestHeaders(url).toNetworkHeaders())
            }
            builder()
        }
    }

    private fun Any?.transformedAsset(): Any? = this?.let(imageAssetTransformer::transform)

    private fun ImageRequest.Builder.applyTransformation(
        transformation: StreamImageLoader.ImageTransformation,
    ): ImageRequest.Builder = when (transformation) {
        is StreamImageLoader.ImageTransformation.None -> this
        is StreamImageLoader.ImageTransformation.Circle -> transformations(
            CircleCropTransformation(),
        )

        is StreamImageLoader.ImageTransformation.RoundedCorners -> transformations(
            RoundedCornersTransformation(
                transformation.radius,
            ),
        )
    }
}

public fun Map<String, String>.toNetworkHeaders(): NetworkHeaders = NetworkHeaders.Builder().apply {
    forEach { (key, value) -> add(key, value) }
}.build()
