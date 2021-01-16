package com.getstream.sdk.chat.images

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.ImageView
import coil.fetch.VideoFrameUriFetcher
import coil.loadAny
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.getstream.sdk.chat.coil.StreamCoil.streamImageLoader
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.withContext

internal object CoilStreamImageLoader : StreamImageLoader {

    override suspend fun loadAsBitmap(
        context: Context,
        url: String,
        transformation: StreamImageLoader.ImageTransformation,
    ): Bitmap? = withContext(DispatcherProvider.IO) {
        url.takeUnless(String::isBlank)
            ?.let { url ->
                val imageResult = context.streamImageLoader.execute(
                    ImageRequest.Builder(context)
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
