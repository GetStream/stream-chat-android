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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.CustomAction
import io.getstream.chat.android.ui.common.state.messages.MessageAction

/**
 * Factory for creating components related to message info.
 */
class MessageInfoComponentFactory : ChatComponentFactory {

    /**
     * Creates a message menu with option for message info.
     */
    @OptIn(ExperimentalMaterial3Api::class)
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
        var showMessageInfoDialog by remember { mutableStateOf(false) }

        val allOptions = listOf(
            MessageOptionItemState(
                title = R.string.message_option_message_info,
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconPainter = rememberVectorPainter(Icons.Outlined.Info),
                iconColor = ChatTheme.colors.textLowEmphasis,
                action = CustomAction(message, mapOf("message_info" to true)),
            )
        ) + messageOptions

        val extendedOnMessageAction: (MessageAction) -> Unit = { action ->
            when {
                action is CustomAction && action.extraProperties.contains("message_info") ->
                    showMessageInfoDialog = true

                else -> onMessageAction(action)
            }
        }

        var dismissed by remember { mutableStateOf(false) }

        if (showMessageInfoDialog) {
            ModalBottomSheet(
                onDismissRequest = {
                    showMessageInfoDialog = false
                    onDismiss()
                    dismissed = true // Mark as dismissed to avoid animating the menu again
                },
                containerColor = ChatTheme.colors.appBackground,
            ) {
                // TODO Replace with a proper Message Info Screen
            }
        } else if (!dismissed) {
            super.MessageMenu(
                modifier = modifier,
                message = message,
                messageOptions = allOptions,
                ownCapabilities = ownCapabilities,
                onMessageAction = extendedOnMessageAction,
                onShowMore = onShowMore,
                onDismiss = onDismiss,
            )
        }
    }
}
