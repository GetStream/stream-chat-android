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

import android.annotation.SuppressLint
import androidx.test.uiautomator.By
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.pages.MessageListPage
import io.getstream.chat.android.compose.pages.MessageListPage.Composer
import io.getstream.chat.android.compose.pages.MessageListPage.MessageList.Message
import io.getstream.chat.android.compose.pages.ThreadPage
import io.getstream.chat.android.compose.uiautomator.appContext
import io.getstream.chat.android.compose.uiautomator.device
import io.getstream.chat.android.compose.uiautomator.findObject
import io.getstream.chat.android.compose.uiautomator.findObjects
import io.getstream.chat.android.compose.uiautomator.height
import io.getstream.chat.android.compose.uiautomator.isDisplayed
import io.getstream.chat.android.compose.uiautomator.retryOnStaleObjectException
import io.getstream.chat.android.compose.uiautomator.wait
import io.getstream.chat.android.compose.uiautomator.waitForCount
import io.getstream.chat.android.compose.uiautomator.waitForText
import io.getstream.chat.android.compose.uiautomator.waitToAppear
import io.getstream.chat.android.compose.uiautomator.waitToDisappear
import io.getstream.chat.android.e2e.test.mockserver.MessageDeliveryStatus
import io.getstream.chat.android.e2e.test.mockserver.ReactionType
import io.getstream.chat.android.e2e.test.robots.ParticipantRobot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue

