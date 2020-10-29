package io.getstream.chat.android.client.poc

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.EventsConsumer
import io.getstream.chat.android.client.utils.TestInitListener
import io.getstream.chat.android.client.utils.Utils.Companion.runOnUi
import org.awaitility.Awaitility.await
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit.SECONDS

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
    lateinit var setUserListener: TestInitListener
    lateinit var connectedEventConsumer: EventsConsumer

    @Before
    fun before() {
        context = getInstrumentation().targetContext
        setUserListener = TestInitListener()
        connectedEventConsumer = EventsConsumer(listOf(ConnectedEvent::class.java))
    }

    @Test
    fun successfulConnect() {
        runOnUi {
            val client = ChatClient.Builder(apiKey, context).build()
            client.setUser(User(userId), token, setUserListener)
            client.subscribe(connectedEventConsumer::onEvent)
        }.andThen {
            await().atMost(5, SECONDS).until(setUserListener::onSuccessIsCalled)
            await().atMost(5, SECONDS).until(connectedEventConsumer::isReceived)
        }
    }

    @Test
    fun invalidToken() {
        val invalidToken = "invalid"

        runOnUi {
            val client = ChatClient.Builder(apiKey, context).build()
            client.setUser(User(userId), invalidToken, setUserListener)
        }.andThen {
            await().atMost(5, SECONDS).until(setUserListener::onErrorIsCalled)
        }
    }

    @Ignore
    @Test
    fun connectedEventDelivery() {
        runOnUi {
            val client = ChatClient.Builder(apiKey, context).build()
            client.setUser(User(userId), token)
            client.subscribeForSingle<ConnectedEvent> {
                client.subscribe(connectedEventConsumer::onEvent)
            }
        }.andThen {
            await().atMost(5, SECONDS).until(connectedEventConsumer::isReceived)
        }
    }

    @Test
    fun anonymousUserConnection() {
        runOnUi {
            val client = ChatClient.Builder(apiKey, context).build()
            client.setAnonymousUser(setUserListener)
            client.subscribe(connectedEventConsumer::onEvent)
        }.andThen {
            await().atMost(5, SECONDS).until(setUserListener::onSuccessIsCalled)
            await().atMost(5, SECONDS).until(connectedEventConsumer::isReceived)
        }
    }

    @Test
    fun guestUserConnection() {
        runOnUi {
            val client = ChatClient.Builder(apiKey, context).build()
            client.getGuestToken("test-user-id", "Test name").enqueue {
                client.setUser(it.data().user, it.data().token, setUserListener)
            }
            client.subscribe(connectedEventConsumer::onEvent)
        }.andThen {
            await().atMost(50, SECONDS).until(setUserListener::onSuccessIsCalled)
            await().atMost(50, SECONDS).until(connectedEventConsumer::isReceived)
        }
    }

    @Test
    fun firstHealth() {
        val consumer = EventsConsumer(listOf(HealthEvent::class.java))

        runOnUi {
            val client = ChatClient.Builder(apiKey, context).build()
            client.setUser(User(userId), token)
            client.subscribeForSingle<HealthEvent>(consumer::onEvent)
        }.andThen {
            await()
                .atMost(10, SECONDS)
                .until { consumer.isReceivedExactly(listOf(HealthEvent::class.java)) }
        }
    }
}
