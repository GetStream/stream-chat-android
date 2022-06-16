package io.getstream.chat.android.client.channel.state

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.ChannelData
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

//Todo: Change this name
public interface ChannelMutableStateInterface: ChannelState {

    public val _messages: MutableStateFlow<Map<String, Message>>
    public val _watcherCount: MutableStateFlow<Int>
    public val _typing: MutableStateFlow<Map<String, ChatEvent>>
    public val _reads: MutableStateFlow<Map<String, ChannelUserRead>>
    public val _read: MutableStateFlow<ChannelUserRead?>
    public val _endOfNewerMessages: MutableStateFlow<Boolean>
    public val _endOfOlderMessages: MutableStateFlow<Boolean>
    public val _loading: MutableStateFlow<Boolean>
    public val _hidden: MutableStateFlow<Boolean>
    public val _muted: MutableStateFlow<Boolean>
    public val _watchers: MutableStateFlow<Map<String, User>>
    public val _members: MutableStateFlow<Map<String, Member>>
    public val _loadingOlderMessages: MutableStateFlow<Boolean>
    public val _loadingNewerMessages: MutableStateFlow<Boolean>
    public val _channelData: MutableStateFlow<ChannelData?>
    public val _oldMessages: MutableStateFlow<Map<String, Message>>
    public val lastMessageAt: MutableStateFlow<Date?>
    public val _repliedMessage: MutableStateFlow<Message?>
    public val _unreadCount: MutableStateFlow<Int>
    public val _membersCount: MutableStateFlow<Int>

    /** Channel config data. */
    public val _channelConfig: MutableStateFlow<Config>

    public var hideMessagesBefore: Date?

    public val messageList: StateFlow<List<Message>>

}
