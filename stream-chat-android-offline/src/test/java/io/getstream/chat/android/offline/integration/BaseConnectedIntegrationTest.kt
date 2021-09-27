package io.getstream.chat.android.offline.integration

import android.content.Context
import android.os.Handler
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.createRoomDB
import io.getstream.chat.android.offline.experimental.plugin.Config
import io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin
import io.getstream.chat.android.offline.model.ChannelConfig
import io.getstream.chat.android.offline.querychannels.QueryChannelsSpec
import io.getstream.chat.android.offline.utils.NoRetryPolicy
import io.getstream.chat.android.offline.utils.TestDataHelper
import io.getstream.chat.android.offline.utils.TestLoggerHandler
import io.getstream.chat.android.offline.utils.waitForSetUser
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeTrue
import org.junit.After
import org.junit.Before

internal open class BaseConnectedIntegrationTest : BaseDomainTest() {

    companion object {
        var data = TestDataHelper()
        var client: ChatClient? = null

        fun createClient(): ChatClient {

            return ChatClient.Builder(data.apiKey, ApplicationProvider.getApplicationContext())
                // TODO Review if we need it
                // .logLevel(data.logLevel)
                .loggerHandler(TestLoggerHandler())
                .callbackExecutor { runnable -> runnable.run() }
                .build()
        }
    }

    suspend fun setupChatDomain(client: ChatClient): ChatDomainImpl {
        db = createRoomDB(testCoroutines.dispatcher)

        val context = ApplicationProvider.getApplicationContext() as Context
        val handler: Handler = mock()
        val offlineEnabled = true
        val userPresence = true
        val recoveryEnabled = false
        val backgroundSyncEnabled = false

        val plugin = OfflinePlugin(
            Config(
                backgroundSyncEnabled = backgroundSyncEnabled,
                userPresence = userPresence,
                persistenceEnabled = offlineEnabled
            )
        )

        chatDomainImpl = ChatDomainImpl(
            client,
            db,
            handler,
            offlineEnabled,
            userPresence,
            recoveryEnabled,
            backgroundSyncEnabled,
            context,
            offlinePlugin = plugin,
        )
        chatDomain = chatDomainImpl
        chatDomainImpl.retryPolicy = NoRetryPolicy()
        chatDomainImpl.repos.insertUsers(data.userMap.values.toList())
        chatDomainImpl.scope.launch {
            chatDomainImpl.errorEvents.collect {
                println("error event$it")
            }
        }
        ChatDomain.instance = chatDomainImpl
        return chatDomainImpl
    }

    @Before
    override fun setup() {
        setupWorkManager()
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
                Companion.client!!.isSocketConnected().shouldBeTrue()
            }

            client = Companion.client!!
            // start from a clean db everytime
            chatDomainImpl = setupChatDomain(client)
            println("setup")

            // setup channel controller and query controllers for tests
            runBlocking { chatDomainImpl.repos.insertChannelConfig(ChannelConfig("messaging", data.config1)) }
            channelControllerImpl = chatDomainImpl.channel(data.channel1.type, data.channel1.id)
            channelControllerImpl.updateDataFromChannel(data.channel1)
            query = QueryChannelsSpec(data.filter1)

            queryControllerImpl = chatDomainImpl.queryChannels(data.filter1, QuerySort())

            client.isSocketConnected().shouldBeTrue()

            chatDomainImpl.isOnline().shouldBeTrue()
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
