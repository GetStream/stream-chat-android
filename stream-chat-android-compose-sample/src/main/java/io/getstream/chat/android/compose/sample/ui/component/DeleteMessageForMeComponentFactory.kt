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

package io.getstream.chat.android.compose.sample.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.CustomAction
import io.getstream.chat.android.ui.common.state.messages.Delete
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.utils.canDeleteMessage

/**
 * Factory for creating components related to deleting messages for the current user.
 */
class DeleteMessageForMeComponentFactory(
    private val delegate: ChatComponentFactory = MessageInfoComponentFactory(),
) : ChatComponentFactory by delegate {

    /**
     * Creates a message menu with option for deleting messages for the current user.
     */
    @Suppress("LongMethod")
    @Composable
    override fun MessageMenu(
        modifier: Modifier,
        message: Message,
        messageOptions: List<MessageOptionItemState>,
        ownCapabilities: Set<String>,
        onMessageAction: (MessageAction) -> Unit,
        onShowMore: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        val chatClient = ChatClient.instance()

        val visibility = ChatTheme.messageOptionsTheme.optionVisibility

        val canDeleteMessage = canDeleteMessage(
            deleteMessageEnabled = visibility.isDeleteMessageVisible,
            currentUser = chatClient.getCurrentUser(),
            message = message,
            ownCapabilities = ownCapabilities,
        )

        var showDeleteForMeConfirmationDialog by remember { mutableStateOf(false) }

        val allOptions = if (canDeleteMessage) {
            buildList {
                messageOptions.forEach { option ->
                    add(option)
                    if (option.action is Delete) {
                        add(
                            MessageOptionItemState(
                                title = R.string.message_option_delete_for_me,
                                titleColor = ChatTheme.colors.errorAccent,
                                iconPainter = painterResource(R.drawable.stream_compose_ic_delete),
                                iconColor = ChatTheme.colors.errorAccent,
                                action = CustomAction(message, mapOf("delete_for_me" to true)),
                            ),
                        )
                    }
                }
            }
        } else {
            messageOptions
        }

        val extendedOnMessageAction: (MessageAction) -> Unit = { action ->
            when {
                action is CustomAction && action.extraProperties.contains("delete_for_me") ->
                    showDeleteForMeConfirmationDialog = true

                else -> onMessageAction(action)
            }
        }

        if (showDeleteForMeConfirmationDialog) {
            SimpleDialog(
                modifier = Modifier.padding(16.dp),
                title = "Delete for Me",
                message = "Are you sure you want to delete this message for you?",
                onPositiveAction = {
                    chatClient.deleteMessageForMe(message.id).enqueue()
                    onDismiss()
                },
                onDismiss = { showDeleteForMeConfirmationDialog = false },
            )
        }

        delegate.MessageMenu(
            modifier = modifier,
            message = message,
            messageOptions = allOptions,
            ownCapabilities = ownCapabilities,
            onMessageAction = extendedOnMessageAction,
            onShowMore = onShowMore,
            onDismiss = onDismiss,
        )
    }

    @Composable
    override fun MessageFooterContent(messageItem: MessageItemState) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (messageItem.message.deletedForMe) {
                Text(
                    modifier = Modifier.padding(vertical = 4.dp),
                    text = "Deleted only for me",
                    style = ChatTheme.typography.footnote,
                    color = ChatTheme.colors.textHighEmphasis,
                )
            }
            super.MessageFooterContent(messageItem)
        }
    }
}
