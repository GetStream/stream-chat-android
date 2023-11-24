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

import io.getstream.chat.android.client.ChatClient
import okhttp3.Interceptor
import okhttp3.Response

internal class HeadersInterceptor(private val isAnonymous: () -> Boolean) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val authType = if (isAnonymous()) "anonymous" else "jwt"
        val request = chain.request()
            .newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("stream-auth-type", authType)
            .addHeader("X-Stream-Client", ChatClient.buildSdkTrackingHeaders())
            .addHeader("Cache-Control", "no-cache")
            .build()
        return chain.proceed(request)
    }
}
