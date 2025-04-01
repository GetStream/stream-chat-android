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

package io.getstream.chat.android.client.persistance.repository.noop

import io.getstream.chat.android.client.persistance.repository.ChannelConfigRepository
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.QueryChannelsRepository
import io.getstream.chat.android.client.persistance.repository.ReactionRepository
import io.getstream.chat.android.client.persistance.repository.SyncStateRepository
import io.getstream.chat.android.client.persistance.repository.ThreadsRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.persistance.repository.factory.RepositoryFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

/**
 * No-Op RepositoryFactory.
 */
internal object NoOpRepositoryFactory : RepositoryFactory {
    override fun createUserRepository(): UserRepository = NoOpUserRepository
    override fun createChannelConfigRepository(): ChannelConfigRepository = NoOpChannelConfigRepository
    override fun createQueryChannelsRepository(): QueryChannelsRepository = NoOpQueryChannelsRepository
    override fun createThreadsRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
        getChannel: suspend (cid: String) -> Channel?,
    ): ThreadsRepository = NoOpThreadsRepository

    override fun createSyncStateRepository(): SyncStateRepository = NoOpSyncStateRepository
    override fun createReactionRepository(
        getUser: suspend (userId: String) -> User,
    ): ReactionRepository = NoOpReactionRepository

    override fun createMessageRepository(
        getUser: suspend (userId: String) -> User,
    ): MessageRepository = NoOpMessageRepository

    override fun createChannelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository = NoOpChannelRepository

    object Provider : RepositoryFactory.Provider {
        override fun createRepositoryFactory(user: User): RepositoryFactory = NoOpRepositoryFactory
    }
}
