package io.getstream.chat.android.client.bitmaps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import io.getstream.chat.android.client.call.ChatCallImpl
import io.getstream.chat.android.client.call.OkHttpCall
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result
import okhttp3.OkHttpClient
import okhttp3.Request


internal class BitmapsLoaderImpl(val context: Context) : BitmapsLoader {

    val TAG = BitmapsLoader::class.java.simpleName

    val cacheSize = 10 * 1024 * 1024 // 10 MiB
    val client = OkHttpClient.Builder().build()
    val uiHandler = Handler(Looper.getMainLooper())

    override fun load(url: String): io.getstream.chat.android.client.call.Call<Bitmap> {

        return try {
            val request = Request.Builder().url(url).build()
            OkHttpCall(client.newCall(request)) {
                BitmapFactory.decodeStream(it)
            }
        } catch (t: Throwable) {
            ErrorCall(ChatError("Error parsing request url: $url", t))
        }
    }

    class ErrorCall<T>(val e: ChatError) : ChatCallImpl<T>() {
        override fun execute(): Result<T> {
            return Result(null, e)
        }

        override fun enqueue(callback: (Result<T>) -> Unit) {
            callback(Result(null, e))
        }

    }
}