package io.getstream.chat.android.client.bitmaps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import io.getstream.chat.android.client.logger.ChatLogger
import okhttp3.*
import java.io.IOException
import java.io.InputStream


internal class BitmapsLoaderImpl(val context: Context) : BitmapsLoader {

    val TAG = BitmapsLoader::class.java.simpleName

    val cacheSize = 10 * 1024 * 1024 // 10 MiB
    val client = OkHttpClient.Builder()
        .build()
    val uiHandler = Handler(Looper.getMainLooper())


    override fun load(url: String, listener: (Bitmap) -> Unit) {

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {

                try {
                    val inputStream: InputStream = response.body!!.byteStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    uiHandler.post {
                        listener(bitmap)
                    }
                } catch (t: Throwable) {
                    ChatLogger.instance?.logT(TAG, t)
                }
            }
        })
    }
}