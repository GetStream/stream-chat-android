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

package io.getstream.chat.android.compose.robots

import androidx.test.uiautomator.By
import io.getstream.chat.android.compose.pages.PinnedMessagesPage
import io.getstream.chat.android.e2e.test.uiautomator.waitDisplayed
import org.junit.Assert.assertTrue

fun UserRobot.assertPinnedMessagesScreen(): UserRobot {
    assertTrue(PinnedMessagesPage.title.waitDisplayed())
    return this
}

fun UserRobot.assertMessageInPinnedMessages(text: String): UserRobot {
    // The row renders the message text through the preview formatter, which appends a trailing
    // space to it, so the text is matched by its start.
    assertTrue(By.textStartsWith(text).waitDisplayed())
    return this
}

fun UserRobot.assertPinnedMessagesAreEmpty(): UserRobot {
    assertTrue(PinnedMessagesPage.emptyTitle.waitDisplayed())
    return this
}