fun UserRobot.assertMessage(
    text: String,
    isDisplayed: Boolean = true,
    isClickable: Boolean = false,
): UserRobot {
    if (isDisplayed) {
        val textLocator = if (isClickable) Message.clickableText else Message.text
        assertEquals(text, textLocator.waitToAppear().waitForText(text).text)
        assertTrue(textLocator.isDisplayed())
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

fun UserRobot.assertMessageTimestamps(count: Int): UserRobot {
    assertEquals(count, Message.timestamp.findObjects().size)
    return this
}

fun UserRobot.assertMessageDeliveryStatus(status: MessageDeliveryStatus, count: Int? = null): UserRobot {
    when (status) {
        MessageDeliveryStatus.READ -> {
            assertTrue(Message.deliveryStatusIsRead.wait().isDisplayed())
            if (count != null) {
                assertEquals(count, Message.deliveryStatusIsRead.waitForCount(count).size)
            }
        }
        MessageDeliveryStatus.PENDING -> {
            assertTrue(Message.deliveryStatusIsPending.wait().isDisplayed())
            if (count != null) {
                assertEquals(count, Message.deliveryStatusIsPending.waitForCount(count).size)
            }
        }
        MessageDeliveryStatus.SENT -> {
            assertTrue(Message.deliveryStatusIsSent.wait().isDisplayed())
            if (count != null) {
                assertEquals(count, Message.deliveryStatusIsSent.waitForCount(count).size)
            }
        }
        MessageDeliveryStatus.FAILED -> {
            assertTrue(Message.deliveryStatusIsFailed.wait().isDisplayed())
            if (count != null) {
                assertEquals(count, Message.deliveryStatusIsFailed.waitForCount(count).size)
            }
        }
        MessageDeliveryStatus.NIL -> {
            assertFalse(Message.deliveryStatusIsRead.waitToDisappear().isDisplayed())
            assertFalse(Message.deliveryStatusIsPending.waitToDisappear().isDisplayed())
            assertFalse(Message.deliveryStatusIsSent.waitToDisappear().isDisplayed())
        }
    }
    return this
}

fun UserRobot.assertMessageFailedIcon(isDisplayed: Boolean): UserRobot {
    if (isDisplayed) {
        assertTrue(Message.deliveryStatusIsFailed.wait().isDisplayed())
    } else {
        assertFalse(Message.deliveryStatusIsFailed.waitToDisappear().isDisplayed())
    }
    return this
}

fun UserRobot.assertEditedMessage(text: String): UserRobot {
    assertMessage(text)
    assertEquals(
        appContext.getString(R.string.stream_compose_message_list_footnote_edited),
        Message.editedLabel.waitToAppear().text,
    )
    return this
}

fun UserRobot.assertDeletedMessage(text: String? = null, hard: Boolean = false): UserRobot {
    if (hard) {
        assertFalse(Message.deletedMessage.isDisplayed())
    } else {
        Message.deletedMessage.waitToAppear()
        assertTrue(Message.deletedMessage.isDisplayed())
        assertTrue(Message.timestamp.isDisplayed())
    }
    if (text != null) {
        assertMessage(text, isDisplayed = false)
    }
    return this
}

fun UserRobot.assertQuotedMessage(text: String, quote: String = "", isDisplayed: Boolean = true): UserRobot {
    if (isDisplayed) {
        assertEquals(quote, Message.quotedMessage.waitToAppear().text)
        assertTrue(Message.quotedMessageAvatar.isDisplayed())
    } else {
        assertFalse(Message.quotedMessage.waitToDisappear().isDisplayed())
    }
    assertMessage(text, isDisplayed = isDisplayed)
    return this
}

fun UserRobot.assertMessageSizeChangesAfterEditing(linesCountShouldBeIncreased: Boolean): UserRobot {
    val cellHeight = MessageListPage.MessageList.messages.waitToAppear(withIndex = 0).height
    val messageText = Message.text.findObject().text
    val newLine = "new line"
    val newText = if (linesCountShouldBeIncreased) "ok\n${messageText}\n$newLine" else newLine

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
    val initialComposerHeight: Int
    if (isChangeable) {
        initialComposerHeight = composer.waitToAppear().height
        val text = "1\n2\n3"
        typeText(text)
        sleep(500)
        assertTrue(initialComposerHeight != composer.findObject().height)
    } else {
        val text = "1\n2\n3\n4\n5\n6"
        typeText(text)
        sleep(500)
        initialComposerHeight = composer.findObject().height
        typeText("${text}\n7")
        assertEquals(initialComposerHeight, composer.findObject().height)
    }
    return this
}

fun UserRobot.assertTypingIndicator(isDisplayed: Boolean): UserRobot {
    if (isDisplayed) {
        assertEquals(
            appContext.resources.getQuantityString(
                R.plurals.stream_compose_message_list_header_typing_users,
                1,
                ParticipantRobot.name,
            ),
            MessageListPage.MessageList.typingIndicator.waitToAppear().text,
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

fun UserRobot.assertMentionWasApplied(): UserRobot {
    val additionalSpace = " "
    val userName = ParticipantRobot.name
    val expectedText = "@${userName}$additionalSpace"
    val actualText = Composer.inputField.findObject().waitForText(expectedText).text
    assertEquals(expectedText, actualText)
    return this
}

fun UserRobot.assertComposerText(expectedText: String): UserRobot {
    val actualText = Composer.inputField.waitToAppear().text
    assertEquals(expectedText, actualText)
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

fun UserRobot.assertThreadIsOpen(): UserRobot {
    assertTrue(ThreadPage.ThreadList.alsoSendToChannelCheckbox.waitToAppear().isDisplayed())
    return this
}

fun UserRobot.assertThreadMessage(text: String): UserRobot {
    assertThreadIsOpen()
    assertMessage(text)
    return this
}

fun UserRobot.assertThreadReplyLabelOnParentMessage(): UserRobot {
    assertEquals(
        appContext.getString(R.string.stream_compose_message_list_thread_footnote_thread_reply),
        Message.threadRepliesLabel.waitToAppear().text,
    )
    assertTrue(Message.threadParticipantAvatar.isDisplayed())
    return this
}

fun UserRobot.assertThreadReplyLabelOnThreadMessage(): UserRobot {
    assertEquals(
        appContext.getString(R.string.stream_compose_thread_reply),
        Message.threadRepliesLabel.waitToAppear().text,
    )
    assertTrue(Message.threadParticipantAvatar.isDisplayed())
    return this
}

fun UserRobot.assertAlsoInTheChannelLabelInChannel(): UserRobot {
    assertEquals(
        appContext.getString(R.string.stream_compose_replied_to_thread),
        Message.messageHeaderLabel.waitToAppear().text,
    )
    return this
}

fun UserRobot.assertAlsoInTheChannelLabelInThread(): UserRobot {
    assertEquals(
        appContext.getString(R.string.stream_compose_also_sent_to_channel),
        Message.messageHeaderLabel.waitToAppear().text,
    )
    return this
}

fun UserRobot.assertGiphyImage(isDisplayed: Boolean = true): UserRobot {
    if (isDisplayed) {
        assertTrue(Message.giphy.waitToAppear().isDisplayed())
    } else {
        assertFalse(Message.giphy.waitToDisappear().isDisplayed())
    }
    return this
}

fun UserRobot.assertGiphyButtons(areDisplayed: Boolean = true): UserRobot {
    if (areDisplayed) {
        assertTrue(Message.GiphyButtons.send.waitToAppear().isDisplayed())
        assertTrue(Message.GiphyButtons.cancel.findObject().isDisplayed())
        assertTrue(Message.GiphyButtons.shuffle.findObject().isDisplayed())
    } else {
        assertFalse(Message.GiphyButtons.send.waitToDisappear().isDisplayed())
        assertTrue(Message.GiphyButtons.cancel.findObjects().isEmpty())
        assertTrue(Message.GiphyButtons.shuffle.findObjects().isEmpty())
    }
    return this
}

fun UserRobot.assertSystemMessage(text: String, isDisplayed: Boolean = true): UserRobot {
    if (isDisplayed) {
        By.text(text).waitToAppear().isDisplayed()
    } else {
        By.text(text).waitToDisappear().isDisplayed()
    }
    return this
}

fun UserRobot.assertInvalidCommandMessage(text: String, isDisplayed: Boolean = true): UserRobot {
    assertSystemMessage(
        text = "Sorry, command $text doesn't exist. Try posting your message without the starting /",
        isDisplayed = isDisplayed,
    )
    return this
}

fun UserRobot.assertReaction(type: ReactionType, isDisplayed: Boolean): UserRobot {
    if (isDisplayed) {
        assertTrue(Message.Reactions.reaction(type).waitToAppear().isDisplayed())
    } else {
        assertFalse(Message.Reactions.reaction(type).waitToDisappear().isDisplayed())
    }
    return this
}

@SuppressLint("ResourceType")
fun UserRobot.assertThreadReplyLabel(replies: Int, inThread: Boolean = false): UserRobot {
    if (inThread) {
        val expectedResult = appContext.resources.getQuantityString(
            R.plurals.stream_compose_message_list_thread_separator,
            replies,
            replies,
        )
        assertEquals(
            expectedResult,
            ThreadPage.ThreadList.repliesCountLabel.waitToAppear().waitForText(expectedResult).text,
        )
    } else {
        val expectedResult = if (replies == 1) {
            appContext.getString(R.string.stream_compose_message_list_thread_footnote_thread_reply)
        } else {
            appContext.getString(R.string.stream_compose_message_list_thread_footnote_thread_replies, replies)
        }
        assertEquals(expectedResult, Message.threadRepliesLabel.waitToAppear().text)
    }
    return this
}

fun UserRobot.assertThreadReplyLabelAvatars(count: Int): UserRobot {
    Message.threadParticipantAvatar.waitToAppear()
    assertEquals(count, Message.threadParticipantAvatar.findObjects().size)
    return this
}

fun UserRobot.assertMessages(text: String, count: Int): UserRobot {
    val actualCount = device.retryOnStaleObjectException {
        Message.text.findObjects().count { it.text == text }
    }
    assertEquals(count, actualCount)
    return this
}

fun UserRobot.assertImage(isDisplayed: Boolean, count: Int = 1): UserRobot {
    if (isDisplayed) {
        assertEquals(count, Message.image.waitForCount(count).size)
        if (count != 1) {
            assertTrue(Message.columnWithMultipleMediaAttachments.isDisplayed())
        }
    } else {
        assertFalse(Message.image.waitToDisappear().isDisplayed())
    }
    return this
}

fun UserRobot.assertVideo(isDisplayed: Boolean, count: Int = 1): UserRobot {
    if (isDisplayed) {
        assertEquals(count, Message.video.waitForCount(count).size)
        if (count != 1) {
            assertTrue(Message.columnWithMultipleMediaAttachments.waitToAppear().isDisplayed())
        }
    } else {
        assertFalse(Message.video.waitToDisappear().isDisplayed())
    }
    return this
}

fun UserRobot.assertFile(isDisplayed: Boolean, count: Int = 1): UserRobot {
    if (isDisplayed) {
        assertEquals(count, Message.fileName.waitForCount(count).size)
        assertEquals(count, Message.fileSize.findObjects().size)
        assertEquals(count, Message.fileDownloadButton.findObjects().size)
        assertEquals(count, Message.fileImage.waitForCount(count).size)
        if (count > 1) {
            assertTrue(Message.columnWithMultipleFileAttachments.isDisplayed())
        }
    } else {
        assertFalse(Message.fileName.waitToDisappear().isDisplayed())
        assertFalse(Message.fileSize.isDisplayed())
        assertFalse(Message.fileImage.isDisplayed())
        assertFalse(Message.fileDownloadButton.isDisplayed())
    }
    return this
}

fun UserRobot.assertMediaAttachmentInPreview(isDisplayed: Boolean, count: Int = 1): UserRobot {
    if (isDisplayed) {
        assertEquals(count, Composer.mediaAttachment.waitForCount(count).size)
        assertEquals(count, Composer.attachmentCancelIcon.findObjects().size)
        if (count != 1) {
            assertTrue(Composer.columnWithMultipleMediaAttachments.isDisplayed())
        }
    } else {
        assertFalse(Composer.mediaAttachment.waitToDisappear().isDisplayed())
        assertFalse(Composer.attachmentCancelIcon.isDisplayed())
    }
    return this
}

fun UserRobot.assertFileAttachmentInPreview(isDisplayed: Boolean, count: Int = 1): UserRobot {
    if (isDisplayed) {
        assertTrue(Composer.fileName.waitToAppear().isDisplayed())
        assertTrue(Composer.fileSize.isDisplayed())
        assertTrue(Composer.fileImage.isDisplayed())
        assertTrue(Composer.attachmentCancelIcon.isDisplayed())
        if (count > 1) {
            assertTrue(Composer.columnWithMultipleFileAttachments.isDisplayed())
        }
    } else {
        assertFalse(Composer.fileName.waitToDisappear().isDisplayed())
        assertFalse(Composer.fileSize.isDisplayed())
        assertFalse(Composer.fileImage.isDisplayed())
        assertFalse(Composer.attachmentCancelIcon.isDisplayed())
    }
    return this
}

fun UserRobot.assertLinkPreviewInMessageList(isDisplayed: Boolean): UserRobot {
    if (isDisplayed) {
        assertTrue(Message.linkPreviewImage.waitToAppear().isDisplayed())
        assertTrue(Message.linkPreviewTitle.isDisplayed())
        assertTrue(Message.linkPreviewDescription.isDisplayed())
    } else {
        assertFalse(Message.linkPreviewImage.waitToDisappear().isDisplayed())
        assertFalse(Message.linkPreviewTitle.isDisplayed())
        assertFalse(Message.linkPreviewDescription.isDisplayed())
    }
    return this
}

fun UserRobot.assertLinkPreviewInComposer(isDisplayed: Boolean): UserRobot {
    if (isDisplayed) {
        assertTrue(Composer.linkPreviewImage.waitToAppear().isDisplayed())
        assertTrue(Composer.linkPreviewTitle.isDisplayed())
        assertTrue(Composer.linkPreviewDescription.isDisplayed())
    } else {
        assertFalse(Composer.linkPreviewImage.waitToDisappear().isDisplayed())
        assertFalse(Composer.linkPreviewTitle.isDisplayed())
        assertFalse(Composer.linkPreviewDescription.isDisplayed())
    }
    return this
}
