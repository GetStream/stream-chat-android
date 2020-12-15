package com.getstream.sdk.chat

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import coil.fetch.VideoFrameUriFetcher
import coil.request.ImageRequest
import coil.transform.BlurTransformation
import coil.transform.CircleCropTransformation
import coil.transform.GrayscaleTransformation
import coil.transform.RoundedCornersTransformation
import com.getstream.sdk.chat.coil.StreamCoil.streamImageLoader
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.withContext
import coil.loadAny as coilLoadAny

@InternalStreamChatApi
public object ImageLoader {
    public suspend fun getBitmap(
        context: Context,
        url: String,
        transformation: ImageTransformation = ImageTransformation.None
    ): Bitmap? = withContext(DispatcherProvider.IO) {
        url.takeUnless { it.isBlank() }
            ?.let {
                val imageResult = context.streamImageLoader.execute(
                    ImageRequest.Builder(context)
                        .data(it)
                        .applyTransformation(transformation, context)
                        .build()
                )
                (imageResult.drawable as? BitmapDrawable)?.bitmap
            }
    }

    public fun ImageView.load(
        data: Any?,
        transformation: ImageTransformation = ImageTransformation.None
    ) {
        coilLoadAny(data, context.streamImageLoader) {
            applyTransformation(transformation, context)
        }
    }

    public fun ImageView.load(@RawRes @DrawableRes drawableResId: Int, onStart: () -> Unit = {}, onComplete: () -> Unit = {}) {
        loadAny(data = drawableResId, onStart = onStart, onComplete = onComplete)
    }

    public fun ImageView.load(
        uri: Uri?,
        @DrawableRes placeholderResId: Int? = null
    ): Unit = loadAny(uri, placeholderResId)

    public fun ImageView.loadVideoThumbnail(
        uri: Uri?,
        @DrawableRes placeholderResId: Int? = null
    ): Unit = loadAny(uri, placeholderResId, true)

    @JvmStatic
    @JvmOverloads
    public fun ImageView.load(
        uri: String?,
        @DrawableRes placeholderResId: Int? = null,
        onStart: () -> Unit = {},
        onComplete: () -> Unit = {},
    ): Unit = loadAny(uri, placeholderResId, false, onStart, onComplete)

    private fun ImageView.loadAny(
        data: Any?,
        @DrawableRes placeholderResId: Int? = null,
        videoContentUri: Boolean = false,
        onStart: () -> Unit = {},
        onComplete: () -> Unit = {},
    ) {
        coilLoadAny(data, context.streamImageLoader) {
            placeholderResId?.let { placeholder(it) }
            listener(
                onStart = { onStart() },
                onCancel = { onComplete() },
                onError = { _, _ -> onComplete() },
                onSuccess = { _, _ -> onComplete() },
            )
            if (videoContentUri) {
                fetcher(VideoFrameUriFetcher(context))
            }
        }
    }

    private fun ImageRequest.Builder.applyTransformation(
        transformation: ImageTransformation,
        context: Context
    ): ImageRequest.Builder =
        when (transformation) {
            is ImageTransformation.None -> this
            is ImageTransformation.Circle -> transformations(CircleCropTransformation())
            is ImageTransformation.Grayscale -> transformations(GrayscaleTransformation())
            is ImageTransformation.Blur -> transformations(
                BlurTransformation(
                    context,
                    transformation.radius,
                    transformation.sampling
                )
            )
            is ImageTransformation.RoundedCorners -> transformations(
                RoundedCornersTransformation(
                    transformation.radius
                )
            )
        }

    public sealed class ImageTransformation {
        public object None : ImageTransformation()
        public object Circle : ImageTransformation()
        public object Grayscale : ImageTransformation()
        public class Blur(
            public val radius: Float = 10f,
            public val sampling: Float = 1f
        ) : ImageTransformation()

        public class RoundedCorners(public val radius: Float) : ImageTransformation()
    }
}
