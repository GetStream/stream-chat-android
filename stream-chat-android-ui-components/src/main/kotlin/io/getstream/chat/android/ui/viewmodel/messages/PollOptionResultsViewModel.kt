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

package io.getstream.chat.android.ui.viewmodel.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.ui.common.feature.messages.poll.PollOptionResultsViewAction
import io.getstream.chat.android.ui.common.feature.messages.poll.PollOptionResultsViewController
import io.getstream.chat.android.ui.common.feature.messages.poll.PollOptionResultsViewEvent
import io.getstream.chat.android.ui.common.state.messages.poll.PollOptionResultsViewState
import io.getstream.chat.android.ui.utils.asSingleLiveEvent

/**
 * ViewModel for managing poll option results state in XML UI components.
 *
 * @param poll The poll for which the results are displayed.
 * @param option The option for which the results are displayed.
 * @param controllerProvider The provider for [PollOptionResultsViewController].
 */
internal class PollOptionResultsViewModel(
    poll: Poll,
    option: Option,
    controllerProvider: ViewModel.() -> PollOptionResultsViewController = {
        PollOptionResultsViewController(
            poll = poll,
            option = option,
            scope = viewModelScope,
        )
    },
) : ViewModel() {

    private val controller: PollOptionResultsViewController by lazy { controllerProvider() }

    /**
     * @see [PollOptionResultsViewController.state]
     */
    val state: LiveData<PollOptionResultsViewState> = controller.state.asLiveData()

    /**
     * @see [PollOptionResultsViewController.events]
     */
    val events: LiveData<PollOptionResultsViewEvent> = controller.events.asSingleLiveEvent()

    /**
     * @see [PollOptionResultsViewController.onViewAction]
     */
    fun onViewAction(action: PollOptionResultsViewAction) {
        controller.onViewAction(action)
    }

    /**
     * Factory for creating [PollOptionResultsViewModel] instances.
     */
    class Factory(
        private val poll: Poll,
        private val option: Option,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == PollOptionResultsViewModel::class.java) {
                "Factory can only create instances of PollOptionResultsViewModel"
            }
            return PollOptionResultsViewModel(poll, option) as T
        }
    }
}
