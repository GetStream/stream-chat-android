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
import android.content.ClipboardManager
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.pages.MessageListPage
import io.getstream.chat.android.compose.pages.MessageListPage.Composer
import io.getstream.chat.android.compose.pages.MessageListPage.MessageList.Message
import io.getstream.chat.android.compose.pages.MessageListPage.MessageList.Message.ContextMenu
import io.getstream.chat.android.compose.pages.ThreadPage
import io.getstream.chat.android.e2e.test.mockserver.MessageDeliveryStatus
import io.getstream.chat.android.e2e.test.mockserver.ReactionType
import io.getstream.chat.android.e2e.test.robots.ParticipantRobot
import io.getstream.chat.android.e2e.test.uiautomator.appContext
import io.getstream.chat.android.e2e.test.uiautomator.defaultTimeout
import io.getstream.chat.android.e2e.test.uiautomator.device
import io.getstream.chat.android.e2e.test.uiautomator.findObject
import io.getstream.chat.android.e2e.test.uiautomator.findObjects
import io.getstream.chat.android.e2e.test.uiautomator.height
import io.getstream.chat.android.e2e.test.uiautomator.isDisplayed
import io.getstream.chat.android.e2e.test.uiautomator.isEnabled
import io.getstream.chat.android.e2e.test.uiautomator.retryOnStaleObjectException
import io.getstream.chat.android.e2e.test.uiautomator.seconds
import io.getstream.chat.android.e2e.test.uiautomator.waitDisplayed
import io.getstream.chat.android.e2e.test.uiautomator.waitForCount
import io.getstream.chat.android.e2e.test.uiautomator.waitForText
import io.getstream.chat.android.e2e.test.uiautomator.waitToAppear
import io.getstream.chat.android.e2e.test.uiautomator.waitToAppearBottomUp
import io.getstream.chat.android.e2e.test.uiautomator.waitToDisappear
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import java.util.regex.Pattern

/**
 * Asserts the selector's visibility with a bounded wait: waits for it to appear when
 * [isDisplayed] is `true`, or to disappear when `false`, then asserts the final state.
 */
private fun assertVisibility(selector: BySelector, isDisplayed: Boolean, timeOutMillis: Long = defaultTimeout) {
    if (isDisplayed) {
        assertTrue(selector.waitDisplayed(timeOutMillis))
    } else {
        assertFalse(selector.waitToDisappear(timeOutMillis).isDisplayed())
    }
}

fun UserRobot.assertMessage(
    text: String,
    isDisplayed: Boolean = true,
    isClickable: Boolean = false,
): UserRobot {
    val textLocator = (if (isClickable) Message.clickableText else Message.text).text(text)
    if (isDisplayed) {
        assertTrue(textLocator.waitDisplayed())
        assertTrue(Message.timestamp.isDisplayed())
    } else {
        assertFalse(textLocator.waitToDisappear().isDisplayed())
    }
    return this
}

fun UserRobot.assertMessageAuthor(isCurrentUser: Boolean): UserRobot {
    assertNotEquals(isCurrentUser, Message.authorName.isDisplayed())
    assertNotEquals(isCurrentUser, Message.avatar.isDisplayed())
    return this
}

fun UserRobot.assertMessageTimestamps(count: Int): UserRobot {
    assertEquals(count, Message.timestamp.waitForCount(count).size)
    return this
}

