/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.pages.MessageListPage
import io.getstream.chat.android.compose.pages.MessageListPage.Composer
import io.getstream.chat.android.compose.pages.MessageListPage.MessageList.Message
import io.getstream.chat.android.compose.uiautomator.appContext
import io.getstream.chat.android.compose.uiautomator.findObject
import io.getstream.chat.android.compose.uiautomator.findObjects
import io.getstream.chat.android.compose.uiautomator.height
import io.getstream.chat.android.compose.uiautomator.isDisplayed
import io.getstream.chat.android.compose.uiautomator.wait
import io.getstream.chat.android.compose.uiautomator.waitForText
import io.getstream.chat.android.compose.uiautomator.waitToAppear
import io.getstream.chat.android.compose.uiautomator.waitToDisappear
import io.getstream.chat.android.e2e.test.mockserver.MessageReadStatus
import io.getstream.chat.android.e2e.test.robots.ParticipantRobot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue

fun UserRobot.assertMessage(text: String, isDisplayed: Boolean = true): UserRobot {
    if (isDisplayed) {
        assertEquals(text, Message.text.waitToAppear().waitForText(text).text)
        assertTrue(Message.text.isDisplayed())
        assertTrue(Message.timestamp.isDisplayed())
    } else {
        MessageListPage.MessageList.messages.findObjects().forEach {
            assertTrue(it.text != text)
        }
    }
    return this
}

fun UserRobot.assertMessageAuthor(isCurrentUser: Boolean): UserRobot {
    assertNotEquals(isCurrentUser, Message.authorName.isDisplayed())
    assertNotEquals(isCurrentUser, Message.avatar.isDisplayed())
    return this
}

fun UserRobot.assertMessageReadStatus(status: MessageReadStatus): UserRobot {
    when (status) {
        MessageReadStatus.READ -> assertTrue(Message.readStatusIsRead.wait().isDisplayed())
        MessageReadStatus.PENDING -> assertTrue(Message.readStatusIsPending.wait().isDisplayed())
        MessageReadStatus.SENT -> assertTrue(Message.readStatusIsSent.wait().isDisplayed())
    }
    return this
}

fun UserRobot.assertMessageFailedIcon(isDisplayed: Boolean): UserRobot {
    if (isDisplayed) {
        assertTrue(Message.failedIcon.wait().isDisplayed())
    } else {
        assertFalse(Message.failedIcon.waitToDisappear().isDisplayed())
    }
    return this
}

fun UserRobot.assertEditedMessage(text: String): UserRobot {
    assertMessage(text)
    assertEquals(
        appContext.getString(R.string.stream_compose_message_list_footnote_edited),
        Message.editedLabel.waitToAppear().text
    )
    return this
}

fun UserRobot.assertMessageSizeChangesAfterEditing(linesCountShouldBeIncreased: Boolean): UserRobot {
    val cellHeight = MessageListPage.MessageList.messages.waitToAppear(withIndex = 0).height
    val messageText = Message.text.findObject().text
    val newLine = "new line"
    val newText = if (linesCountShouldBeIncreased) "ok\n${messageText}\n${newLine}" else newLine

    editMessage(newText)
    assertMessage(newText)

    val updatedCellHeight = MessageListPage.MessageList.messages.findObjects().first().height
    if (linesCountShouldBeIncreased) {
        assertTrue(cellHeight < updatedCellHeight)
    } else {
        assertTrue(cellHeight > updatedCellHeight)
    }
    return this
}

fun UserRobot.assertComposerSize(isChangeable: Boolean): UserRobot {
    val composer = Composer.inputField
    val composerHeight: Int
    if (isChangeable) {
        composerHeight = composer.findObject().height
        val text = "1\n2\n3"
        typeText(text)
    } else {
        val text = "1\n2\n3\n4\n5"
        typeText(text)
        composerHeight = composer.findObject().height
        typeText("${text}\n6")
    }
    val updatedComposerHeight = composer.findObject().height
    assertEquals(composerHeight, updatedComposerHeight)
    return this
}

fun UserRobot.assertTypingIndicator(isDisplayed: Boolean): UserRobot {
    if (isDisplayed) {
        assertEquals(
            appContext.resources.getQuantityString(
                R.plurals.stream_compose_message_list_header_typing_users,
                1,
                ParticipantRobot.name
            ),
            MessageListPage.MessageList.typingIndicator.waitToAppear().text
        )
    } else {
        assertFalse(MessageListPage.MessageList.typingIndicator.waitToDisappear().isDisplayed())
    }
    return this
}

fun UserRobot.assertAttachmentsMenu(isDisplayed: Boolean): UserRobot {
    if (isDisplayed) {
        assertTrue(MessageListPage.AttachmentPicker.view.waitToAppear().isDisplayed())
    } else {
        assertFalse(MessageListPage.AttachmentPicker.view.waitToDisappear().isDisplayed())
    }
    return this
}

fun UserRobot.assertComposerCommandsMenu(isDisplayed: Boolean): UserRobot {
    if (isDisplayed) {
        assertTrue(Composer.suggestionList.waitToAppear().isDisplayed())
        assertTrue(Composer.suggestionListTitle.isDisplayed())
    } else {
        assertFalse(Composer.suggestionList.waitToDisappear().isDisplayed())
        assertFalse(Composer.suggestionListTitle.isDisplayed())
    }
    return this
}

fun UserRobot.assertComposerMentionsMenu(isDisplayed: Boolean): UserRobot {
    if (isDisplayed) {
        assertTrue(Composer.participantMentionSuggestion.waitToAppear().isDisplayed())
    } else {
        assertFalse(Composer.participantMentionSuggestion.waitToDisappear().isDisplayed())
    }
    return this
}

fun UserRobot.assertScrollToBottomButton(isDisplayed: Boolean): UserRobot {
    if (isDisplayed) {
        assertTrue(MessageListPage.MessageList.scrollToBottomButton.waitToAppear().isDisplayed())
    } else {
        assertFalse(MessageListPage.MessageList.scrollToBottomButton.waitToDisappear().isDisplayed())
    }
    return this
}

