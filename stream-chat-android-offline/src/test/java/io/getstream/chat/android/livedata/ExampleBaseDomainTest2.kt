package io.getstream.chat.android.livedata

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ExampleBaseDomainTest2 : BaseDomainTest2() {
    @Test
    fun `test that room testing setup is configured correctly`(): Unit = runBlocking {
        chatDomainImpl.repos.selectChannelWithoutMessages(data.channel1.cid)
        queryControllerImpl.query(10)
    }
}
