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
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class CacheableTokenProviderTest {

    private val delegatedTokenProvider: TokenProvider = mock()
    private val cacheableTokenProvider = CacheableTokenProvider(delegatedTokenProvider)

    @Test
    fun `Initial cached token should be empty`() {
        cacheableTokenProvider.getCachedToken() `should be equal to` ""
    }

    @Test
    fun `CacheableTokenProvider should store last value`() {
        val tokens = List(positiveRandomInt(20)) { randomString() }
        whenever(delegatedTokenProvider.loadToken()) doReturnConsecutively tokens

        tokens.forEach { cacheableTokenProvider.loadToken() }
        val result = cacheableTokenProvider.getCachedToken()

        result `should be equal to` tokens.last()
        verify(delegatedTokenProvider, times(tokens.size)).loadToken()
    }

    @Test
    fun `CacheableTokenProvider should delegate the process to obtain a token to his delegated token provider`() {
        val token = randomString()
        whenever(delegatedTokenProvider.loadToken()) doReturn token

        val result = cacheableTokenProvider.loadToken()

        result `should be equal to` token
        verify(delegatedTokenProvider).loadToken()
    }
}
