package io.getstream.chat.android.client.token

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class TestTokenManager {

    @get:Rule
    val testCoroutines: TestCoroutineRule = TestCoroutineRule()

    val token = "token"
    private lateinit var manager: TokenManager

    @Before
    fun before() {
        manager = TokenManagerImpl()
    }

    @Test
    fun `Should return a valid token when the default immediate token provider is used`() {
        manager.setTokenProvider(FakeTokenProvider(token))

        val listener = CallListener()

        manager.loadAsyncAndRetry {
            listener.called(it.data())
        }

        listener.data shouldBeEqualTo token
        manager.hasTokenProvider() shouldBeEqualTo true
        manager.hasToken() shouldBeEqualTo true
        manager.getToken() shouldBeEqualTo token
    }

    @Test
    fun `Should return a valid token when custom token provider is used`() {
        manager.setTokenProvider(
            object : TokenProvider {
                override fun loadToken(): String {
                    Thread.sleep(1000)
                    return token
                }
            }
        )
        manager.loadAsyncAndRetry()

        manager.hasTokenProvider() shouldBeEqualTo true
        manager.hasToken() shouldBeEqualTo true
        manager.getToken() shouldBeEqualTo token
    }

    @Test
    fun `Should return an error when token is fetched with an exception`() = testCoroutines.scope.runBlockingTest {
        manager.setTokenProvider(
            object : TokenProvider {
                override fun loadToken(): String {
                    throw RuntimeException()
                }
            }
        )

        val listener = CallListener()

        manager.loadAsyncAndRetry {
            listener.called(it.error())
        }

        // handle the retry delay
        advanceUntilIdle()

        manager.hasToken() shouldBeEqualTo false
        listener.data shouldBeInstanceOf ChatError::class
    }

    @Test
    fun `Should return a valid token when token is fetched after unsuccessful attempt`() {
        testCoroutines.scope.runBlockingTest {
            manager.setTokenProvider(
                object : TokenProvider {
                    var attempts = 0
                    override fun loadToken(): String {
                        if (++attempts < 3) {
                            throw RuntimeException()
                        } else {
                            return token
                        }
                    }
                }
            )

            val listener = CallListener()

            manager.loadAsyncAndRetry {
                listener.called(it.data())
            }

            // handle the retry delay
            advanceUntilIdle()

            manager.hasToken() shouldBeEqualTo true
            listener.data shouldBeEqualTo token
        }
    }

    @Test
    fun `Should invalidate existing token when expire token is called`() {
        val tokenA = "token-a"
        val tokenB = "token-b"

        manager.setTokenProvider(FakeTokenProvider(tokenA, tokenB))
        manager.loadAsyncAndRetry()
        manager.getToken() shouldBeEqualTo tokenA
        manager.expireToken()
        manager.hasToken() shouldBeEqualTo false
        manager.loadAsyncAndRetry()
        manager.getToken() shouldBeEqualTo tokenB
    }

    class CallListener {

        var data: Any? = null

        fun called(data: Any) {
            this.data = data
        }
    }
}
