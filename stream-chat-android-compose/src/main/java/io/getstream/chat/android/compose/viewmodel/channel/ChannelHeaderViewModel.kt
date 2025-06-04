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

package io.getstream.chat.android.compose.viewmodel.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.common.feature.channel.header.ChannelHeaderViewController
import io.getstream.chat.android.ui.common.state.messages.list.ChannelHeaderViewState
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing the state of the channel header.
 *
 * @param cid The full channel identifier (e.g., "messaging:123").
 * @param controllerProvider The provider for [ChannelHeaderViewController].
 */
@ExperimentalStreamChatApi
public class ChannelHeaderViewModel(
    private val cid: String,
    controllerProvider: ViewModel.() -> ChannelHeaderViewController = {
        ChannelHeaderViewController(cid = cid, scope = viewModelScope)
    },
) : ViewModel() {

    private val controller: ChannelHeaderViewController by lazy { controllerProvider() }

    /**
     * @see [ChannelHeaderViewController.state]
     */
    public val state: StateFlow<ChannelHeaderViewState> = controller.state
}
