package io.getstream.chat.android.compose.sample.feature.reminders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.ui.components.selectedmessage.SelectedMessageMenu
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.CustomAction
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import java.util.Date
import kotlin.time.Duration.Companion.minutes

class MessageRemindersComponentFactory {

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
            SelectReminderTimeDialog(
                onDismiss = { showReminderTimeDialog = false },
                onOptionSelected = { minutes ->
                    addReminder(message.id, minutes)
                    showReminderTimeDialog = false
                    onDismiss()
                }
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

    @Composable
    private fun SelectReminderTimeDialog(
        onDismiss: () -> Unit,
        onOptionSelected: (minutes: Int) -> Unit,
    ) {
        Dialog(
            onDismissRequest = onDismiss,
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = ChatTheme.colors.barsBackground)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "Select Reminder Time",
                        color = ChatTheme.colors.textHighEmphasis,
                        style = ChatTheme.typography.title3Bold
                    )
                    Text(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                        text = "When would you like to be reminded?",
                        color = ChatTheme.colors.textLowEmphasis,
                        style = ChatTheme.typography.body,
                    )
                    HorizontalDivider()
                    SelectReminderTimeOption("2 Minutes") { onOptionSelected(2) }
                    SelectReminderTimeOption("5 Minutes") { onOptionSelected(5) }
                    SelectReminderTimeOption("1 Hour") { onOptionSelected(60) }
                }
            }
        }
    }

    @Composable
    private fun SelectReminderTimeOption(
        text: String,
        onClick: () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = null,
                    indication = ripple(),
                    onClick = onClick,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = text,
                color = ChatTheme.colors.primaryAccent,
            )
            HorizontalDivider()
        }
    }

    private fun addReminder(messageId: String, remindAfter: Int) {
        val remindAt = Date().apply {
            time += remindAfter.minutes.inWholeMilliseconds
        }
        val client = ChatClient.instance()
        client.createReminder(messageId, remindAt).enqueue()
    }

    private fun saveForLater(messageId: String) {
        val client = ChatClient.instance()
        client.createReminder(messageId, remindAt = null).enqueue()
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