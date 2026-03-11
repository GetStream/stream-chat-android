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

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

internal class MessageFooterTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `own message footer`() {
        snapshotWithDarkModeRow {
            MessageFooter(
                messageItem = MessageItemState(
                    message = Message(
                        id = "msg-1",
                        text = "Hello!",
                        createdAt = FixedDate,
                        user = PreviewUserData.user1,
                    ),
                    isMine = true,
                    showMessageFooter = true,
                    ownCapabilities = ChannelCapabilities.toSet(),
                ),
            )
        }
    }

    @Test
    fun `other user message footer`() {
        snapshotWithDarkModeRow {
            MessageFooter(
                messageItem = MessageItemState(
                    message = Message(
                        id = "msg-2",
                        text = "Hey there!",
                        createdAt = FixedDate,
                        user = PreviewUserData.user2,
                    ),
                    isMine = false,
                    showMessageFooter = true,
                    ownCapabilities = ChannelCapabilities.toSet(),
                ),
            )
        }
    }

    @Test
    fun `edited message footer`() {
        snapshotWithDarkModeRow {
            MessageFooter(
                messageItem = MessageItemState(
                    message = Message(
                        id = "msg-4",
                        text = "Edited message",
                        createdAt = FixedDate,
                        messageTextUpdatedAt = FixedDate,
                        user = PreviewUserData.user2,
                    ),
                    isMine = false,
                    showMessageFooter = true,
                    ownCapabilities = ChannelCapabilities.toSet(),
                ),
            )
        }
    }

    @Test
    fun `edited and deleted message footer`() {
        snapshotWithDarkModeRow {
            MessageFooter(
                messageItem = MessageItemState(
                    message = Message(
                        id = "msg-edited-deleted",
                        text = "Edited and deleted message",
                        createdAt = FixedDate,
                        messageTextUpdatedAt = FixedDate,
                        deletedAt = FixedDate,
                        user = PreviewUserData.user2,
                    ),
                    isMine = false,
                    showMessageFooter = true,
                    ownCapabilities = ChannelCapabilities.toSet(),
                ),
            )
        }
    }

    @Test
    fun `thread start with no replies`() {
        snapshotWithDarkModeRow {
            MessageFooter(
                messageItem = MessageItemState(
                    message = Message(
                        id = "msg-5",
                        text = "Thread start",
                        createdAt = FixedDate,
                        replyCount = 0,
                        threadParticipants = listOf(PreviewUserData.user1),
                    ),
                    isMine = false,
                    isInThread = false,
                    showMessageFooter = false,
                    ownCapabilities = ChannelCapabilities.toSet(),
                ),
            )
        }
    }

    @Test
    fun `thread start with replies`() {
        snapshotWithDarkModeRow {
            MessageFooter(
                messageItem = MessageItemState(
                    message = Message(
                        id = "msg-6",
                        text = "Thread with replies",
                        createdAt = FixedDate,
                        replyCount = 5,
                        threadParticipants = listOf(PreviewUserData.user1, PreviewUserData.user2),
                    ),
                    isMine = false,
                    isInThread = false,
                    showMessageFooter = false,
                    ownCapabilities = ChannelCapabilities.toSet(),
                ),
            )
        }
    }

    @Test
    fun `thread start with footer`() {
        snapshotWithDarkModeRow {
            MessageFooter(
                messageItem = MessageItemState(
                    message = Message(
                        id = "msg-7",
                        text = "Thread start with footer",
                        createdAt = FixedDate,
                        replyCount = 3,
                        threadParticipants = listOf(PreviewUserData.user3),
                        user = PreviewUserData.user2,
                    ),
                    isMine = false,
                    isInThread = false,
                    showMessageFooter = true,
                    ownCapabilities = ChannelCapabilities.toSet(),
                ),
            )
        }
    }
}

private val FixedDate: Date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
    set(2022, 1, 1, 10, 30, 0)
    set(Calendar.MILLISECOND, 0)
}.time
