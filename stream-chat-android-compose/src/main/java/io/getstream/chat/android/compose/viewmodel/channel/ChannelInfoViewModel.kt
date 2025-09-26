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
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewEvent
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewController
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent
import io.getstream.chat.android.ui.common.helper.CopyToClipboardHandler
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing channel information and its actions.
 *
 * @param cid The full channel identifier (e.g., "messaging:123").
 * @param copyToClipboardHandler The handler for copying text to the clipboard.
 * @param optionFilter A filter function for channel options, allowing customization of which options are displayed.
 *                      Defaults to a function that returns true for all options.
 * @param controllerProvider The provider for [ChannelInfoViewController].
 */
public class ChannelInfoViewModel(
    private val cid: String,
    private val copyToClipboardHandler: CopyToClipboardHandler,
    private val optionFilter: (option: ChannelInfoViewState.Content.Option) -> Boolean = { true },
    private val controllerProvider: ViewModel.() -> ChannelInfoViewController = {
        ChannelInfoViewController(
            cid = cid,
            scope = viewModelScope,
            copyToClipboardHandler = copyToClipboardHandler,
            optionFilter = optionFilter,
        )
    },
) : ViewModel() {

    private val controller: ChannelInfoViewController by lazy { controllerProvider() }

    /**
     * @see [ChannelInfoViewController.state]
     */
    public val state: StateFlow<ChannelInfoViewState> get() = controller.state

    /**
     * @see [ChannelInfoViewController.events]
     */
    public val events: SharedFlow<ChannelInfoViewEvent> get() = controller.events

    /**
     * @see [ChannelInfoViewController.onViewAction]
     */
    public fun onViewAction(action: ChannelInfoViewAction) {
        controller.onViewAction(action)
    }

    /**
     * @see [ChannelInfoViewController.onMemberViewEvent]
     */
    public fun onMemberViewEvent(event: ChannelInfoMemberViewEvent) {
        controller.onMemberViewEvent(event)
    }
}
