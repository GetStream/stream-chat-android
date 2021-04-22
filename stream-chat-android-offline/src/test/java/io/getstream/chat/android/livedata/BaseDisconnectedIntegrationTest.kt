package io.getstream.chat.android.livedata

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.livedata.controller.QueryChannelsSpec
import io.getstream.chat.android.livedata.utils.NoRetryPolicy
import io.getstream.chat.android.livedata.utils.TestDataHelper
import io.getstream.chat.android.livedata.utils.TestLoggerHandler
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.model.ChannelConfig
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

internal open class BaseDisconnectedIntegrationTest : BaseDomainTest() {
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
            client.subscribe {
                if (it is ConnectedEvent) {
                    println("connceted")
                }
            }
            return client
        }
    }

    fun setupChatDomain(client: ChatClient): ChatDomainImpl {
        db = createRoomDb()

        val context = ApplicationProvider.getApplicationContext() as Context
        chatDomainImpl =
            ChatDomain.Builder(context, client, data.user1).database(db).offlineEnabled().userPresenceEnabled()
                .recoveryDisabled().buildImpl()
        chatDomainImpl.retryPolicy = NoRetryPolicy()

        chatDomainImpl.scope.launch {
            chatDomainImpl.errorEvents.collect {
                println("error event$it")
            }
        }
        return chatDomainImpl
    }

    @Before
    override fun setup() {
        runBlocking {
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
            println("setup")

            // setup channel controller and query controllers for tests
            chatDomainImpl.repos.insertChannelConfig(ChannelConfig("messaging", data.config1))
            channelControllerImpl = chatDomainImpl.channel(data.channel1.type, data.channel1.id)
            channelControllerImpl.updateDataFromChannel(data.channel1)
            query = QueryChannelsSpec(data.filter1, QuerySort())

            queryControllerImpl = chatDomainImpl.queryChannels(data.filter1, QuerySort())

            Truth.assertThat(client.isSocketConnected()).isFalse()

            Truth.assertThat(chatDomainImpl.isOnline()).isFalse()
        }
    }

    @After
    override fun tearDown() {
        runBlocking {
            // things to do after each test
            println("tearDown")
            chatDomainImpl.disconnect()
        }
    }
}
