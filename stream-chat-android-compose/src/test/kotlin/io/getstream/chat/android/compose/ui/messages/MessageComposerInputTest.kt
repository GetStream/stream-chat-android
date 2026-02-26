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

package io.getstream.chat.android.compose.ui.messages

import androidx.compose.ui.Alignment
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.compose.ui.components.composer.MessageComposerInputAttachments
import io.getstream.chat.android.compose.ui.components.composer.MessageComposerInputAttachmentsAndLink
import io.getstream.chat.android.compose.ui.components.composer.MessageComposerInputEdit
import io.getstream.chat.android.compose.ui.components.composer.MessageComposerInputEditAttachmentsAndLink
import io.getstream.chat.android.compose.ui.components.composer.MessageComposerInputFilled
import io.getstream.chat.android.compose.ui.components.composer.MessageComposerInputLink
import io.getstream.chat.android.compose.ui.components.composer.MessageComposerInputOverflow
import io.getstream.chat.android.compose.ui.components.composer.MessageComposerInputPlaceholder
import io.getstream.chat.android.compose.ui.components.composer.MessageComposerInputReply
import io.getstream.chat.android.compose.ui.components.composer.MessageComposerInputReplyAttachmentsAndLink
import io.getstream.chat.android.compose.ui.components.composer.MessageComposerInputSlowMode
import org.junit.Rule
import org.junit.Test

internal class MessageComposerInputTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun placeholder() {
        snapshotWithDarkMode(contentAlignment = Alignment.BottomCenter) {
            MessageComposerInputPlaceholder()
        }
    }

    @Test
    fun filled() {
        snapshotWithDarkMode(contentAlignment = Alignment.BottomCenter) {
            MessageComposerInputFilled()
        }
    }

    @Test
    fun overflow() {
        snapshotWithDarkMode(contentAlignment = Alignment.BottomCenter) {
            MessageComposerInputOverflow()
        }
    }

    @Test
    fun `slow mode`() {
        snapshotWithDarkMode(contentAlignment = Alignment.BottomCenter) {
            MessageComposerInputSlowMode()
        }
    }

    @Test
    fun attachments() {
        snapshotWithDarkMode(contentAlignment = Alignment.BottomCenter) {
            MessageComposerInputAttachments()
        }
    }

    @Test
    fun link() {
        snapshotWithDarkMode(contentAlignment = Alignment.BottomCenter) {
            MessageComposerInputLink()
        }
    }

    @Test
    fun reply() {
        snapshotWithDarkMode(contentAlignment = Alignment.BottomCenter) {
            MessageComposerInputReply()
        }
    }

    @Test
    fun edit() {
        snapshotWithDarkMode(contentAlignment = Alignment.BottomCenter) {
            MessageComposerInputEdit()
        }
    }

    @Test
    fun `edit empty`() {
        snapshotWithDarkMode(contentAlignment = Alignment.BottomCenter) {
            MessageComposerInputEdit()
        }
    }

    @Test
    fun `attachments and link`() {
        snapshotWithDarkMode(contentAlignment = Alignment.BottomCenter) {
            MessageComposerInputAttachmentsAndLink()
        }
    }

    @Test
    fun `reply, attachments, and link`() {
        snapshotWithDarkMode(contentAlignment = Alignment.BottomCenter) {
            MessageComposerInputReplyAttachmentsAndLink()
        }
    }

    @Test
    fun `edit, attachments, and link`() {
        snapshotWithDarkMode(contentAlignment = Alignment.BottomCenter) {
            MessageComposerInputEditAttachmentsAndLink()
        }
    }
}
