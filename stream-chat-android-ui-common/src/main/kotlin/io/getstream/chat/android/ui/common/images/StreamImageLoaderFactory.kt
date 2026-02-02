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
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.video.VideoFrameDecoder
import io.getstream.chat.android.client.internal.file.StreamFileManager
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
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

    private val fileManager = StreamFileManager()

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache { MemoryCache.Builder().maxSizePercent(context, DEFAULT_MEMORY_PERCENTAGE).build() }
            .allowHardware(false)
            .crossfade(true)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = {
                            val cacheControlInterceptor = Interceptor { chain ->
                                chain.proceed(chain.request())
                                    .newBuilder()
                                    .header("Cache-Control", "max-age=3600,public")
                                    .build()
                            }
                            // Don't limit concurrent network requests by host.
                            val dispatcher = Dispatcher().apply { maxRequestsPerHost = maxRequests }

                            OkHttpClient.Builder()
                                .dispatcher(dispatcher)
                                .addNetworkInterceptor(cacheControlInterceptor)
                                .build()
                        },
                    ),
                )
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(fileManager.getImageCache(context).toOkioPath())
                    .maxSizePercent(DEFAULT_DISK_CACHE_PERCENTAGE)
                    .build()
            }
            .components {
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
