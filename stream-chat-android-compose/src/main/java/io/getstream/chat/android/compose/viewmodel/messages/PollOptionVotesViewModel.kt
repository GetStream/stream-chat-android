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

package io.getstream.chat.android.compose.viewmodel.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.ui.common.feature.messages.poll.PollOptionVotesViewAction
import io.getstream.chat.android.ui.common.feature.messages.poll.PollOptionVotesViewController
import io.getstream.chat.android.ui.common.feature.messages.poll.PollOptionVotesViewEvent
import io.getstream.chat.android.ui.common.state.messages.poll.PollOptionVotesViewState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing poll option votes state in Compose UI components.
 *
 * @param poll The poll for which the votes are displayed.
 * @param option The option for which the votes are displayed.
 * @param controllerProvider The provider for [PollOptionVotesViewController].
 */
internal class PollOptionVotesViewModel(
    poll: Poll,
    option: Option,
    controllerProvider: ViewModel.() -> PollOptionVotesViewController = {
        PollOptionVotesViewController(
            poll = poll,
            option = option,
            scope = viewModelScope,
        )
    },
) : ViewModel() {

    private val controller: PollOptionVotesViewController by lazy { controllerProvider() }

    /**
     * @see [PollOptionVotesViewController.state]
     */
    val state: StateFlow<PollOptionVotesViewState> = controller.state

    /**
     * @see [PollOptionVotesViewController.events]
     */
    val events: SharedFlow<PollOptionVotesViewEvent> = controller.events

    /**
     * @see [PollOptionVotesViewController.onViewAction]
     */
    fun onViewAction(action: PollOptionVotesViewAction) {
        controller.onViewAction(action)
    }
}
