/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.components.moderatedmessage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.common.model.ModeratedMessageOption
import io.getstream.chat.android.common.model.messageModerationOptions
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Dialog that is shown when user clicks or long taps on a moderated message. Gives the user the ability to either
 * send the message again, edit it and then send it or to delete it.
 *
 * @param message The moderated [Message] upon which the user can take action.
 * @param onDismissRequest Handler for dialog dismissal.
 * @param onSendAnyway Handler for sending the message without editing.
 * @param onEditMessage Handler for editing the message before sending again.
 * @param onDeleteMessage Handler for deleting the message.
 * @param titleText Text to be shown for the title of the dialog.
 * Defaults to [R.string.stream_ui_moderation_dialog_title].
 * @param descriptionText Text to be shown as the description of the dialog.
 * Defaults to [R.string.stream_ui_moderation_dialog_description].
 */
@Composable
public fun ModeratedMessageDialog(
    message: Message,
    onDismissRequest: () -> Unit,
    onSendAnyway: (Message) -> Unit,
    onEditMessage: (Message) -> Unit,
    onDeleteMessage: (Message) -> Unit,
    titleText: String = stringResource(id = R.string.stream_ui_moderation_dialog_title),
    descriptionText: String = stringResource(id = R.string.stream_ui_moderation_dialog_description),
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Column(
            modifier = Modifier.background(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colors.surface
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            val painter = painterResource(id = R.drawable.stream_compose_ic_flag)
            Image(
                painter = painter,
                contentDescription = "",
                colorFilter = ColorFilter.tint(ChatTheme.colors.primaryAccent),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = titleText,
                textAlign = TextAlign.Center,
                style = ChatTheme.typography.title3
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = descriptionText,
                    textAlign = TextAlign.Center,
                    style = ChatTheme.typography.body
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn {
                items(messageModerationOptions()) { option ->
                    Divider(color = ChatTheme.colors.borders)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clickable {
                                when (option) {
                                    ModeratedMessageOption.DeleteMessage -> onDeleteMessage(message)
                                    ModeratedMessageOption.EditMessage -> onEditMessage(message)
                                    ModeratedMessageOption.SendAnyway -> onSendAnyway(message)
                                }
                                onDismissRequest()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = option.text),
                            style = ChatTheme.typography.body,
                            color = ChatTheme.colors.primaryAccent
                        )
                    }
                }
            }
        }
    }
}
