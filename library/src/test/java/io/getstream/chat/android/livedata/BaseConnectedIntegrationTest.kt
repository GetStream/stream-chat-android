package io.getstream.chat.android.livedata

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.utils.TestDataHelper
import io.getstream.chat.android.livedata.utils.TestLoggerHandler
import io.getstream.chat.android.livedata.utils.waitForSetUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
open class BaseConnectedIntegrationTest: BaseDomainTest() {
    companion object {

        var data = TestDataHelper()
        var client: ChatClient? = null

        fun createClient(): ChatClient {
            val client = ChatClient.Builder(data.apiKey, ApplicationProvider.getApplicationContext())
                .logLevel(
                    ChatLogLevel.ALL
                ).loggerHandler(TestLoggerHandler()).build()
            return client
        }

    }

    fun setupChatDomain(client: ChatClient): ChatDomain {
        db = createRoomDb()
        val context = ApplicationProvider.getApplicationContext() as Context
        chatDomain = ChatDomain.Builder(context, client, data.user1).database(
            db
        ).offlineEnabled().userPresenceEnabled().build()
        chatDomain.eventHandler = EventHandlerImpl(chatDomain, true)
        chatDomain.retryPolicy = object: RetryPolicy {
            override fun shouldRetry(client: ChatClient, attempt: Int, error: ChatError): Boolean {
                return false
            }

            override fun retryTimeout(client: ChatClient, attempt: Int, error: ChatError): Int? {
                return 1000
            }
        }

        chatDomain.errorEvents.observeForever(io.getstream.chat.android.livedata.EventObserver {
            System.out.println("error event$it")
        })
        return chatDomain
    }

    @Before
    override fun setup() {
        if (BaseConnectedIntegrationTest.client == null) {
            // do one time setup here
            // doing this here since context is not available in before all
            // see https://github.com/android/android-test/issues/409
            Companion.client = Companion.createClient()
            waitForSetUser(
                Companion.client!!,
                Companion.data.user1,
                Companion.data.user1Token
            )
            Truth.assertThat(Companion.client!!.isSocketConnected()).isTrue()
        }

        client = Companion.client!!
        // start from a clean db everytime
        chatDomain = setupChatDomain(client)

        // setup channel controller and query controllers for tests
        runBlocking(Dispatchers.IO) {chatDomain.repos.configs.insertConfigs(mutableMapOf("messaging" to data.config1))}
        channelController = chatDomain.channel(data.channel1.type, data.channel1.id)
        channelController.updateChannel(data.channel1)
        query = QueryChannelsEntity(data.filter1, null)

        queryController = chatDomain.queryChannels(data.filter1)

        Truth.assertThat(client.isSocketConnected()).isTrue()

        Truth.assertThat(chatDomain.isOnline()).isTrue()
    }

    @After
    override fun tearDown() {
        // things to do after each test
    }

}