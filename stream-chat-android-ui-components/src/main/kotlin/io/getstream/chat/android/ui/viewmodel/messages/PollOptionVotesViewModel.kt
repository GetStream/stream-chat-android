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

package io.getstream.chat.android.ui.viewmodel.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.ui.common.feature.messages.poll.PollOptionVotesViewAction
import io.getstream.chat.android.ui.common.feature.messages.poll.PollOptionVotesViewController
import io.getstream.chat.android.ui.common.feature.messages.poll.PollOptionVotesViewEvent
import io.getstream.chat.android.ui.common.state.messages.poll.PollOptionVotesViewState
import io.getstream.chat.android.ui.utils.asSingleLiveEvent

/**
 * ViewModel for managing poll option votes state in XML UI components.
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
    val state: LiveData<PollOptionVotesViewState> = controller.state.asLiveData()

    /**
     * @see [PollOptionVotesViewController.events]
     */
    val events: LiveData<PollOptionVotesViewEvent> = controller.events.asSingleLiveEvent()

    /**
     * @see [PollOptionVotesViewController.onViewAction]
     */
    fun onViewAction(action: PollOptionVotesViewAction) {
        controller.onViewAction(action)
    }

    /**
     * Factory for creating [PollOptionVotesViewModel] instances.
     */
    class Factory(
        private val poll: Poll,
        private val option: Option,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == PollOptionVotesViewModel::class.java) {
                "Factory can only create instances of PollOptionVotesViewModel"
            }
            return PollOptionVotesViewModel(poll, option) as T
        }
    }
}
