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

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.handlers.LoadMoreHandler
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.ui.component.AppToolbar
import io.getstream.chat.android.compose.ui.components.EmptyContent
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageReminder
import io.getstream.chat.android.models.User
import kotlinx.coroutines.flow.collectLatest
import java.util.Date
import kotlin.math.abs
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.toDuration

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
                showBack = false,
                onBack = { /* not handled */ },
            )
        },
        content = { padding ->
            var selectedReminder by remember { mutableStateOf<MessageReminder?>(null) }
            var showEditReminderOptions by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChatTheme.colors.appBackground)
                    .padding(padding),
            ) {
                MessageRemindersFilterSelection(
                    selectedFilter = state.filter,
                    onFilterChanged = viewModel::onFilterChanged,
                )
                when {
                    state.isLoading -> MessageRemindersLoadingContent()
                    state.reminders.isEmpty() -> MessageRemindersEmptyContent(state.filter)
                    else -> MessageRemindersResultList(
                        reminders = state.reminders,
                        isLoadingMore = state.isLoadingMore,
                        onEndReached = viewModel::onEndReached,
                        onReminderClick = {},
                        onReminderLongClick = { selectedReminder = it },
                    )
                }
            }
            selectedReminder?.let { reminder ->
                ReminderOptionsDialog(
                    onEdit = { showEditReminderOptions = true },
                    onDelete = {
                        viewModel.onDeleteReminder(reminder.id)
                        selectedReminder = null
                    },
                    onDismiss = { selectedReminder = null },
                )
                if (showEditReminderOptions) {
                    EditReminderDialog(
                        reminder = reminder,
                        onRemindAtSelected = { remindAt ->
                            viewModel.onEditReminder(reminder.id, remindAt)
                            selectedReminder = null
                            showEditReminderOptions = false
                        },
                        onDismiss = {
                            selectedReminder = null
                            showEditReminderOptions = false
                        },
                    )
                }
            }
        },
    )
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.errorEvent.collectLatest {
            val errorMessage = context.getString(it.label)
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Displays a loading content while the reminders are being fetched.
 */
@Composable
private fun MessageRemindersLoadingContent() {
    LoadingIndicator(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.appBackground),
    )
}

/**
 * Displays an empty content when there are no message reminders.
 */
@Composable
private fun MessageRemindersEmptyContent(filter: MessageRemindersFilter) {
    EmptyContent(
        text = stringResource(filter.emptyLabel),
        painter = painterResource(id = R.drawable.ic_bell_24),
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.appBackground)
            .padding(16.dp),
    )
}

/**
 * Displays a list of message reminders.
 *
 * @param reminders The list of message reminders to be displayed.
 * @param isLoadingMore Indicates whether more reminders are being loaded.
 * @param onEndReached Callback invoked when the end of the list is reached.
 * @param onReminderClick Callback invoked when a reminder is clicked.
 * @param onReminderLongClick Callback invoked when a reminder is long-clicked.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageRemindersResultList(
    reminders: List<MessageReminder>,
    isLoadingMore: Boolean,
    onEndReached: () -> Unit,
    onReminderClick: (MessageReminder) -> Unit,
    onReminderLongClick: (MessageReminder) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.appBackground),
    ) {
        items(reminders) {
            MessageReminderItem(
                modifier = Modifier
                    .animateItem()
                    .combinedClickable(
                        interactionSource = null,
                        indication = ripple(),
                        onClick = { onReminderClick(it) },
                        onLongClick = { onReminderLongClick(it) },
                    ),
                reminder = it,
            )
            HorizontalDivider(color = ChatTheme.colors.borders)
        }
        if (isLoadingMore) {
            item {
                LoadingMoreItem()
            }
        }
    }
    LoadMoreHandler(
        lazyListState = lazyListState,
        loadMore = onEndReached,
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
        contentPadding = PaddingValues(all = 16.dp),
    ) {
        items(MessageRemindersFilter.entries) {
            MessageReminderFilterItem(
                filter = it,
                isSelected = it == selectedFilter,
                onClick = { onFilterChanged(it) },
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
 * Displays a single message reminder item.
 *
 * @param reminder The message reminder to be displayed.
 */
