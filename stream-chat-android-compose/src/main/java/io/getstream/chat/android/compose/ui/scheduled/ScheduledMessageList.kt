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

package io.getstream.chat.android.compose.ui.scheduled

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.channels.list.ChannelListLoadingItem
import io.getstream.chat.android.compose.ui.components.EmptyContent
import io.getstream.chat.android.compose.ui.components.button.StreamButtonSize
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyleDefaults
import io.getstream.chat.android.compose.ui.components.button.StreamTextButton
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ScheduleMessageTimeSheetParams
import io.getstream.chat.android.compose.ui.theme.ScheduledMessageActionsSheetParams
import io.getstream.chat.android.compose.ui.theme.ScheduledMessageDeleteDialogParams
import io.getstream.chat.android.compose.ui.theme.ScheduledMessageListEmptyContentParams
import io.getstream.chat.android.compose.ui.theme.ScheduledMessageListEmptySearchContentParams
import io.getstream.chat.android.compose.ui.theme.ScheduledMessageListErrorContentParams
import io.getstream.chat.android.compose.ui.theme.ScheduledMessageListItemParams
import io.getstream.chat.android.compose.ui.theme.ScheduledMessageListLoadingContentParams
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.StreamSnackbarHost
import io.getstream.chat.android.compose.ui.util.StreamSnackbarVariant
import io.getstream.chat.android.compose.ui.util.StreamSnackbarVisuals
import io.getstream.chat.android.compose.viewmodel.scheduled.ScheduledMessageListViewModel
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.feature.messages.composer.scheduled.scheduleMessageQuickDates
import io.getstream.chat.android.ui.common.helper.CopyToClipboardHandler
import io.getstream.chat.android.ui.common.state.messages.composer.ScheduleMessageState
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageAction
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageItemState
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageListEvent
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageListState
import kotlinx.coroutines.launch
import java.util.Date

/**
 * The list of the current user's scheduled messages, for apps that build their own screen around it. A click on a
 * message opens the actions its status allows: edit, reschedule, send now and delete, edit or reschedule a message the
 * server failed to send (it is scheduled again as a new message), or copy the text of a message whose channel was
 * deleted. A message that is sending, or whose attachments are uploading, has no actions: a click tells the user to
 * wait. Deleting asks for confirmation. A message that is already sent or deleted on another device leaves the list
 * with a short notice. Send now tells the user that the message was sent, or, while the client is not connected, that
 * it is sent once the device is back online. Apps add their own actions with
 * [io.getstream.chat.android.compose.ui.theme.ScheduledMessageActionsSheetParams.customActions].
 *
 * The list shows the messages that match the search query set with [ScheduledMessageListViewModel.setSearchQuery].
 *
 * Edit opens a composer below the list, with the content of the message. Its check button saves the message and keeps
 * its date. For a message the server failed to send, it picks a date and schedules the message again as a new
 * message. Saving is disabled while the message is no longer scheduled, while it quotes a deleted message until the
 * quote is removed, and while it starts with a command. The channel of the message does not need to be open.
 *
 * @param viewModel The [ScheduledMessageListViewModel] holding the scheduled messages and their actions.
 * @param modifier Modifier for styling.
 */
@Composable
public fun ScheduledMessageList(
    viewModel: ScheduledMessageListViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentUser by viewModel.user.collectAsStateWithLifecycle()
    val snackbarHostState = remember(::SnackbarHostState)
    val snackbarScope = rememberCoroutineScope()
    val sheets = rememberSaveable(saver = ScheduledMessageSheets.Saver) { ScheduledMessageSheets() }
    val edit = viewModel.edit
    val editSession = edit.session
    val editedItem = state.itemOf(editSession?.id)

    fun showSnackbar(message: String, variant: StreamSnackbarVariant = StreamSnackbarVariant.Default) {
        snackbarScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(StreamSnackbarVisuals(message = message, variant = variant))
        }
    }

    // Nothing is known yet after a restart, until the scheduled messages load.
    val isKnown = !state.isLoading && !state.isLoadFailed
    ScheduledMessageSheetsGoneNotice(state, sheets, isKnown, ::showSnackbar)
    ScheduledMessageEditGoneNotice(editSession, editedItem, isKnown, ::showSnackbar)
    val isEditedQuoteDeleted = editedItem?.isQuoteDeleted == true && editSession?.isQuoteRemoved == false
    ScheduledMessageListEvents(viewModel = viewModel, showSnackbar = ::showSnackbar)

    Column(modifier = modifier) {
        if (isEditedQuoteDeleted) {
            ScheduledMessageQuoteDeletedBanner(onRemoveQuoteClick = edit::removeQuote)
        }
        Box(modifier = Modifier.weight(1f)) {
            ScheduledMessageListContent(
                state = state,
                currentUser = currentUser,
                onItemClick = { item ->
                    if (item.actions.isEmpty()) {
                        showSnackbar(
                            message = context.getString(R.string.stream_compose_scheduled_message_list_still_sending),
                            variant = StreamSnackbarVariant.Loading,
                        )
                    } else {
                        sheets.selectedId = item.scheduledMessage.id
                    }
                },
                onRetryClick = viewModel::load,
            )
            StreamSnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
        }
        if (editSession != null) {
            ScheduledMessageEditComposer(
                edit = edit,
                item = editedItem,
                isQuoteDeleted = isEditedQuoteDeleted,
                currentUser = currentUser,
                viewModel = viewModel,
            )
        }
    }
    ScheduledMessageListSheets(
        state = state,
        currentUser = currentUser,
        sheets = sheets,
        viewModel = viewModel,
        onEditClick = edit::start,
    )
}

