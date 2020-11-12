package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class GetTotalUnreadCountImplTest : BaseConnectedIntegrationTest() {

    @Ignore("Failing for unknown reasons")
    @Test
    fun getUnreadCount() = runBlocking {
        // use case style syntax
        val result = chatDomain.useCases.getTotalUnreadCount().execute()
        assertSuccess(result)
        chatDomainImpl.eventHandler.handleEvent(data.connectedEvent2)
        val count = result.data().getOrAwaitValue()
        Truth.assertThat(count).isEqualTo(3)
    }
}
