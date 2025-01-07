/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.messages.attachments.poll

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * A dialog to ask if a user wants to discard the current poll creation information.
 *
 * @param onCancelClicked A lambda will be invoked when a user clicks on the cancel button.
 * @param onDiscardClicked A lambda will be invoked when a user clicks on the discard button.
 */
@Composable
public fun PollCreationDiscardDialog(
    usePlatformDefaultWidth: Boolean = false,
    onCancelClicked: () -> Unit,
    onDiscardClicked: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDiscardClicked,
        properties = DialogProperties(
            usePlatformDefaultWidth = usePlatformDefaultWidth,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(ChatTheme.colors.overlay),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 34.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ChatTheme.colors.appBackground),
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp),
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 8.dp),
                        text = stringResource(id = R.string.stream_compose_poll_option_discard_dialog_title),
                        color = ChatTheme.colors.textHighEmphasis,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                    )

                    Text(
                        text = stringResource(id = R.string.stream_compose_poll_option_discard_dialog_description),
                        color = ChatTheme.colors.textHighEmphasis,
                        fontSize = 17.sp,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        modifier = Modifier
                            .padding(end = 32.dp)
                            .clickable { onCancelClicked.invoke() },
                        text = stringResource(id = R.string.stream_compose_poll_option_discard_dialog_cancel),
                        color = ChatTheme.colors.primaryAccent,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                    )

                    Text(
                        modifier = Modifier.clickable { onDiscardClicked.invoke() },
                        text = stringResource(id = R.string.stream_compose_poll_option_discard_dialog_confirm),
                        color = ChatTheme.colors.primaryAccent,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PollCreationDiscardDialogPreview() {
    ChatTheme {
        PollCreationDiscardDialog(
            onCancelClicked = {},
            onDiscardClicked = {},
        )
    }
}
