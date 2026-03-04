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

package io.getstream.chat.android.ui.common.images

import android.os.Build
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.intercept.Interceptor
import coil3.memory.MemoryCache
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.video.VideoFrameDecoder
import io.getstream.chat.android.client.internal.file.StreamFileManager
import kotlinx.coroutines.Dispatchers
import okio.Path.Companion.toOkioPath

private const val DEFAULT_MEMORY_PERCENTAGE = 0.25
private const val DEFAULT_DISK_CACHE_PERCENTAGE = 0.02

/**
 * Factory for creating Coil ImageLoader instances with Stream Chat specific configuration.
 *
 * This factory configures:
 * - Memory cache with 25% of available memory
 * - Disk cache using FileCacheManager's Coil cache directory (2% of disk space)
 * - OkHttp with cache control and optimized network dispatcher
 * - Support for GIFs, animated images, and video frames
 *
 * @param builder Optional lambda to customize the ImageLoader configuration
 */
public class StreamImageLoaderFactory(
    private val builder: ImageLoader.Builder.() -> Unit = {},
) : SingletonImageLoader.Factory {

    /**
     * Creates a [StreamImageLoaderFactory] with additional [Interceptor]s prepended to the
     * component registry, before any decoders and before Coil's built-in [EngineInterceptor].
     *
     * This constructor preserves the existing primary constructor signature and is purely additive.
     *
     * @param interceptors Coil [Interceptor]s to register ahead of all other components.
     * @param builder Optional lambda to further customize the [ImageLoader] configuration.
     */
    public constructor(
        interceptors: List<Interceptor>,
        builder: ImageLoader.Builder.() -> Unit = {},
    ) : this(builder) {
        this.interceptors = interceptors
    }

    private var interceptors: List<Interceptor> = emptyList()

    private val fileManager = StreamFileManager()

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache { MemoryCache.Builder().maxSizePercent(context, DEFAULT_MEMORY_PERCENTAGE).build() }
            .allowHardware(false)
            .crossfade(true)
            .diskCache {
                DiskCache.Builder()
                    .directory(fileManager.getImageCache(context).toOkioPath())
                    .maxSizePercent(DEFAULT_DISK_CACHE_PERCENTAGE)
                    .build()
            }
            .interceptorCoroutineContext(Dispatchers.IO)
            .components {
                interceptors.forEach { add(it) }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(AnimatedImageDecoder.Factory(enforceMinimumFrameDelay = true))
                } else {
                    add(GifDecoder.Factory(enforceMinimumFrameDelay = true))
                }
                add(VideoFrameDecoder.Factory())
            }
            .apply(builder)
            .build()
    }
}
