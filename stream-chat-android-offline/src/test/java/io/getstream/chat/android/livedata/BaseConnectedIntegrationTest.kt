package io.getstream.chat.android.livedata

import android.content.Context
import android.os.Handler
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.livedata.controller.QueryChannelsSpec
import io.getstream.chat.android.livedata.utils.NoRetryPolicy
import io.getstream.chat.android.livedata.utils.TestDataHelper
import io.getstream.chat.android.livedata.utils.TestLoggerHandler
import io.getstream.chat.android.livedata.utils.waitForSetUser
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.model.ChannelConfig
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

internal open class BaseConnectedIntegrationTest : BaseDomainTest() {

    companion object {
        var data = TestDataHelper()
        var client: ChatClient? = null

        fun createClient(): ChatClient {

            return ChatClient.Builder(data.apiKey, ApplicationProvider.getApplicationContext())
                .logLevel(data.logLevel)
                .loggerHandler(TestLoggerHandler())
                .callbackExecutor { runnable -> runnable.run() }
                .build()
        }
    }

    suspend fun setupChatDomain(client: ChatClient): ChatDomainImpl {
        db = createRoomDb()

        val context = ApplicationProvider.getApplicationContext() as Context
        val handler: Handler = mock()
        val offlineEnabled = true
        val userPresence = true
        val recoveryEnabled = false
        val backgroundSyncEnabled = false
        chatDomainImpl = ChatDomainImpl(
            client,
            data.user1,
            db,
            handler,
            offlineEnabled,
            userPresence,
            recoveryEnabled,
            backgroundSyncEnabled,
            context
        )
        chatDomain = chatDomainImpl
        chatDomainImpl.retryPolicy = NoRetryPolicy()
        chatDomainImpl.repos.insertUsers(data.userMap.values.toList())
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
                waitForSetUser(
                    Companion.client!!,
                    Companion.data.user1,
                    Companion.data.user1Token
                )
                Truth.assertThat(Companion.client!!.isSocketConnected()).isTrue()
            }

            client = Companion.client!!
            // start from a clean db everytime
            chatDomainImpl = setupChatDomain(client)
            println("setup")

            // setup channel controller and query controllers for tests
            runBlocking { chatDomainImpl.repos.insertChannelConfig(ChannelConfig("messaging", data.config1)) }
            channelControllerImpl = chatDomainImpl.channel(data.channel1.type, data.channel1.id)
            channelControllerImpl.updateDataFromChannel(data.channel1)
            query = QueryChannelsSpec(data.filter1, QuerySort())

            queryControllerImpl = chatDomainImpl.queryChannels(data.filter1, QuerySort())

            Truth.assertThat(client.isSocketConnected()).isTrue()

            Truth.assertThat(chatDomainImpl.isOnline()).isTrue()
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
