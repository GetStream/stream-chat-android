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

package io.getstream.chat.android.compose.sample.feature.scheduled

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.sample.ui.SampleChatTheme
import io.getstream.chat.android.compose.sample.ui.channel.ChannelActivity
import io.getstream.chat.android.models.ScheduledMessage

/**
 * Lists the current user's scheduled messages.
 */
class ScheduledMessagesActivity : ComponentActivity() {

    private val viewModel: ScheduledMessagesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleChatTheme {
                ScheduledMessagesScreen(
                    modifier = Modifier.statusBarsPadding(),
                    viewModel = viewModel,
                    onEditClick = ::openEditor,
                    onBack = ::finish,
                )
            }
        }
    }

    private fun openEditor(scheduledMessage: ScheduledMessage, isScheduledAgain: Boolean) {
        startActivity(
            ChannelActivity.createIntent(
                context = this,
                channelId = scheduledMessage.cid,
                parentMessageId = scheduledMessage.message.parentId,
                scheduledMessageId = scheduledMessage.id,
                isScheduledAgain = isScheduledAgain,
            ),
        )
    }
}
