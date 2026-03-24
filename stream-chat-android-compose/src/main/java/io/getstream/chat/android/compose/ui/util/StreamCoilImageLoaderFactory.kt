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
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.intercept.Interceptor
import io.getstream.chat.android.ui.common.images.StreamImageLoaderFactory

/**
 * A factory that creates new Coil [ImageLoader] instances.
 */
public fun interface StreamCoilImageLoaderFactory {

    /**
     * Returns a new Coil [ImageLoader].
     */
    public fun imageLoader(context: Context): ImageLoader

    /**
     * Returns a new Coil [ImageLoader] with the given [interceptors] prepended to the component
     * registry, ahead of all decoders and Coil's built-in EngineInterceptor.
     *
     * The default implementation **ignores [interceptors]** and delegates to [imageLoader].
     * This means that when a custom [StreamCoilImageLoaderFactory] is used alongside
     * [ChatTheme]'s `asyncImageHeadersProvider`, the async headers will **not** be injected —
     * the custom factory's loader is returned as-is.
     *
     * Custom class implementations that want to support interceptor injection should override this
     * method, for example by forwarding [interceptors] to [StreamImageLoaderFactory]:
     * ```kotlin
     * override fun imageLoader(context: Context, interceptors: List<Interceptor>): ImageLoader =
     *     StreamImageLoaderFactory(interceptors = interceptors, builder = myCustomBuilder)
     *         .newImageLoader(context)
     * ```
     *
     * Integrators using a custom [StreamCoilImageLoaderFactory] who also need auth headers on
     * image requests should either override this method or inject the headers directly inside
     * their factory's [imageLoader] implementation (e.g. via a custom OkHttp client).
     *
     * @param context The [Context] to build the [ImageLoader] with.
     * @param interceptors Coil [Interceptor]s to prepend to the component registry.
     */
    public fun imageLoader(context: Context, interceptors: List<Interceptor>): ImageLoader =
        imageLoader(context)

    public companion object {
        /**
         * Returns the default singleton instance of [StreamCoilImageLoaderFactory].
         *
         * @return The default implementation of [StreamCoilImageLoaderFactory].
         */
        public fun defaultFactory(): StreamCoilImageLoaderFactory = DefaultStreamCoilImageLoaderFactory
    }
}

/**
 * Provides a custom image loader that uses the [StreamImageLoaderFactory] to build the default loading settings.
 *
 * Gives support to load GIFs.
 */
internal object DefaultStreamCoilImageLoaderFactory : StreamCoilImageLoaderFactory {

    /**
     * Loads images in the app, with our default settings.
     */
    private var imageLoader: ImageLoader? = null

    /**
     * Provides an [ImageLoader] using our custom [ImageLoaderFactory].
     */
    private var imageLoaderFactory: SingletonImageLoader.Factory? = null

    /**
     * Returns either the currently available [ImageLoader] or builds a new one.
     *
     * @param context - The [Context] to build the [ImageLoader] with.
     * @return [ImageLoader] that loads images in the app.
     */
    override fun imageLoader(context: Context): ImageLoader = imageLoader ?: newImageLoader(context)

    override fun imageLoader(context: Context, interceptors: List<Interceptor>): ImageLoader =
        if (interceptors.isEmpty()) {
            imageLoader(context)
        } else {
            StreamImageLoaderFactory(interceptors = interceptors).newImageLoader(context)
        }

    /**
     * Builds a new [ImageLoader] using the given Android [Context]. If the loader already exists, we return it.
     *
     * @param context - The [Context] to build the [ImageLoader] with.
     * @return [ImageLoader] that loads images in the app.
     */
    @Synchronized
    private fun newImageLoader(context: Context): ImageLoader {
        imageLoader?.let { return it }

        val imageLoaderFactory = imageLoaderFactory ?: newImageLoaderFactory()
        return imageLoaderFactory.newImageLoader(context).apply {
            imageLoader = this
        }
    }

    /**
     * Uses Android [Context] to build a new [StreamImageLoaderFactory].
     *
     * @return [ImageLoaderFactory] that loads all the images in the app.
     */
    private fun newImageLoaderFactory(): SingletonImageLoader.Factory {
        return StreamImageLoaderFactory().apply {
            imageLoaderFactory = this
        }
    }
}