@Composable
private fun MessageReminderItem(
    reminder: MessageReminder,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.appBackground)
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.reminders_channel_name, reminder.channel.name),
                fontSize = 14.sp,
                color = ChatTheme.colors.textLowEmphasis,
                modifier = Modifier,
            )
            // Due date
            val remindAt = reminder.remindAt
            val (text, color) = if (remindAt == null) {
                stringResource(R.string.reminders_status_save_for_later) to ChatTheme.colors.primaryAccent
            } else {
                val delta = (remindAt.time - Date().time) / 1000 / 60
                val isOverdue = delta < 0
                val duration = abs(delta).toDuration(DurationUnit.MINUTES)
                if (isOverdue) {
                    stringResource(R.string.reminders_status_overdue_by, duration) to ChatTheme.colors.errorAccent
                } else {
                    stringResource(R.string.reminders_status_due_in, duration) to ChatTheme.colors.primaryAccent
                }
            }
            Text(
                text = text,
                fontSize = 14.sp,
                color = color,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(
                user = reminder.message.user,
                modifier = Modifier.size(40.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = reminder.message.user.name,
                    fontSize = 16.sp,
                    color = ChatTheme.colors.textHighEmphasis,
                    style = ChatTheme.typography.bodyBold,
                )
                Text(
                    text = reminder.message.text,
                    fontSize = 14.sp,
                    color = ChatTheme.colors.textHighEmphasis,
                    style = ChatTheme.typography.body,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Displays a loading more item at the end of the list.
 */
@Composable
private fun LoadingMoreItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(strokeWidth = 2.dp, color = ChatTheme.colors.primaryAccent)
    }
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

/**
 * Resolves the string resource ID for the empty label.
 */
private val MessageRemindersFilter.emptyLabel: Int
    get() = when (this) {
        MessageRemindersFilter.ALL -> R.string.reminders_no_results
        MessageRemindersFilter.OVERDUE -> R.string.reminders_no_overdue
        MessageRemindersFilter.UPCOMING -> R.string.reminders_no_upcoming
        MessageRemindersFilter.SCHEDULED -> R.string.reminders_no_scheduled
        MessageRemindersFilter.SAVED_FOR_LATER -> R.string.reminders_no_saved_for_later
    }

/**
 * Resolves the string resource ID for the error label.
 */
private val MessageRemindersError.label: Int
    get() = when (this) {
        MessageRemindersError.QUERY_FAILED -> R.string.reminders_query_failed
        MessageRemindersError.UPDATE_FAILED -> R.string.reminders_update_failed
        MessageRemindersError.DELETE_FAILED -> R.string.reminders_delete_failed
    }

@Preview
@Composable
private fun MessageRemindersResultListPreview() {
    val baseReminder = MessageReminder(
        id = "id1",
        remindAt = null,
        message = Message(
            id = "id1",
            text = "Very long and important message that we need to go back to in the future sent by an important channel member.",
            user = User(
                id = "userId",
                name = "User Name",
            ),
        ),
        channel = Channel(
            type = "messaging",
            id = "id1",
            name = "Work discussions",
        ),
        createdAt = Date(),
        updatedAt = Date(),
    )
    val savedForLater = baseReminder.copy(remindAt = null)
    val overdueReminder = baseReminder.copy(
        remindAt = Date().apply {
            time -= 5.minutes.inWholeMilliseconds
        },
    )
    val upcomingReminder = baseReminder.copy(
        remindAt = Date().apply {
            time += 1.days.inWholeMilliseconds
        },
    )
    val reminders = listOf(
        savedForLater,
        overdueReminder,
        upcomingReminder,
    )
    ChatTheme {
        MessageRemindersResultList(
            reminders = reminders,
            isLoadingMore = true,
            onEndReached = {},
            onReminderClick = {},
            onReminderLongClick = {},
        )
    }
}
