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
import io.getstream.chat.android.compose.pages.MessageListPage.Composer
import io.getstream.chat.android.compose.pages.MessageListPage.MessageList
import io.getstream.chat.android.compose.pages.ScheduledMessagesPage.Actions
import io.getstream.chat.android.compose.pages.ScheduledMessagesPage.Banner
import io.getstream.chat.android.compose.pages.ScheduledMessagesPage.Editor
import io.getstream.chat.android.compose.pages.ScheduledMessagesPage.ScheduleSheet
import io.getstream.chat.android.compose.pages.ScheduledMessagesPage.ScheduleTimeSheet
import io.getstream.chat.android.compose.pages.ScheduledMessagesPage.Screen
import io.getstream.chat.android.compose.pages.ScheduledMessagesPage.Snackbar
import io.getstream.chat.android.e2e.test.uiautomator.device
import io.getstream.chat.android.e2e.test.uiautomator.isDisplayed
import io.getstream.chat.android.e2e.test.uiautomator.isEnabled
import io.getstream.chat.android.e2e.test.uiautomator.waitDisplayed
import io.getstream.chat.android.e2e.test.uiautomator.waitToAppear
import io.getstream.chat.android.e2e.test.uiautomator.waitToDisappear
import org.junit.Assert.assertEquals
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
 * Asserts the row whose preview ends with [text]. When [time] is set, the row is sent at that time.
 */
fun UserRobot.assertScheduledMessage(text: String, time: String? = null): UserRobot {
    val item = if (time == null) Screen.item(text) else Screen.item(text).hasDescendant(Screen.date(time))
    assertTrue(item.waitDisplayed())
    return this
}

/** Asserts the row whose preview ends with [text] shows the avatar and the name of the channel [channelName]. */
fun UserRobot.assertScheduledMessageChannel(text: String, channelName: String): UserRobot {
    val item = Screen.item(text).hasDescendant(Screen.channelName(channelName)).hasDescendant(Screen.channelAvatar)
    assertTrue(item.waitDisplayed())
    return this
}

/** Asserts the row of [text] with [time] is still in the list after waiting for it to leave. */
fun UserRobot.assertScheduledMessageStays(text: String, time: String): UserRobot {
    val item = Screen.item(text).hasDescendant(Screen.date(time))
    assertTrue(item.waitToDisappear().isDisplayed())
    return this
}

/** Asserts the row of the reply [text] says which message it quotes. */
fun UserRobot.assertScheduledMessageReply(text: String): UserRobot {
    assertTrue(Screen.reply(text).waitDisplayed())
    return this
}

/**
 * Asserts the list opened from the banner shows the row whose preview ends with [preview]. Then
 * goes back to the channel.
 */
fun UserRobot.assertScheduledMessageListedFromBanner(preview: String): UserRobot {
    openScheduledMessagesFromBanner()
    assertScheduledMessage(preview)
    pressBack()
    return this
}

/** Asserts the snackbar that tells a message scheduled offline was sent right away. */
fun UserRobot.assertScheduledMessageSentSnackbar(): UserRobot {
    assertTrue(Snackbar.sent.waitDisplayed())
    return this
}

/** Asserts the composer of the list edits the scheduled message [text]. */
fun UserRobot.assertScheduledMessageEditor(text: String): UserRobot {
    assertTrue(Editor.header.waitDisplayed())
    assertEquals(text, Composer.inputField.waitToAppear().text)
    return this
}

/** Asserts the edited scheduled message can be saved, or not. */
fun UserRobot.assertScheduledMessageCanBeSaved(canBeSaved: Boolean): UserRobot {
    assertTrue(Composer.saveButton.enabled(canBeSaved).waitDisplayed())
    return this
}

/** Asserts the banner that tells the edited message quotes a deleted message, or that it is hidden. */
fun UserRobot.assertScheduledMessageQuoteDeletedBanner(isDisplayed: Boolean = true): UserRobot {
    if (isDisplayed) {
        assertTrue(Editor.quoteDeletedBanner.waitDisplayed())
    } else {
        assertFalse(Editor.quoteDeletedBanner.waitToDisappear().isDisplayed())
    }
    return this
}

/** Asserts the notice that commands can't be scheduled. */
fun UserRobot.assertScheduledMessageCommandNotAllowed(): UserRobot {
    assertTrue(Editor.commandNotAllowed.waitDisplayed())
    return this
}

/** Asserts the row of the message [text] no longer shows it as a reply. */
fun UserRobot.assertScheduledMessageIsNotAReply(text: String): UserRobot {
    assertFalse(Screen.reply(text).waitToDisappear().isDisplayed())
    return this
}

fun UserRobot.assertScheduledMessageIsNotShown(text: String): UserRobot {
    assertFalse(Screen.item(text).waitToDisappear().isDisplayed())
    return this
}

/** Asserts the row of the message [text] shows why it failed: [reason]. */
fun UserRobot.assertScheduledMessageFailed(text: String, reason: String): UserRobot {
    assertTrue(Screen.item(text).hasDescendant(Screen.status(reason)).waitDisplayed())
    return this
}

fun UserRobot.assertScheduledMessagesLoadFailed(): UserRobot {
    assertTrue(Screen.loadFailed.waitDisplayed())
    assertTrue(Screen.retryButton.isDisplayed())
    return this
}

/** Asserts the notice that the message the user acted on was already sent or deleted elsewhere. */
fun UserRobot.assertScheduledMessageGoneNotice(): UserRobot {
    assertTrue(Snackbar.gone.waitDisplayed())
    return this
}

/** Asserts the notice that a message the user deleted had already been sent, so it is in the channel. */
fun UserRobot.assertScheduledMessageDeletedAlreadySentNotice(): UserRobot {
    assertTrue(Snackbar.deletedAlreadySent.waitDisplayed())
    return this
}

/** Asserts the notice that a message sent now while offline is sent once back online. */
fun UserRobot.assertScheduledMessageSendNowQueued(): UserRobot {
    assertTrue(Snackbar.sendNowQueued.waitDisplayed())
    return this
}

fun UserRobot.assertScheduledMessageActionsAreClosed(): UserRobot {
    assertFalse(Actions.view.waitToDisappear().isDisplayed())
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

/** Asserts the open schedule sheet says the channel has [limit] scheduled messages, and its options are disabled. */
fun UserRobot.assertScheduleSheetLimitReached(limit: Int): UserRobot {
    assertTrue(ScheduleSheet.limitReachedTitle(limit).waitDisplayed())
    assertFalse(ScheduleSheet.tomorrowMorning.isEnabled())
    return this
}

/** Asserts the open time sheet shows the [error] note and can't schedule. */
fun UserRobot.assertScheduleTimeSheetError(error: String): UserRobot {
    assertTrue(ScheduleTimeSheet.error(error).waitDisplayed())
    assertFalse(ScheduleTimeSheet.confirmButton.isEnabled())
    return this
}
