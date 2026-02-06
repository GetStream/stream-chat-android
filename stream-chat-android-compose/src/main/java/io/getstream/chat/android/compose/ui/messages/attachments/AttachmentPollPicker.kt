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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.PollPickerMode
import io.getstream.chat.android.compose.ui.components.FullscreenDialog
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerAction
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerCreatePollClick
import io.getstream.chat.android.compose.ui.messages.attachments.poll.CreatePollScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens

@Suppress("LongMethod")
@Composable
internal fun AttachmentPollPicker(
    pickerMode: PollPickerMode,
    onAttachmentPickerAction: (AttachmentPickerAction) -> Unit = {},
) {
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    val onCreatePollClick = {
        showCreateDialog = true
        onAttachmentPickerAction(AttachmentPickerCreatePollClick)
    }
    LaunchedEffect(pickerMode.autoShowCreateDialog) {
        if (pickerMode.autoShowCreateDialog) {
            onCreatePollClick()
        }
    }
    Column(
        modifier = Modifier
            .padding(
                start = StreamTokens.spacing2xl,
                end = StreamTokens.spacing2xl,
                bottom = StreamTokens.spacing3xl,
            )
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs, Alignment.CenterVertically),
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                painter = painterResource(id = R.drawable.stream_compose_ic_attachment_polls_picker),
                contentDescription = null,
                tint = ChatTheme.colors.textTertiary,
            )
            Text(
                text = stringResource(id = R.string.stream_compose_attachment_poll_picker_content),
                style = ChatTheme.typography.bodyDefault,
                color = ChatTheme.colors.textTertiary,
                textAlign = TextAlign.Center,
            )
        }
        OutlinedButton(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth(),
            onClick = onCreatePollClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ChatTheme.colors.buttonSecondaryText,
            ),
        ) {
            Text(text = stringResource(id = R.string.stream_compose_attachment_poll_picker_cta))
        }
    }
    if (showCreateDialog) {
        FullscreenDialog(onDismissRequest = { showCreateDialog = false }) {
            CreatePollScreen(
                onAttachmentPickerAction = { action ->
                    showCreateDialog = false
                    onAttachmentPickerAction(action)
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AttachmentPollPickerPreview() {
    ChatTheme {
        AttachmentPollPicker()
    }
}

@Composable
internal fun AttachmentPollPicker() {
    AttachmentPollPicker(
        pickerMode = PollPickerMode(autoShowCreateDialog = false),
    )
}
