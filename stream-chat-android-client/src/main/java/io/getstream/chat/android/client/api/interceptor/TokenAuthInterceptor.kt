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

import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatRequestError
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.token.TokenManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Interceptor adding the authorization token to the request.
 *
 * @param tokenManager The token manager to get the token from.
 * @param parser The parser to parse the error response.
 * @param isAnonymous Lambda checking if the currently logged in user is anonymous.
 */
internal class TokenAuthInterceptor internal constructor(
    private val tokenManager: TokenManager,
    private val parser: ChatParser,
    private val isAnonymous: () -> Boolean,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (isAnonymous()) {
            return chain.proceed(chain.request())
        } else {
            if (!tokenManager.hasTokenProvider()) {
                val description = ChatErrorCode.UNDEFINED_TOKEN.description
                val code = ChatErrorCode.UNDEFINED_TOKEN.code
                throw ChatRequestError(description, code, -1)
            }

            tokenManager.ensureTokenLoaded()

            val request: Request = addTokenHeader(chain.request())
            var response: Response = chain.proceed(request)

            if (!response.isSuccessful) {
                val err = parser.toError(response)
                if (err.serverErrorCode == ChatErrorCode.TOKEN_EXPIRED.code) {
                    tokenManager.expireToken()
                    tokenManager.loadSync()
                    response.close()
                    // Rebuild the request with the new token (retrieved from the TokenManager) and retry the request.
                    val requestWithFreshToken = addTokenHeader(chain.request())
                    response = chain.proceed(requestWithFreshToken)
                } else {
                    throw ChatRequestError(err.message, err.serverErrorCode, err.statusCode, err.cause)
                }
            }
            return response
        }
    }

    private fun addTokenHeader(request: Request): Request = tokenManager.getToken().let { token ->
        try {
            request.newBuilder().header(AUTH_HEADER, token).build()
        } catch (e: IllegalArgumentException) {
            throw ChatRequestError(
                "${ChatErrorCode.INVALID_TOKEN.description}: '$token'",
                ChatErrorCode.INVALID_TOKEN.code,
                -1,
                e,
            )
        }
    }

    companion object {
        const val AUTH_HEADER = "Authorization"
    }
}
