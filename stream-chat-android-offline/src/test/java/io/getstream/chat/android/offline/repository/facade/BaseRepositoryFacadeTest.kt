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

package io.getstream.chat.android.offline.repository.facade

import androidx.annotation.CallSuper
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade
import io.getstream.chat.android.offline.repository.domain.channel.internal.ChannelRepository
import io.getstream.chat.android.offline.repository.domain.channelconfig.internal.ChannelConfigRepository
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.AttachmentRepository
import io.getstream.chat.android.offline.repository.domain.message.internal.MessageRepository
import io.getstream.chat.android.offline.repository.domain.queryChannels.internal.QueryChannelsRepository
import io.getstream.chat.android.offline.repository.domain.reaction.internal.ReactionRepository
import io.getstream.chat.android.offline.repository.domain.syncState.internal.SyncStateRepository
import io.getstream.chat.android.offline.repository.domain.user.internal.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.mock

@ExperimentalCoroutinesApi
internal open class BaseRepositoryFacadeTest {

    protected lateinit var users: UserRepository
    protected lateinit var configs: ChannelConfigRepository
    protected lateinit var channels: ChannelRepository
    protected lateinit var queryChannels: QueryChannelsRepository
    protected lateinit var messages: MessageRepository
    protected lateinit var reactions: ReactionRepository
    protected lateinit var syncState: SyncStateRepository
    protected lateinit var attachmentRepository: AttachmentRepository

    protected val scope = TestCoroutineScope()

    protected lateinit var sut: RepositoryFacade

    @CallSuper
    @BeforeEach
    fun setUp() {
        users = mock()
        configs = mock()
        channels = mock()
        queryChannels = mock()
        messages = mock()
        reactions = mock()
        syncState = mock()
        attachmentRepository = mock()

        sut = RepositoryFacade(
            userRepository = users,
            configsRepository = configs,
            channelsRepository = channels,
            queryChannelsRepository = queryChannels,
            messageRepository = messages,
            reactionsRepository = reactions,
            syncStateRepository = syncState,
            attachmentRepository = attachmentRepository,
            scope = scope,
            defaultConfig = mock(),
        )
    }
}
