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

package io.getstream.chat.android.uitests.snapshot.compose.components

import io.getstream.chat.android.compose.ui.components.channels.MessageReadStatusIcon
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.uitests.snapshot.compose.ComposeScreenshotTest
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Test

class MessageReadStatusIconTest : ComposeScreenshotTest() {

    @Test
    fun messageReadStatusIconForSeenMessage() = runScreenshotTest {
        MessageReadStatusIcon(
            message = TestData.message1(),
            isMessageRead = true,
        )
    }

    @Test
    fun messageReadStatusIconForSentMessage() = runScreenshotTest {
        MessageReadStatusIcon(
            message = TestData.message1().copy(syncStatus = SyncStatus.COMPLETED),
            isMessageRead = false,
        )
    }

    @Test
    fun messageReadStatusIconForSyncNeededMessage() = runScreenshotTest {
        MessageReadStatusIcon(
            message = TestData.message1().copy(syncStatus = SyncStatus.SYNC_NEEDED),
            isMessageRead = false,
        )
    }
}
