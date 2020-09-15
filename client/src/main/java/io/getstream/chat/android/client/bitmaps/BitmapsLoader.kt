package io.getstream.chat.android.client.bitmaps

import android.graphics.Bitmap
import io.getstream.chat.android.client.call.Call

interface BitmapsLoader {
    fun load(url: String): Call<Bitmap>
}
