package com.getstream.sdk.chat

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.Coil
import coil.request.GetRequest
import coil.request.GetRequestBuilder
import coil.transform.BlurTransformation
import coil.transform.CircleCropTransformation
import coil.transform.GrayscaleTransformation
import coil.transform.RoundedCornersTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal object ImageLoader {
    suspend fun getBitmap(
        context: Context,
        url: String,
        transformation: ImageTransformation = ImageTransformation.None
    ): Bitmap? = withContext(Dispatchers.IO) {
        url.takeUnless { it.isBlank() }
            ?.let {
                (
                    Coil.execute(
                        GetRequest.Builder(context)
                            .data(it)
                            .applyTransformation(transformation, context)
                            .build()
                    )
                        .drawable as? BitmapDrawable
                    )?.bitmap
            }
    }

    private fun GetRequestBuilder.applyTransformation(
        transformation: ImageTransformation,
        context: Context
    ): GetRequestBuilder =
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

    sealed class ImageTransformation {
        object None : ImageTransformation()
        object Circle : ImageTransformation()
        object Grayscale : ImageTransformation()
        class Blur(val radius: Float = 10f, val sampling: Float = 1f) : ImageTransformation()
        class RoundedCorners(val radius: Float) : ImageTransformation()
    }
}
