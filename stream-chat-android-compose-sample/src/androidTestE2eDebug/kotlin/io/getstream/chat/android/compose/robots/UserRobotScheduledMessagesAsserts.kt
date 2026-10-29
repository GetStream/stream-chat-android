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

import androidx.test.uiautomator.BySelector
import io.getstream.chat.android.compose.pages.MessageListPage.MessageList
import io.getstream.chat.android.compose.pages.ScheduledMessagesPage.Actions
import io.getstream.chat.android.compose.pages.ScheduledMessagesPage.Banner
import io.getstream.chat.android.compose.pages.ScheduledMessagesPage.ScheduleSheet
import io.getstream.chat.android.compose.pages.ScheduledMessagesPage.Screen
import io.getstream.chat.android.compose.pages.ScheduledMessagesPage.Snackbar
import io.getstream.chat.android.e2e.test.uiautomator.device
import io.getstream.chat.android.e2e.test.uiautomator.isDisplayed
import io.getstream.chat.android.e2e.test.uiautomator.waitDisplayed
import io.getstream.chat.android.e2e.test.uiautomator.waitToDisappear
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

fun UserRobot.assertScheduledMessagesBanner(banner: BySelector): UserRobot {
    assertTrue(banner.waitDisplayed())
    return this
}

fun UserRobot.assertScheduledMessagesBannerIsHidden(): UserRobot {
    assertFalse(Banner.view.waitToDisappear().isDisplayed())
    return this
}

fun UserRobot.assertScheduledMessagesScreen(): UserRobot {
    assertTrue(Screen.title.waitDisplayed())
    return this
}

/**
 * Asserts the row whose second line is [text], once the server has it. When [time] is set, the row
 * also shows that time.
 */
fun UserRobot.assertScheduledMessage(text: String, time: String? = null): UserRobot {
    val item = if (time == null) Screen.item(text) else Screen.item(text).hasDescendant(Screen.status.text(time))
    assertTrue(item.waitDisplayed())
    assertFalse(Screen.item(text).hasDescendant(Screen.inProgress).waitToDisappear().isDisplayed())
    return this
}

/** Asserts the snackbar that tells the server has the scheduled message. */
fun UserRobot.assertScheduledMessageSaved(): UserRobot {
    assertTrue(Snackbar.scheduled.waitDisplayed())
    return this
}

fun UserRobot.assertScheduledMessageIsNotShown(text: String): UserRobot {
    assertFalse(Screen.item(text).waitToDisappear().isDisplayed())
    return this
}

/** Asserts a failed row that shows [reason] as its second line. */
fun UserRobot.assertScheduledMessageFailed(reason: String): UserRobot {
    assertTrue(Screen.failedHeader.waitDisplayed())
    assertTrue(Screen.item(reason).waitDisplayed())
    return this
}

fun UserRobot.assertScheduledMessagesAreEmpty(): UserRobot {
    assertTrue(Screen.empty.waitDisplayed())
    return this
}

/** Asserts which actions the open actions sheet offers, then closes it. */
fun UserRobot.assertScheduledMessageActions(shown: List<BySelector>, hidden: List<BySelector>): UserRobot {
    assertTrue(Actions.view.waitDisplayed())
    shown.forEach { assertTrue(it.waitDisplayed()) }
    hidden.forEach { assertFalse(it.isDisplayed()) }
    device.pressBack()
    assertFalse(Actions.view.waitToDisappear().isDisplayed())
    return this
}

/** Asserts a message in the channel that has both [text] and an image. */
fun UserRobot.assertMessageWithImage(text: String): UserRobot {
    val message = MessageList.messages
        .hasDescendant(MessageList.Message.text.text(text))
        .hasDescendant(MessageList.Message.image)
    assertTrue(message.waitDisplayed())
    return this
}

fun UserRobot.assertScheduleSheetIsNotShown(): UserRobot {
    assertFalse(ScheduleSheet.view.waitDisplayed(timeOutMillis = 3_000))
    return this
}