/**
 * Closes the [sheets] of a message that leaves the list while the user acts on it, as it was sent, or deleted on
 * another device, and tells the user with [showSnackbar].
 *
 * @param isKnown Whether the scheduled messages are loaded, so a message missing from the list is gone.
 */
@Composable
private fun ScheduledMessageSheetsGoneNotice(
    state: ScheduledMessageListState,
    sheets: ScheduledMessageSheets,
    isKnown: Boolean,
    showSnackbar: (message: String, variant: StreamSnackbarVariant) -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(state, sheets.openIds) {
        if (isKnown && sheets.openIds.any { id -> state.itemOf(id) == null }) {
            sheets.closeAll()
            showSnackbar(
                context.getString(R.string.stream_compose_scheduled_message_list_gone),
                StreamSnackbarVariant.Default,
            )
        }
    }
}

/**
 * Tells the user, with [showSnackbar], when the message of [editSession] is sending, or was sent or deleted on another
 * device, once the composer is filled with it. The composer keeps its content, and can't save it.
 *
 * @param editedItem The edited message in the list, or null when it is not there.
 * @param isKnown Whether the scheduled messages are loaded, so a message missing from the list is gone.
 */
@Composable
private fun ScheduledMessageEditGoneNotice(
    editSession: ScheduledMessageEditSession?,
    editedItem: ScheduledMessageItemState?,
    isKnown: Boolean,
    showSnackbar: (message: String, variant: StreamSnackbarVariant) -> Unit,
) {
    val context = LocalContext.current
    // Before the composer is filled, a message that is gone ends the edit and is told by the view model.
    val isEditedMessageGone = editSession?.isFilled == true && isKnown &&
        (editedItem == null || editedItem.scheduledMessage.status == ScheduledMessageStatus.Sending)
    LaunchedEffect(editSession?.token, isEditedMessageGone) {
        if (isEditedMessageGone) {
            showSnackbar(
                context.getString(R.string.stream_compose_scheduled_message_list_gone),
                StreamSnackbarVariant.Default,
            )
        }
    }
}

/**
 * Tells the user the results of the actions of [viewModel], with [showSnackbar].
 */
@Composable
private fun ScheduledMessageListEvents(
    viewModel: ScheduledMessageListViewModel,
    showSnackbar: (message: String, variant: StreamSnackbarVariant) -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ScheduledMessageListEvent.MessageGone -> showSnackbar(
                    context.getString(R.string.stream_compose_scheduled_message_list_gone),
                    StreamSnackbarVariant.Default,
                )
                is ScheduledMessageListEvent.DeletedMessageAlreadySent -> showSnackbar(
                    context.getString(R.string.stream_compose_scheduled_message_list_deleted_already_sent),
                    StreamSnackbarVariant.Default,
                )
                is ScheduledMessageListEvent.SendNowQueued -> showSnackbar(
                    context.getString(R.string.stream_compose_scheduled_message_list_send_now_queued),
                    StreamSnackbarVariant.Default,
                )
                is ScheduledMessageListEvent.SentNow -> showSnackbar(
                    context.getString(R.string.stream_compose_scheduled_messages_sent),
                    StreamSnackbarVariant.Default,
                )
                is ScheduledMessageListEvent.ActionFailed -> showSnackbar(
                    context.getString(R.string.stream_compose_scheduled_message_list_action_failed),
                    StreamSnackbarVariant.Error,
                )
                else -> Unit
            }
        }
    }
}

/**
 * The sheets and the dialog open on a scheduled message: its actions, its reschedule sheet and its delete dialog.
 */
