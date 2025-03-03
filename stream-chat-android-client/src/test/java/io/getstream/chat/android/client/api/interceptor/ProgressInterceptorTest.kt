/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.api.interceptor

import io.getstream.chat.android.client.api.FakeChain
import io.getstream.chat.android.client.api.FakeResponse
import io.getstream.chat.android.client.api.models.ProgressRequestBody
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.result.Error
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.amshove.kluent.`should be instance of`
import org.junit.Test

internal class ProgressInterceptorTest {

    @Test
    fun testProgressInterceptor() {
        // given
        val interceptor = ProgressInterceptor()
        val progressCallback = object : ProgressCallback {
            override fun onSuccess(url: String?) { /* No-Op */ }
            override fun onError(error: Error) { /* No-Op */ }
            override fun onProgress(bytesUploaded: Long, totalBytes: Long) { /* No-Op */ }
        }
        val request = Request.Builder()
            .url("https://hello.url")
            .post("body".toRequestBody())
            .tag(ProgressCallback::class.java, progressCallback)
            .build()
        val chain = FakeChain(FakeResponse(200), request = request)
        // when
        val response = interceptor.intercept(chain)
        // then
        response.request.body `should be instance of` ProgressRequestBody::class
    }
}
