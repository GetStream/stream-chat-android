package io.getstream.chat.android.livedata

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ExampleBaseDomainTest2 : BaseDomainTest2() {
    @Test
    internal fun `test that room testing setup is configured correctly`() = testIODispatcher.runBlockingTest {
        testIOScope.launch {
            chatDomainImpl.repos.channels.select(listOf(data.channel1.cid))
            queryControllerImpl.query(10)
        }
    }
}