@Composable
private fun ScheduledMessageListSheets(
    state: ScheduledMessageListState,
    currentUser: User?,
    sheets: ScheduledMessageSheets,
    viewModel: ScheduledMessageListViewModel,
    onEditClick: (ScheduledMessage) -> Unit,
) {
    val context = LocalContext.current
    val clipboard = remember(context) { CopyToClipboardHandler(context) }
    state.itemOf(sheets.selectedId)?.let { item ->
        ChatTheme.componentFactory.ScheduledMessageActionsSheet(
            params = ScheduledMessageActionsSheetParams(
                item = item,
                actions = item.actions,
                onActionClick = { action ->
                    sheets.selectedId = null
                    val scheduledMessage = item.scheduledMessage
                    when (action) {
                        ScheduledMessageAction.Edit,
                        ScheduledMessageAction.EditAndReschedule,
                        -> onEditClick(scheduledMessage)
                        ScheduledMessageAction.Reschedule -> sheets.rescheduledId = scheduledMessage.id
                        ScheduledMessageAction.SendNow -> viewModel.sendNow(scheduledMessage.id)
                        ScheduledMessageAction.CopyText -> clipboard.copy(scheduledMessage.message.text)
                        ScheduledMessageAction.Delete -> sheets.deletedId = scheduledMessage.id
                        else -> Unit
                    }
                },
                onDismiss = { sheets.selectedId = null },
            ),
        )
    }
    state.itemOf(sheets.rescheduledId)?.let { item ->
        val scheduledMessage = item.scheduledMessage
        ChatTheme.componentFactory.ScheduleMessageTimeSheet(
            params = ScheduleMessageTimeSheetParams(
                message = scheduledMessage.message,
                channelName = item.channelName(currentUser),
                initialDate = remember(scheduledMessage.id) { rescheduleInitialDate(scheduledMessage.scheduledAt) },
                state = scheduleMessageState(viewModel.channelConfig(scheduledMessage.cid)),
                error = null,
                onConfirm = { date ->
                    sheets.rescheduledId = null
                    viewModel.reschedule(scheduledMessage.id, date)
                },
                onDismiss = { sheets.rescheduledId = null },
                scheduledAt = scheduledMessage.scheduledAt,
            ),
        )
    }
    state.itemOf(sheets.deletedId)?.let { item ->
        ChatTheme.componentFactory.ScheduledMessageDeleteDialog(
            params = ScheduledMessageDeleteDialogParams(
                channelName = item.channelName(currentUser),
                onConfirm = {
                    sheets.deletedId = null
                    viewModel.delete(item.scheduledMessage.id)
                },
                onDismiss = { sheets.deletedId = null },
            ),
        )
    }
}

/**
 * The date the reschedule sheet starts at: the date of the message, or the first quick date once that date has passed,
 * as for a failed message, since a date that passed can't be picked again.
 */
private fun rescheduleInitialDate(scheduledAt: Date): Date {
    val now = Date()
    return scheduledAt.takeIf { it.after(now) } ?: scheduleMessageQuickDates(now).first()
}

/**
 * The IDs of the scheduled messages whose actions, reschedule sheet or delete dialog is open.
 */
private class ScheduledMessageSheets(
    selectedId: String? = null,
    rescheduledId: String? = null,
    deletedId: String? = null,
) {
    var selectedId by mutableStateOf(selectedId)
    var rescheduledId by mutableStateOf(rescheduledId)
    var deletedId by mutableStateOf(deletedId)

    val openIds: List<String>
        get() = listOfNotNull(selectedId, rescheduledId, deletedId)

    fun closeAll() {
        selectedId = null
        rescheduledId = null
        deletedId = null
    }

    companion object {
        val Saver = listSaver<ScheduledMessageSheets, String?>(
            save = { listOf(it.selectedId, it.rescheduledId, it.deletedId) },
            restore = { ScheduledMessageSheets(it[0], it[1], it[2]) },
        )
    }
}

/**
 * The content of the list for [state]: the loading, error or empty content, the content for a search without
 * results, or the scheduled messages that match the search.
 */
@Composable
internal fun ScheduledMessageListContent(
    state: ScheduledMessageListState,
    currentUser: User?,
    onItemClick: (ScheduledMessageItemState) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentModifier = modifier
        .fillMaxSize()
        .background(ChatTheme.colors.backgroundCoreApp)
    when {
        state.isLoading -> ChatTheme.componentFactory.ScheduledMessageListLoadingContent(
            params = ScheduledMessageListLoadingContentParams(modifier = contentModifier),
        )
        state.isLoadFailed -> ChatTheme.componentFactory.ScheduledMessageListErrorContent(
            params = ScheduledMessageListErrorContentParams(onRetryClick = onRetryClick, modifier = contentModifier),
        )
        state.items.isEmpty() -> ChatTheme.componentFactory.ScheduledMessageListEmptyContent(
            params = ScheduledMessageListEmptyContentParams(modifier = contentModifier),
        )
        state.searchResults.isEmpty() -> ChatTheme.componentFactory.ScheduledMessageListEmptySearchContent(
            params = ScheduledMessageListEmptySearchContentParams(
                searchQuery = state.searchQuery,
                modifier = contentModifier,
            ),
        )
        else -> LazyColumn(modifier = contentModifier.testTag("Stream_ScheduledMessageList")) {
            items(state.searchResults, key = { it.scheduledMessage.id }) { item ->
                ChatTheme.componentFactory.ScheduledMessageListItem(
                    params = ScheduledMessageListItemParams(
                        item = item,
                        currentUser = currentUser,
                        onClick = onItemClick,
                    ),
                )
            }
        }
    }
}

