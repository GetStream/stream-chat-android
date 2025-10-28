/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.api.models.ProgressRequestBody
import io.getstream.chat.android.client.uploader.StreamFileUploader
import io.getstream.chat.android.client.utils.ProgressCallback
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Finds requests tagged with [ProgressCallback] instances, and wraps the request
 * in a [ProgressRequestBody] that will issue updates to this callback.
 *
 * These callbacks are added in [StreamFileUploader].
 */
internal class ProgressInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val progressCallback = request.tag(ProgressCallback::class.java)
        if (progressCallback != null) {
            return chain.proceed(wrapRequest(request, progressCallback))
        }

        return chain.proceed(request)
    }

    private fun wrapRequest(request: Request, progressCallback: ProgressCallback): Request = request.newBuilder()
        // Assume that any request tagged with a ProgressCallback is a POST
        // request and has a non-null body
        .post(ProgressRequestBody(request.body!!, progressCallback))
        .build()
}
