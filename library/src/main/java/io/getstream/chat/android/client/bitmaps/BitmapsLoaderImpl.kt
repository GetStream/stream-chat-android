package io.getstream.chat.android.client.bitmaps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

internal class BitmapsLoaderImpl(val context: Context) : BitmapsLoader {

    override fun load(url: String, listener: (Bitmap) -> Unit) {
        Glide.with(context).asBitmap().load(url).into(object : CustomTarget<Bitmap>() {
            override fun onLoadCleared(p0: Drawable?) {

            }

            override fun onResourceReady(p0: Bitmap, p1: Transition<in Bitmap>?) {
                listener(p0)
            }
        })
    }
}