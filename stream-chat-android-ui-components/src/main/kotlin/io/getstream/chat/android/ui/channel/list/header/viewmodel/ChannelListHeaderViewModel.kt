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
 
package io.getstream.chat.android.ui.channel.list.header.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.extensions.globalState
import io.getstream.chat.android.offline.model.connection.ConnectionState
import io.getstream.chat.android.offline.plugin.state.global.GlobalState

/**
 * ViewModel class for [io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView].
 * Responsible for updating current user information.
 * Can be bound to the view using [ChannelListHeaderViewModel.bindView] function.
 *
 * @param globalState Global state of OfflinePlugin. Contains information
 * such as the current user, connection state, unread counts etc.
 */
public class ChannelListHeaderViewModel @JvmOverloads constructor(
    globalState: GlobalState = ChatClient.instance().globalState,
) : ViewModel() {

    /**
     * The user who is currently logged in.
     */
    public val currentUser: LiveData<User?> = globalState.user.asLiveData()

    /**
     * The state of the connection for the user currently logged in.
     */
    public val connectionState: LiveData<ConnectionState> = globalState.connectionState.asLiveData()
}
