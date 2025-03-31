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

package io.getstream.chat.android.client.persistance.repository.factory

import io.getstream.chat.android.client.persistance.repository.ChannelConfigRepository
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.PollRepository
import io.getstream.chat.android.client.persistance.repository.QueryChannelsRepository
import io.getstream.chat.android.client.persistance.repository.ReactionRepository
import io.getstream.chat.android.client.persistance.repository.SyncStateRepository
import io.getstream.chat.android.client.persistance.repository.ThreadsRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

/**
 * Factory that creates all repositories of SDK.
 */
public interface RepositoryFactory {

    /**
     * Creates [UserRepository]
     */
    public fun createUserRepository(): UserRepository

    /**
     * Creates [ChannelConfigRepository]
     */
    public fun createChannelConfigRepository(): ChannelConfigRepository

    /**
     * Creates [ChannelRepository]
     *
     * @param getUser function that provides userId.
     * @param getMessage function that provides messageId.
     */
    public fun createChannelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository

    /**
     * Creates [QueryChannelsRepository]
     */
    public fun createQueryChannelsRepository(): QueryChannelsRepository

    /**
     * Creates [ThreadsRepository].
     *
     * @param getUser Function returning a [User] for a given ID.
     * @param getMessage Function returning a [Message] for a given ID.
     * @param getChannel Function returning a [Channel] for a given ID.
     */
    public fun createThreadsRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
        getChannel: suspend (cid: String) -> Channel?,
    ): ThreadsRepository

    /**
     * Creates [MessageRepository]
     *
     * @param getUser function that provides userId.
     */
    public fun createMessageRepository(
        getUser: suspend (userId: String) -> User,
    ): MessageRepository

    /**
     * Creates [ReactionRepository]
     *
     * @param getUser function that provides userId.
     */
    public fun createReactionRepository(getUser: suspend (userId: String) -> User): ReactionRepository

    /**
     * Creates [SyncStateRepository]
     */
    public fun createSyncStateRepository(): SyncStateRepository

    /**
     * Creates [PollRepository].
     */
    public fun createPollRepository(): PollRepository

    /**
     * Interface to delegate creation of [RepositoryFactory].
     */
    public interface Provider {

        /**
         * Create a [RepositoryFactory] for the given [User].
         */
        public fun createRepositoryFactory(user: User): RepositoryFactory
    }
}
