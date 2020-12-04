package com.getstream.sdk.chat.coil

import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public object StreamCoil {

    private var imageLoader: ImageLoader? = null
    private var imageLoaderFactory: ImageLoaderFactory? = null

    public fun imageLoader(context: Context): ImageLoader = imageLoader ?: newImageLoader(context)

    @Synchronized
    public fun setImageLoader(factory: ImageLoaderFactory) {
        imageLoaderFactory = factory
        imageLoader = null
    }

    @Synchronized
    private fun newImageLoader(context: Context): ImageLoader {
        imageLoader?.let { return it }

        val imageLoaderFactory = imageLoaderFactory ?: newImageLoaderFactory(context)
        return imageLoaderFactory.newImageLoader().apply {
            imageLoader = this
        }
    }

    private fun newImageLoaderFactory(context: Context): ImageLoaderFactory {
        return StreamImageLoaderFactory(context).apply {
            imageLoaderFactory = this
        }
    }

    public inline val Context.streamImageLoader: ImageLoader
        get() = imageLoader(this)
}
