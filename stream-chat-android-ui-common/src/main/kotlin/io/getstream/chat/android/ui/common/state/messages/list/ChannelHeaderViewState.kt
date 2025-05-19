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

package io.getstream.chat.android.ui.common.state.messages.list

import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.User

/**
 * Represents the state of the channel header in the UI.
 */
@ExperimentalStreamChatApi
public data class ChannelHeaderViewState(
    /**
     * The current connected user.
     */
    val currentUser: User? = null,

    /**
     * The connection state of the chat client.
     */
    val connectionState: ConnectionState = ConnectionState.Offline,

    /**
     * The channel associated with the message list.
     */
    val channel: Channel = Channel(),
)
