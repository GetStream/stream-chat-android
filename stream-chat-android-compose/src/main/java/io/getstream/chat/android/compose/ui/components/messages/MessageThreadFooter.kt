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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.state.messages.MessageAlignment
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.User

/**
 * Shows a row of participants in the message thread, if they exist.
 *
 * @param participants List of users in the thread.
 * @param text Text of the label.
 * @param messageAlignment The alignment of the message, used for the content orientation.
 * @param modifier Modifier for styling.
 */
@Composable
public fun MessageThreadFooter(
    participants: List<User>,
    text: String,
    messageAlignment: MessageAlignment,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.padding(top = 4.dp)) {
        if (messageAlignment == MessageAlignment.Start) {
            ThreadParticipants(
                modifier = Modifier
                    .padding(end = 4.dp),
                participants = participants,
                alignment = messageAlignment,
            )
        }

        Text(
            modifier = Modifier.testTag("Stream_ThreadRepliesLabel"),
            text = text,
            style = ChatTheme.typography.footnoteBold,
            color = ChatTheme.colors.primaryAccent,
        )

        if (messageAlignment == MessageAlignment.End) {
            ThreadParticipants(
                modifier = Modifier
                    .padding(start = 4.dp),
                participants = participants,
                alignment = messageAlignment,
            )
        }
    }
}
