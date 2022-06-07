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

package com.getstream.sdk.chat.coil

import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.util.CoilUtils
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient

private const val DEFAULT_MEMORY_PERCENTAGE = 0.25
public class StreamImageLoaderFactory(
    private val context: Context,
    private val builder: ImageLoader.Builder.() -> Unit = {}
) : ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(context)
            .availableMemoryPercentage(DEFAULT_MEMORY_PERCENTAGE)
            .allowHardware(false)
            .crossfade(true)
            .okHttpClient {
                val cacheControlInterceptor = Interceptor { chain ->
                    chain.proceed(chain.request())
                        .newBuilder()
                        .header("Cache-Control", "max-age=3600,public")
                        .build()
                }
                // Don't limit concurrent network requests by host.
                val dispatcher = Dispatcher().apply { maxRequestsPerHost = maxRequests }

                OkHttpClient.Builder()
                    .cache(CoilUtils.createDefaultCache(context))
                    .dispatcher(dispatcher)
                    .addNetworkInterceptor(cacheControlInterceptor)
                    .build()
            }
            .componentRegistry {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder(context, enforceMinimumFrameDelay = true))
                } else {
                    add(GifDecoder(enforceMinimumFrameDelay = true))
                }
            }
            .apply(builder)
            .build()
    }
}
