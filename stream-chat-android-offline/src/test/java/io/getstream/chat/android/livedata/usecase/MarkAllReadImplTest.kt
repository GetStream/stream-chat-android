package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseDomainTest2
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class MarkAllReadImplTest : BaseDomainTest2() {

    @Test
    fun markAllRead() = runBlocking {
        chatDomainImpl.allActiveChannels().let { activeChannels ->

            // set up unread states
            activeChannels.forEach { channel ->
                channel.unreadCount.getOrAwaitValue().let { count ->
                    Truth.assertThat(count).isEqualTo(0)
                }

                channel.handleEvent(data.newMessageEvent)

                channel.unreadCount.getOrAwaitValue().let { unreadCount ->
                    Truth.assertThat(unreadCount).isEqualTo(1)
                }
            }

            // mark all as read
            chatDomainImpl.useCases.markAllRead().execute()

            // verify result
            activeChannels.forEach { channel ->
                channel.unreadCount.getOrAwaitValue().let { unreadCount ->
                    Truth.assertThat(unreadCount).isEqualTo(0)
                }
            }
        }
    }
}
