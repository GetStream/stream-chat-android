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

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.ui.components.selectedmessage.SelectedMessageMenu
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.CustomAction
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import java.util.Date

/**
 * Factory for creating components related to message reminders.
 */
class MessageRemindersComponentFactory {

    /**
     * Creates a message menu with options for setting reminders and saving messages for later.
     *
     * @param modifier Modifier to be applied to the message menu.
     * @param message The message for which the menu is created.
     * @param messageOptions List of available message options.
     * @param ownCapabilities Set of capabilities of the current user.
     * @param onMessageAction Callback invoked when a message action is selected.
     * @param onShowMore Callback invoked when the "show more" option is selected.
     * @param onDismiss Callback invoked when the menu is dismissed.
     */
    @Composable
    fun MessageMenu(
        modifier: Modifier,
        message: Message,
        messageOptions: List<MessageOptionItemState>,
        ownCapabilities: Set<String>,
        onMessageAction: (MessageAction) -> Unit,
        onShowMore: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        val remindMeOption = remindMeOption(message)
        val saveForLaterOption = saveForLaterOption(message)
        var showReminderTimeDialog by remember { mutableStateOf(false) }
        val extendedAction: (MessageAction) -> Unit = { action ->
            when (action) {
                is CustomAction -> {
                    when (action.extraProperties[ACTION_TYPE]) {
                        ACTION_TYPE_ADD_REMINDER, ACTION_TYPE_UPDATE_REMINDER -> {
                            showReminderTimeDialog = true
                        }
                        ACTION_TYPE_SAVE_FOR_LATER -> {
                            saveForLater(message.id)
                            onDismiss()
                        }
                        ACTION_TYPE_REMOVE_FROM_LATER -> {
                            removeFromLater(message.id)
                            onDismiss()
                        }
                    }
                }
                else -> onMessageAction(action)
            }
        }

        if (showReminderTimeDialog) {
            CreateReminderDialog(
                onDismiss = { showReminderTimeDialog = false },
                onRemindAtSelected = { remindAt ->
                    addReminder(message.id, remindAt)
                    showReminderTimeDialog = false
                    onDismiss()
                },
            )
        }

        SelectedMessageMenu(
            modifier = modifier,
            message = message,
            messageOptions = messageOptions + remindMeOption + saveForLaterOption,
            ownCapabilities = ownCapabilities,
            onMessageAction = extendedAction,
            onShowMoreReactionsSelected = onShowMore,
            onDismiss = onDismiss,
        )
    }

    @Composable
    private fun remindMeOption(message: Message): MessageOptionItemState {
        val hasReminder = message.reminder != null
        val title = if (hasReminder) {
            R.string.message_menu_update_reminder
        } else {
            R.string.message_menu_add_reminder
        }
        val icon = if (hasReminder) {
            R.drawable.ic_bell_filled_24
        } else {
            R.drawable.ic_bell_24
        }
        val actionType = if (hasReminder) {
            ACTION_TYPE_UPDATE_REMINDER
        } else {
            ACTION_TYPE_ADD_REMINDER
        }
        return MessageOptionItemState(
            title = title,
            titleColor = ChatTheme.colors.textHighEmphasis,
            iconPainter = painterResource(icon),
            iconColor = ChatTheme.colors.textLowEmphasis,
            action = CustomAction(message, mapOf(ACTION_TYPE to actionType)),
        )
    }

    @Composable
    private fun saveForLaterOption(message: Message): MessageOptionItemState {
        val hasReminder = message.reminder != null
        val title = if (hasReminder) {
            R.string.message_menu_remove_from_later
        } else {
            R.string.message_menu_save_for_later
        }
        val icon = if (hasReminder) {
            R.drawable.ic_bookmark_filled_24
        } else {
            R.drawable.ic_bookmark_24
        }
        val actionType = if (hasReminder) {
            ACTION_TYPE_REMOVE_FROM_LATER
        } else {
            ACTION_TYPE_SAVE_FOR_LATER
        }
        return MessageOptionItemState(
            title = title,
            titleColor = ChatTheme.colors.textHighEmphasis,
            iconPainter = painterResource(icon),
            iconColor = ChatTheme.colors.textLowEmphasis,
            action = CustomAction(message, mapOf(ACTION_TYPE to actionType)),
        )
    }

    private fun addReminder(messageId: String, remindAt: Date) {
        val client = ChatClient.instance()
        client.createReminder(messageId, remindAt).enqueue()
    }

    private fun saveForLater(messageId: String) {
        val client = ChatClient.instance()
        client.createReminder(messageId, remindAt = null).enqueue {
            it
                .onSuccess {
                    Log.d("X_PETAR", "Saved for later: $it")
                }
                .onError {
                    Log.d("X_PETAR", "Error saving for later: $it")
                }
        }
    }

    private fun removeFromLater(messageId: String) {
        val client = ChatClient.instance()
        client.deleteReminder(messageId).enqueue()
    }

    companion object {
        private const val ACTION_TYPE = "type"
        private const val ACTION_TYPE_ADD_REMINDER = "add_reminder"
        private const val ACTION_TYPE_UPDATE_REMINDER = "update_reminder"
        private const val ACTION_TYPE_SAVE_FOR_LATER = "save_for_later"
        private const val ACTION_TYPE_REMOVE_FROM_LATER = "remove_from_later"
    }
}
