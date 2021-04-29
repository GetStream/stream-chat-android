package io.getstream.chat.android.offline.integration

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

/**
 * Inherit from this class if you want to mock the client object and return a disconnected client
 */
internal open class BaseDisconnectedMockedTest : BaseDomainTest() {
    @Before
    override fun setup() {
        client = createDisconnectedMockClient()
        setupChatDomain(client, false)
    }

    @After
    override fun tearDown() {
        runBlocking {
            chatDomainImpl.disconnect()
            db.close()
        }
    }
}
