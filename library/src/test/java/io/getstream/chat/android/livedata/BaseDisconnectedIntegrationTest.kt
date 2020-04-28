package io.getstream.chat.android.livedata

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ConnectedEvent
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

            val logLevel = System.getenv("STREAM_LOG_LEVEL") ?: "ALL"
            val client =
                ChatClient.Builder(data.apiKey, ApplicationProvider.getApplicationContext())
                    .logLevel(
                        logLevel
                    ).loggerHandler(TestLoggerHandler()).build()
            client.events().subscribe {
                if (it is ConnectedEvent) {
                    System.out.println("connceted")
                }
            }
            return client
        }
    }

    fun setupChatDomain(client: ChatClient): ChatDomainImpl {
        db = createRoomDb()

        val context = ApplicationProvider.getApplicationContext() as Context
        chatDomainImpl = ChatDomain.Builder(context, client, data.user1).database(
            db
        ).offlineEnabled().userPresenceEnabled().buildImpl()
        chatDomainImpl.eventHandler = EventHandlerImpl(chatDomainImpl, true)
        chatDomainImpl.retryPolicy = object : RetryPolicy {
            override fun shouldRetry(client: ChatClient, attempt: Int, error: ChatError): Boolean {
                return false
            }

            override fun retryTimeout(client: ChatClient, attempt: Int, error: ChatError): Int? {
                return 1000
            }
        }

        chatDomainImpl.errorEvents.observeForever(io.getstream.chat.android.livedata.EventObserver {
            System.out.println("error event$it")
        })
        return chatDomainImpl
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
        chatDomainImpl = setupChatDomain(client)
        System.out.println("setup")

        // setup channel controller and query controllers for tests
        runBlocking(Dispatchers.IO) { chatDomainImpl.repos.configs.insertConfigs(mutableMapOf("messaging" to data.config1)) }
        channelControllerImpl = chatDomainImpl.channel(data.channel1.type, data.channel1.id)
        channelControllerImpl.updateLiveDataFromChannel(data.channel1)
        query = QueryChannelsEntity(data.filter1, null)

        queryControllerImpl = chatDomainImpl.queryChannels(data.filter1)

        Truth.assertThat(client.isSocketConnected()).isFalse()

        Truth.assertThat(chatDomainImpl.isOnline()).isFalse()
    }

    @After
    override fun tearDown() {
        // things to do after each test
        System.out.println("tearDown")
        chatDomainImpl.disconnect()
    }
}
