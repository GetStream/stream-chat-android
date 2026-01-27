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

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerPollCreation
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel

/**
 * An attachments picker menu that expands and collapses.
 *
 * @param attachmentsPickerViewModel The [AttachmentsPickerViewModel] used to read state and
 * perform actions.
 * @param composerViewModel The [MessageComposerViewModel] used to read state and
 * perform actions.
 */
@Composable
public fun AttachmentsPickerMenu(
    attachmentsPickerViewModel: AttachmentsPickerViewModel,
    composerViewModel: MessageComposerViewModel,
) {
    val isShowingAttachments = attachmentsPickerViewModel.isShowingAttachments
    val messageMode by composerViewModel.messageMode.collectAsStateWithLifecycle()

    // Ensure keyboard is closed when the attachments picker is shown (if instructed by ChatTheme)
    val keyboardController = LocalSoftwareKeyboardController.current
    val shouldCloseKeyboard = ChatTheme.keyboardBehaviour.closeKeyboardOnAttachmentPickerOpen
    LaunchedEffect(isShowingAttachments) {
        if (shouldCloseKeyboard && isShowingAttachments) {
            keyboardController?.hide()
        }
    }

    // Ensure attachments picker is not visible when keyboard is visible
    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    LaunchedEffect(isKeyboardVisible) {
        if (isKeyboardVisible) {
            attachmentsPickerViewModel.changeAttachmentState(showAttachments = false)
        }
    }

    val menuHeight = when {
        ChatTheme.useDefaultSystemMediaPicker -> ChatTheme.dimens.attachmentsSystemPickerHeight
        else -> ChatTheme.dimens.attachmentsPickerHeight
    }

    val animatedHeight by animateDpAsState(
        targetValue = if (isShowingAttachments) menuHeight else 0.dp,
        label = "actions menu height",
    )

    AttachmentsPicker(
        attachmentsPickerViewModel = attachmentsPickerViewModel,
        modifier = Modifier
            .height(animatedHeight),
        onAttachmentsSelected = { attachments ->
            attachmentsPickerViewModel.changeAttachmentState(showAttachments = false)
            composerViewModel.addSelectedAttachments(attachments)
        },
        onAttachmentPickerAction = { action ->
            if (action is AttachmentPickerPollCreation) {
                composerViewModel.createPoll(action.pollConfig)
            }
        },
        messageMode = messageMode,
    )
}
