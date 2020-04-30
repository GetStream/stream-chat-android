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
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowLooper
import java.lang.Thread.sleep
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
            ChatLogLevel.ALL
        ).loggerHandler(TestLoggerHandler()).build()
    }

    @After
    fun teardown() {
        client.disconnect()
    }

    @Test
    fun setUserCallback() {
        val latch = CountDownLatch(1)
        val user = User("broad-lake-3")
        val token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYnJvYWQtbGFrZS0zIn0.SIb263bpikToka22ofV-9AakJhXzfeF8pU9cstvzInE"
        logger.logI("Waiting for setUser to trigger callback...")
        client.setUser(user, token, object : InitConnectionListener() {
            override fun onSuccess(data: ConnectionData) {
                logger.logI("setUser onSuccess")
                latch.countDown()
            }

            override fun onError(error: ChatError) {
                logger.logE("setUser onError", error)
            }
        })
        sleep(2000)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        if (!latch.await(2, TimeUnit.SECONDS)) {
            throw TimeoutException("setUser onSuccess wasn't called")
        }
    }

    @Test
    fun connectedEvent() {
        val latch = CountDownLatch(1)
        val user = User("broad-lake-3")
        val token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYnJvYWQtbGFrZS0zIn0.SIb263bpikToka22ofV-9AakJhXzfeF8pU9cstvzInE"

        client.events().subscribe {
            System.out.println("event received $it")
            if (it is ConnectedEvent) {
                latch.countDown()
            }
        }
        logger.logI("Waiting for setUser to trigger ConnectedEvent...")
        client.setUser(user, token)
        sleep(2000)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        // stay alive so we don't reset the debugger while debugging this
        if (!latch.await(2, TimeUnit.SECONDS)) {
            throw TimeoutException("ConnectedEvent wasnt received")
        }
    }
}