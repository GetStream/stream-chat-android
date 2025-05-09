package io.getstream.chat.android.compose.sample.feature.reminders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.ui.component.AppToolbar
import io.getstream.chat.android.compose.ui.components.EmptyContent
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Displays the message reminders screen.
 *
 * @param viewModel The ViewModel responsible for managing the state of the screen.
 */
@Composable
fun MessageRemindersScreen(viewModel: MessageRemindersViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            AppToolbar(
                title = "Message Reminders",
                onBack = { /* not handled */ },
                // TODO: Remove back button
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChatTheme.colors.appBackground)
                    .padding(padding)
            ) {
                MessageRemindersFilterSelection(
                    selectedFilter = state.filter,
                    onFilterChanged = viewModel::onFilterChanged,
                )
                when {
                    state.isLoading -> MessageRemindersLoadingContent()
                    state.reminders.isEmpty() -> MessageRemindersEmptyContent()
                    else -> TODO("List of reminders")
                }
            }
        }
    )
}

/**
 * Displays a loading content while the reminders are being fetched.
 */
@Composable
private fun MessageRemindersLoadingContent() {
    LoadingIndicator(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.appBackground)
    )
}

/**
 * Displays an empty content when there are no message reminders.
 */
@Composable
private fun MessageRemindersEmptyContent() {
    EmptyContent(
        text = stringResource(id = R.string.reminders_no_results),
        painter = painterResource(id = R.drawable.ic_bell_24),
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.appBackground)
            .padding(16.dp)
    )
}

/**
 * Displays a filter selection for message reminders.
 *
 * @param selectedFilter The currently selected filter.
 * @param onFilterChanged Callback invoked when the filter is changed.
 */
@Composable
private fun MessageRemindersFilterSelection(
    selectedFilter: MessageRemindersFilter,
    onFilterChanged: (MessageRemindersFilter) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(all = 16.dp)
    ) {
        items(MessageRemindersFilter.entries) {
            MessageReminderFilterItem(
                filter = it,
                isSelected = it == selectedFilter,
                onClick = { onFilterChanged(it) }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

/**
 * Displays a single filter item for message reminders.
 *
 * @param filter The filter to be displayed.
 * @param isSelected Indicates whether the filter is selected.
 * @param onClick Callback invoked when the filter is clicked.
 */
@Composable
private fun MessageReminderFilterItem(
    filter: MessageRemindersFilter,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    SuggestionChip(
        modifier = Modifier.height(32.dp),
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = if (isSelected) ChatTheme.colors.primaryAccent else ChatTheme.colors.appBackground,
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) null else BorderStroke(1.dp, ChatTheme.colors.borders),
        label = {
            Text(
                text = stringResource(filter.label),
                fontSize = 14.sp,
                color = if (isSelected) Color.White else ChatTheme.colors.textLowEmphasis,
            )
        },
        onClick = onClick,
    )
}

/**
 * Resolves the string resource ID for the filter label.
 */
private val MessageRemindersFilter.label: Int
    get() = when (this) {
        MessageRemindersFilter.ALL -> R.string.reminders_filter_all
        MessageRemindersFilter.OVERDUE -> R.string.reminders_filter_overdue
        MessageRemindersFilter.UPCOMING -> R.string.reminders_filter_upcoming
        MessageRemindersFilter.SCHEDULED -> R.string.reminders_filter_scheduled
        MessageRemindersFilter.SAVED_FOR_LATER -> R.string.reminders_filter_saved_for_later
    }
