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

package io.getstream.chat.android.state.facade

import androidx.annotation.CallSuper
import io.getstream.chat.android.client.persistance.repository.ChannelConfigRepository
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.QueryChannelsRepository
import io.getstream.chat.android.client.persistance.repository.ReactionRepository
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.persistance.repository.SyncStateRepository
import io.getstream.chat.android.client.persistance.repository.ThreadsRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.persistance.repository.factory.RepositoryFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.mock

@ExperimentalCoroutinesApi
internal open class BaseRepositoryFacadeTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    protected lateinit var users: UserRepository
    protected lateinit var configs: ChannelConfigRepository
    protected lateinit var channels: ChannelRepository
    protected lateinit var queryChannels: QueryChannelsRepository
    protected lateinit var threadsRepository: ThreadsRepository
    protected lateinit var messages: MessageRepository
    protected lateinit var reactions: ReactionRepository
    protected lateinit var syncState: SyncStateRepository

    protected lateinit var sut: RepositoryFacade

    @CallSuper
    @BeforeEach
    fun setUp() {
        users = mock()
        configs = mock()
        channels = mock()
        queryChannels = mock()
        threadsRepository = mock()
        messages = mock()
        reactions = mock()
        syncState = mock()

        val repositoryFactory = object : RepositoryFactory {
            override fun createUserRepository(): UserRepository = users
            override fun createChannelConfigRepository(): ChannelConfigRepository = configs
            override fun createChannelRepository(
                getUser: suspend (userId: String) -> User,
                getMessage: suspend (messageId: String) -> Message?,
            ): ChannelRepository = channels

            override fun createQueryChannelsRepository(): QueryChannelsRepository = queryChannels
            override fun createThreadsRepository(
                getUser: suspend (userId: String) -> User,
                getMessage: suspend (messageId: String) -> Message?,
                getChannel: suspend (cid: String) -> Channel?,
            ): ThreadsRepository = threadsRepository

            override fun createMessageRepository(getUser: suspend (userId: String) -> User): MessageRepository = messages

            override fun createReactionRepository(getUser: suspend (userId: String) -> User): ReactionRepository = reactions

            override fun createSyncStateRepository(): SyncStateRepository = syncState
        }
        sut = RepositoryFacade.create(repositoryFactory, testCoroutines.scope)
    }
}
