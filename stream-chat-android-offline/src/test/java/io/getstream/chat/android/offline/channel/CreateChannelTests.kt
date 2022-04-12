package io.getstream.chat.android.offline.channel

import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.plugin.listener.internal.CreateChannelListenerImpl
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import io.getstream.chat.android.offline.randomChannel
import io.getstream.chat.android.offline.randomMember
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
internal class CreateChannelTests {
    @get:Rule
    val testCoroutines: TestCoroutineRule = TestCoroutineRule()

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @Test
    fun `Given offline user When creating channel Should mark it with sync needed and store in database`(): Unit =
        runTest {
            val members = listOf(randomMember())
            val repos = mock<RepositoryFacade> {
                on(it.selectUsers(members.map(Member::getUserId))) doReturn members.map(Member::user)
            }
            val sut = Fixture()
                .givenMockedRepos(repos)
                .givenOfflineState()
                .get()

            val channelType = "channelType"
            val channelId = "channelId"
            val currentUser = randomUser()

            sut.onCreateChannelRequest(
                channelType = channelType,
                channelId = channelId,
                memberIds = members.map(Member::getUserId),
                extraData = emptyMap(),
                currentUser = currentUser,
            )

            verify(repos).insertChannel(
                argThat {
                    this.type == channelType &&
                        this.id == channelId &&
                        this.members.map(Member::getUserId).containsAll(members.map(Member::getUserId)) &&
                        createdBy == currentUser &&
                        syncStatus == SyncStatus.SYNC_NEEDED
                }
            )
        }

    @Test
    fun `Given online user When creating channel Should mark it with in progress and store in database`(): Unit =
        runTest {
            val members = listOf(randomMember())
            val repos = mock<RepositoryFacade> {
                on(it.selectUsers(members.map(Member::getUserId))) doReturn members.map(Member::user)
            }
            val sut = Fixture()
                .givenMockedRepos(repos)
                .givenOnlineState()
                .get()

            val channelType = "channelType"
            val channelId = "channelId"
            val currentUser = randomUser()

            sut.onCreateChannelRequest(
                channelType = channelType,
                channelId = channelId,
                memberIds = members.map(Member::getUserId),
                extraData = emptyMap(),
                currentUser = currentUser,
            )

            verify(repos).insertChannel(
                argThat {
                    this.type == channelType &&
                        this.id == channelId &&
                        this.members.map(Member::getUserId).containsAll(members.map(Member::getUserId)) &&
                        createdBy == currentUser &&
                        syncStatus == SyncStatus.IN_PROGRESS
                }
            )
        }

    @Test
    fun `Given successful result When creating channel Should mark it as completed and store in database`(): Unit =
        runTest {
            val repos = mock<RepositoryFacade>()
            val sut = Fixture()
                .givenMockedRepos(repos)
                .givenOnlineState()
                .get()

            val channelType = "channelType"
            val channelId = "channelId"
            val result =
                Result.success(randomChannel(id = channelId, type = channelType, cid = "$channelType:$channelId"))

            sut.onCreateChannelResult(
                channelType = channelType,
                channelId = channelId,
                memberIds = emptyList(),
                result = result,
            )

            verify(repos).insertChannel(result.data())
        }

    @Test
    fun `Given successful result When creating channel Should delete local channel from DB if result has different cid`(): Unit =
        runTest {
            val repos = mock<RepositoryFacade>()
            val sut = Fixture()
                .givenMockedRepos(repos)
                .givenOnlineState()
                .get()

            val channelType = "channelType"
            val channelId = "channel"
            val result =
                Result.success(randomChannel())

            sut.onCreateChannelResult(
                channelType = channelType,
                channelId = channelId,
                memberIds = emptyList(),
                result = result,
            )

            verify(repos).deleteChannel("$channelType:$channelId")
        }

    @Test
    fun `Given failed result When creating channel Should mark it as sync needed and store in the DB`(): Unit =
        runTest {
            val channelType = "channelType"
            val channelId = "channel"
            val cid = "$channelType:$channelId"
            val channel =
                randomChannel(id = channelId, type = channelType, cid = cid, syncStatus = SyncStatus.IN_PROGRESS)
            val repos = mock<RepositoryFacade> {
                on(it.selectChannels(listOf(cid))) doReturn listOf(channel)
            }
            val result = Result.error<Channel>(ChatNetworkError.create(0, "", 500, null))
            val sut = Fixture()
                .givenMockedRepos(repos)
                .givenOnlineState()
                .get()

            sut.onCreateChannelResult(
                channelType = channelType,
                channelId = channelId,
                memberIds = emptyList(),
                result = result,
            )

            verify(repos).insertChannel(
                argThat {
                    this.type == channelType &&
                        this.id == channelId &&
                        this.cid == cid &&
                        syncStatus == SyncStatus.SYNC_NEEDED
                }
            )
        }

    @Test
    fun `Given failed result When creating channel Should mark it as failed permanently and store in the DB`(): Unit =
        runTest {
            val channelType = "channelType"
            val channelId = "channel"
            val cid = "$channelType:$channelId"
            val channel =
                randomChannel(id = channelId, type = channelType, cid = cid, syncStatus = SyncStatus.IN_PROGRESS)
            val repos = mock<RepositoryFacade> {
                on(it.selectChannels(listOf(cid))) doReturn listOf(channel)
            }
            val result = Result.error<Channel>(ChatNetworkError.create(60, "", 403, null))
            val sut = Fixture()
                .givenMockedRepos(repos)
                .givenOnlineState()
                .get()

            sut.onCreateChannelResult(
                channelType = channelType,
                channelId = channelId,
                memberIds = emptyList(),
                result = result,
            )

            verify(repos).insertChannel(
                argThat {
                    this.type == channelType &&
                        this.id == channelId &&
                        this.cid == cid &&
                        syncStatus == SyncStatus.FAILED_PERMANENTLY
                }
            )
        }

    @Test
    fun `Given user not set user When creating channel Should return error`(): Unit =
        runTest {
            val sut = Fixture().get()

            val result = sut.onCreateChannelPrecondition(
                channelId = "channelId",
                memberIds = listOf(randomMember().getUserId()),
                currentUser = null,
            )

            result.isError shouldBeEqualTo true
        }

    @Test
    fun `Given empty both id and member list When creating channel Should return error`(): Unit =
        runTest {
            val sut = Fixture().get()

            val result = sut.onCreateChannelPrecondition(
                channelId = "",
                memberIds = emptyList(),
                currentUser = randomUser(),
            )

            result.isError shouldBeEqualTo true
        }

    @Test
    fun `Given user set and correct id When creating channel Should return success`(): Unit =
        runTest {
            val sut = Fixture().get()

            val result = sut.onCreateChannelPrecondition(
                channelId = "channelId",
                memberIds = emptyList(),
                currentUser = randomUser(),
            )

            result.isSuccess shouldBeEqualTo true
        }

    private inner class Fixture {

        private val globalState = mock<GlobalState>()
        private var repositoryFacade = mock<RepositoryFacade>()

        fun givenOnlineState() = apply {
            whenever(globalState.isOnline()) doReturn true
        }

        fun givenOfflineState() = apply {
            whenever(globalState.isOnline()) doReturn false
        }

        fun givenMockedRepos(repos: RepositoryFacade) = apply {
            repositoryFacade = repos
        }

        fun get(): CreateChannelListenerImpl {
            return CreateChannelListenerImpl(globalState, repositoryFacade)
        }
    }
}
