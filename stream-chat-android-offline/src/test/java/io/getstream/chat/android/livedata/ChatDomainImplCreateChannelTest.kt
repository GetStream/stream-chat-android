package io.getstream.chat.android.livedata

import android.content.Context
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class ChatDomainImplCreateChannelTest {

    private val channelId = "ChannelId"
    private val channelType = "ChannelType"
    private val channelCid = "$channelType:$channelId"
    private val channelMock: Channel = mock {
        on(it.cid) doReturn channelCid
        on(it.id) doReturn channelId
        on(it.type) doReturn channelType
    }
    private val channelMembers = listOf(randomMember(), randomMember())
    private val channelExtraData = mutableMapOf<String, Any>("extraData" to true)

    @Test
    fun `given offline chat domain when creating channel should mark it with sync needed and store in database`(): Unit =
        runBlocking {
            val currentUser = randomUser()
            val repositoryFacade: RepositoryFacade = mock()
            val channel = Channel(
                cid = channelCid,
                id = channelId,
                type = channelType,
                members = channelMembers,
                extraData = channelExtraData,
            )
            val sut = Fixture()
                .givenUser(currentUser)
                .givenRepositoryFacade(repositoryFacade)
                .givenOffline()
                .get()

            val result = sut.createNewChannel(channel)

            argumentCaptor<Channel>().apply {
                verify(repositoryFacade).insertChannel(capture())
                with(firstValue) {
                    syncStatus `should be equal to` SyncStatus.SYNC_NEEDED
                    cid `should be equal to` channelCid
                    id `should be equal to` channelId
                    type `should be equal to` channelType
                    members `should be equal to` members
                    extraData `should be equal to` extraData
                }
            }
            with(result) {
                isSuccess `should be equal to` true
                with(data()) {
                    syncStatus `should be equal to` SyncStatus.SYNC_NEEDED
                    createdBy `should be equal to` currentUser
                    cid `should be equal to` channelCid
                    members `should be equal to` channelMembers
                    extraData `should be equal to` channelExtraData
                }
            }
        }

    @Test
    fun `given online chat domain when creating channel should store it in database `(): Unit =
        runBlocking {
            val currentUser = randomUser()
            val repositoryFacade: RepositoryFacade = mock()
            val channel = Channel(
                cid = channelCid,
                id = channelId,
                type = channelType,
                members = channelMembers,
                extraData = channelExtraData,
            )
            val sut = Fixture()
                .givenUser(currentUser)
                .givenRepositoryFacade(repositoryFacade)
                .givenOnline()
                .givenChatClientResult(Result(channel))
                .get()

            val result = sut.createNewChannel(channel)

            argumentCaptor<Channel>().apply {
                verify(repositoryFacade, times(2)).insertChannel(capture())
                with(firstValue) {
                    cid `should be equal to` channelCid
                    id `should be equal to` channelId
                    type `should be equal to` channelType
                    members `should be equal to` members
                    extraData `should be equal to` extraData
                }
            }
            with(result) {
                isSuccess `should be equal to` true
                with(data()) {
                    syncStatus `should be equal to` SyncStatus.COMPLETED
                    createdBy `should be equal to` currentUser
                    cid `should be equal to` channelCid
                    members `should be equal to` channelMembers
                    extraData `should be equal to` channelExtraData
                }
            }
        }

    @Test
    fun `given online chat domain when creating channel is completed should mark it with proper sync states`(): Unit =
        runBlocking {
            val repositoryFacade: RepositoryFacade = mock()

            val sut = Fixture()
                .givenRepositoryFacade(repositoryFacade)
                .givenOnline()
                .givenChatClientResult(Result(channelMock))
                .get()

            val result = sut.createNewChannel(channelMock)

            result.isSuccess `should be equal to` true
            inOrder(channelMock) {
                verify(channelMock).syncStatus = SyncStatus.IN_PROGRESS
                verify(channelMock).syncStatus = SyncStatus.COMPLETED
            }
            argumentCaptor<Channel>().apply {
                verify(repositoryFacade, times(2)).insertChannel(capture())
                with(allValues[1]) {
                    syncStatus = SyncStatus.COMPLETED
                }
            }
        }

    @Test
    fun `given online chat domain when creating channel failed should mark it with proper sync states`(): Unit =
        runBlocking {
            val repositoryFacade: RepositoryFacade = mock()

            val sut = Fixture()
                .givenRepositoryFacade(repositoryFacade)
                .givenOnline()
                .givenChatClientResult(Result(ChatError()))
                .get()

            val result = sut.createNewChannel(channelMock)

            result.isSuccess `should be equal to` false
            inOrder(channelMock) {
                verify(channelMock).syncStatus = SyncStatus.IN_PROGRESS
                verify(channelMock).syncStatus = SyncStatus.SYNC_NEEDED
            }
            argumentCaptor<Channel>().apply {
                verify(repositoryFacade, times(2)).insertChannel(capture())
                with(allValues[1]) {
                    syncStatus = SyncStatus.SYNC_NEEDED
                }
            }
        }

    @Test
    fun `given online chat domain when creating channel failed permanently should mark it with proper sync states`(): Unit =
        runBlocking {
            val repositoryFacade: RepositoryFacade = mock()

            val sut = Fixture()
                .givenRepositoryFacade(repositoryFacade)
                .givenOnline()
                .givenChatClientResult(Result(ChatNetworkError.create(code = ChatErrorCode.NETWORK_FAILED)))
                .get()

            val result = sut.createNewChannel(channelMock)

            result.isSuccess `should be equal to` false
            inOrder(channelMock) {
                verify(channelMock).syncStatus = SyncStatus.IN_PROGRESS
                verify(channelMock).syncStatus = SyncStatus.FAILED_PERMANENTLY
            }
            argumentCaptor<Channel>().apply {
                verify(repositoryFacade, times(2)).insertChannel(capture())
                with(allValues[1]) {
                    syncStatus = SyncStatus.FAILED_PERMANENTLY
                }
            }
        }

    @Test
    fun `Given failed network response When create distinct channel Should return failed response And do not insert any channel to DB`() {
        val repositoryFacade: RepositoryFacade = mock()
        val sut = Fixture()
            .givenChatClientResult(Result(mock<ChatError>()))
            .givenRepositoryFacade(repositoryFacade)
            .get()

        val result = sut.createDistinctChannel("channelType", mock(), mock()).execute()

        Truth.assertThat(result.isError).isTrue()
        verifyZeroInteractions(repositoryFacade)
    }

    @Test
    fun `Given successful network response When create distinct channel Should return channel result And insert channel to DB`() = runBlockingTest {
        val repositoryFacade: RepositoryFacade = mock()
        val networkChannel = randomChannel()
        val sut = Fixture()
            .givenChatClientResult(Result(networkChannel))
            .givenRepositoryFacade(repositoryFacade)
            .get()

        val result = sut.createDistinctChannel("channelType", mock(), mock()).execute()

        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.data()).isEqualTo(networkChannel)
        verify(repositoryFacade).insertChannel(networkChannel)
    }

    private inner class Fixture {
        private val context: Context = mock()
        private val chatClient: ChatClient = mock()
        private var user: User = randomUser()
        private val testScope = TestCoroutineScope()
        private var isOnline: Boolean = true
        private var repositoryFacade: RepositoryFacade = mock()

        fun givenUser(user: User) = apply {
            this.user = user
        }

        fun givenRepositoryFacade(repositoryFacade: RepositoryFacade): Fixture = apply {
            this.repositoryFacade = repositoryFacade
        }

        fun givenChatClientResult(result: Result<Channel>): Fixture = apply {
            whenever(chatClient.createChannel(any(), any(), any(), any())) doReturn TestCall(result)
            whenever(chatClient.createChannel(any<String>(), any<List<String>>(), any<Map<String, Any>>())) doReturn
                TestCall(result)
        }

        fun givenOnline(): Fixture = apply {
            isOnline = true
        }

        fun givenOffline(): Fixture = apply {
            isOnline = false
        }

        fun get(): ChatDomainImpl {
            return ChatDomain.Builder(context, chatClient).buildImpl().apply {
                repos = repositoryFacade
                currentUser = user
                scope = testScope
                if (isOnline) setOnline() else setOffline()
            }
        }
    }
}
