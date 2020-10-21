package io.getstream.chat.android.livedata

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

/**
 * Inherit from this class if you want to mock the client object and return a connected client
 */
internal open class BaseConnectedMockedTest : BaseDomainTest() {
    @Before
    override fun setup() {
        client = createConnectedMockClient()
        setupChatDomain(client, true)
        chatDomainImpl.setOnline()
    }

    @After
    override fun tearDown() = runBlocking(Dispatchers.IO) {
        chatDomainImpl.disconnect()
        db.close()
    }
}
