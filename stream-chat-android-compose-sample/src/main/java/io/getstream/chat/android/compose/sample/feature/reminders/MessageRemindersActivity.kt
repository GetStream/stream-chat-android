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

package io.getstream.chat.android.compose.sample.feature.reminders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.sample.ui.MessagesActivity
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message

/**
 * Activity displaying the list of message reminders.
 */
class MessageRemindersActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatTheme {
                MessageRemindersScreen(
                    modifier = Modifier.statusBarsPadding(),
                    onReminderClick = { reminder ->
                        reminder.message?.let(::openMessages)
                    },
                    onBack = ::finish,
                )
            }
        }
    }

    private fun openMessages(message: Message) {
        startActivity(
            MessagesActivity.createIntent(
                context = this,
                channelId = message.cid,
                messageId = message.id,
                parentMessageId = message.parentId,
            ),
        )
    }
}
