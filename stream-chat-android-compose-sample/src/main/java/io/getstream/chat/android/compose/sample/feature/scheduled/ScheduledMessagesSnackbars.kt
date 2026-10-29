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

package io.getstream.chat.android.compose.sample.feature.scheduled

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.models.ScheduledMessageFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Shows the results in [events] as snackbars in [snackbarHostState], with a View action that calls [onViewClick]
 * when it is set, and passes the other events to [onEvent].
 */
@Composable
internal fun ScheduledMessagesSnackbars(
    events: Flow<ScheduledMessagesEvent>,
    snackbarHostState: SnackbarHostState,
    onViewClick: (() -> Unit)? = null,
    onEvent: (ScheduledMessagesEvent) -> Unit = {},
) {
    val context = LocalContext.current
    val currentOnViewClick by rememberUpdatedState(onViewClick)
    val currentOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(events) {
        events.collect { event ->
            val snackbar = context.snackbarOf(event, hasViewAction = currentOnViewClick != null)
            if (snackbar == null) {
                currentOnEvent(event)
                return@collect
            }
            // Shown in its own coroutine, so the next events are handled while it shows.
            launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showSnackbar(
                    message = snackbar.first,
                    actionLabel = context.getString(R.string.scheduled_messages_view).takeIf { snackbar.second },
                    duration = SnackbarDuration.Short,
                )
                if (result == SnackbarResult.ActionPerformed) currentOnViewClick?.invoke()
            }
        }
    }
}

/**
 * The text of the snackbar for [event], and whether it has a View action, or null for an event that is not shown.
 */
private fun Context.snackbarOf(event: ScheduledMessagesEvent, hasViewAction: Boolean): Pair<String, Boolean>? =
    when (event) {
        is ScheduledMessagesEvent.Scheduled ->
            getString(R.string.scheduled_messages_scheduled_for, formatScheduledDate(event.scheduledAt)) to
                hasViewAction
        ScheduledMessagesEvent.Queued -> getString(R.string.scheduled_messages_queued_until_online) to hasViewAction
        ScheduledMessagesEvent.SentRightAway -> getString(R.string.scheduled_messages_sent_right_away) to false
        ScheduledMessagesEvent.Sent -> getString(R.string.scheduled_messages_sent) to false
        ScheduledMessagesEvent.Deleted -> getString(R.string.scheduled_messages_deleted) to false
        ScheduledMessagesEvent.StillSending -> getString(R.string.scheduled_messages_still_sending) to false
        is ScheduledMessagesEvent.Failed -> if (hasViewAction) {
            getString(R.string.scheduled_messages_failed) to true
        } else {
            event.failure.text(this) to false
        }
        is ScheduledMessagesEvent.Error -> event.message to false
        ScheduledMessagesEvent.EditedMessageSent -> getString(R.string.scheduled_messages_edited_sent) to false
        ScheduledMessagesEvent.EditedMessageDeleted -> getString(R.string.scheduled_messages_edited_deleted) to false
        ScheduledMessagesEvent.ClearComposer,
        is ScheduledMessagesEvent.EditInComposer,
        -> null
    }

/**
 * The text that says why the message failed.
 */
internal fun ScheduledMessageFailure?.text(context: Context): String =
    this?.description?.let(context::getString)
        ?: (this as? ScheduledMessageFailure.ServerRejectedScheduling)?.message
        ?: context.getString(R.string.scheduled_messages_failed_to_send)
