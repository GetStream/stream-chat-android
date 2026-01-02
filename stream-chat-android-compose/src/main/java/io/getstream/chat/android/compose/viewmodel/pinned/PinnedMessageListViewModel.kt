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

package io.getstream.chat.android.compose.viewmodel.pinned

import androidx.lifecycle.ViewModel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.feature.pinned.PinnedMessageListController
import io.getstream.chat.android.ui.common.state.pinned.PinnedMessageListState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel responsible for providing pinned messages in the channel.
 * Pinned messages are provided in a descending order based on [Message.pinnedAt].
 *
 * @param controller the [PinnedMessageListController] handling the business logic and the state management for the
 * pinned message list.
 */
public class PinnedMessageListViewModel(private val controller: PinnedMessageListController) : ViewModel() {

    /**
     * The current pinned messages' state.
     */
    public val state: StateFlow<PinnedMessageListState> = controller.state

    /**
     * One shot error events when a query fails.
     */
    public val errorEvents: SharedFlow<Unit> = controller.errorEvents

    init {
        controller.load()
    }

    /**
     * Loads more data when requested.
     *
     * Does nothing if the end of the list has already been reached or loading is already in progress.
     */
    public fun loadMore() {
        controller.loadMore()
    }
}
