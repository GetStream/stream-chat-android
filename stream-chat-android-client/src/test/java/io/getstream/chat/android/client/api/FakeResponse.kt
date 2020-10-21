package io.getstream.chat.android.client.api

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import java.nio.charset.Charset

internal data class FakeResponse(val statusCode: Int, val body: Body? = null) {
    class Body(data: String) : ResponseBody() {

        val buffer = Buffer().writeString(data, Charset.defaultCharset())

        override fun contentLength(): Long {
            return buffer.size
        }

        override fun contentType(): MediaType? {
            return "application/json".toMediaType()
        }

        override fun source(): BufferedSource {
            return buffer
        }
    }
}
