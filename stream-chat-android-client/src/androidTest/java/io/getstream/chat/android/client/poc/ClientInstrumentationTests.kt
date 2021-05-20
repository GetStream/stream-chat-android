package io.getstream.chat.android.client.poc

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.subscribeForSingle
import io.getstream.chat.android.client.utils.EventsConsumer
import io.getstream.chat.android.client.utils.TestInitCallback
import io.getstream.chat.android.client.utils.Utils.Companion.runOnUi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
internal class ClientInstrumentationTests {

    val apiKey = "qk4nn7rpcn75"
    val token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ"
    val userId = "bender"
    lateinit var context: Context
    lateinit var initCallback: TestInitCallback
    lateinit var connectedEventConsumer: EventsConsumer

    @Before
    fun before() {
        context = getInstrumentation().targetContext
        initCallback = TestInitCallback()
        connectedEventConsumer = EventsConsumer(listOf(ConnectedEvent::class.java))
    }

    @Test
    fun successfulConnect() {
        runOnUi {
            val client = ChatClient.Builder(apiKey, context).build()
            client.connectUser(User(userId), token).enqueue(initCallback)
            client.subscribe(connectedEventConsumer::onEvent)
        }.andThen {
            awaitUntil(5, initCallback::onSuccessIsCalled)
            awaitUntil(5, connectedEventConsumer::isReceived)
        }
    }

    @Test
    fun invalidToken() {
        val invalidToken = "invalid"

        runOnUi {
            val client = ChatClient.Builder(apiKey, context).build()
            client.connectUser(User(userId), invalidToken).enqueue(initCallback)
        }.andThen {
            awaitUntil(5, initCallback::onErrorIsCalled)
        }
    }

    @Ignore
    @Test
    fun connectedEventDelivery() {
        runOnUi {
            val client = ChatClient.Builder(apiKey, context).build()
            client.connectUser(User(userId), token).enqueue()
            client.subscribeForSingle<ConnectedEvent> {
                client.subscribe(connectedEventConsumer::onEvent)
            }
        }.andThen {
            awaitUntil(5, connectedEventConsumer::isReceived)
        }
    }

    @Test
    fun anonymousUserConnection() {
        runOnUi {
            val client = ChatClient.Builder(apiKey, context).build()
            client.connectAnonymousUser().enqueue(initCallback)
            client.subscribe(connectedEventConsumer::onEvent)
        }.andThen {
            awaitUntil(5, initCallback::onSuccessIsCalled)
            awaitUntil(5, connectedEventConsumer::isReceived)
        }
    }

    @Test
    fun guestUserConnection() {
        runOnUi {
            val client = ChatClient.Builder(apiKey, context).build()
            client.getGuestToken("test-user-id", "Test name").enqueue {
                client.connectUser(it.data().user, it.data().token).enqueue(initCallback)
            }
            client.subscribe(connectedEventConsumer::onEvent)
        }.andThen {
            awaitUntil(20, initCallback::onSuccessIsCalled)
            awaitUntil(20, connectedEventConsumer::isReceived)
        }
    }

    @Test
    fun firstHealth() {
        val consumer = EventsConsumer(listOf(HealthEvent::class.java))

        runOnUi {
            val client = ChatClient.Builder(apiKey, context).build()
            client.connectUser(User(userId), token).enqueue()
            client.subscribeForSingle<HealthEvent>(consumer::onEvent)
        }.andThen {
            awaitUntil(10) { consumer.isReceivedExactly(listOf(HealthEvent::class.java)) }
        }
    }
}

/**
 * Awaits at most [timeoutSeconds] for the provided [predicate] to yield
 * a true result.
 */
private fun awaitUntil(timeoutSeconds: Long, predicate: () -> Boolean) {
    runBlocking {
        val timeoutMs = timeoutSeconds * 1_000
        var waited = 0L
        while (waited < timeoutMs) {
            if (predicate()) {
                return@runBlocking
            }

            delay(100)
            waited += 100
        }

        throw AssertionError("Predicate was not fulfilled within ${timeoutMs}ms")
    }
}
