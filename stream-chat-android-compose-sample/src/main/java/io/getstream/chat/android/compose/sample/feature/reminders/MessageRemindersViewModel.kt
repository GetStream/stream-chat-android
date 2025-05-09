package io.getstream.chat.android.compose.sample.feature.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.MessageReminder
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.result.Result
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
        )
    )
    val state: StateFlow<MessageRemindersViewState>
        get() = _state

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
    fun onEndOfListReached() {
        if (next == null) return
        _state.update { it.copy(isLoadingMore = true) }
        queryRemainders(_state.value.filter)
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
                    // TODO: Handle error
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