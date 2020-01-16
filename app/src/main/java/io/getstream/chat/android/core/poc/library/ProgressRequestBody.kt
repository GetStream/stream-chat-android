package io.getstream.chat.android.core.poc.library

import android.os.Handler
import android.os.Looper
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream


class ProgressRequestBody(
    val mFile: File,
    val content_type: String,
    val mListener: UploadFileCallback<File, Int>
) :
    RequestBody() {

    override fun contentType(): MediaType? {
        return content_type.toMediaTypeOrNull()
    }

    override fun contentLength(): Long {
        return mFile.length()
    }

    override fun writeTo(sink: BufferedSink) {
        val fileLength: Long = mFile.length()
        val buffer =
            ByteArray(DEFAULT_BUFFER_SIZE)
        val `in` = FileInputStream(mFile)
        var uploaded: Long = 0
        try {
            var read = 0
            val handler = Handler(Looper.getMainLooper())
            while (`in`.read(buffer).also({ read = it }) != -1) { // update progress on UI thread
                handler.post(ProgressUpdater(uploaded, fileLength))
                uploaded += read.toLong()
                sink.write(buffer, 0, read)
            }
        } finally {
            `in`.close()
        }
    }

    private inner class ProgressUpdater(private val mUploaded: Long, private val mTotal: Long) :
        Runnable {
        override fun run() {
            mListener.onProgress((100 * mUploaded / mTotal).toInt())
        }

    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }
}
