package com.getstream.sdk.chat.images

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public interface StreamImageLoader {
    public companion object {
        public fun instance(): StreamImageLoader = CoilStreamImageLoader
    }

    public fun load(
        target: ImageView,
        data: Any?,
        @DrawableRes placeholderResId: Int? = null,
        transformation: ImageTransformation = ImageTransformation.None,
        onStart: () -> Unit = {},
        onComplete: () -> Unit = {},
    )

    public fun loadVideoThumbnail(
        target: ImageView,
        uri: Uri?,
        @DrawableRes placeholderResId: Int? = null,
        transformation: ImageTransformation = ImageTransformation.None,
        onStart: () -> Unit = {},
        onComplete: () -> Unit = {},
    )

    public suspend fun loadAsBitmap(
        context: Context,
        url: String,
        transformation: ImageTransformation = ImageTransformation.None,
    ): Bitmap?

    public sealed class ImageTransformation {
        public object None : ImageTransformation()
        public object Circle : ImageTransformation()
        public class RoundedCorners(@Px public val radius: Float) : ImageTransformation()
    }
}
