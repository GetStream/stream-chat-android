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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.StreamCardBottomSheet
import io.getstream.chat.android.compose.ui.components.button.StreamButtonSize
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyleDefaults
import io.getstream.chat.android.compose.ui.components.button.StreamTextButton
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageAction

/**
 * The sheet with the actions on a scheduled message. The sheet closes when an action is picked.
 *
 * @param actions The actions to show, in order.
 * @param onActionClick Called with the action the user picked.
 * @param onDismiss Called when the sheet is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduledMessageActionsSheet(
    actions: List<ScheduledMessageAction>,
    onActionClick: (ScheduledMessageAction) -> Unit,
    onDismiss: () -> Unit,
) {
    StreamCardBottomSheet(
        modifier = Modifier.testTag("Stream_ScheduledMessageActions"),
        onDismissRequest = onDismiss,
    ) {
        ScheduledMessageActionsContent(
            modifier = Modifier.navigationBarsPadding().padding(bottom = StreamTokens.spacingXl),
            actions = actions,
            onActionClick = onActionClick,
        )
    }
}

@Composable
internal fun ScheduledMessageActionsContent(
    actions: List<ScheduledMessageAction>,
    onActionClick: (ScheduledMessageAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        actions.forEach { action ->
            action.content()?.let { content ->
                ScheduledMessageActionItem(
                    action = action,
                    content = content,
                    onClick = { onActionClick(action) },
                )
            }
        }
    }
}

@Composable
private fun ScheduledMessageActionItem(
    action: ScheduledMessageAction,
    content: ActionContent,
    onClick: () -> Unit,
) {
    val color = if (action == ScheduledMessageAction.Delete) {
        ChatTheme.colors.accentError
    } else {
        ChatTheme.colors.textPrimary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ActionItemHeight)
            .testTag(content.testTag)
            .clickable(onClick = onClick)
            .padding(horizontal = StreamTokens.spacing2xs + StreamTokens.spacingSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingSm),
    ) {
        Icon(
            modifier = Modifier.size(ActionIconSize),
            painter = painterResource(content.icon),
            contentDescription = null,
            tint = color,
        )
        Text(text = stringResource(content.label), style = ChatTheme.typography.bodyDefault, color = color)
    }
}

/**
 * The dialog that asks to confirm the deletion of a scheduled message.
 *
 * @param channelName The name of the channel the message is sent to.
 * @param onConfirm Called when the user confirms the deletion.
 * @param onDismiss Called when the dialog is dismissed without deleting.
 */
@Composable
internal fun ScheduledMessageDeleteDialog(
    channelName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        ScheduledMessageDeleteDialogContent(channelName = channelName, onConfirm = onConfirm, onDismiss = onDismiss)
    }
}

@Composable
internal fun ScheduledMessageDeleteDialogContent(
    channelName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        // The dialog renders in its own window, which the app-level `testTagsAsResourceId` does not reach.
        modifier = Modifier
            .semantics { testTagsAsResourceId = true }
            .testTag("Stream_ScheduledMessageDeleteDialog")
            .clip(RoundedCornerShape(StreamTokens.radius4xl))
            .background(ChatTheme.colors.backgroundCoreElevation1)
            .padding(
                start = StreamTokens.spacingXl,
                end = StreamTokens.spacingXl,
                top = StreamTokens.spacing2xl,
                bottom = StreamTokens.spacingXl,
            ),
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xl),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs)) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.stream_compose_scheduled_message_delete_title),
                style = ChatTheme.typography.headingSmall,
                color = ChatTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.stream_compose_scheduled_message_delete_description, channelName),
                style = ChatTheme.typography.captionDefault,
                color = ChatTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs)) {
            StreamTextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("Stream_ScheduledMessageDeleteConfirm"),
                onClick = onConfirm,
                text = stringResource(R.string.stream_compose_scheduled_message_action_delete),
                style = StreamButtonStyleDefaults.destructiveSolid,
                size = StreamButtonSize.Large,
            )
            StreamTextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onDismiss,
                text = stringResource(R.string.stream_compose_scheduled_message_delete_cancel),
                style = StreamButtonStyleDefaults.secondaryOutline,
                size = StreamButtonSize.Large,
            )
        }
    }
}

/**
 * The icon, the label and the test tag of an action.
 */
private class ActionContent(val icon: Int, val label: Int, val testTag: String)

/**
 * The content of the action, or null for an action this version does not know.
 */
private fun ScheduledMessageAction.content(): ActionContent? = when (this) {
    ScheduledMessageAction.Edit -> ActionContent(
        icon = R.drawable.stream_design_ic_edit,
        label = R.string.stream_compose_scheduled_message_action_edit,
        testTag = "Stream_ScheduledMessageAction_Edit",
    )
    ScheduledMessageAction.EditAndReschedule -> ActionContent(
        icon = R.drawable.stream_design_ic_edit,
        label = R.string.stream_compose_scheduled_message_action_edit_and_reschedule,
        testTag = "Stream_ScheduledMessageAction_EditAndReschedule",
    )
    ScheduledMessageAction.Reschedule -> ActionContent(
        icon = R.drawable.stream_design_ic_clock,
        label = R.string.stream_compose_scheduled_message_action_reschedule,
        testTag = "Stream_ScheduledMessageAction_Reschedule",
    )
    ScheduledMessageAction.SendNow -> ActionContent(
        icon = R.drawable.stream_design_ic_send,
        label = R.string.stream_compose_scheduled_message_action_send_now,
        testTag = "Stream_ScheduledMessageAction_SendNow",
    )
    ScheduledMessageAction.CopyText -> ActionContent(
        icon = R.drawable.stream_design_ic_copy,
        label = R.string.stream_compose_scheduled_message_action_copy_text,
        testTag = "Stream_ScheduledMessageAction_CopyText",
    )
    ScheduledMessageAction.Delete -> ActionContent(
        icon = R.drawable.stream_design_ic_delete,
        label = R.string.stream_compose_scheduled_message_action_delete,
        testTag = "Stream_ScheduledMessageAction_Delete",
    )
    else -> null
}

private val ActionItemHeight = 48.dp
private val ActionIconSize = 20.dp

@Preview(showBackground = true)
@Composable
private fun ScheduledMessageActionsPreview() {
    ChatTheme {
        ScheduledMessageActions()
    }
}

/** Every action, as the sheet shows them. */
@Composable
internal fun ScheduledMessageActions() {
    ScheduledMessageActionsContent(
        modifier = Modifier.background(ChatTheme.colors.backgroundCoreElevation1),
        actions = listOf(
            ScheduledMessageAction.Edit,
            ScheduledMessageAction.EditAndReschedule,
            ScheduledMessageAction.Reschedule,
            ScheduledMessageAction.SendNow,
            ScheduledMessageAction.CopyText,
            ScheduledMessageAction.Delete,
        ),
        onActionClick = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduledMessageDeleteDialogPreview() {
    ChatTheme {
        ScheduledMessageDeleteDialogContent(channelName = "Maya Ross", onConfirm = {}, onDismiss = {})
    }
}
