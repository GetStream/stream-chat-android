package io.getstream.chat.android.client.token

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doReturnConsecutively
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomString
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

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