fun UserRobot.assertMessageDeliveryStatus(status: MessageDeliveryStatus, count: Int? = null): UserRobot {
    when (status) {
        MessageDeliveryStatus.READ -> {
            assertVisibility(Message.deliveryStatusIsRead, isDisplayed = true, timeOutMillis = 30.seconds)
            if (count != null) {
                assertEquals(count, Message.deliveryStatusIsRead.waitForCount(count).size)
            }
        }
        MessageDeliveryStatus.PENDING -> {
            assertVisibility(Message.deliveryStatusIsPending, isDisplayed = true)
            if (count != null) {
                assertEquals(count, Message.deliveryStatusIsPending.waitForCount(count).size)
            }
        }
        MessageDeliveryStatus.SENT -> {
            assertVisibility(Message.deliveryStatusIsSent, isDisplayed = true)
            if (count != null) {
                assertEquals(count, Message.deliveryStatusIsSent.waitForCount(count).size)
            }
        }
        MessageDeliveryStatus.FAILED -> {
            assertVisibility(Message.deliveryStatusIsFailed, isDisplayed = true)
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
    assertVisibility(Message.deliveryStatusIsFailed, isDisplayed)
    return this
}

fun UserRobot.assertEditedMessage(text: String): UserRobot {
    assertMessage(text)
    val expectedLabel = appContext.getString(R.string.stream_compose_message_list_footnote_edited)
    assertEquals(expectedLabel, Message.editedLabel.waitForText(expectedLabel))
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

fun UserRobot.assertQuotedMessage(text: String? = null, quote: String = "", isDisplayed: Boolean = true): UserRobot {
    val quotedMessageInList = Message.quotedMessage.hasAncestor(MessageListPage.MessageList.messages)
    if (isDisplayed) {
        assertEquals(quote, quotedMessageInList.waitForText(quote))
    } else {
        assertFalse(quotedMessageInList.waitToDisappear().isDisplayed())
    }
    if (text != null) {
        assertMessage(text, isDisplayed = isDisplayed)
    }
    return this
}

fun UserRobot.assertMessageSizeChangesAfterEditing(linesCountShouldBeIncreased: Boolean): UserRobot {
    val cellHeight = MessageListPage.MessageList.messages.waitToAppearBottomUp(withIndex = 0).height
    val messageText = Message.text.waitToAppearBottomUp(withIndex = 0).text
    val newLine = "new line"
    val newText = if (linesCountShouldBeIncreased) "ok\n${messageText}\n$newLine" else newLine

    editMessage(newText)
    assertMessage(newText)

    val updatedCellHeight = MessageListPage.MessageList.messages.waitToAppearBottomUp(withIndex = 0).height
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
    assertVisibility(MessageListPage.MessageList.typingIndicator, isDisplayed)
    return this
}

fun UserRobot.assertAttachmentsMenu(isDisplayed: Boolean): UserRobot {
    assertVisibility(MessageListPage.AttachmentPicker.view, isDisplayed)
    return this
}

fun UserRobot.assertComposerCommandsMenu(isDisplayed: Boolean): UserRobot {
    if (isDisplayed) {
        assertTrue(Composer.commandSuggestionList.waitDisplayed())
        assertTrue(Composer.commandSuggestionListTitle.isDisplayed())
    } else {
        assertFalse(Composer.commandSuggestionList.waitToDisappear().isDisplayed())
        assertFalse(Composer.commandSuggestionListTitle.isDisplayed())
    }
    return this
}

fun UserRobot.assertComposerMentionsMenu(isDisplayed: Boolean): UserRobot {
    assertVisibility(Composer.userSuggestion, isDisplayed)
    return this
}

fun UserRobot.assertMentionWasApplied(): UserRobot {
    val additionalSpace = " "
    val userName = ParticipantRobot.name
    val expectedText = "@${userName}$additionalSpace"
    val actualText = Composer.inputField.waitForText(expectedText)
    assertEquals(expectedText, actualText)
    return this
}

fun UserRobot.assertComposerText(expectedText: String): UserRobot {
    assertEquals(expectedText, Composer.inputField.waitForText(expectedText))
    return this
}

fun UserRobot.assertCooldownIsShown(): UserRobot {
    assertTrue(Composer.cooldownIndicator.waitDisplayed())
    assertFalse(Composer.sendButton.isDisplayed())
    return this
}

fun UserRobot.assertCooldownIsNotShown(): UserRobot {
    assertFalse(Composer.cooldownIndicator.waitToDisappear().isDisplayed())
    return this
}

fun UserRobot.assertComposerIsDisabledInSlowMode(): UserRobot {
    assertFalse(Composer.inputField.isEnabled())
    assertFalse(Composer.attachmentsButton.isEnabled())
    return this
}

fun UserRobot.assertScrollToBottomButton(isDisplayed: Boolean): UserRobot {
    assertVisibility(MessageListPage.MessageList.scrollToBottomButton, isDisplayed)
    return this
}

fun UserRobot.assertMessageCount(count: Int): UserRobot {
    assertEquals(count, MessageListPage.MessageList.messages.waitForCount(count).size)
    return this
}

fun UserRobot.assertComposerAttachmentsButton(isDisplayed: Boolean = true): UserRobot {
    assertVisibility(Composer.attachmentsButton, isDisplayed)
    return this
}

fun UserRobot.assertScrollToBottomButtonUnreadCount(count: Int): UserRobot {
    val badge = MessageListPage.MessageList.scrollToBottomButtonUnreadCount
    if (count > 0) {
        assertEquals(count.toString(), badge.waitForText(count.toString()))
    } else {
        assertFalse(badge.waitToDisappear().isDisplayed())
    }
    return this
}

fun UserRobot.assertThreadIsOpen(): UserRobot {
    assertTrue(ThreadPage.ThreadList.alsoSendToChannelCheckbox.waitDisplayed())
    return this
}

fun UserRobot.assertThreadMessage(text: String): UserRobot {
    assertThreadIsOpen()
    assertMessage(text)
    return this
}

fun UserRobot.assertThreadReplyLabelOnParentMessage(): UserRobot {
    val expectedResult = appContext.resources.getQuantityString(
        R.plurals.stream_compose_message_list_thread_footnote,
        1,
        1,
    )
    assertEquals(expectedResult, Message.threadRepliesLabel.waitForText(expectedResult))
    assertTrue(Message.threadParticipantAvatar.isDisplayed())
    return this
}

fun UserRobot.assertAlsoInTheChannelLabelInChannel(): UserRobot {
    val expectedLabel = appContext.getString(R.string.stream_compose_replied_to_thread)
    assertEquals(expectedLabel, Message.messageHeaderLabel.waitForText(expectedLabel))
    return this
}

fun UserRobot.assertAlsoInTheChannelLabelInThread(): UserRobot {
    val expectedLabel = appContext.getString(R.string.stream_compose_also_sent_to_channel)
    assertEquals(expectedLabel, Message.messageHeaderLabel.waitForText(expectedLabel))
    return this
}

fun UserRobot.assertUnreadSeparator(unreadCount: Int, isDisplayed: Boolean = true): UserRobot {
    if (isDisplayed) {
        val expectedText = appContext.resources.getQuantityString(
            R.plurals.stream_compose_message_list_unread_separator,
            unreadCount,
            unreadCount,
        )
        assertEquals(expectedText, MessageListPage.MessageList.unreadMessagesBadge.waitForText(expectedText))
    } else {
        assertVisibility(MessageListPage.MessageList.unreadMessagesBadge, isDisplayed = false)
    }
    return this
}

fun UserRobot.assertScrollToFirstUnreadButton(unreadCount: Int? = null, isDisplayed: Boolean = true): UserRobot {
    assertVisibility(MessageListPage.MessageList.scrollToFirstUnreadButton, isDisplayed)
    if (isDisplayed && unreadCount != null) {
        val expectedText = appContext.resources.getQuantityString(
            R.plurals.stream_compose_scroll_to_first_unread_count,
            unreadCount,
            unreadCount,
        )
        // Comparing against the rendered count so a failure reports the actual text. The
        // count is the only text inside the button and carries no test tag of its own.
        val countText = By.text(Pattern.compile(".+"))
            .hasAncestor(MessageListPage.MessageList.scrollToFirstUnreadButton)
        assertEquals(expectedText, countText.waitForText(expectedText))
    }
    return this
}

fun UserRobot.assertMessageCopied(text: String): UserRobot {
    val clipboard = appContext.getSystemService(ClipboardManager::class.java)
    assertEquals(text, clipboard.primaryClip?.getItemAt(0)?.text?.toString())
    return this
}

/**
 * Asserts the "Pinned by X" label above a message. [pinnedBy] is the name shown in the label;
 * `null` expects the current user's label ("Pinned by You").
 */
fun UserRobot.assertMessagePinnedLabel(pinnedBy: String? = null, isDisplayed: Boolean = true): UserRobot {
    val pinnedByName = pinnedBy ?: appContext.getString(R.string.stream_compose_message_list_you)
    val expectedLabel = appContext.getString(R.string.stream_compose_pinned_to_channel_by, pinnedByName)
    if (isDisplayed) {
        assertEquals(expectedLabel, Message.messageHeaderLabel.waitForText(expectedLabel))
    } else {
        assertVisibility(Message.messageHeaderLabel.text(expectedLabel), isDisplayed = false)
    }
    return this
}

fun UserRobot.assertGiphyImage(isDisplayed: Boolean = true): UserRobot {
    if (isDisplayed) {
        assertTrue(Message.giphy.waitDisplayed())
    } else {
        assertFalse(Message.giphy.waitToDisappear().isDisplayed())
    }
    return this
}

fun UserRobot.assertGiphyButtons(areDisplayed: Boolean = true): UserRobot {
    if (areDisplayed) {
        assertTrue(Message.GiphyButtons.send.waitDisplayed())
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
    assertVisibility(By.text(text), isDisplayed)
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
    assertVisibility(Message.Reactions.reaction(type), isDisplayed)
    return this
}

fun UserRobot.assertReactionAuthor(name: String): UserRobot {
    assertTrue(Message.Reactions.reactionAuthor.hasDescendant(By.text(name)).waitDisplayed())
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
            ThreadPage.ThreadList.repliesCountLabel.waitForText(expectedResult),
        )
    } else {
        val expectedResult = appContext.resources.getQuantityString(
            R.plurals.stream_compose_message_list_thread_footnote,
            replies,
            replies,
        )
        assertEquals(expectedResult, Message.threadRepliesLabel.waitForText(expectedResult))
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
            assertTrue(Message.columnWithMultipleMediaAttachments.waitDisplayed())
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
        assertEquals(count, Message.fileImage.waitForCount(count).size)
        if (count > 1) {
            assertTrue(Message.columnWithMultipleFileAttachments.isDisplayed())
        }
    } else {
        assertFalse(Message.fileName.waitToDisappear().isDisplayed())
        assertFalse(Message.fileSize.isDisplayed())
        assertFalse(Message.fileImage.isDisplayed())
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
        assertTrue(Composer.fileName.waitDisplayed())
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
        assertTrue(Message.linkPreviewImage.waitDisplayed())
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
        assertTrue(Composer.linkPreviewImage.waitDisplayed())
        assertTrue(Composer.linkPreviewTitle.isDisplayed())
        assertTrue(Composer.linkPreviewDescription.isDisplayed())
    } else {
        assertFalse(Composer.linkPreviewImage.waitToDisappear().isDisplayed())
        assertFalse(Composer.linkPreviewTitle.isDisplayed())
        assertFalse(Composer.linkPreviewDescription.isDisplayed())
    }
    return this
}

fun UserRobot.assertFlagMessageDialog(isDisplayed: Boolean): UserRobot {
    assertVisibility(MessageListPage.FlagMessageDialog.body, isDisplayed)
    return this
}

/**
 * Opens the message menu of the message with [messageText] and asserts the mute option it offers
 * for the author. The sample has no muted users list, so the option label is the only place the
 * mute state is visible.
 */
fun UserRobot.assertMuteMessageAuthorOption(messageText: String, isAuthorMuted: Boolean): UserRobot {
    val expectedOption = if (isAuthorMuted) ContextMenu.unmuteUser else ContextMenu.muteUser
    openContextMenuWithOption(messageText, expectedOption)
    assertTrue(expectedOption.isDisplayed())
    return this
}

/**
 * Opens the message menu of the message with [messageText] and asserts the block option it offers
 * for the author. The sample has no blocked users list, so the option label is the only place the
 * block state is visible.
 */
fun UserRobot.assertBlockMessageAuthorOption(messageText: String, isAuthorBlocked: Boolean): UserRobot {
    val expectedOption = if (isAuthorBlocked) ContextMenu.unblock else ContextMenu.block
    openContextMenuWithOption(messageText, expectedOption)
    assertTrue(expectedOption.isDisplayed())
    return this
}
