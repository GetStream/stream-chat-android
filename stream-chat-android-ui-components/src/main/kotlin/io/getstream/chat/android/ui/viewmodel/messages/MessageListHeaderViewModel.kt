/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.viewmodel.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.chat.android.ui.feature.messages.header.MessageListHeaderView
import io.getstream.log.taggedLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/**
 * ViewModel class for [MessageListHeaderView].
 *
 * @param cid The CID of the current channel.
 * @param chatClient An instance of the low level chat client.
 * @param clientState Client state of SDK that contains information such as the current user and connection state.
 * such as the current user, connection state...
 * @param messageId The id of a message we wish to scroll to in messages list. Used to control the number of channel
 * queries executed on screen initialization.
 */
public class MessageListHeaderViewModel(
    private val cid: String,
    private val chatClient: ChatClient = ChatClient.instance(),
    clientState: ClientState = chatClient.clientState,
    private val messageId: String? = null,
) : ViewModel() {

    private val logger by taggedLogger("Chat:MessagesHeaderVM")

    /**
     * Holds information about the current channel and is actively updated.
     */
    private val channelState: Flow<ChannelState> = observeChannelState()

    /**
     * The current [Channel] created from [ChannelState]. It emits new data either when
     * channel data or the list of members in [ChannelState] updates.
     *
     * Combining the two is important because members changing online status does not result in
     * channel events being received.
     */
    public val channel: LiveData<Channel> =
        channelState.flatMapLatest { state ->
            combine(
                state.channelData,
                state.membersCount,
                state.watcherCount,
            ) { _, _, _ ->
                state.toChannel()
            }
        }.asLiveData()

    /**
     * A list of users who are currently typing.
     */
    public val typingUsers: LiveData<List<User>> =
        channelState.flatMapLatest { it.typing }.map { typingEvent ->
            typingEvent.users
        }.asLiveData()

    /**
     * A list of [Channel] members.
     */
    public val members: LiveData<List<Member>> = channelState.flatMapLatest { it.members }.asLiveData()

    /**
     * Number of [Channel] members.
     */
    public val membersCount: LiveData<Int?> = channelState.flatMapLatest { it.membersCount }.asLiveData()

    /**
     * Current user's online status.
     */
    public val online: LiveData<ConnectionState> = clientState.connectionState.asLiveData()

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

    private fun observeChannelState(): Flow<ChannelState> {
        val messageLimit = if (messageId != null) 0 else DEFAULT_MESSAGES_LIMIT
        logger.d { "[observeChannelState] cid: $cid, messageId: $messageId, messageLimit: $messageLimit" }
        return chatClient.watchChannelAsState(
            cid = cid,
            messageLimit = messageLimit,
            coroutineScope = viewModelScope,
        ).filterNotNull()
    }

    private companion object {

        /**
         * The default limit for messages that will be requested.
         */
        private const val DEFAULT_MESSAGES_LIMIT: Int = 30
    }
}
