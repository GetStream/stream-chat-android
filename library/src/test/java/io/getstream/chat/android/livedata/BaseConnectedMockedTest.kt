package io.getstream.chat.android.livedata

import org.junit.After
import org.junit.Before

/**
 * Inherit from this class if you want to mock the client object and return a connected client
 */
open class BaseConnectedMockedTest: BaseDomainTest() {
    @Before
    override fun setup() {
        client = createConnectedMockClient()
        setupChatDomain(client, true)
    }

    @After
    override fun tearDown() {
        chatDomainImpl.disconnect()
        db.close()
    }
}