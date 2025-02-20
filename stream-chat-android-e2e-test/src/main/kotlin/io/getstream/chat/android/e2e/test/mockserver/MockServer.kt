/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.e2e.test.mockserver

import io.getstream.chat.android.compose.uiautomator.device
import io.getstream.chat.android.compose.uiautomator.enableInternetConnection
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Assert.fail

public const val forbiddenWord: String = "wth"

public class MockServer(testName: String) {
    public val url: String
    private val driverUrl: String = "http://10.0.2.2:4567"
    private val okHttp: OkHttpClient = OkHttpClient()

    init {
        val request = Request.Builder().url("$driverUrl/start/$testName").build()
        val response = execute(request)
        val mockServerPort = response.body?.string().toString()
        val driverPort = driverUrl.split(":").last()
        url = driverUrl.replace(driverPort, mockServerPort)
    }

    public fun stop() {
        getRequest("stop")
    }

    public fun postRequest(
        endpoint: String,
        body: RequestBody = "".toRequestBody("text".toMediaTypeOrNull()),
    ): ResponseBody? {
        val request = Request.Builder()
            .url("$url/$endpoint")
            .post(body)
            .build()
        val response = execute(request)
        return response.body
    }

    public fun getRequest(endpoint: String): ResponseBody? {
        val request = Request.Builder()
            .url("$url/$endpoint")
            .build()
        val response = execute(request)
        return response.body
    }

    private fun execute(request: Request): Response {
        try {
            return okHttp.newCall(request).execute()
        } catch (e: Exception) {
            device.enableInternetConnection()
            fail(e.message)
        }
        return Response.Builder().build()
    }
}
