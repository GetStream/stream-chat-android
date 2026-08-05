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

import io.getstream.chat.android.compose.pages.ThreadListPage
import io.getstream.chat.android.e2e.test.uiautomator.waitDisplayed
import org.junit.Assert.assertTrue

fun UserRobot.assertThreadInThreadList(parentMessageText: String, replies: Int): UserRobot {
    // The preview is prefixed with the sender name, so it is matched as a substring.
    assertTrue(ThreadListPage.parentMessagePreview.textContains(parentMessageText).waitDisplayed())
    assertTrue(ThreadListPage.repliesCountLabel(replies).waitDisplayed())
    return this
}

fun UserRobot.assertThreadListIsEmpty(): UserRobot {
    assertTrue(ThreadListPage.emptyTitle.waitDisplayed())
    return this
}
