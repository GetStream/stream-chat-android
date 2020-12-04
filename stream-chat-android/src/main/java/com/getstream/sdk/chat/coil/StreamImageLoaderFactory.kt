package com.getstream.sdk.chat.coil

import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.util.CoilUtils
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
                OkHttpClient.Builder()
                    .cache(CoilUtils.createDefaultCache(context))
                    .build()
            }
            .componentRegistry {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder())
                } else {
                    add(GifDecoder())
                }
            }
            .apply(builder)
            .build()
    }
}
