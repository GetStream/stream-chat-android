package io.getstream.chat.android.livedata.mockwebserver

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

internal interface MockWebServerCall {

    fun isApplicable(request: RecordedRequest): Boolean

    fun executeCall(): MockResponse
}
