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

package io.getstream.chat.android.compose.ui.components.reactionpicker

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.ui.components.reactionoptions.ExtendedReactionsOptions
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.React

/**
 * Displays all of the available reactions the user can set on a message.
 *
 * @param message The selected message.
 * @param onMessageAction Handler that propagates click events on each item.
 * @param modifier Modifier for styling.
 * @param onDismiss Handler called when the menu is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun ReactionsPicker(
    message: Message,
    onMessageAction: (MessageAction) -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
) {
    ModalBottomSheet(
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(),
        containerColor = ChatTheme.colors.backgroundCoreApp,
        onDismissRequest = onDismiss,
    ) {
        ChatTheme.componentFactory.MessageReactionsPickerContent(
            modifier = Modifier,
            message = message,
            onMessageAction = onMessageAction,
        )
    }
}

/**
 * Default content for the reactions picker bottom sheet.
 *
 * @param message The selected message.
 * @param onMessageAction Handler that propagates click events on each item.
 * @param modifier Modifier for styling.
 */
@Composable
public fun ReactionsPickerContent(
    message: Message,
    onMessageAction: (MessageAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ExtendedReactionsOptions(
        modifier = modifier
            .fillMaxWidth(),
        onReactionOptionSelected = {
            onMessageAction(
                React(
                    reaction = Reaction(messageId = message.id, type = it.type, emojiCode = it.emojiCode),
                    message = message,
                ),
            )
        },
        ownReactions = message.ownReactions,
    )
}
