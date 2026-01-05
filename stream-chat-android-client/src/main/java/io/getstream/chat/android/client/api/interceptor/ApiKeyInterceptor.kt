/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor adding the API_KEY query parameter to the request.
 *
 * @param apiKey the API key to be added to the request.
 */
internal class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter(PARAM_API_KEY, apiKey)
            .build()
        val request = original.newBuilder()
            .url(url)
            .build()
        return chain.proceed(request)
    }

    companion object {
        private const val PARAM_API_KEY = "api_key"
    }
}
