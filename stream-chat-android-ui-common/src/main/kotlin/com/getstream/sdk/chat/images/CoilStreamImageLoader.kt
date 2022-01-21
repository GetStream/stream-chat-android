package com.getstream.sdk.chat.images

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import coil.drawable.MovieDrawable
import coil.drawable.ScaleDrawable
import coil.fetch.VideoFrameUriFetcher
import coil.loadAny
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.getstream.sdk.chat.coil.StreamCoil.streamImageLoader
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.withContext
import okhttp3.Headers.Companion.toHeaders
import kotlin.math.roundToInt

internal fun Int.dpToPx(): Int = dpToPxPrecise().roundToInt()
internal fun Int.dpToPxPrecise(): Float = (this * displayMetrics().density)
internal fun displayMetrics(): DisplayMetrics = Resources.getSystem().displayMetrics

internal object CoilStreamImageLoader : StreamImageLoader {

    override var imageHeadersProvider: ImageHeadersProvider = DefaultImageHeadersProvider

    override suspend fun loadAsBitmap(
        context: Context,
        url: String,
        transformation: StreamImageLoader.ImageTransformation,
    ): Bitmap? = withContext(DispatcherProvider.IO) {
        url.takeUnless(String::isBlank)
            ?.let { url ->
                val imageResult = context.streamImageLoader.execute(
                    ImageRequest.Builder(context)
                        .headers(imageHeadersProvider.getImageRequestHeaders().toHeaders())
                        .data(url)
                        .applyTransformation(transformation)
                        .build()
                )
                (imageResult.drawable as? BitmapDrawable)?.bitmap
            }
    }

    override fun load(
        target: ImageView,
        data: Any?,
        placeholderResId: Int?,
        transformation: StreamImageLoader.ImageTransformation,
        onStart: () -> Unit,
        onComplete: () -> Unit,
    ) {
        val context = target.context
        target.loadAny(data, context.streamImageLoader) {
            headers(imageHeadersProvider.getImageRequestHeaders().toHeaders())
            placeholderResId?.let(::placeholder)
            listener(
                onStart = { onStart() },
                onCancel = { onComplete() },
                onError = { _, _ -> onComplete() },
                onSuccess = { _, _ -> onComplete() },
            )
            applyTransformation(transformation)
        }
    }

    override fun load(
        target: ImageView,
        data: Any?,
        placeholderDrawable: Drawable?,
        transformation: StreamImageLoader.ImageTransformation,
        onStart: () -> Unit,
        onComplete: () -> Unit,
    ) {
        val context = target.context
        target.loadAny(data, context.streamImageLoader) {
            headers(imageHeadersProvider.getImageRequestHeaders().toHeaders())
            placeholderDrawable?.let(::placeholder)
            listener(
                onStart = { onStart() },
                onCancel = { onComplete() },
                onError = { _, _ -> onComplete() },
                onSuccess = { _, _ -> onComplete() },
            )
            applyTransformation(transformation)
        }
    }

    override suspend fun loadAndResize(
        target: ImageView,
        data: Any?,
        container: ViewGroup,
        placeholderDrawable: Drawable?,
        maxHeight: Int,
        transformation: StreamImageLoader.ImageTransformation,
        onStart: () -> Unit,
        onComplete: () -> Unit,
    ) {
        val context = target.context

        val drawable = withContext(DispatcherProvider.IO) {
            val result = context.streamImageLoader.execute(
                ImageRequest.Builder(context)
                    .headers(imageHeadersProvider.getImageRequestHeaders().toHeaders())
                    .data(data)
                    .placeholder(placeholderDrawable)
                    .applyTransformation(transformation)
                    .listener(
                        onStart = { onStart() },
                        onCancel = { onComplete() },
                        onError = { _, _ -> onComplete() },
                        onSuccess = { _, _ -> onComplete() },
                    )
                    .build()
            )

            result.drawable
        } ?: return

        val widthToHeightRatio = (drawable.intrinsicWidth / drawable.intrinsicHeight.toFloat()).coerceAtMost(1.3f)
        val height = drawable.intrinsicHeight.dpToPx().coerceAtMost(maxHeight)
        val width = drawable.intrinsicWidth.dpToPx().coerceAtMost((height * widthToHeightRatio).toInt())

        target.updateLayoutParams {
            this.height = height
            this.width = width
        }
        container.updateLayoutParams {
            this.height = height
            this.width = width
        }

        if (drawable is ScaleDrawable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable.child is AnimatedImageDrawable) {
            (drawable.child as AnimatedImageDrawable).start()
        } else if (drawable is MovieDrawable) {
            drawable.start()
        }

        target.setImageDrawable(drawable)
    }

    override fun loadVideoThumbnail(
        target: ImageView,
        uri: Uri?,
        placeholderResId: Int?,
        transformation: StreamImageLoader.ImageTransformation,
        onStart: () -> Unit,
        onComplete: () -> Unit,
    ) {
        val context = target.context
        target.loadAny(uri, context.streamImageLoader) {
            headers(imageHeadersProvider.getImageRequestHeaders().toHeaders())
            placeholderResId?.let(::placeholder)
            listener(
                onStart = { onStart() },
                onCancel = { onComplete() },
                onError = { _, _ -> onComplete() },
                onSuccess = { _, _ -> onComplete() },
            )
            fetcher(VideoFrameUriFetcher(context))
            applyTransformation(transformation)
        }
    }

    private fun ImageRequest.Builder.applyTransformation(
        transformation: StreamImageLoader.ImageTransformation,
    ): ImageRequest.Builder =
        when (transformation) {
            is StreamImageLoader.ImageTransformation.None -> this
            is StreamImageLoader.ImageTransformation.Circle -> transformations(
                CircleCropTransformation()
            )
            is StreamImageLoader.ImageTransformation.RoundedCorners -> transformations(
                RoundedCornersTransformation(
                    transformation.radius
                )
            )
        }
}
