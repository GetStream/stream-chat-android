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

package io.getstream.chat.android.client.token

import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomString
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.lang.Thread.sleep
import kotlin.concurrent.Volatile
import kotlin.concurrent.thread

internal class CacheableTokenProviderTest {

    @Test
    fun `Initial cached token should be empty`() {
        val cacheableTokenProvider = Fixture()
            .get()
        cacheableTokenProvider.getCachedToken() `should be equal to` ""
    }

    @Test
    fun `CacheableTokenProvider should store last value`() {
        val tokens = List(positiveRandomInt(20)) { randomString() }
        val cacheableTokenProvider = Fixture()
            .withTokens(tokens)
            .get()

        repeat(tokens.size) { cacheableTokenProvider.loadToken() }
        val result = cacheableTokenProvider.getCachedToken()

        result `should be equal to` tokens.last()
    }

    @Test
    fun `CacheableTokenProvider should delegate the process to obtain a token to his delegated token provider`() {
        val token = randomString()
        val cacheableTokenProvider = Fixture()
            .withToken(token)
            .get()
        val result = cacheableTokenProvider.loadToken()

        result `should be equal to` token
    }

    @Test
    fun `CacheableTokenProvider should avoid multiple calls to its delegate while previous call to loadToken() has not been completed`() = runTest {
        val tokens = List(positiveRandomInt(20)) { randomString() }
        val cacheableTokenProvider = Fixture()
            .withDelay(1000L)
            .withTokens(tokens)
            .get()

        repeat(tokens.size) {
            thread { cacheableTokenProvider.loadToken() }
        }
        sleep(100)

        val result = cacheableTokenProvider.loadToken()

        result `should be equal to` tokens.first()
    }

    private class Fixture {
        @Volatile var tokenIndex = 0
        private val delegatedTokenProvider: TokenProvider = mock()
        private var delayMiliseconds = 0L
        private var tokens = listOf(randomString())

        fun withToken(token: String) = apply {
            tokens = listOf(token)
        }

        fun withTokens(tokens: List<String>) = apply {
            this.tokens = tokens
        }

        fun withDelay(delayMiliseconds: Long) = apply {
            this.delayMiliseconds = delayMiliseconds
        }

        fun get(): CacheableTokenProvider {
            whenever(delegatedTokenProvider.loadToken()) doAnswer {
                if (delayMiliseconds > 0) {
                    sleep(delayMiliseconds)
                }
                tokens[tokenIndex++]
            }
            return CacheableTokenProvider(delegatedTokenProvider)
        }
    }
}
