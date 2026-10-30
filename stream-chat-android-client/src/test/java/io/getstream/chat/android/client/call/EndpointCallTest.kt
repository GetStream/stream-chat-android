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

package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatRequestError
import io.getstream.chat.android.client.errors.fromChatErrorCode
import io.getstream.chat.android.client.parser2.ParserFactory
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.io.IOException

internal class EndpointCallTest {

    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `A successful request gives its response`() = runTest {
        val result = endpointCall(parser, scope()) { "response" }.await()

        result shouldBeEqualTo Result.Success("response")
    }

    @Test
    fun `An error response fails like the retrofit call`() = runTest {
        val error = RetroError<String>(statusCode = 403, streamCode = 17, message = "Not allowed")

        val result = endpointCall<String>(parser, scope()) { throw error.toHttpException() }.await()

        result shouldBeEqualTo error.toRetrofitCall().await()
    }

    @Test
    fun `A request that can't reach the server fails as a network failure`() = runTest {
        val cause = IOException("offline")

        val result = endpointCall<String>(parser, scope()) { throw cause }.await()

        result shouldBeEqualTo Result.Failure(
            Error.NetworkError.fromChatErrorCode(ChatErrorCode.NETWORK_FAILED, cause = cause),
        )
    }

    @Test
    fun `A request error raised by an interceptor keeps its codes`() = runTest {
        val cause = ChatRequestError(message = "Token expired", streamCode = 40, statusCode = 401)

        val result = endpointCall<String>(parser, scope()) { throw cause }.await()

        result shouldBeEqualTo Result.Failure(
            Error.NetworkError(serverErrorCode = 40, message = "Token expired", statusCode = 401, cause = null),
        )
    }

    private fun TestScope.scope() = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
}
