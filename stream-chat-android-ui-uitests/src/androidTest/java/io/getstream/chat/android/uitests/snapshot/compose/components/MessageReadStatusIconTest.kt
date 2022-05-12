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

package io.getstream.chat.android.uitests.snapshot.compose.components

import androidx.compose.ui.test.junit4.createComposeRule
import com.karumi.shot.ScreenshotTest
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.compose.ui.components.channels.MessageReadStatusIcon
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.uitests.snapshot.compose.TestChatTheme
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Rule
import org.junit.Test

@OptIn(InternalStreamChatApi::class)
class MessageReadStatusIconTest : ScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun messageReadStatusIconForSeenMessage() {
        renderMessageReadStatusIcon(
            message = TestData.message1(),
            isMessageRead = true,
        )
    }

    @Test
    fun messageReadStatusIconForSentMessage() {
        renderMessageReadStatusIcon(
            message = TestData.message1().apply { syncStatus = SyncStatus.COMPLETED },
            isMessageRead = false,
        )
    }

    @Test
    fun messageReadStatusIconForSyncNeededMessage() {
        renderMessageReadStatusIcon(
            message = TestData.message1().apply { syncStatus = SyncStatus.SYNC_NEEDED },
            isMessageRead = false,
        )
    }

    private fun renderMessageReadStatusIcon(
        message: Message,
        isMessageRead: Boolean,
    ) {
        composeRule.setContent {
            TestChatTheme {
                MessageReadStatusIcon(
                    message = message,
                    isMessageRead = isMessageRead
                )
            }
        }
        compareScreenshot(composeRule)
    }
}
