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

public class StreamImageLoaderFactory(
    private val context: Context,
    private val builder: ImageLoader.Builder.() -> Unit = {}
) : ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(context)
            .availableMemoryPercentage(0.25)
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
                    add(ImageDecoderDecoder(context))
                } else {
                    add(GifDecoder())
                }
            }
            .apply(builder)
            .build()
    }
}
