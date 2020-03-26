package io.getstream.chat.android.client

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.InitConnectionListener
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


@RunWith(AndroidJUnit4::class)
class ChatClientIntegrationTest {
    lateinit var client: ChatClient
    private val logger = ChatLogger.get("ChatClientIntegrationTest")

    @Before
    fun setup() {
        Log.i("Hello", "world")
        client = ChatClient.Builder("b67pax5b2wdq", ApplicationProvider.getApplicationContext()).logLevel(
            ChatLogLevel.ALL).loggerHandler(TestLoggerHandler()).build()
    }

    @After
    fun teardown() {
        client.disconnect()
    }

    @Test
    fun setUserCallback() {
        val latch = CountDownLatch(1)
        val user = User("broad-lake-3")
        val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYnJvYWQtbGFrZS0zIn0.SIb263bpikToka22ofV-9AakJhXzfeF8pU9cstvzInE"
        client.setUser(user, token, object: InitConnectionListener() {
            override fun onSuccess(data: ConnectionData) {
                logger.logI("setUser onSuccess")
                latch.countDown()
            }

            override fun onError(error: ChatError) {
                logger.logE("setUser onError", error)
            }
        })
        if (!latch.await(2, TimeUnit.SECONDS)) {
            throw TimeoutException("setUser onSuccess wasn't called")
        }
    }

    @Test
    fun connectedEvent() {
        val latch = CountDownLatch(1)
        val user = User("broad-lake-3")
        val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYnJvYWQtbGFrZS0zIn0.SIb263bpikToka22ofV-9AakJhXzfeF8pU9cstvzInE"

        client.events().subscribe {
            logger.logI("event received $it")
            if (it is ConnectedEvent) {
                latch.countDown()
            }
        }
        client.setUser(user, token)
        if (!latch.await(2, TimeUnit.SECONDS)) {
            throw TimeoutException("ConnectedEvent wasnt received")
        }
    }
}