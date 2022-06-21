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

package io.getstream.chat.android.client.channel.state

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import java.util.Date

@Suppress("TooManyFunctions")
/**
 * The logic of the state of a channel. This class contains the logic of how to
 * update the state of the channel in the SDK.
 */
public interface ChannelStateLogic {

    /**
     * Return [ChannelState] representing the state of the channel. Use this when you would like to
     * keep track of the state without changing it.
     */
    public fun listenForChannelState(): ChannelState

    /**
     * Return [ChannelState] representing the state of the channel. Use this when you would like to
     * keep track of the state and would like to write a new state too.
     */
    public fun writeChannelState(): ChannelMutableState

    /**
     * Increments the unread count of the Channel if necessary.
     *
     * @param message [Message].
     */
    public fun incrementUnreadCountIfNecessary(message: Message)

    /**
     * Updates the channel data of the state of the SDK.
     *
     * @param channel the data of [Channel] to be updated.
     */
    public fun updateChannelData(channel: Channel)

    /**
     * Updates the read information of this channel.
     *
     * @param reads the information about the read.
     */
    public fun updateReads(reads: List<ChannelUserRead>)

    /**
     * Updates the read information of this channel.
     *
     * @param read the information about the read.
     */
    public fun updateRead(read: ChannelUserRead)

    /**
     * Sets the typing status for a user in the channel.
     *
     * @param userId the id of the user
     * @param event the event of typing.
     */
    public fun setTyping(userId: String, event: ChatEvent?)

    /**
     * Sets the watcher count for the channel.
     *
     * @param watcherCount the count of watchers.
     */
    public fun setWatcherCount(watcherCount: Int)

    /**
     * Sets the members of the channel.
     */
    public fun setMembers(members: List<Member>)

    /**
     * Sets the watchers of the channel.
     *
     * @param watchers the [User] to be added or updated
     */
    public fun setWatchers(watchers: List<User>)

    /**
     * Upsert members in the channel.
     *
     * @param message The message to be added or updated.
     */
    public fun upsertMessage(message: Message)

    /**
     * Upsert members in the channel.
     *
     * @param messages the list of [Message] to be upserted
     * @param shouldRefreshMessages if the current messages should be removed or not and only
     * new messages should be kept.
     */
    public fun upsertMessages(messages: List<Message>, shouldRefreshMessages: Boolean = false)

    /**
     * Removes messages before a certain date
     *
     * @param date all messages will be removed before this date.
     * @param systemMessage the system message to be added to inform the user.
     */
    public fun removeMessagesBefore(date: Date, systemMessage: Message? = null)

    /**
     * Removes local messages. Doesn't remove message in database.
     *
     * @param message The [Message] to be deleted.
     */
    public fun removeLocalMessage(message: Message)

    /**
     * Hides the messages created before the given date.
     *
     * @param date The date used for generating result.
     */
    public fun hideMessagesBefore(date: Date)

    /**
     * Upsert member in the channel.
     *
     * @param member the member to be upserted.
     */
    public fun upsertMember(member: Member)

    /**
     * Upsert members in the channel.
     *
     * @param members list of members to be upserted.
     */
    public fun upsertMembers(members: List<Member>)

    /**
     * Upsert old messages.
     *
     * @param messages The list of messages to be upserted.
     */
    public fun upsertOldMessages(messages: List<Message>)

    /**
     * Deletes a member. Doesn't delete in the database.
     *
     * @param userId Id of the user.
     */
    public fun deleteMember(userId: String)

    /**
     * Deletes channel.
     *
     * @param deleteDate The date when the channel was deleted.
     */
    public fun deleteChannel(deleteDate: Date)

    /**
     * Upsert watcher.
     *
     * @param user [User]
     */
    public fun upsertWatcher(user: User)

    /**
     * Removes watcher.
     *
     * @param user [User]
     */
    public fun deleteWatcher(user: User)

    /**
     * Sets channel as hidden.
     *
     * @param hidden Boolean.
     */
    public fun setHidden(hidden: Boolean)

    /**
     * Sets a replied message.
     *
     * @param repliedMessage The message that contains the reply.
     */
    public fun replyMessage(repliedMessage: Message?)

    /**
     * Updates data from channel.
     *
     * @param c [Channel]
     * @param shouldRefreshMessages If true, removed the current messages and only new messages are kept.
     * @param scrollUpdate Notifies that this is a scroll update. Only scroll updates will be accepted
     * when the user is searching in the channel.
     */
    public fun updateDataFromChannel(
        c: Channel,
        shouldRefreshMessages: Boolean = false,
        scrollUpdate: Boolean = false,
    )

    /**
     * Update the old messages for channel. It doesn't add new messages.
     *
     * @param c [Channel] the channel containing the data to be updated.
     */
    public fun updateOldMessagesFromChannel(c: Channel)

    /**
     * Propagates the error in a query.
     *
     * @param error [ChatError]
     */
    public fun propagateQueryError(error: ChatError)
}
