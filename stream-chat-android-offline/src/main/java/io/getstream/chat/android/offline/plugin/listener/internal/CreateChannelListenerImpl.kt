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

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.plugin.listeners.CreateChannelListener
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade
import io.getstream.chat.android.offline.utils.internal.generateChannelIdIfNeeded
import java.util.Date

/**
 * [CreateChannelListener] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Handles creating the channel offline and updates the database.
 * Does not perform optimistic UI update as it's impossible to determine whether a particular channel should be visible for the current user or not.
 *
 * @param globalState [GlobalState] provided by the [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * @param repositoryFacade [RepositoryFacade] to cache intermediate data and final result.
 */
internal class CreateChannelListenerImpl(
    private val globalState: GlobalState,
    private val repositoryFacade: RepositoryFacade,
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
            cid = "$channelType:$generatedChannelId",
            members = getMembers(memberIds),
            extraData = extraData.toMutableMap(),
            createdAt = Date(),
            createdBy = currentUser,
            syncStatus = if (globalState.isOnline()) SyncStatus.IN_PROGRESS else SyncStatus.SYNC_NEEDED,
        ).apply {
            name = getExtraValue("name", "")
            image = getExtraValue("image", "")
        }

        repositoryFacade.insertChannel(channel)
    }

    /**
     * Converts member's id to [Member] object.
     * Tries to fetch users from cache and fallbacks to the empty [User] object with an [User.id] if the user wasn't cached yet.
     *
     * @return The list of members.
     */
    private suspend fun getMembers(memberIds: List<String>): List<Member> {
        val cachedUsers = repositoryFacade.selectUsers(memberIds)
        val missingUserIds = memberIds.minus(cachedUsers.map(User::id))

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
        if (result.isSuccess) {
            val channel = result.data().apply {
                syncStatus = SyncStatus.COMPLETED
            }

            // Generated if might differ from the actual one. This might happen when the channel already exists.
            if (channel.cid != generatedCid) {
                repositoryFacade.deleteChannel(generatedCid)
            }
            repositoryFacade.insertChannel(channel)
        } else {
            repositoryFacade.selectChannels(listOf(generatedCid)).firstOrNull()?.let { cachedChannel ->
                cachedChannel.syncStatus = if (result.error().isPermanent()) {
                    SyncStatus.FAILED_PERMANENTLY
                } else {
                    SyncStatus.SYNC_NEEDED
                }
                repositoryFacade.insertChannel(cachedChannel)
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
                Result.error(ChatError(message = "Either channelId or memberIds cannot be empty!"))
            }
            currentUser == null -> {
                Result.error(ChatError(message = "Current user is null!"))
            }
            else -> {
                Result.success(Unit)
            }
        }
    }
}
