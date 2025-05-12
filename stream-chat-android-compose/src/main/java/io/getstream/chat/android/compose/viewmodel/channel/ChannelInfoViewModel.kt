/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

@file:OptIn(InternalStreamChatApi::class)

package io.getstream.chat.android.compose.viewmodel.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewController
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing channel information and its actions.
 *
 * @param cid The full channel identifier (e.g., "messaging:123").
 */
@ExperimentalStreamChatApi
public class ChannelInfoViewModel(
    private val cid: String,
    controllerProvider: ViewModel.() -> ChannelInfoViewController = {
        ChannelInfoViewController(cid = cid, scope = viewModelScope)
    },
) : ViewModel() {

    private val controller: ChannelInfoViewController by lazy { controllerProvider() }

    /**
     * @see [ChannelInfoViewController.state]
     */
    public val state: StateFlow<ChannelInfoViewState> = controller.state

    /**
     * @see [ChannelInfoViewController.events]
     */
    public val events: SharedFlow<ChannelInfoViewEvent> = controller.events

    /**
     * @see [ChannelInfoViewController.expandMembers]
     */
    public fun expandMembers() {
        controller.expandMembers()
    }

    /**
     * @see [ChannelInfoViewController.collapseMembers]
     */
    public fun collapseMembers() {
        controller.collapseMembers()
    }

    /**
     * @see [ChannelInfoViewController.renameChannel]
     */
    public fun renameChannel(name: String) {
        controller.renameChannel(name)
    }

    /**
     * @see [ChannelInfoViewController.setChannelMute]
     */
    public fun setChannelMute(mute: Boolean) {
        controller.setChannelMute(mute)
    }

    /**
     * @see [ChannelInfoViewController.setChannelHide]
     */
    public fun setChannelHide(hide: Boolean, clearHistory: Boolean) {
        controller.setChannelHide(hide, clearHistory)
    }

    /**
     * @see [ChannelInfoViewController.leaveChannel]
     */
    public fun leaveChannel(quitMessage: Message?) {
        controller.leaveChannel(quitMessage)
    }

    /**
     * @see [ChannelInfoViewController.deleteChannel]
     */
    public fun deleteChannel() {
        controller.deleteChannel()
    }
}
