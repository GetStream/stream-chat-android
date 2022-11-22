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

package io.getstream.chat.android.ui.viewmodel.typing

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.chat.android.ui.widgets.typing.TypingIndicatorView
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/**
 * ViewModel used by [TypingIndicatorView].
 * It is responsible for updating the state of users who are currently typing.
 *
 * @param cid The full channel id, i.e. "messaging:123".
 * @param chatClient The main entry point for all low-level chat operations.
 * @param messageId The id of a message we wish to scroll to in messages list. Used to control the number of channel
 * queries executed on screen initialization.
 */
public class TypingIndicatorViewModel(
    cid: String,
    chatClient: ChatClient = ChatClient.instance(),
    messageId: String? = null,
) : ViewModel() {

    /**
     * Holds information about the current channel and is actively updated.
     */
    private val channelState: StateFlow<ChannelState?> =
        chatClient.watchChannelAsState(
            cid = cid,
            messageLimit = if (messageId != null) 0 else DEFAULT_MESSAGES_LIMIT,
            coroutineScope = viewModelScope,
        )

    /**
     * A list of users who are currently typing.
     */
    public val typingUsers: LiveData<List<User>> =
        channelState.filterNotNull().flatMapLatest { it.typing }.map { typingEvent ->
            typingEvent.users
        }.asLiveData()

    private companion object {

        /**
         * The default limit for messages that will be requested.
         */
        private const val DEFAULT_MESSAGES_LIMIT: Int = 30
    }
}