/**
 * The default content of the list when there are no scheduled messages.
 *
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultScheduledMessageListEmptyContent(modifier: Modifier = Modifier) {
    EmptyContent(
        modifier = modifier
            .testTag("Stream_ScheduledMessageListEmpty")
            .padding(horizontal = StreamTokens.spacing2xl),
        painter = painterResource(R.drawable.stream_design_ic_clock),
        title = stringResource(R.string.stream_compose_scheduled_message_list_empty_title),
        text = stringResource(R.string.stream_compose_scheduled_message_list_empty_description),
    )
}

/**
 * The default content of the list when no scheduled message matches the search query.
 *
 * @param searchQuery The search query no scheduled message matches.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultScheduledMessageListEmptySearchContent(searchQuery: String, modifier: Modifier = Modifier) {
    EmptyContent(
        modifier = modifier
            .testTag("Stream_ScheduledMessageListEmptySearch")
            .padding(horizontal = StreamTokens.spacing2xl),
        painter = painterResource(R.drawable.stream_design_ic_search),
        text = stringResource(R.string.stream_compose_scheduled_message_list_empty_search_results, searchQuery.trim()),
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduledMessageListEmptySearchPreview() {
    ChatTheme {
        ScheduledMessageListEmptySearch()
    }
}

/** A search that no scheduled message matches. */
@Composable
internal fun ScheduledMessageListEmptySearch() {
    ScheduledMessageListContent(
        state = ScheduledMessageListState(
            items = PreviewScheduledMessageItems[0],
            isLoading = false,
            searchQuery = "launch party",
            searchResults = emptyList(),
        ),
        currentUser = null,
        onItemClick = {},
        onRetryClick = {},
    )
}

/**
 * The default content of the list while the scheduled messages load: placeholder rows.
 *
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultScheduledMessageListLoadingContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier.testTag("Stream_ScheduledMessageListLoading")) {
        repeat(LoadingRows) { ChannelListLoadingItem() }
    }
}

/**
 * The default content of the list when the scheduled messages could not be loaded.
 *
 * @param onRetryClick Called when the user wants to load the scheduled messages again.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultScheduledMessageListErrorContent(onRetryClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .testTag("Stream_ScheduledMessageListError")
            .padding(horizontal = StreamTokens.spacing2xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            modifier = Modifier.size(ErrorIconSize),
            painter = painterResource(R.drawable.stream_design_ic_exclamation_circle),
            contentDescription = null,
            tint = ChatTheme.colors.textTertiary,
        )
        Spacer(modifier = Modifier.size(StreamTokens.spacingSm))
        Text(
            text = stringResource(R.string.stream_compose_scheduled_message_list_load_failed),
            style = ChatTheme.typography.bodyDefault,
            color = ChatTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.size(StreamTokens.spacingMd))
        StreamTextButton(
            modifier = Modifier.testTag("Stream_ScheduledMessageListRetry"),
            onClick = onRetryClick,
            text = stringResource(R.string.stream_compose_scheduled_message_list_try_again),
            style = StreamButtonStyleDefaults.secondaryOutline,
            size = StreamButtonSize.Medium,
        )
    }
}

/**
 * The name of the channel of the scheduled message, from its summary, or its ID when there is no summary.
 */
@Composable
internal fun ScheduledMessageItemState.channelName(currentUser: User?): String =
    channel?.let { ChatTheme.channelNameFormatter.formatChannelName(it, currentUser) }
        ?: scheduledMessage.cid.substringAfter(':')

/**
 * The lead times of the channel [config], for the reschedule sheet, or the defaults when it is not known. The message
 * already counts toward the limits.
 */
private fun scheduleMessageState(config: Config?): ScheduleMessageState {
    val defaults = ScheduleMessageState()
    return defaults.copy(
        minLeadTimeSeconds = config?.scheduledMessagesMinLeadTimeSeconds ?: defaults.minLeadTimeSeconds,
        maxLeadTimeSeconds = config?.scheduledMessagesMaxLeadTimeSeconds ?: defaults.maxLeadTimeSeconds,
    )
}

private fun ScheduledMessageListState.itemOf(id: String?): ScheduledMessageItemState? =
    id?.let { items.find { item -> item.scheduledMessage.id == it } }

private val ErrorIconSize = 32.dp

private const val LoadingRows = 6
