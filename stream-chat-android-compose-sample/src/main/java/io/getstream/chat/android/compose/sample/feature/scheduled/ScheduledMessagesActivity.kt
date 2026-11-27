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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.sample.data.customSettings
import io.getstream.chat.android.compose.sample.ui.SampleChatTheme
import io.getstream.chat.android.compose.ui.scheduled.ScheduledMessagesScreen
import io.getstream.chat.android.compose.ui.theme.AttachmentPickerConfig
import io.getstream.chat.android.compose.ui.theme.ChatUiConfig
import io.getstream.chat.android.compose.ui.theme.ComposerConfig

class ScheduledMessagesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = customSettings()
        setContent {
            // The composer that edits a scheduled message looks like the one of the channel.
            SampleChatTheme(
                config = ChatUiConfig(
                    composer = ComposerConfig(
                        linkPreviewEnabled = settings.isComposerLinkPreviewEnabled,
                        floatingStyleEnabled = settings.isComposerFloatingStyleEnabled,
                    ),
                    attachmentPicker = AttachmentPickerConfig(
                        useSystemPicker = settings.isSystemAttachmentPickerEnabled,
                    ),
                ),
            ) {
                Box(modifier = Modifier.systemBarsPadding()) {
                    ScheduledMessagesScreen(onBackPressed = ::finish)
                }
            }
        }
    }
}
