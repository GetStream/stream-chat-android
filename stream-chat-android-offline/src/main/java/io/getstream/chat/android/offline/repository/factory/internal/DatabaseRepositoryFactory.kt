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
import io.getstream.chat.android.offline.repository.domain.channel.internal.ChannelRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.channelconfig.internal.ChannelConfigRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.AttachmentRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.message.internal.MessageRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.queryChannels.internal.QueryChannelsRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.reaction.internal.ReactionRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.syncState.internal.SyncStateRepositoryImpl
import io.getstream.chat.android.offline.repository.domain.user.internal.UserRepositoryImpl

internal class DatabaseRepositoryFactory(
    private val database: ChatDatabase,
    private val currentUser: User?,
): RepositoryFactory {
    override fun userRepository(): UserRepository = UserRepositoryImpl(database.userDao(), 100)

    override fun channelConfigRepository(): ChannelConfigRepository =
        ChannelConfigRepositoryImpl(database.channelConfigDao())

    override fun channelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository = ChannelRepositoryImpl(database.channelStateDao(), getUser, getMessage, 100)

    override fun queryChannelsRepository(): QueryChannelsRepository =
        QueryChannelsRepositoryImpl(database.queryChannelsDao())

    override fun messageRepository(
        getUser: suspend (userId: String) -> User,
    ): MessageRepository = MessageRepositoryImpl(database.messageDao(), getUser, currentUser, 100)

    override fun reactionRepository(getUser: suspend (userId: String) -> User): ReactionRepository =
        ReactionRepositoryImpl(database.reactionDao(), getUser)

    override fun syncStateRepository(): SyncStateRepository = SyncStateRepositoryImpl(database.syncStateDao())

    override fun attachmentRepository(): AttachmentRepository = AttachmentRepositoryImpl(database.attachmentDao())
}
