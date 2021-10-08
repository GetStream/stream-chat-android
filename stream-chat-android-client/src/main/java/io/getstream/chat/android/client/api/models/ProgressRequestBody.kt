package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.utils.ProgressCallback
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.Sink
import okio.buffer

internal class ProgressRequestBody(
    private val delegate: RequestBody,
    private val callback: ProgressCallback,
) : RequestBody() {

    override fun contentType(): MediaType? = delegate.contentType()
    override fun contentLength(): Long = delegate.contentLength()

    override fun writeTo(sink: BufferedSink) {
        val countingSink = CountingSink(sink).buffer()
        delegate.writeTo(countingSink)
        countingSink.flush()
    }

    private inner class CountingSink(delegate: Sink) : ForwardingSink(delegate) {
        val total = contentLength()
        var uploaded = 0L
        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            uploaded += byteCount

            callback.onProgress(uploaded, total)
            callback.onProgress((100 * uploaded) / total) // Invoked for backwards compatibility.
        }
    }
}
