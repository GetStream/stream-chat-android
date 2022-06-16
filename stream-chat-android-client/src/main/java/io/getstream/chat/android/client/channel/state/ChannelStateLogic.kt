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
public interface ChannelStateLogic {

    public fun listerForChannelState(): ChannelState

    /**
     * Kdocs here.
     */
    public fun writeChannelState(): ChannelMutableState

    /**
     * Kdocs here.
     */
    public fun incrementUnreadCountIfNecessary(message: Message)

    /**
     * Kdocs here.
     */
    public fun updateChannelData(channel: Channel)

    /**
     * Kdocs here.
     */
    public fun updateReads(reads: List<ChannelUserRead>)

    /**
     * Kdocs here.
     */
    public fun updateRead(read: ChannelUserRead)

    /**
     * Kdocs here.
     */
    public fun setTyping(userId: String, event: ChatEvent?)

    /**
     * Kdocs here.
     */
    public fun setWatcherCount(watcherCount: Int)

    /**
     * Kdocs here.
     */
    public fun setMembers(members: List<Member>)

    /**
     * Kdocs here.
     */
    public fun setWatchers(watchers: List<User>)

    /**
     * Kdocs here.
     */
    public fun upsertMessage(message: Message)

    /**
     * Kdocs here.
     */
    public fun upsertMessages(messages: List<Message>)

    /**
     * Kdocs here.
     */
    public fun removeMessagesBefore(date: Date, systemMessage: Message? = null)

    /**
     * Kdocs here.
     */
    public fun removeLocalMessage(message: Message)

    /**
     * Kdocs here.
     */
    public fun hideMessagesBefore(date: Date)

    /**
     * Kdocs here.
     */
    public fun upsertMember(member: Member)

    /**
     * Kdocs here.
     */
    public fun upsertMembers(members: List<Member>)

    /**
     * Kdocs here.
     */
    public fun upsertOldMessages(messages: List<Message>)

    /**
     * Kdocs here.
     */
    public fun deleteMember(userId: String)

    public fun deleteChannel(deleteDate: Date)

    /**
     * Kdocs here.
     */
    public fun upsertWatcher(user: User)

    /**
     * Kdocs here.
     */
    public fun deleteWatcher(user: User)

    /**
     * Kdocs here.
     */
    public fun setHidden(hidden: Boolean)

    /**
     * Kdocs here.
     */
    public fun replyMessage(repliedMessage: Message?)

    /**
     * Kdocs here.
     */
    public fun updateDataFromChannel(c: Channel)

    /**
     * Kdocs here.
     */
    public fun updateOldMessagesFromChannel(c: Channel)

    /**
     * Kdocs here.
     */
    public fun propagateQueryError(error: ChatError)
    /**
     * Kdocs here.
     */
}
