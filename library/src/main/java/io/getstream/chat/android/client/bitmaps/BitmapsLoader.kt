package io.getstream.chat.android.client.bitmaps

import android.graphics.Bitmap

interface BitmapsLoader {
    fun load(url: String, listener: (Bitmap) -> Unit)
}