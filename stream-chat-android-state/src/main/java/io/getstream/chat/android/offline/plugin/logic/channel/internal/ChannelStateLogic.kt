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

package io.getstream.chat.android.offline.plugin.logic.channel.internal

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.channel.internal.ChannelMutableState
import java.util.Date

@Suppress("TooManyFunctions")
/**
 * The logic of the state of a channel. This class contains the logic of how to
 * update the state of the channel in the SDK.
 */
internal interface ChannelStateLogic {

    /**
     * Return [ChannelState] representing the state of the channel. Use this when you would like to
     * keep track of the state without changing it.
     */
    fun listenForChannelState(): ChannelState

    /**
     * Return [ChannelState] representing the state of the channel. Use this when you would like to
     * keep track of the state and would like to write a new state too.
     */
    fun writeChannelState(): ChannelMutableState

    /**
     * Increments the unread count of the Channel if necessary.
     *
     * @param message [Message].
     */
    fun incrementUnreadCountIfNecessary(message: Message)

    /**
     * Updates the channel data of the state of the SDK.
     *
     * @param channel the data of [Channel] to be updated.
     */
    fun updateChannelData(channel: Channel)

    /**
     * Updates the read information of this channel.
     *
     * @param reads the information about the read.
     */
    fun updateReads(reads: List<ChannelUserRead>)

    /**
     * Updates the read information of this channel.
     *
     * @param read the information about the read.
     */
    fun updateRead(read: ChannelUserRead)

    /**
     * Sets the typing status for a user in the channel.
     *
     * @param userId the id of the user
     * @param event the event of typing.
     */
    fun setTyping(userId: String, event: TypingStartEvent?)

    /**
     * Sets the watcher count for the channel.
     *
     * @param watcherCount the count of watchers.
     */
    fun setWatcherCount(watcherCount: Int)

    /**
     * Sets the members of the channel.
     */
    fun setMembers(members: List<Member>)

    /**
     * Sets the watchers of the channel.
     *
     * @param watchers the [User] to be added or updated
     */
    fun setWatchers(watchers: List<User>)

    /**
     * Upsert members in the channel.
     *
     * @param message The message to be added or updated.
     */
    fun upsertMessage(message: Message)

    /**
     * Upsert members in the channel.
     *
     * @param messages the list of [Message] to be upserted
     * @param shouldRefreshMessages if the current messages should be removed or not and only
     * new messages should be kept.
     */
    fun upsertMessages(messages: List<Message>, shouldRefreshMessages: Boolean = false)

    /**
     * Removes messages before a certain date
     *
     * @param date all messages will be removed before this date.
     * @param systemMessage the system message to be added to inform the user.
     */
    fun removeMessagesBefore(date: Date, systemMessage: Message? = null)

    /**
     * Removes local messages. Doesn't remove message in database.
     *
     * @param message The [Message] to be deleted.
     */
    fun removeLocalMessage(message: Message)

    /**
     * Hides the messages created before the given date.
     *
     * @param date The date used for generating result.
     */
    fun hideMessagesBefore(date: Date)

    /**
     * Upsert member in the channel.
     *
     * @param member the member to be upserted.
     */
    fun upsertMember(member: Member)

    /**
     * Upsert members in the channel.
     *
     * @param members list of members to be upserted.
     */
    fun upsertMembers(members: List<Member>)

    /**
     * Upsert old messages.
     *
     * @param messages The list of messages to be upserted.
     */
    fun upsertOldMessages(messages: List<Message>)

    /**
     * Deletes a member. Doesn't delete in the database.
     *
     * @param member The member to be removed.
     */
    fun deleteMember(member: Member)

    /**
     * Deletes channel.
     *
     * @param deleteDate The date when the channel was deleted.
     */
    fun deleteChannel(deleteDate: Date)

    /**
     * Upsert watcher.
     *
     * @param user [User]
     */
    fun upsertWatcher(user: User)

    /**
     * Removes watcher.
     *
     * @param user [User]
     */
    fun deleteWatcher(user: User)

    /**
     * Deletes a message for the channel
     *
     * @param message [Message]
     */
    fun deleteMessage(message: Message)

    /**
     * Sets channel as hidden.
     *
     * @param hidden Boolean.
     */
    fun toggleHidden(hidden: Boolean)

    /**
     * Sets a replied message.
     *
     * @param repliedMessage The message that contains the reply.
     */
    fun replyMessage(repliedMessage: Message?)

    /**
     * Sets the channels as muted or unmuted.
     *
     * @param isMuted
     */
    fun updateMute(isMuted: Boolean)

    /**
     * Updates data from channel.
     *
     * @param channel [Channel]
     * @param shouldRefreshMessages If true, removed the current messages and only new messages are kept.
     * @param scrollUpdate Notifies that this is a scroll update. Only scroll updates will be accepted
     * when the user is searching in the channel.
     */
    fun updateDataFromChannel(
        channel: Channel,
        shouldRefreshMessages: Boolean = false,
        scrollUpdate: Boolean = false,
    )

    /**
     * Update the old messages for channel. It doesn't add new messages.
     *
     * @param c [Channel] the channel containing the data to be updated.
     */
    fun updateOldMessagesFromChannel(c: Channel)

    /**
     * Propagates the channel query. The data of the channel will be propagated to the SDK.
     *
     * @param channel [Channel]
     * @param request [QueryChannelRequest]
     */
    fun propagateChannelQuery(channel: Channel, request: QueryChannelRequest)

    /**
     * Propagates the error in a query.
     *
     * @param error [ChatError]
     */
    fun propagateQueryError(error: ChatError)

    /**
     * Refreshes the mute state for the channel
     */
    fun refreshMuteState()
}
