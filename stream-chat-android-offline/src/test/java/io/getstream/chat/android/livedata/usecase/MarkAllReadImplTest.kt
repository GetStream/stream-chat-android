package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.test.getOrAwaitValue
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class MarkAllReadImplTest : BaseConnectedIntegrationTest() {

    @Test
    fun markAllRead() = testCoroutines.dispatcher.runBlockingTest {
        chatDomainImpl.allActiveChannels().let { activeChannels ->

            // set up unread states
            activeChannels.forEach { channel ->
                channel.unreadCount.getOrAwaitValue().let { count ->
                    Truth.assertThat(count).isEqualTo(0)
                }

                channel.handleEvent(data.newMessageFromUser2)
                advanceUntilIdle()

                channel.unreadCount.getOrAwaitValue().let { unreadCount ->
                    Truth.assertThat(unreadCount).isEqualTo(1)
                }
            }

            // mark all as read
            chatDomainImpl.useCases.markAllRead().execute()
            advanceUntilIdle()

            // verify result
            activeChannels.forEach { channel ->
                channel.unreadCount.getOrAwaitValue().let { unreadCount ->
                    Truth.assertThat(unreadCount).isEqualTo(0)
                }
            }
        }
    }
}
