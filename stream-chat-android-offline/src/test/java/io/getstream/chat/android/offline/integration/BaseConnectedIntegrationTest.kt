package io.getstream.chat.android.offline.integration

import android.content.Context
import android.os.Handler
import androidx.test.core.app.ApplicationProvider
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.createRoomDB
import io.getstream.chat.android.offline.model.ChannelConfig
import io.getstream.chat.android.offline.model.ConnectionState
import io.getstream.chat.android.offline.querychannels.QueryChannelsSpec
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.repository.creation.factory.RepositoryFactory
import io.getstream.chat.android.offline.utils.TestDataHelper
import io.getstream.chat.android.offline.utils.TestLoggerHandler
import io.getstream.chat.android.offline.utils.waitForSetUser
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeTrue
import org.junit.After
import org.junit.Before
import org.mockito.kotlin.mock

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
        val userPresence = true
        val recoveryEnabled = false
        val backgroundSyncEnabled = false

        chatDomainImpl = ChatDomainImpl(
            client,
            handler,
            userPresence,
            recoveryEnabled,
            backgroundSyncEnabled,
            context,
            globalState = globalMutableState
        )

        chatDomain = chatDomainImpl

        chatDomainImpl.repos =
            RepositoryFacade.create(RepositoryFactory(db, data.user1), chatDomainImpl.scope, mock())

        globalMutableState._user.value = data.user1
        globalMutableState._connectionState.value = ConnectionState.CONNECTED
        chatDomainImpl.userConnected(data.user1)

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
            query = QueryChannelsSpec(data.filter1, QuerySort())

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
