package io.getstream.chat.android.livedata

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.utils.TestDataHelper
import io.getstream.chat.android.livedata.utils.TestLoggerHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

open class BaseDisconnectedIntegrationTest : BaseDomainTest() {
    companion object {

        var data = TestDataHelper()
        var client: ChatClient? = null

        fun createClient(): ChatClient {
            val client =
                ChatClient.Builder(data.apiKey, ApplicationProvider.getApplicationContext())
                    .logLevel(
                        ChatLogLevel.ALL
                    ).loggerHandler(TestLoggerHandler()).build()
            client.events().subscribe {
                if (it is ConnectedEvent) {
                    System.out.println("connceted")
                }
            }
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
        chatDomain.retryPolicy = object : RetryPolicy {
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
        if (Companion.client == null) {
            // do one time setup here
            // doing this here since context is not available in before all
            // see https://github.com/android/android-test/issues/409
            Companion.client = Companion.createClient()
            Truth.assertThat(Companion.client!!.isSocketConnected()).isFalse()
        }

        client = Companion.client!!
        // start from a clean db everytime
        chatDomain = setupChatDomain(client)
        System.out.println("setup")

        // setup channel controller and query controllers for tests
        runBlocking(Dispatchers.IO) { chatDomain.repos.configs.insertConfigs(mutableMapOf("messaging" to data.config1)) }
        channelController = chatDomain.channel(data.channel1.type, data.channel1.id)
        channelController.updateChannel(data.channel1)
        query = QueryChannelsEntity(data.filter1, null)

        queryController = chatDomain.queryChannels(data.filter1)

        Truth.assertThat(client.isSocketConnected()).isFalse()

        Truth.assertThat(chatDomain.isOnline()).isFalse()
    }

    @After
    override fun tearDown() {
        // things to do after each test
        System.out.println("tearDown")
        chatDomain.disconnect()
    }
}
