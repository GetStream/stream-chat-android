package io.getstream.chat.android.ui.common.internal

import android.content.Context
import android.os.Build
import androidx.startup.Initializer
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.getstream.sdk.chat.coil.StreamCoil
import com.getstream.sdk.chat.coil.StreamImageLoaderFactory

@Suppress("unused")
internal class ImageLoaderFactoryInitializer : Initializer<ImageLoaderFactory> {
    override fun create(context: Context): ImageLoaderFactory {
        return StreamImageLoaderFactory(context) {
            componentRegistry {
                // duplicated as we can not extend component
                // registry of existing image loader builder
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder(context))
                } else {
                    add(GifDecoder())
                }

                add(AvatarFetcher())
            }
        }.apply {
            StreamCoil.setImageLoader(this)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
