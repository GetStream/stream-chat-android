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
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.ui.common.feature.messages.poll.PollResultsViewController
import io.getstream.chat.android.ui.common.state.messages.poll.PollResultsViewState
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing poll results state in Compose UI components.
 *
 * @param poll The poll to display results for.
 * @param controllerProvider The provider for [PollResultsViewController].
 */
public class PollResultsViewModel(
    poll: Poll,
    controllerProvider: ViewModel.() -> PollResultsViewController = {
        PollResultsViewController(
            poll = poll,
        )
    },
) : ViewModel() {

    private val controller: PollResultsViewController by lazy { controllerProvider() }

    /**
     * @see [PollResultsViewController.state]
     */
    public val state: StateFlow<PollResultsViewState> = controller.state
}
