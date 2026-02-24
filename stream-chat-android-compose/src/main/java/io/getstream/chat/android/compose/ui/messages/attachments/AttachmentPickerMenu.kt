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

package io.getstream.chat.android.compose.ui.messages.attachments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.util.isKeyboardVisibleAsState
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel

/**
 * A container component that manages the attachment picker's visibility and animations.
 *
 * This composable wraps [AttachmentPicker] and handles:
 * - Animated expand/collapse transitions when showing/hiding the picker
 * - Keyboard coordination (hides keyboard when picker opens if configured)
 * - Auto-dismiss when keyboard appears
 * - Height configuration based on picker type (system vs in-app)
 * - Integration between the attachment picker and message composer
 *
 * The picker visibility is controlled by [AttachmentsPickerViewModel.isPickerVisible].
 * Toggle it using [AttachmentsPickerViewModel.setPickerVisible].
 *
 * This component is typically used within [MessagesScreen] and handles the complete flow of:
 * 1. Displaying the attachment picker when triggered
 * 2. Managing attachment selection
 * 3. Adding selected attachments to the message composer
 * 4. Creating polls when the poll action is triggered
 * 5. Inserting commands when selected
 *
 * @param attachmentsPickerViewModel The [AttachmentsPickerViewModel] that controls picker visibility,
 * manages attachment loading, and tracks selection state.
 * @param composerViewModel The [MessageComposerViewModel] that receives selected attachments
 * and handles poll creation and command insertion.
 */
@Suppress("LongMethod")
@Composable
public fun AttachmentPickerMenu(
    attachmentsPickerViewModel: AttachmentsPickerViewModel,
    composerViewModel: MessageComposerViewModel,
) {
    val isPickerVisible = attachmentsPickerViewModel.isPickerVisible
    val messageMode by composerViewModel.messageMode.collectAsStateWithLifecycle()

    var isShowingDialog by rememberSaveable { mutableStateOf(false) }

    // Dismiss the keyboard when the attachments picker is shown (if instructed by ChatTheme).
    val focusManager = LocalFocusManager.current
    LaunchedEffect(isPickerVisible) {
        if (isPickerVisible) {
            focusManager.clearFocus()
        }
    }

    // Dismiss the attachments picker when the keyboard becomes visible.
    val isKeyboardVisible by isKeyboardVisibleAsState()
    LaunchedEffect(isKeyboardVisible) {
        if (isKeyboardVisible && !isShowingDialog) {
            attachmentsPickerViewModel.setPickerVisible(visible = false)
        }
    }

    val menuHeight = when {
        ChatTheme.attachmentPickerConfig.useSystemPicker -> 72.dp
        else -> 350.dp
    }

    val actions = remember(attachmentsPickerViewModel, composerViewModel) {
        val baseActions = AttachmentPickerActions.defaultActions(attachmentsPickerViewModel, composerViewModel)
        baseActions.copy(
            onCreatePollClick = {
                isShowingDialog = true
                baseActions.onCreatePollClick()
            },
            onCreatePoll = { pollConfig ->
                isShowingDialog = false
                baseActions.onCreatePoll(pollConfig)
            },
            onCreatePollDismissed = {
                isShowingDialog = false
                baseActions.onCreatePollDismissed()
            },
            onCommandSelected = { command ->
                isShowingDialog = false
                baseActions.onCommandSelected(command)
            },
            onDismiss = {
                isShowingDialog = false
                baseActions.onDismiss()
            },
        )
    }

    AnimatedVisibility(
        visible = isPickerVisible,
        enter = expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top),
    ) {
        ChatTheme.componentFactory.AttachmentPicker(
            modifier = Modifier.height(menuHeight),
            attachmentsPickerViewModel = attachmentsPickerViewModel,
            messageMode = messageMode,
            actions = actions,
        )
    }
}
