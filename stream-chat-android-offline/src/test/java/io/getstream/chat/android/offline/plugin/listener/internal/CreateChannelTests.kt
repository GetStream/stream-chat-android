/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.query.CreateChannelParams
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomUser
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class CreateChannelTests {

    @Test
    fun `Given offline user When creating channel Should mark it with sync needed and store in database`(): Unit = runTest {
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
        val params = CreateChannelParams(
            members = members.map { MemberData(it.getUserId(), extraData = it.extraData) },
            extraData = emptyMap(),
        )
        val currentUser = randomUser()

        sut.onCreateChannelRequest(
            channelType = channelType,
            channelId = channelId,
            params = params,
            currentUser = currentUser,
        )

        verify(repos).insertChannel(
            argThat {
                this.type == channelType &&
                    this.id == channelId &&
                    this.members.map(Member::getUserId).containsAll(members.map(Member::getUserId)) &&
                    createdBy == currentUser &&
                    syncStatus == SyncStatus.SYNC_NEEDED
            },
        )
    }

    @Test
    fun `Given online user When creating channel Should mark it with in progress and store in database`(): Unit = runTest {
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
        val params = CreateChannelParams(
            members = members.map { MemberData(it.getUserId(), extraData = it.extraData) },
            extraData = emptyMap(),
        )
        val currentUser = randomUser()

        sut.onCreateChannelRequest(
            channelType = channelType,
            channelId = channelId,
            params = params,
            currentUser = currentUser,
        )

        verify(repos).insertChannel(
            argThat {
                this.type == channelType &&
                    this.id == channelId &&
                    this.members.map(Member::getUserId).containsAll(members.map(Member::getUserId)) &&
                    createdBy == currentUser &&
                    syncStatus == SyncStatus.IN_PROGRESS
            },
        )
    }

    @Test
    fun `Given successful result When creating channel Should mark it as completed and store in database`(): Unit = runTest {
        val repos = mock<RepositoryFacade>()
        val sut = Fixture()
            .givenMockedRepos(repos)
            .givenOnlineState()
            .get()

        val channelType = "channelType"
        val channelId = "channelId"
        val result =
            Result.Success(randomChannel(id = channelId, type = channelType))
        val expected = result.value.copy(syncStatus = SyncStatus.COMPLETED)

        sut.onCreateChannelResult(
            channelType = channelType,
            channelId = channelId,
            memberIds = emptyList(),
            result = result,
        )

        verify(repos).insertChannel(expected)
    }

    @Test
    fun `Given successful result When creating channel Should delete local channel from DB if result has different cid`(): Unit = runTest {
        val repos = mock<RepositoryFacade>()
        val sut = Fixture()
            .givenMockedRepos(repos)
            .givenOnlineState()
            .get()

        val channelType = "channelType"
        val channelId = "channel"
        val result =
            Result.Success(randomChannel())

        sut.onCreateChannelResult(
            channelType = channelType,
            channelId = channelId,
            memberIds = emptyList(),
            result = result,
        )

        verify(repos).deleteChannel("$channelType:$channelId")
    }

    @Test
    fun `Given failed result When creating channel Should mark it as sync needed and store in the DB`(): Unit = runTest {
        val channelType = "channelType"
        val channelId = "channel"
        val cid = "$channelType:$channelId"
        val channel =
            randomChannel(id = channelId, type = channelType, syncStatus = SyncStatus.IN_PROGRESS)
        val repos = mock<RepositoryFacade> {
            on(it.selectChannels(listOf(cid))) doReturn listOf(channel)
        }
        val result = Result.Failure(Error.NetworkError(message = "", serverErrorCode = 0, statusCode = 500))
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
            },
        )
    }

    @Test
    fun `Given failed result When creating channel Should mark it as failed permanently and store in the DB`(): Unit = runTest {
        val channelType = "channelType"
        val channelId = "channel"
        val cid = "$channelType:$channelId"
        val channel =
            randomChannel(id = channelId, type = channelType, syncStatus = SyncStatus.IN_PROGRESS)
        val repos = mock<RepositoryFacade> {
            on(it.selectChannels(listOf(cid))) doReturn listOf(channel)
        }
        val result = Result.Failure(Error.NetworkError(message = "", serverErrorCode = 60, statusCode = 403))
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
            },
        )
    }

    @Test
    fun `Given user not set user When creating channel Should return error`(): Unit = runTest {
        val sut = Fixture().get()

        val result = sut.onCreateChannelPrecondition(
            channelId = "channelId",
            memberIds = listOf(randomMember().getUserId()),
            currentUser = null,
        )

        result shouldBeInstanceOf Result.Failure::class
    }

    @Test
    fun `Given empty both id and member list When creating channel Should return error`(): Unit = runTest {
        val sut = Fixture().get()

        val result = sut.onCreateChannelPrecondition(
            channelId = "",
            memberIds = emptyList(),
            currentUser = randomUser(),
        )

        result shouldBeInstanceOf Result.Failure::class
    }

    @Test
    fun `Given user set and correct id When creating channel Should return success`(): Unit = runTest {
        val sut = Fixture().get()

        val result = sut.onCreateChannelPrecondition(
            channelId = "channelId",
            memberIds = emptyList(),
            currentUser = randomUser(),
        )

        result shouldBeInstanceOf Result.Success::class
    }

    private inner class Fixture {

        private val clientState = mock<ClientState>()
        private var repositoryFacade = mock<RepositoryFacade>()

        fun givenOnlineState() = apply {
            whenever(clientState.isOnline) doReturn true
        }

        fun givenOfflineState() = apply {
            whenever(clientState.isOnline) doReturn false
        }

        fun givenMockedRepos(repos: RepositoryFacade) = apply {
            repositoryFacade = repos
        }

        fun get(): CreateChannelListenerDatabase = CreateChannelListenerDatabase(
            clientState,
            repositoryFacade,
            repositoryFacade,
        )
    }
}
