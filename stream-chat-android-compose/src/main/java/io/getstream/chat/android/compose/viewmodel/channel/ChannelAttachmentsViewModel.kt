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
import io.getstream.chat.android.ui.common.feature.channel.attachments.ChannelAttachmentsViewAction
import io.getstream.chat.android.ui.common.feature.channel.attachments.ChannelAttachmentsViewController
import io.getstream.chat.android.ui.common.feature.channel.attachments.ChannelAttachmentsViewEvent
import io.getstream.chat.android.ui.common.state.channel.attachments.ChannelAttachmentsViewState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing channel attachments and their actions.
 *
 * @param cid The full channel identifier (e.g., "messaging:123").
 * @param attachmentTypes The list of attachment types (e.g., "image", "file").
 * @param controllerProvider The provider for [ChannelAttachmentsViewController].
 */
public class ChannelAttachmentsViewModel(
    private val cid: String,
    private val attachmentTypes: List<String>,
    private val controllerProvider: ViewModel.() -> ChannelAttachmentsViewController = {
        ChannelAttachmentsViewController(
            cid = cid,
            attachmentTypes = attachmentTypes,
            scope = viewModelScope,
        )
    },
) : ViewModel() {

    private val controller: ChannelAttachmentsViewController get() = controllerProvider()

    /**
     * @see [ChannelAttachmentsViewController.state]
     */
    public val state: StateFlow<ChannelAttachmentsViewState> get() = controller.state

    /**
     * @see [ChannelAttachmentsViewController.events]
     */
    public val events: SharedFlow<ChannelAttachmentsViewEvent> get() = controller.events

    /**
     * @see [ChannelAttachmentsViewController.onViewAction]
     */
    public fun onViewAction(action: ChannelAttachmentsViewAction) {
        controller.onViewAction(action)
    }
}
