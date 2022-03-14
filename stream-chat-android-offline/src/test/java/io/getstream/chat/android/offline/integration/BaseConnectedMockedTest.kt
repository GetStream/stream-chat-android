package io.getstream.chat.android.offline.integration

import io.getstream.chat.android.offline.model.ConnectionState
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

/**
 * Inherit from this class if you want to mock the client object and return a connected client
 */
internal open class BaseConnectedMockedTest : BaseDomainTest() {
    @Before
    override fun setup() {
        setupWorkManager()
        client = createConnectedMockClient()
        setupChatDomain(client, currentUser)
        globalMutableState._connectionState.value = ConnectionState.CONNECTED
    }

    @After
    override fun tearDown() {
        runBlocking {
            db.close()
            chatDomainImpl.disconnect()
            chatDomainImpl.scope.coroutineContext.cancelChildren()
        }
    }
}
