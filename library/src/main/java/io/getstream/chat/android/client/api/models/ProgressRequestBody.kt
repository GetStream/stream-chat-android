package io.getstream.chat.android.client.api.models

import android.os.Handler
import android.os.Looper
import android.webkit.MimeTypeMap
import io.getstream.chat.android.client.utils.ProgressCallback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream

class ProgressRequestBody(
    private val file: File,
    private val callback: ProgressCallback
) : RequestBody() {

    override fun contentType(): MediaType? {
        return getMimeType(file.path).toMediaType()
    }

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
                handler.post {
                    callback.onProgress((100 * uploaded / total))
                }
                uploaded += read.toLong()
                sink.write(buffer, 0, read)
            }
        }
    }

    private fun getMimeType(url: String): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        var result = "application/octet-stream"
        if (extension != null) {
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            if (mime != null) result = mime
        }

        return result
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }
}