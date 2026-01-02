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

package io.getstream.chat.android.compose.viewmodel.threads

import androidx.lifecycle.ViewModel
import io.getstream.chat.android.ui.common.feature.threads.ThreadListController
import io.getstream.chat.android.ui.common.state.threads.ThreadListState
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel responsible for managing the state of a threads list.
 *
 * @param controller The [ThreadListController] handling the business logic and the state management for the threads
 * list.
 */
public class ThreadListViewModel(private val controller: ThreadListController) : ViewModel() {

    /**
     * The current thread list state.
     */
    public val state: StateFlow<ThreadListState> = controller.state

    /**
     * Loads the initial data when requested.
     * Overrides all previously retrieved data.
     */
    public fun load() {
        controller.load()
    }

    /**
     * Loads more data when requested.
     *
     * Does nothing if the end of the list has already been reached or loading is already in progress.
     */
    public fun loadNextPage() {
        controller.loadNextPage()
    }
}
