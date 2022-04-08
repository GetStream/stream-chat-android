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
import io.getstream.chat.android.client.persistence.repository.AttachmentRepository
import io.getstream.chat.android.client.persistence.repository.ChannelConfigRepository
import io.getstream.chat.android.client.persistence.repository.ChannelRepository
import io.getstream.chat.android.client.persistence.repository.MessageRepository
import io.getstream.chat.android.client.persistence.repository.QueryChannelsRepository
import io.getstream.chat.android.client.persistence.repository.ReactionRepository
import io.getstream.chat.android.client.persistence.repository.SyncStateRepository
import io.getstream.chat.android.client.persistence.repository.UserRepository
import io.getstream.chat.android.client.persistence.repository.factory.RepositoryFactory
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.offline.repository.domain.channel.internal.DatabaseChannelRepository
import io.getstream.chat.android.offline.repository.domain.channelconfig.internal.DatabaseChannelConfigRepository
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.DatabaseAttachmentRepository
import io.getstream.chat.android.offline.repository.domain.message.internal.DatabaseMessageRepository
import io.getstream.chat.android.offline.repository.domain.queryChannels.internal.DatabaseQueryChannelsRepository
import io.getstream.chat.android.offline.repository.domain.reaction.internal.DatabaseReactionRepository
import io.getstream.chat.android.offline.repository.domain.syncState.internal.DatabaseSyncStateRepository
import io.getstream.chat.android.offline.repository.domain.user.internal.DatabaseUserRepository

internal class DatabaseRepositoryFactory(
    private val database: ChatDatabase,
    private val currentUser: User?,
): RepositoryFactory {
    override fun userRepository(): UserRepository = DatabaseUserRepository(database.userDao(), 100)

    override fun channelConfigRepository(): ChannelConfigRepository =
        DatabaseChannelConfigRepository(database.channelConfigDao())

    override fun channelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository = DatabaseChannelRepository(database.channelStateDao(), getUser, getMessage, 100)

    override fun queryChannelsRepository(): QueryChannelsRepository =
        DatabaseQueryChannelsRepository(database.queryChannelsDao())

    override fun messageRepository(
        getUser: suspend (userId: String) -> User,
    ): MessageRepository = DatabaseMessageRepository(database.messageDao(), getUser, currentUser, 100)

    override fun reactionRepository(getUser: suspend (userId: String) -> User): ReactionRepository =
        DatabaseReactionRepository(database.reactionDao(), getUser)

    override fun syncStateRepository(): SyncStateRepository = DatabaseSyncStateRepository(database.syncStateDao())

    override fun attachmentRepository(): AttachmentRepository = DatabaseAttachmentRepository(database.attachmentDao())
}
