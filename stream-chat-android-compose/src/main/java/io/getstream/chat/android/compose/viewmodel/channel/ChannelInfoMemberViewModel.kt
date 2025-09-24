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

@file:OptIn(InternalStreamChatApi::class)

package io.getstream.chat.android.compose.viewmodel.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewController
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewEvent
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoMemberViewState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing channel member information and its actions.
 *
 * @param cid The full channel identifier (e.g., "messaging:123").
 * @param memberId The member ID of the user whose information is being managed.
 * @param controllerProvider The provider for [ChannelInfoMemberViewController].
 */
public class ChannelInfoMemberViewModel(
    private val cid: String,
    private val memberId: String,
    private val controllerProvider: ViewModel.() -> ChannelInfoMemberViewController = {
        ChannelInfoMemberViewController(
            cid = cid,
            memberId = memberId,
            scope = viewModelScope,
        )
    },
) : ViewModel() {

    private val controller: ChannelInfoMemberViewController get() = controllerProvider()

    /**
     * @see [ChannelInfoMemberViewController.state]
     */
    public val state: StateFlow<ChannelInfoMemberViewState> get() = controller.state

    /**
     * @see [ChannelInfoMemberViewController.events]
     */
    public val events: SharedFlow<ChannelInfoMemberViewEvent> get() = controller.events

    /**
     * @see [ChannelInfoMemberViewController.onViewAction]
     */
    public fun onViewAction(action: ChannelInfoMemberViewAction) {
        controller.onViewAction(action)
    }
}
