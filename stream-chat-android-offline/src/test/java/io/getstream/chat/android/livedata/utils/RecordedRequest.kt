package io.getstream.chat.android.livedata.utils

import com.google.gson.JsonParser
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

/**
 * Checks if request body recorded by [MockWebServer] is equal to the expected request body.
 * That is, all the elements in both json objects are equal, regardless of their order.
 */
internal fun RecordedRequest.isRequestBodyEqualTo(expectedRequestBody: String): Boolean {
    val actualJsonElement = JsonParser.parseString(body.clone().readUtf8())
    val expectedJsonElement = JsonParser.parseString(expectedRequestBody)
    return actualJsonElement == expectedJsonElement
}
