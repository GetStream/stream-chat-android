package io.getstream.chat.android.ui.message.list.header.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.extensions.globalState
import io.getstream.chat.android.offline.extensions.watchChannelAsState
import io.getstream.chat.android.offline.model.connection.ConnectionState
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * @param cid The CID of the current channel.
 * @param chatClient An instance of the low level chat client.
 * @param globalState Global state of OfflinePlugin. Contains information
 * such as the current user, connection state, unread counts etc.
 */
public class MessageListHeaderViewModel(
    cid: String,
    chatClient: ChatClient = ChatClient.instance(),
    globalState: GlobalState = chatClient.globalState,
) : ViewModel() {

    /**
     * Holds information about the current channel and is actively updated.
     */
    public val channelState: ChannelState =
        chatClient.watchChannelAsState(
            cid = cid,
            messageLimit = DEFAULT_MESSAGES_LIMIT,
            coroutineScope = viewModelScope
        )

    /**
     * The current [Channel] created from [ChannelState]. It emits new data either when
     * channel data or the list of members in [ChannelState] updates.
     *
     * Combining the two is important because members changing online status does not result in
     * channel events being received.
     */
    public val channel: LiveData<Channel> =
        channelState.channelData.combine(channelState.members) { _, _ ->
            channelState.toChannel()
        }.asLiveData()

    /**
     * A list of users who are currently typing.
     */
    public val typingUsers: LiveData<List<User>> =
        channelState.typing.map { typingEvent ->
            typingEvent.users
        }.asLiveData()

    /**
     * A list of [Channel] members.
     */
    public val members: LiveData<List<Member>> = channelState.members.asLiveData()

    /**
     * Current user's online status.
     */
    public val online: LiveData<ConnectionState> = globalState.connectionState.asLiveData()

    /**
     * Signals that we are currently in thread mode if the value is non-null.
     * If the value is null we are in normal mode.
     */
    private val _activeThread = MutableLiveData<Message?>()

    /**
     * Signals that we are currently in thread mode if the value is non-null.
     * If the value is null we are in normal mode.
     */
    public val activeThread: LiveData<Message?> = _activeThread

    /**
     * Sets thread mode.
     *
     * @param message The original message on which the thread is based on.
     */
    public fun setActiveThread(message: Message) {
        _activeThread.postValue(message)
    }

    /**
     *  Switches to normal (non-thread) mode.
     */
    public fun resetThread() {
        _activeThread.postValue(null)
    }

    private companion object {

        /**
         * The default limit for messages that will be requested.
         */
        private const val DEFAULT_MESSAGES_LIMIT: Int = 0
    }
}
