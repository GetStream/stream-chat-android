package io.getstream.chat.android.client.api.models

import android.os.Handler
import android.os.Looper
import android.util.Log
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
            val handler = Handler(Looper.getMainLooper())
            while (fis.read(buffer).also { read = it } != -1) {
                // Log.d("ProgressRequestBody", "uploaded: $uploaded ; total: $total")
                handler.post {
                    callback.onProgress((100 * uploaded / total))
                }
                uploaded += read.toLong()
                sink.write(buffer, 0, read)
            }
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }
}
