package io.getstream.chat.android.client.api.interceptor

import io.getstream.chat.android.client.api.FakeChain
import io.getstream.chat.android.client.api.FakeResponse
import io.getstream.chat.android.client.api.FakeResponse.Body
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatRequestError
import io.getstream.chat.android.client.parser2.MoshiChatParser
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.token.FakeTokenProvider
import io.getstream.chat.android.client.token.TokenManagerImpl
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Assert.assertThrows
import org.junit.Test

internal class TokenAuthInterceptorTests {

    val token = "token"
    val parser = MoshiChatParser()

    @Test
    fun undefinedToken() {
        val tm = TokenManagerImpl()
        val interceptor = TokenAuthInterceptor(tm, parser) { false }

        val exception = assertThrows(ChatRequestError::class.java) {
            interceptor.intercept(FakeChain(FakeResponse(200)))
        }

        exception.streamCode shouldBeEqualTo ChatErrorCode.UNDEFINED_TOKEN.code
    }

    @Test
    fun error500() {
        val tm = FakeTokenManager("token")
        val interceptor = TokenAuthInterceptor(tm, parser) { false }

        val exception = assertThrows(ChatRequestError::class.java) {
            interceptor.intercept(FakeChain(FakeResponse(500)))
        }

        exception.statusCode shouldBeEqualTo 500
    }

    @Test
    fun validTokenAttachment() {
        val tm = FakeTokenManager(token)
        val interceptor = TokenAuthInterceptor(tm, parser) { false }

        val response = interceptor.intercept(FakeChain(FakeResponse(200)))

        val headerValue = response.request.headers[TokenAuthInterceptor.AUTH_HEADER]

        headerValue shouldBeEqualTo token
    }

    @Test
    fun invalidTokenAttachment() {
        val invalidHeader = "🤢"

        val tm = FakeTokenManager(invalidHeader)
        val interceptor = TokenAuthInterceptor(tm, parser) { false }

        val exception = assertThrows(ChatRequestError::class.java) {
            interceptor.intercept(FakeChain(FakeResponse(200)))
        }

        exception.streamCode shouldBeEqualTo ChatErrorCode.INVALID_TOKEN.code
    }

    @Test
    fun expiredToken() {
        val tm = TokenManagerImpl()
        val interceptor = TokenAuthInterceptor(tm, parser) { false }

        tm.setTokenProvider(FakeTokenProvider("token-a", "token-b"))

        val chain = FakeChain(
            FakeResponse(444, Body("""{ "code": 40 }""")),
            FakeResponse(200, Body("""{}"""))
        )
        interceptor.intercept(chain)
        chain.processChain()
        interceptor.intercept(chain)
    }
}
