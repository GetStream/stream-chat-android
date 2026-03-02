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

import io.getstream.chat.android.compose.pages.ChannelListPage.ChannelList.Channel
import io.getstream.chat.android.compose.uiautomator.isDisplayed
import io.getstream.chat.android.compose.uiautomator.waitToAppear
import io.getstream.chat.android.compose.uiautomator.waitToDisappear
import io.getstream.chat.android.e2e.test.robots.ParticipantRobot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

fun UserRobot.assertChannelAvatar(): UserRobot {
    assertTrue(Channel.avatar.isDisplayed())
    return this
}

fun UserRobot.assertMessageInChannelPreview(text: String, fromCurrentUser: Boolean? = null): UserRobot {
    val expectedPreview = when (fromCurrentUser) {
        true -> "You: $text"
        false -> "${ParticipantRobot.name}: $text"
        null -> text
    }
    assertEquals(expectedPreview, Channel.messagePreview.waitToAppear().text.trimEnd())
    return this
}

fun UserRobot.assertMessagePreviewTimestamp(isDisplayed: Boolean = true): UserRobot {
    if (isDisplayed) {
        assertTrue(Channel.timestamp.waitToAppear().isDisplayed())
    } else {
        assertFalse(Channel.timestamp.waitToDisappear().isDisplayed())
    }
    return this
}
