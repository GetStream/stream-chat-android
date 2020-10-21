package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.livedata.BaseDisconnectedIntegrationTest
import io.getstream.chat.android.livedata.utils.ChannelUnreadCountLiveData
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
internal class ChannelUnreadCountDomainTest : BaseDisconnectedIntegrationTest() {

    @Test
    fun testUnreadCount() {
        val messages = listOf(data.message1, data.message2Older)
        val read = ChannelUserRead(data.user1, data.message2Older.createdAt)
        val messagesLd = MutableLiveData(messages)
        val readLd = MutableLiveData(read)
        // use user2 since the messages are written by user 1 (own messages are ignored)
        val unreadLd =
            ChannelUnreadCountLiveData(
                data.user2,
                readLd,
                messagesLd
            )
        // count should be one since we only read the old message
        Truth.assertThat(unreadLd.getOrAwaitValue()).isEqualTo(1)
        // set lastRead to now, count should be 0
        read.lastRead = Date()
        readLd.postValue(read)
        Truth.assertThat(unreadLd.getOrAwaitValue()).isEqualTo(0)
    }
}
