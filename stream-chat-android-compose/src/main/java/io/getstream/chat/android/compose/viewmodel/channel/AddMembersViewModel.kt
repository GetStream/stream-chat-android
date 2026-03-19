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

@file:OptIn(InternalStreamChatApi::class)

package io.getstream.chat.android.compose.viewmodel.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.feature.channel.info.AddMembersViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.AddMembersViewController
import io.getstream.chat.android.ui.common.feature.channel.info.AddMembersViewEvent
import io.getstream.chat.android.ui.common.state.channel.info.AddMembersViewState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing the "Add Members" view and its actions.
 *
 * @param cid The full channel identifier (e.g., "messaging:123").
 * @param controllerProvider The provider for [AddMembersViewController].
 */
public class AddMembersViewModel(
    private val cid: String,
    private val controllerProvider: ViewModel.() -> AddMembersViewController = {
        AddMembersViewController(
            cid = cid,
            scope = viewModelScope,
        )
    },
) : ViewModel() {

    private val controller: AddMembersViewController by lazy { controllerProvider() }

    /**
     * @see [AddMembersViewController.state]
     */
    public val state: StateFlow<AddMembersViewState> get() = controller.state

    /**
     * @see [AddMembersViewController.events]
     */
    public val events: SharedFlow<AddMembersViewEvent> get() = controller.events

    /**
     * @see [AddMembersViewController.onViewAction]
     */
    public fun onViewAction(action: AddMembersViewAction) {
        controller.onViewAction(action)
    }
}
