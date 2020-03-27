package io.getstream.chat.android.client.poc

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.TestInitListener
import io.getstream.chat.android.client.utils.Utils.Companion.runOnUi
import org.awaitility.Awaitility.await
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit.SECONDS

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ClientInstrumentationTests {

    val apiKey = "qk4nn7rpcn75"
    val token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ"
    val userId = "bender"
    lateinit var context: Context
    lateinit var listener: TestInitListener

    @Before
    fun before() {
        context = getInstrumentation().targetContext
        listener = TestInitListener()
    }

    @Test
    fun successfulConnect() {

        runOnUi {
            val client = ChatClient.Builder(apiKey, context).build()
            client.setUser(User(userId), token, listener)
        }.andThen {
            await().atMost(5, SECONDS).until { listener.onSuccessIsCalled() }
        }

    }

    @Test
    fun invalidToken() {

        val invalidToken = "invalid"

        runOnUi {
            val client = ChatClient.Builder(apiKey, context).build()
            client.setUser(User(userId), invalidToken, listener)
        }.andThen {
            await().atMost(5, SECONDS).until { listener.onErrorIsCalled() }
        }
    }

}
