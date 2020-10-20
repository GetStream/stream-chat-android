package io.getstream.chat.android.client.token

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit.SECONDS

internal class TestTokenManager {

    val token = "token"
    private lateinit var manager: TokenManager

    @Before
    fun before() {
        manager = TokenManagerImpl()
    }

    @Test
    fun basic() {

        manager.setTokenProvider(FakeTokenProvider(token))

        val listenerA = CallListener()
        val listenerB = CallListener()

        manager.loadAsync {
            listenerA.called(it.data())
        }
        manager.loadAsync {
            listenerB.called(it.data())
        }

        await().atMost(2, SECONDS).until { listenerA.isCalledWith(token) }
        await().atMost(2, SECONDS).until { listenerB.isCalledWith(token) }

        await().atMost(2, SECONDS).until { manager.hasTokenProvider() }
        await().atMost(2, SECONDS).until { manager.hasToken() }
        await().atMost(2, SECONDS).until { manager.getToken() == token }
    }

    @Test
    fun async() {

        manager.setTokenProvider(
            object : TokenProvider {
                override fun loadToken(): String {
                    Thread {
                        Thread.sleep(1000)
                    }.start()
                    return token
                }
            }
        )

        manager.loadAsync()

        await().atMost(2, SECONDS).until { manager.hasTokenProvider() }
        await().atMost(2, SECONDS).until { manager.hasToken() }
        await().atMost(2, SECONDS).until { manager.getToken() == token }
    }

    @Test
    fun error() {

        val listener = CallListener()
        val error = RuntimeException()

        manager.setTokenProvider(
            object : TokenProvider {
                override fun loadToken(): String {
                    throw error
                }
            }
        )

        manager.loadAsync {
            listener.called(it.error().cause!!)
        }

        await().atMost(2, SECONDS).until { !manager.hasToken() }
        await().atMost(2, SECONDS).until { listener.isCalledWith(error) }
    }

    @Test
    fun expire() {

        val tokenA = "token-a"
        val tokenB = "token-b"

        manager.setTokenProvider(FakeTokenProvider(tokenA, tokenB))

        manager.loadAsync()

        await().atMost(2, SECONDS).until { manager.getToken() == tokenA }

        manager.expireToken()

        assertThat(manager.hasToken()).isFalse()

        manager.loadAsync()

        await().atMost(2, SECONDS).until { manager.getToken() == tokenB }
    }

    class CallListener {

        var called = false
        var data: Any? = null

        fun called() {
            this.called = true
        }

        fun called(data: Any) {
            this.data = data
        }

        fun isCalled(): Boolean {
            return called
        }

        fun isCalledWith(data: Any): Boolean {
            return this.data == data
        }
    }
}
