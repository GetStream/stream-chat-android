package io.getstream.chat.android.client.api.models

import android.os.Handler
import android.os.Looper
import io.getstream.chat.android.client.di.BaseChatModule
import io.getstream.chat.android.client.extensions.getMediaType
import io.getstream.chat.android.client.utils.ProgressCallback
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream

internal class ProgressRequestBody(
    private val file: File,
    private val callback: ProgressCallback
) : RequestBody() {

    private val handler = Handler(Looper.getMainLooper())
    private var writeCount: Int = 0

    override fun contentType(): MediaType = file.getMediaType()

    override fun contentLength(): Long {
        return file.length()
    }

    override fun writeTo(sink: BufferedSink) {
        val total = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var uploaded = 0L

        FileInputStream(file).use { fis ->
            var read: Int
            while (fis.read(buffer).also { read = it } != -1) {
                sink.write(buffer, 0, read)
                uploaded += read.toLong()
                withCallback { onProgress((100 * uploaded / total)) }
            }
        }

        writeCount++
    }

    /**
     * Only do progress updates if we've already
     * ignored enough writes of this body.
     */
    private inline fun withCallback(crossinline actions: ProgressCallback.() -> Unit) {
        if (writeCount >= PROGRESS_UPDATES_TO_SKIP) {
            handler.post {
                callback.actions()
            }
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048

        /**
         * A number of writes to ignore (not issue progress updates for).
         *
         * This accounts for the [HttpLoggingInterceptor] and [CurlInterceptor] configured in
         * [BaseChatModule] that will trigger writing the body into their logs.
         *
         * We don't want to issue progress updates for these writes, only the real write that's
         * going out to the network that happens after these.
         */
        private const val PROGRESS_UPDATES_TO_SKIP = 2
    }
}
