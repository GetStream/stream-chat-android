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

package io.getstream.chat.android.compose.sample.feature.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.MessageReminder
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel responsible for managing the state of the message reminders screen.
 *
 * @property client The ChatClient instance used for API calls.
 */
class MessageRemindersViewModel(
    private val client: ChatClient = ChatClient.instance(),
) : ViewModel() {

    /**
     * The current state of the message reminders screen.
     */
    private val _state: MutableStateFlow<MessageRemindersViewState> = MutableStateFlow(
        MessageRemindersViewState(
            filter = MessageRemindersFilter.ALL,
            reminders = emptyList(),
            isLoading = true,
            isLoadingMore = false,
        ),
    )
    val state: StateFlow<MessageRemindersViewState>
        get() = _state

    /**
     * Event triggered when an error occurs while managing message reminders.
     */
    private val _errorEvent: MutableSharedFlow<MessageRemindersError> = MutableSharedFlow(extraBufferCapacity = 1)
    val errorEvent: Flow<MessageRemindersError>
        get() = _errorEvent

    private var next: String? = null

    init {
        // Load the initial reminders when the ViewModel is created
        queryRemainders(_state.value.filter)
    }

    /**
     * Called when the filter for the message reminders is changed.
     *
     * @param filter The new filter to be applied.
     */
    fun onFilterChanged(filter: MessageRemindersFilter) {
        if (_state.value.filter == filter) return
        // Reset the pagination cursor to null when the filter changes
        next = null
        _state.update {
            it.copy(
                filter = filter,
                reminders = emptyList(),
                isLoading = true,
                isLoadingMore = false,
            )
        }
        // Query the reminders with the new filter
        queryRemainders(filter)
    }

    /**
     * Called when the end of the list is reached, indicating that more reminders should be loaded.
     */
    fun onEndReached() {
        if (next == null) return
        _state.update { it.copy(isLoadingMore = true) }
        queryRemainders(_state.value.filter)
    }

    /**
     * Called when the user wants to edit a reminder.
     *
     * @param id The ID of the reminder to be edited.
     * @param remindAt The new date and time to set for the reminder.
     */
    fun onEditReminder(id: String, remindAt: Date?) {
        viewModelScope.launch {
            val result = client.updateReminder(id, remindAt).await()
            when (result) {
                is Result.Success -> {
                    // Update the reminder in the state
                    _state.update {
                        it.copy(
                            reminders = it.reminders.map { reminder ->
                                if (reminder.id == id) {
                                    reminder.copy(remindAt = remindAt)
                                } else {
                                    reminder
                                }
                            },
                        )
                    }
                }

                is Result.Failure -> {
                    _errorEvent.tryEmit(MessageRemindersError.UPDATE_FAILED)
                }
            }
        }
    }

    /**
     * Called when the user wants to delete a reminder.
     *
     * @param id The ID of the reminder to be deleted.
     */
    fun onDeleteReminder(id: String) {
        viewModelScope.launch {
            val result = client.deleteReminder(id).await()
            when (result) {
                is Result.Success -> {
                    // Remove the deleted reminder from the state
                    _state.update {
                        it.copy(reminders = it.reminders.filter { reminder -> reminder.id != id })
                    }
                }

                is Result.Failure -> {
                    _errorEvent.tryEmit(MessageRemindersError.DELETE_FAILED)
                }
            }
        }
    }

    private fun queryRemainders(filter: MessageRemindersFilter) {
        viewModelScope.launch {
            val result = client.queryReminders(
                filter = queryFilter(filter),
                limit = 25,
                sort = querySort(filter),
                next = next,
            ).await()
            when (result) {
                is Result.Success -> {
                    next = result.value.next
                    _state.update {
                        it.copy(
                            reminders = it.reminders + result.value.reminders,
                            isLoading = false,
                            isLoadingMore = false,
                        )
                    }
                }

                is Result.Failure -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                        )
                    }
                    _errorEvent.tryEmit(MessageRemindersError.QUERY_FAILED)
                }
            }
        }
    }

    private fun queryFilter(filter: MessageRemindersFilter): FilterObject = when (filter) {
        MessageRemindersFilter.ALL -> Filters.neutral()
        MessageRemindersFilter.OVERDUE -> Filters.lessThanEquals("remind_at", Date())
        MessageRemindersFilter.UPCOMING -> Filters.greaterThan("remind_at", Date())
        MessageRemindersFilter.SCHEDULED -> Filters.exists("remind_at")
        MessageRemindersFilter.SAVED_FOR_LATER -> Filters.notExists("remind_at")
    }

    private fun querySort(filter: MessageRemindersFilter): QuerySortByField<MessageReminder> = when (filter) {
        MessageRemindersFilter.ALL,
        MessageRemindersFilter.UPCOMING,
        MessageRemindersFilter.SCHEDULED,
        -> QuerySortByField.ascByName("remind_at")
        MessageRemindersFilter.OVERDUE,
        MessageRemindersFilter.SAVED_FOR_LATER,
        -> QuerySortByField.descByName("remind_at")
    }
}

/**
 * Represents the state of the message reminders screen.
 *
 * @property filter The current filter applied to the message reminders.
 * @property reminders The list of message reminders to be displayed.
 * @property isLoading Indicates whether the reminders are currently being loaded.
 * @property isLoadingMore Indicates whether more reminders are being loaded.
 */
data class MessageRemindersViewState(
    val filter: MessageRemindersFilter,
    val reminders: List<MessageReminder>,
    val isLoading: Boolean,
    val isLoadingMore: Boolean,
)

/**
 * Represents the different filters available for message reminders.
 */
enum class MessageRemindersFilter {
    ALL,
    OVERDUE,
    UPCOMING,
    SCHEDULED,
    SAVED_FOR_LATER,
}

/**
 * Represents the different error states that can occur while managing message reminders.
 */
enum class MessageRemindersError {
    QUERY_FAILED,
    UPDATE_FAILED,
    DELETE_FAILED,
}
