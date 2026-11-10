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

package io.getstream.chat.android.compose.viewmodel.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.ui.common.feature.messages.poll.PollAnswersViewAction
import io.getstream.chat.android.ui.common.feature.messages.poll.PollAnswersViewController
import io.getstream.chat.android.ui.common.feature.messages.poll.PollAnswersViewEvent
import io.getstream.chat.android.ui.common.state.messages.poll.PollAnswersViewState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

internal class PollAnswersViewModel(
    poll: Poll,
    controllerProvider: PollAnswersViewModel.() -> PollAnswersViewController = {
        PollAnswersViewController(
            pollId = poll.id,
            scope = viewModelScope,
            cachedAnswers = { latestPoll.answers },
        )
    },
) : ViewModel() {

    /**
     * The latest version of the poll, kept current by the UI. Its answers are shown if the first page fails to load.
     */
    var latestPoll: Poll = poll

    private val controller: PollAnswersViewController by lazy { controllerProvider() }

    /**
     * @see [PollAnswersViewController.state]
     */
    val state: StateFlow<PollAnswersViewState> = controller.state

    /**
     * @see [PollAnswersViewController.events]
     */
    val events: SharedFlow<PollAnswersViewEvent> = controller.events

    /**
     * @see [PollAnswersViewController.onViewAction]
     */
    fun onViewAction(action: PollAnswersViewAction) {
        controller.onViewAction(action)
    }
}
