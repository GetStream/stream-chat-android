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

package io.getstream.chat.android.compose.sample.feature.channel.draft

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.feature.channel.draft.DraftChannelViewAction
import io.getstream.chat.android.ui.common.feature.channel.draft.DraftChannelViewController
import io.getstream.chat.android.ui.common.feature.channel.draft.DraftChannelViewEvent
import io.getstream.chat.android.ui.common.state.channel.draft.DraftChannelViewState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class DraftChannelViewModel(
    private val memberIds: List<String>,
    controllerProvider: ViewModel.() -> DraftChannelViewController = {
        DraftChannelViewController(
            memberIds = memberIds,
            scope = viewModelScope,
        )
    },
) : ViewModel() {

    private val controller: DraftChannelViewController by lazy { controllerProvider() }

    /**
     * @see [DraftChannelViewController.state]
     */
    val state: StateFlow<DraftChannelViewState> = controller.state

    /**
     * @see [DraftChannelViewController.events]
     */
    val events: SharedFlow<DraftChannelViewEvent> = controller.events

    /**
     * @see [DraftChannelViewController.onViewAction]
     */
    fun onViewAction(action: DraftChannelViewAction) {
        controller.onViewAction(action)
    }
}
