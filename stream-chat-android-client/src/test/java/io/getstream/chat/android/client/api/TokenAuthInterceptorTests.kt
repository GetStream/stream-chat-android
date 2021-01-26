package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.api.FakeResponse.Body
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatRequestError
import io.getstream.chat.android.client.parser.GsonChatParser
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.token.FakeTokenProvider
import io.getstream.chat.android.client.token.TokenManagerImpl
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.Test

internal class TokenAuthInterceptorTests {

    val token = "token"
    val parser = GsonChatParser()

    @Test
    fun undefinedToken() {
        val tm = TokenManagerImpl()
        val interceptor = TokenAuthInterceptor(tm, parser) { false }

        val exception = catchThrowableOfType(
            {
                interceptor.intercept(FakeChain(FakeResponse(200)))
            },
            ChatRequestError::class.java
        )

        assertThat(exception.streamCode).isEqualTo(ChatErrorCode.UNDEFINED_TOKEN.code)
    }

    @Test
    fun error500() {
        val tm = FakeTokenManager("token")
        val interceptor = TokenAuthInterceptor(tm, parser) { false }

        val exception = catchThrowableOfType(
            {
                interceptor.intercept(FakeChain(FakeResponse(500)))
            },
            ChatRequestError::class.java
        )

        assertThat(exception.statusCode).isEqualTo(500)
    }

    @Test
    fun validTokenAttachment() {
        val tm = FakeTokenManager(token)
        val interceptor = TokenAuthInterceptor(tm, parser) { false }

        val response = interceptor.intercept(FakeChain(FakeResponse(200)))

        val headerValue = response.request.headers[TokenAuthInterceptor.AUTH_HEADER]

        assertThat(headerValue).isEqualTo(token)
    }

    @Test
    fun invalidTokenAttachment() {

        val invalidHeader = "🤢"

        val tm = FakeTokenManager(invalidHeader)
        val interceptor = TokenAuthInterceptor(tm, parser) { false }

        val exception = catchThrowableOfType(
            {
                interceptor.intercept(FakeChain(FakeResponse(200)))
            },
            ChatRequestError::class.java
        )

        assertThat(exception.streamCode).isEqualTo(ChatErrorCode.INVALID_TOKEN.code)
    }

    @Test
    fun expiredToken() {

        val tm = TokenManagerImpl()
        val interceptor = TokenAuthInterceptor(tm, parser) { false }

        tm.setTokenProvider(FakeTokenProvider("token-a", "token-b"))

        val chain = FakeChain(
            FakeResponse(444, Body("{code:40}")),
            FakeResponse(200, Body("{}"))
        )
        interceptor.intercept(chain)
        chain.processChain()
        interceptor.intercept(chain)
    }
}
