package io.getstream.chat.android.livedata

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.livedata.controller.QueryChannelsSpec
import io.getstream.chat.android.livedata.utils.EventObserver
import io.getstream.chat.android.livedata.utils.RetryPolicy
import io.getstream.chat.android.livedata.utils.TestDataHelper
import io.getstream.chat.android.livedata.utils.TestLoggerHandler
import io.getstream.chat.android.livedata.utils.waitForSetUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

internal open class BaseConnectedIntegrationTest : BaseDomainTest() {
    companion object {

        var data = TestDataHelper()
        var client: ChatClient? = null

        fun createClient(): ChatClient {

            return ChatClient.Builder(data.apiKey, ApplicationProvider.getApplicationContext())
                .logLevel(
                    data.logLevel
                ).loggerHandler(TestLoggerHandler()).build()
        }
    }

    fun setupChatDomain(client: ChatClient): ChatDomainImpl {
        db = createRoomDb()

        val context = ApplicationProvider.getApplicationContext() as Context
        chatDomainImpl = ChatDomain.Builder(context, client, data.user1).database(db)
            .offlineEnabled()
            .userPresenceEnabled()
            .recoveryDisabled()
            .buildImpl()
        chatDomain = chatDomainImpl
        chatDomainImpl.retryPolicy = object :
            RetryPolicy {
            override fun shouldRetry(client: ChatClient, attempt: Int, error: ChatError): Boolean {
                return false
            }

            override fun retryTimeout(client: ChatClient, attempt: Int, error: ChatError): Int {
                return 1000
            }
        }

        chatDomainImpl.errorEvents.observeForever(
            EventObserver {
                println("error event$it")
            }
        )
        return chatDomainImpl
    }

    @Before
    override fun setup() {
        if (Companion.client == null) {
            // do one time setup here
            // doing this here since context is not available in before all
            // see https://github.com/android/android-test/issues/409
            Companion.client = Companion.createClient()
            runBlocking {
                waitForSetUser(
                    Companion.client!!,
                    Companion.data.user1,
                    Companion.data.user1Token
                )
            }
            Truth.assertThat(Companion.client!!.isSocketConnected()).isTrue()
        }

        client = Companion.client!!
        // start from a clean db everytime
        chatDomainImpl = setupChatDomain(client)
        println("setup")

        // setup channel controller and query controllers for tests
        runBlocking(Dispatchers.IO) { chatDomainImpl.repos.configs.insertConfigs(mutableMapOf("messaging" to data.config1)) }
        channelControllerImpl = chatDomainImpl.channel(data.channel1.type, data.channel1.id)
        channelControllerImpl.updateLiveDataFromChannel(data.channel1)
        query = QueryChannelsSpec(data.filter1, QuerySort())

        queryControllerImpl = chatDomainImpl.queryChannels(data.filter1, QuerySort())

        Truth.assertThat(client.isSocketConnected()).isTrue()

        Truth.assertThat(chatDomainImpl.isOnline()).isTrue()
    }

    @After
    override fun tearDown() = runBlocking(Dispatchers.IO) {
        // things to do after each test
        println("tearDown")
        chatDomainImpl.disconnect()
    }
}
