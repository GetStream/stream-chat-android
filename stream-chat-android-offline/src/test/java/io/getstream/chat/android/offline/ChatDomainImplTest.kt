package io.getstream.chat.android.offline

import android.os.Handler
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.offline.repository.database.ChatDatabase
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
internal class ChatDomainImplTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var sut: ChatDomainImpl

    @BeforeEach
    fun setUp() {
        val client: ChatClient = mock {
            on { it.channel(any()) } doReturn mock()
        }
        val db: ChatDatabase = mock {
            on { userDao() } doReturn mock()
            on { channelConfigDao() } doReturn mock()
            on { channelStateDao() } doReturn mock()
            on { queryChannelsDao() } doReturn mock()
            on { messageDao() } doReturn mock()
            on { reactionDao() } doReturn mock()
            on { syncStateDao() } doReturn mock()
            on { attachmentDao() } doReturn mock()
        }
        val handler: Handler = mock()
        val offlineEnabled = false
        val userPresence = true
        val recoveryEnabled = true
        sut = ChatDomainImpl(
            client,
            db,
            handler,
            offlineEnabled,
            userPresence,
            recoveryEnabled,
            false,
            mock()
        )
        sut.setUser(randomUser())
    }

    @Test
    fun `When create a new channel without author should set current user as author and return channel with author`() =
        testCoroutines.scope.runBlockingTest {
            val newChannel = randomChannel(cid = "channelType:channelId", createdBy = randomUser())

            val result = sut.createNewChannel(newChannel)

            result.isSuccess shouldBeEqualTo true
            result.data().createdBy shouldBeEqualTo sut.user.value
        }
}
