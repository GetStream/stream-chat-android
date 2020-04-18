package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseIntegrationTest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GetTotalUnreadCountTest: BaseIntegrationTest() {

    @Test
    fun getUnreadCount() = runBlocking(Dispatchers.IO) {
        // use case style syntax
        var result = chatDomain.useCases.getTotalUnreadCount().execute()
        Truth.assertThat(result.isSuccess).isTrue()
        chatDomain.eventHandler.handleEvent(data.connectedEvent2)
        val count = result.data().getOrAwaitValue()
        Truth.assertThat(count).isEqualTo(3)
    }

}