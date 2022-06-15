package io.getstream.chat.android.client.channel.state

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import java.util.Date

public interface ChannelStateLogic {

    public fun listerForChannelState(): ChannelState
    public fun incrementUnreadCountIfNecessary(message: Message)
    public fun updateChannelData(channel: Channel)
    public fun updateReads(reads: List<ChannelUserRead>)
    public fun updateRead(read: ChannelUserRead)
    public fun setTyping(userId: String, event: ChatEvent?)
    public fun setWatcherCount(watcherCount: Int)
    public fun setMembers(members: List<Member>)
    public fun setWatchers(watchers: List<User>)
    public fun upsertMessage(message: Message)
    public fun upsertMessages(messages: List<Message>)
    public fun removeMessagesBefore(date: Date, systemMessage: Message? = null)
    public fun removeLocalMessage(message: Message)
    public fun hideMessagesBefore(date: Date)
    public fun upsertMember(member: Member)
    public fun upsertMembers(members: List<Member>)
    public fun upsertOldMessages(messages: List<Message>)
    public fun deleteMember(userId: String)
    public fun upsertWatcher(user: User)
    public fun deleteWatcher(user: User)
    public fun setHidden(hidden: Boolean)
    public fun replyMessage(repliedMessage: Message?)
    public fun updateDataFromChannel(c: Channel)
    public fun updateOldMessagesFromChannel(c: Channel)
}
