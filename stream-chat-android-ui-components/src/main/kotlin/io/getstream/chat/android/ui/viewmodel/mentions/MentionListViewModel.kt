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

package io.getstream.chat.android.ui.viewmodel.mentions

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.state.utils.Event
import io.getstream.chat.android.ui.common.feature.mentions.MentionListController
import io.getstream.chat.android.ui.common.state.mentions.MentionListState
import kotlinx.coroutines.flow.map

/**
 * ViewModel class responsible for managing the [MentionListState] and actions of mention list.
 *
 * @param sort The sorting options for the messages.
 * @param controllerProvider The provider for [MentionListController]
 * which handles the business logic of the mention list.
 */
public class MentionListViewModel(
    sort: QuerySorter<Message>? = null,
    controllerProvider: ViewModel.() -> MentionListController = { MentionListController(viewModelScope, sort) },
) : ViewModel() {
    private val controller: MentionListController by lazy { controllerProvider() }

    /**
     * The current mention list state.
     */
    public val state: LiveData<MentionListState> = controller.state.asLiveData()

    /**
     * One shot error events.
     */
    public val errorEvents: LiveData<Event<Unit>> = controller.events.map { Event(Unit) }.asLiveData()

    /**
     * Loads more mentions if there are more to load.
     */
    public fun loadMore() {
        controller.loadMore()
    }
}
