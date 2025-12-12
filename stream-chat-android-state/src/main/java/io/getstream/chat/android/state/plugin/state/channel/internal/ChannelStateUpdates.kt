/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.plugin.state.channel.internal

import io.getstream.chat.android.client.events.HasChannel
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.User
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

/**
 * Defines a set of operations for updating the state of a channel.
 */
@Suppress("TooManyFunctions")
internal interface ChannelStateUpdates {

    /**
     * Inserts or updates a message in the channel state.
     *
     * @param message The message to be upserted.
     */
    fun upsertMessage(message: Message)

    /**
     * Updates an existing message in the channel state.
     *
     * @param message The message to be updated.
     */
    fun updateMessage(message: Message)

    /**
     * Updates the read state of the current user in the channel state.
     *
     * @param eventCreatedAt The timestamp of the event that triggered the read update.
     * @param message The message associated with the read update.
     */
    fun updateCurrentUserRead(eventCreatedAt: Date, message: Message)

    /**
     * Sets the hidden state of the channel.
     *
     * @param hidden True to hide the channel, false to unhide it.
     */
    fun setHidden(hidden: Boolean)

    /**
     * Sets the muted state of the channel.
     *
     * @param muted True to mute the channel, false to unmute it.
     */
    fun setMuted(muted: Boolean)

    /**
     * Sets the message count in the channel state.
     *
     * @param count The new message count.
     */
    fun setMessageCount(count: Int)

    /**
     * Deletes a message from the channel state.
     *
     * @param message The message to be deleted.
     */
    fun deleteMessage(message: Message)

    /**
     * Deletes all messages for a specific user.
     *
     * @param userId The ID of the user whose messages are to be deleted.
     * @param hard True for hard delete, false for soft delete.
     * @param deletedAt The timestamp when the messages were deleted.
     */
    fun deleteMessagesFromUser(userId: String, hard: Boolean, deletedAt: Date)

    /**
     * Deletes or inserts a pinned message in the channel state.
     *
     * @param message The message to be deleted/inserted.
     */
    fun delsertPinnedMessage(message: Message)

    /**
     * Adds a member to the channel state.
     *
     * @param member The member to be added.
     */
    fun addMember(member: Member)

    /**
     * Adds the membership to the channel state.
     *
     * @param member The member whose membership is to be added.
     */
    fun addMembership(member: Member)

    /**
     * Inserts or updates a member in the channel state.
     *
     * @param member The member to be upserted.
     */
    fun upsertMember(member: Member)

    /**
     * Inserts or updates multiple members in the channel state.
     *
     * @param members The list of members to be upserted.
     */
    fun upsertMembers(members: List<Member>)

    /**
     * Updates the membership of the channel.
     *
     * @param membership The new membership.
     */
    fun updateMembership(membership: Member)

    /**
     * Removes a member from the channel state.
     *
     * @param member The member to be removed.
     */
    fun deleteMember(member: Member)

    /**
     * Removes the membership from the channel state.
     */
    fun removeMembership()

    /**
     * Sets the members of the channel state.
     *
     * @param members The list of members to be set.
     * @param memberCount The total count of members.
     */
    fun setMembers(members: List<Member>, memberCount: Int)

    /**
     * Inserts or updates a watcher in the channel state.
     *
     * @param event The event containing watcher information.
     */
    fun upsertWatcher(event: UserStartWatchingEvent)

    /**
     * Deletes a watcher from the channel state.
     *
     * @param event The event containing watcher information.
     */
    fun deleteWatcher(event: UserStopWatchingEvent)

    /**
     * Sets the watchers of the channel state.
     *
     * @param watchers The list of watchers to be set.
     * @param watcherCount The total count of watchers.
     */
    fun setWatchers(watchers: List<User>, watcherCount: Int)

    /**
     * Updates the channel data in the channel state based on an event.
     *
     * @param event The event containing the new channel data.
     */
    fun updateChannelData(event: HasChannel)

    /**
     * Removes messages from the channel state that were created before the specified date.
     *
     * @param date The cutoff date; messages created before this date will be removed.
     * @param systemMessage An optional system message to be added after removal.
     */
    fun removeMessagesBefore(date: Date, systemMessage: Message? = null)

    /**
     * Deletes the channel.
     *
     * @param deletedAt The timestamp when the channel was deleted.
     */
    fun deleteChannel(deletedAt: Date)

    /**
     * Sets the typing status of a user in the channel state.
     *
     * @param userId The ID of the user whose typing status is to be set.
     * @param event The typing start event, or null to indicate that the user has stopped typing.
     */
    fun setTyping(userId: String, event: TypingStartEvent?)

    /**
     * Updates the read status of a user in the channel state.
     *
     * @param read The read status to be updated.
     */
    fun updateRead(read: ChannelUserRead)

    /**
     * Updates the delivered status of a user in the channel state.
     *
     * @param read The delivered status to be updated.
     */
    fun updateDelivered(read: ChannelUserRead)

    /**
     * Updates the ban status of a member in the channel state.
     *
     * @param memberId The ID of the member whose ban status is to be updated.
     * @param banned True if the member is banned, false otherwise.
     * @param expiry The expiry date of the ban, or null if there is no expiry.
     * @param shadow True if the ban is a shadow ban, false otherwise.
     */
    fun updateMemberBan(memberId: String?, banned: Boolean, expiry: Date?, shadow: Boolean)

    /**
     * Inserts or updates a poll in the channel state.
     *
     * @param poll The poll to be upserted.
     */
    fun upsertPoll(poll: Poll)

    /**
     * Deletes a poll from the channel state.
     *
     * @param poll The poll to be deleted.
     */
    fun deletePoll(poll: Poll)

    /**
     * Inserts or updates a user in the channel state.
     *
     * @param user The user to be upserted.
     */
    fun upsertUserPresence(user: User)

    // State retrieval methods, perhaps it should be revisited

    /**
     * Retrieves a message by its ID from the channel state.
     *
     * @param id The ID of the message to be retrieved.
     */
    fun getMessageById(id: String): Message?

    /**
     * Retrieves a poll by its ID from the channel state.
     *
     * @param id The ID of the poll to be retrieved.
     */
    fun getPoll(id: String): Poll?

    /**
     * Retrieves all visible messages from the channel state as a flow.
     *
     * @return A flow emitting a map of message IDs to messages.
     */
    val visibleMessages: StateFlow<Map<String, Message>>
}
