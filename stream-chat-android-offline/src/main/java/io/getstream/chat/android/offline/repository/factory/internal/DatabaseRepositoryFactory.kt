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

package io.getstream.chat.android.offline.repository.factory.internal

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistance.repository.AttachmentRepository
import io.getstream.chat.android.client.persistance.repository.ChannelConfigRepository
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.QueryChannelsRepository
import io.getstream.chat.android.client.persistance.repository.ReactionRepository
import io.getstream.chat.android.client.persistance.repository.SyncStateRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.persistance.repository.factory.RepositoryFactory
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.offline.repository.domain.channel.internal.DatabaseChannelRepository
import io.getstream.chat.android.offline.repository.domain.channelconfig.internal.DatabaseChannelConfigRepository
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.DatabaseAttachmentRepository
import io.getstream.chat.android.offline.repository.domain.message.internal.DatabaseMessageRepository
import io.getstream.chat.android.offline.repository.domain.queryChannels.internal.DatabaseQueryChannelsRepository
import io.getstream.chat.android.offline.repository.domain.reaction.internal.DatabaseReactionRepository
import io.getstream.chat.android.offline.repository.domain.syncState.internal.DatabaseSyncStateRepository
import io.getstream.chat.android.offline.repository.domain.user.internal.DatabaseUserRepository

private const val DEFAULT_CACHE_SIZE = 100

internal class DatabaseRepositoryFactory(
    private val database: ChatDatabase,
    private val currentUser: User?,
) : RepositoryFactory {

    override fun createUserRepository(): UserRepository = DatabaseUserRepository(database.userDao(), DEFAULT_CACHE_SIZE)
    override fun createChannelConfigRepository(): ChannelConfigRepository =
        DatabaseChannelConfigRepository(database.channelConfigDao())

    override fun createChannelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository =
        DatabaseChannelRepository(database.channelStateDao(), getUser, getMessage, DEFAULT_CACHE_SIZE)

    override fun createQueryChannelsRepository(): QueryChannelsRepository =
        DatabaseQueryChannelsRepository(database.queryChannelsDao())

    override fun createMessageRepository(
        getUser: suspend (userId: String) -> User,
    ): MessageRepository = DatabaseMessageRepository(database.messageDao(), getUser, currentUser, DEFAULT_CACHE_SIZE)

    override fun createReactionRepository(getUser: suspend (userId: String) -> User): ReactionRepository =
        DatabaseReactionRepository(database.reactionDao(), getUser)

    override fun createSyncStateRepository(): SyncStateRepository = DatabaseSyncStateRepository(database.syncStateDao())

    override fun createAttachmentRepository(): AttachmentRepository =
        DatabaseAttachmentRepository(database.attachmentDao())
}
