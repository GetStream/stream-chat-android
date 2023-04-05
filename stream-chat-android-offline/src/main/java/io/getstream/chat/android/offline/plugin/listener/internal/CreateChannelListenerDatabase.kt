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

import io.getstream.chat.android.client.errors.isPermanent
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.plugin.listeners.CreateChannelListener
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.channel.generateChannelIdIfNeeded
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.result.Error
import io.getstream.result.Result
import java.util.Date

/**
 * [CreateChannelListener] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Handles creating the channel offline and updates the database.
 * Does not perform optimistic UI update as it's impossible to determine whether a particular channel should be visible
 * for the current user or not.
 *
 * @param clientState [ClientState]
 * @param channelRepository [ChannelRepository] to cache intermediate data and final result of channels.
 * @param userRepository [UserRepository] Requests users from database.
 */
internal class CreateChannelListenerDatabase(
    private val clientState: ClientState,
    private val channelRepository: ChannelRepository,
    private val userRepository: UserRepository,
) : CreateChannelListener {

    /**
     * A method called before making an API call to create the channel.
     * Creates the channel based on provided data and updates the database.
     * Channel's id will be automatically generated based on the members list if provided id is empty.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param memberIds The list of members' ids.
     * @param extraData Map of key-value pairs that let you store extra data
     * @param currentUser The currently logged in user.
     */
    override suspend fun onCreateChannelRequest(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        extraData: Map<String, Any>,
        currentUser: User,
    ) {
        val generatedChannelId = generateChannelIdIfNeeded(channelId, memberIds)
        val channel = Channel(
            id = generatedChannelId,
            type = channelType,
            members = getMembers(memberIds),
            extraData = extraData.toMutableMap(),
            createdAt = Date(),
            createdBy = currentUser,
            syncStatus = if (clientState.isOnline) SyncStatus.IN_PROGRESS else SyncStatus.SYNC_NEEDED,
        ).apply {
            name = getExtraValue("name", "")
            image = getExtraValue("image", "")
        }

        channelRepository.upsertChannel(channel)
    }

    /**
     * Converts member's id to [Member] object.
     * Tries to fetch users from cache and fallbacks to the empty [User] object with an [User.id] if the user
     * wasn't cached yet.
     *
     * @return The list of members.
     */
    private suspend fun getMembers(memberIds: List<String>): List<Member> {
        val cachedUsers = userRepository.selectUsers(memberIds)
        val missingUserIds = memberIds.minus(cachedUsers.map(User::id).toSet())

        return (cachedUsers + missingUserIds.map(::User)).map(::Member)
    }

    /**
     * A method called after receiving the response from the create channel call.
     * Updates channel's sync status stored in the database based on API result.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param memberIds The list of members' ids.
     * @param result The API call result.
     */
    override suspend fun onCreateChannelResult(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        result: Result<Channel>,
    ) {
        val generatedCid = "$channelType:${generateChannelIdIfNeeded(channelId, memberIds)}"
        when (result) {
            is Result.Success -> {
                val channel = result.value.apply {
                    syncStatus = SyncStatus.COMPLETED
                }

                // Generated if might differ from the actual one. This might happen when the channel already exists.
                if (channel.cid != generatedCid) {
                    channelRepository.deleteChannel(generatedCid)
                }
                channelRepository.upsertChannel(channel)
            }
            is Result.Failure -> {
                channelRepository.selectChannels(listOf(generatedCid)).firstOrNull()?.let { cachedChannel ->
                    cachedChannel.syncStatus = if (result.value.isPermanent()) {
                        SyncStatus.FAILED_PERMANENTLY
                    } else {
                        SyncStatus.SYNC_NEEDED
                    }
                    channelRepository.upsertChannel(cachedChannel)
                }
            }
        }
    }

    /**
     * Checks if current user is set and channel's id conditions are met.
     *
     * @param currentUser The currently logged in user.
     * @param channelId The channel id. ie 123.
     * @param memberIds The list of members' ids.
     */
    override fun onCreateChannelPrecondition(
        currentUser: User?,
        channelId: String,
        memberIds: List<String>,
    ): Result<Unit> {
        return when {
            channelId.isBlank() && memberIds.isEmpty() -> {
                Result.Failure(Error.GenericError(message = "Either channelId or memberIds cannot be empty!"))
            }
            currentUser == null -> {
                Result.Failure(Error.GenericError(message = "Current user is null!"))
            }
            else -> {
                Result.Success(Unit)
            }
        }
    }
}
