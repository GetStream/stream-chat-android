package io.getstream.chat.android.livedata

import android.content.Context
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.repository.RepositoryFacade
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension

@ExtendWith(InstantTaskExecutorExtension::class)
internal class ChatDomainImplCreateChannelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private val currentUser = User()
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
            val repositoryFacade: RepositoryFacade = mock()
            val channel = Channel(
                cid = channelCid,
                id = channelId,
                type = channelType,
                members = channelMembers,
                extraData = channelExtraData,
            )
            val sut = Fixture(testCoroutines.scope, currentUser)
                .givenRepositoryFacade(repositoryFacade)
                .givenOffline()
                .get()

            val result = sut.createChannel(channel)

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
            val repositoryFacade: RepositoryFacade = mock()
            val channel = Channel(
                cid = channelCid,
                id = channelId,
                type = channelType,
                members = channelMembers,
                extraData = channelExtraData,
            )
            val sut = Fixture(testCoroutines.scope, currentUser)
                .givenRepositoryFacade(repositoryFacade)
                .givenOnline()
                .givenChatClientResult(Result(channel))
                .get()

            val result = sut.createChannel(channel)

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

            val sut = Fixture(testCoroutines.scope, currentUser)
                .givenRepositoryFacade(repositoryFacade)
                .givenOnline()
                .givenChatClientResult(Result(channelMock))
                .get()

            val result = sut.createChannel(channelMock)

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

            val sut = Fixture(testCoroutines.scope, currentUser)
                .givenRepositoryFacade(repositoryFacade)
                .givenOnline()
                .givenChatClientResult(Result(ChatError()))
                .get()

            val result = sut.createChannel(channelMock)

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

            val sut = Fixture(testCoroutines.scope, currentUser)
                .givenRepositoryFacade(repositoryFacade)
                .givenOnline()
                .givenChatClientResult(Result(ChatNetworkError.create(code = ChatErrorCode.NETWORK_FAILED)))
                .get()

            val result = sut.createChannel(channelMock)

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
}

private class Fixture(scope: CoroutineScope, user: User) {
    private val context: Context = mock()
    private val chatClient: ChatClient = mock()
    private val chatDomainImpl: ChatDomainImpl = ChatDomain.Builder(context, chatClient).buildImpl()

    init {
        chatDomainImpl.apply {
            currentUser = user
            this.scope = scope
        }
    }

    fun givenRepositoryFacade(repositoryFacade: RepositoryFacade): Fixture {
        chatDomainImpl.repos = repositoryFacade
        return this
    }

    fun givenChatClientResult(result: Result<Channel>): Fixture {
        whenever(chatClient.createChannel(any(), any(), any(), any())).doAnswer {
            TestCall(result)
        }
        return this
    }

    fun givenOnline(): Fixture {
        chatDomainImpl.setOnline()
        return this
    }

    fun givenOffline(): Fixture {
        chatDomainImpl.setOffline()
        return this
    }

    fun get(): ChatDomainImpl = chatDomainImpl
}
