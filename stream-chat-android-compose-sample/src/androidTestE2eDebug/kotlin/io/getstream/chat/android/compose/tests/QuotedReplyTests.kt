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

package io.getstream.chat.android.compose.tests

import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.robots.assertDeletedMessage
import io.getstream.chat.android.compose.robots.assertGiphyImage
import io.getstream.chat.android.compose.robots.assertInvalidCommandMessage
import io.getstream.chat.android.compose.robots.assertMessage
import io.getstream.chat.android.compose.robots.assertMessages
import io.getstream.chat.android.compose.robots.assertQuotedMessage
import io.getstream.chat.android.compose.robots.assertScrollToBottomButton
import io.getstream.chat.android.compose.robots.assertThreadReplyLabel
import io.getstream.chat.android.compose.robots.assertThreadReplyLabelAvatars
import io.getstream.chat.android.compose.uiautomator.appContext
import io.getstream.chat.android.e2e.test.mockserver.AttachmentType
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Ignore
import org.junit.Test

class QuotedReplyTests : StreamTestCase() {

    private val sampleText = "Test"
    private var quoteReply = "Alright"
    private val messagesCount = 30

    @AllureId("5923")
    @Test
    fun test_whenSwipingMessage_thenMessageIsQuotedReply() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant replies") {
            participantRobot.sendMessage(sampleText)
        }
        step("WHEN user swipes a participant's message") {
            userRobot.swipeMessage()
        }
        step("AND user sends a message") {
            userRobot.sendMessage(quoteReply)
        }
        step("THEN a quote reply is sent") {
            userRobot.assertQuotedMessage(text = quoteReply, quote = sampleText)
        }
    }

    @AllureId("5869")
    @Test
    fun test_quotedReplyInList_whenUserAddsQuotedReply() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends a message") {
            participantRobot.sendMessage(sampleText)
        }
        step("WHEN user adds a quoted reply to participant's message") {
            userRobot.quoteMessage(quoteReply)
        }
        step("THEN user observes the quote reply in message list") {
            userRobot
                .assertQuotedMessage(text = quoteReply, quote = sampleText)
                .assertScrollToBottomButton(isDisplayed = false)
        }
    }

    @AllureId("test_quotedReplyInList_whenParticipantAddsQuotedReply_Message")
    @Test
    fun test_quotedReplyInList_whenParticipantAddsQuotedReply_Message() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("WHEN participant adds a quoted reply to user's message") {
            participantRobot.quoteMessage(quoteReply)
        }
        step("THEN user observes the quote reply in message list") {
            userRobot
                .assertQuotedMessage(text = quoteReply, quote = "1")
                .assertScrollToBottomButton(isDisplayed = false)
        }
    }

    @AllureId("5684")
    @Ignore("https://linear.app/stream/issue/AND-76")
    @Test
    fun test_quotedReplyNotInList_whenUserAddsQuotedReply() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = messagesCount)
            userRobot.login().openChannel().waitForMessageListToLoad()
        }
        step("WHEN user adds a quoted reply to message") {
            userRobot
                .scrollMessageListUp(times = 3)
                .quoteMessage(quoteReply, messageCellIndex = messagesCount - 1)
        }
        step("THEN user observes the quote reply in message list") {
            userRobot
                .assertQuotedMessage(text = quoteReply, quote = messagesCount.toString(), isDisplayed = true)
                .assertScrollToBottomButton(isDisplayed = false)
        }
        step("WHEN user taps on a quoted message") {
            userRobot.tapOnQuotedMessage()
        }
        step("THEN user is scrolled up to the quote") {
            userRobot
                .assertQuotedMessage(text = quoteReply, quote = messagesCount.toString(), isDisplayed = false)
                .assertScrollToBottomButton(isDisplayed = true)
        }
    }

    @AllureId("5685")
    @Test
    fun test_quotedReplyNotInList_whenParticipantAddsQuotedReply_Message() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = messagesCount)
            userRobot.login().openChannel()
        }
        step("WHEN participants adds a quoted reply to user's message") {
            participantRobot.quoteMessage(quoteReply, last = false)
        }
        step("THEN user observes the quote reply in message list") {
            userRobot
                .assertQuotedMessage(text = quoteReply, quote = messagesCount.toString(), isDisplayed = true)
                .assertScrollToBottomButton(isDisplayed = false)
        }
        step("WHEN user taps on a quoted message") {
            userRobot.tapOnQuotedMessage()
        }
        step("THEN user is scrolled up to the quote") {
            userRobot
                .assertQuotedMessage(text = quoteReply, quote = messagesCount.toString(), isDisplayed = false)
                .assertScrollToBottomButton(isDisplayed = true)
        }
    }

    @AllureId("5865")
    @Ignore("https://linear.app/stream/issue/AND-266")
    @Test
    fun test_quotedReplyNotInList_whenParticipantAddsQuotedReply_File() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = messagesCount)
            userRobot.login().openChannel()
        }
        step("WHEN participants adds a quoted reply to user's message with a file") {
            participantRobot.quoteMessageWithAttachment(type = AttachmentType.FILE, last = false)
        }
        step("THEN user observes the quote reply in message list") {
            userRobot
                .assertQuotedMessage(text = quoteReply, quote = messagesCount.toString(), isDisplayed = true)
                .assertScrollToBottomButton(isDisplayed = false)
        }
        step("WHEN user taps on a quoted message") {
            userRobot.tapOnQuotedMessage()
        }
        step("THEN user is scrolled up to the quote") {
            userRobot
                .assertQuotedMessage(text = quoteReply, quote = messagesCount.toString(), isDisplayed = false)
                .assertScrollToBottomButton(isDisplayed = true)
        }
    }

    @AllureId("5866")
    @Ignore("https://linear.app/stream/issue/AND-266")
    @Test
    fun test_quotedReplyNotInList_whenParticipantAddsQuotedReply_Giphy() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = messagesCount)
            userRobot.login().openChannel()
        }
        step("WHEN participants adds a quoted reply to user's message with a giphy") {
            participantRobot.quoteMessageWithGiphy(last = false)
        }
        step("THEN user observes the quote reply in message list") {
            userRobot
                .assertGiphyImage(isDisplayed = true)
                .assertQuotedMessage(text = quoteReply, quote = messagesCount.toString(), isDisplayed = true)
                .assertScrollToBottomButton(isDisplayed = false)
        }
        step("WHEN user taps on a quoted message") {
            userRobot.tapOnQuotedMessage()
        }
        step("THEN user is scrolled up to the quote") {
            userRobot
                .assertGiphyImage(isDisplayed = false)
                .assertQuotedMessage(text = quoteReply, quote = messagesCount.toString(), isDisplayed = false)
                .assertScrollToBottomButton(isDisplayed = true)
        }
    }

    @AllureId("5722")
    @Test
    fun test_quotedReplyIsDeletedByParticipant_deletedMessageIsShown() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("AND participant adds a quoted reply") {
            participantRobot.quoteMessage(quoteReply)
        }
        step("WHEN participant deletes a quoted message") {
            participantRobot.deleteMessage()
        }
        step("THEN user observes Message deleted") {
            userRobot
                .assertDeletedMessage(quoteReply)
                .assertQuotedMessage(text = quoteReply, isDisplayed = false)
        }
    }

    @AllureId("6786")
    @Test
    fun test_originalQuoteIsDeletedByParticipant_deletedMessageIsShown() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends a message") {
            participantRobot.sendMessage("1")
        }
        step("AND user adds a quoted reply") {
            userRobot.quoteMessage(quoteReply)
        }
        step("WHEN participant deletes an original message") {
            participantRobot.deleteMessage()
        }
        step("THEN deleted message is shown") {
            userRobot.assertDeletedMessage(quoteReply)
        }
        step("AND deleted message is shown in quoted reply bubble") {
            userRobot.assertQuotedMessage(
                text = quoteReply,
                quote = appContext.getString(R.string.stream_ui_message_list_message_deleted),
            )
        }
    }

    @AllureId("5723")
    @Test
    fun test_quotedReplyIsDeletedByUser_deletedMessageIsShown() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("AND user adds a quoted reply") {
            userRobot.quoteMessage(quoteReply)
        }
        step("WHEN user deletes a quoted message") {
            userRobot.deleteMessage()
        }
        step("THEN user observes Message deleted") {
            userRobot
                .assertDeletedMessage(quoteReply)
                .assertQuotedMessage(text = quoteReply, isDisplayed = false)
        }
    }

    @AllureId("6787")
    @Test
    fun test_originalQuoteIsDeletedByUser_deletedMessageIsShown() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("AND user adds a quoted reply") {
            userRobot.quoteMessage(quoteReply)
        }
        step("WHEN user deletes an original message") {
            userRobot.deleteMessage(messageCellIndex = 1)
        }
        step("THEN deleted message is shown") {
            userRobot.assertDeletedMessage(quoteReply)
        }
        step("AND deleted message is shown in quoted reply bubble") {
            userRobot.assertQuotedMessage(
                text = quoteReply,
                quote = appContext.getString(R.string.stream_ui_message_list_message_deleted),
            )
        }
    }

    @AllureId("5876")
    @Test
    fun test_userAddsQuotedReplyWithInvalidCommand() {
        val invalidCommand = "invalid command"

        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = messagesCount)
            userRobot.login().openChannel()
        }
        step("WHEN user quotes a message with invalid command") {
            userRobot.quoteMessage("/$invalidCommand")
        }
        step("THEN user observes invalid command alert") {
            userRobot
                .assertInvalidCommandMessage(invalidCommand)
                .assertQuotedMessage(text = quoteReply, isDisplayed = false)
        }
    }

    @AllureId("5890")
    @Test
    fun test_whenUserAddsQuotedReply_InThread() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(
                channelsCount = 1,
                messagesCount = 1,
                repliesCount = 1,
                repliesText = sampleText,
            )
            userRobot.login().openChannel()
        }
        step("WHEN user adds a quoted reply to message in thread") {
            userRobot
                .openThread()
                .quoteMessage(quoteReply)
        }
        step("THEN user observes the quote reply in thread") {
            userRobot
                .assertQuotedMessage(text = quoteReply, quote = sampleText, isDisplayed = true)
                .assertScrollToBottomButton(isDisplayed = false)
        }
    }

    @AllureId("6788")
    @Test
    fun test_whenParticipantAddsQuotedReply_InThread() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("AND user sends a message in thread") {
            userRobot
                .openThread()
                .sendMessage(sampleText)
                .assertMessage(sampleText)
        }
        step("WHEN participant adds a quoted reply to user's message in thread") {
            participantRobot.quoteMessageInThread(quoteReply)
        }
        step("THEN user observes the quote reply in thread") {
            userRobot
                .assertQuotedMessage(text = quoteReply, quote = sampleText, isDisplayed = true)
                .assertScrollToBottomButton(isDisplayed = false)
        }
    }

    @AllureId("5892")
    @Ignore("https://linear.app/stream/issue/AND-76")
    @Test
    fun test_quotedReplyNotInList_whenUserAddsQuotedReply_InThread() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1, repliesCount = messagesCount)
            userRobot.login().openChannel().waitForMessageListToLoad()
        }
        step("WHEN user adds a quoted reply to message in thread") {
            userRobot
                .openThread()
                .scrollMessageListUp(times = 3)
                .quoteMessage(quoteReply, messageCellIndex = messagesCount - 1)
        }
        step("THEN user observes the quote reply in thread") {
            userRobot
                .assertQuotedMessage(text = quoteReply, quote = messagesCount.toString(), isDisplayed = true)
                .assertScrollToBottomButton(isDisplayed = false)
        }
        step("WHEN user taps on a quoted message") {
            userRobot.tapOnQuotedMessage()
        }
        step("THEN user is scrolled up to the quote") {
            userRobot
                .assertQuotedMessage(text = quoteReply, quote = messagesCount.toString(), isDisplayed = false)
                .assertScrollToBottomButton(isDisplayed = true)
        }
    }

    @AllureId("5893")
    @Test
    fun test_quotedReplyNotInList_whenParticipantAddsQuotedReply_Message_InThread() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(
                channelsCount = 1,
                messagesCount = 1,
                messagesText = sampleText,
                repliesCount = messagesCount,
            )
            userRobot.login().openChannel()
        }
        step("WHEN participants adds a quoted reply to user's message") {
            participantRobot.quoteMessageInThread(quoteReply, last = false)
        }
        step("THEN user observes the quote reply in thread") {
            userRobot
                .openThread()
                .assertQuotedMessage(text = quoteReply, quote = sampleText, isDisplayed = true)
                .assertScrollToBottomButton(isDisplayed = false)
        }
        step("WHEN user taps on a quoted message") {
            userRobot.tapOnQuotedMessage()
        }
        step("THEN user is scrolled up to the quote") {
            userRobot
                .assertQuotedMessage(text = quoteReply, quote = sampleText, isDisplayed = false)
                .assertScrollToBottomButton(isDisplayed = true)
        }
    }

    @AllureId("5894")
    @Ignore("https://linear.app/stream/issue/AND-266")
    @Test
    fun test_quotedReplyNotInList_whenParticipantAddsQuotedReply_File_InThread() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(
                channelsCount = 1,
                messagesCount = 1,
                messagesText = sampleText,
                repliesCount = messagesCount,
            )
            userRobot.login().openChannel()
        }
        step("WHEN participants adds a quoted reply to user's message with a file") {
            participantRobot.quoteMessageWithAttachmentInThread(type = AttachmentType.FILE, last = false)
        }
        step("THEN user observes the quote reply in thread") {
            userRobot
                .openThread()
                .assertQuotedMessage(text = quoteReply, quote = sampleText, isDisplayed = true)
                .assertScrollToBottomButton(isDisplayed = false)
        }
        step("WHEN user taps on a quoted message") {
            userRobot.tapOnQuotedMessage()
        }
        step("THEN user is scrolled up to the quote") {
            userRobot
                .assertQuotedMessage(text = quoteReply, quote = sampleText, isDisplayed = false)
                .assertScrollToBottomButton(isDisplayed = true)
        }
    }

    @AllureId("5895")
    @Ignore("https://linear.app/stream/issue/AND-266")
    @Test
    fun test_quotedReplyNotInList_whenParticipantAddsQuotedReply_Giphy_InThread() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(
                channelsCount = 1,
                messagesCount = 1,
                messagesText = sampleText,
                repliesCount = messagesCount,
            )
            userRobot.login().openChannel()
        }
        step("WHEN participants adds a quoted reply to user's message with a giphy") {
            participantRobot.quoteMessageWithGiphyInThread(last = false)
        }
        step("THEN user observes the quote reply in message list") {
            userRobot
                .openThread()
                .assertGiphyImage(isDisplayed = true)
                .assertQuotedMessage(text = quoteReply, quote = sampleText, isDisplayed = true)
                .assertScrollToBottomButton(isDisplayed = false)
        }
        step("WHEN user taps on a quoted message") {
            userRobot.tapOnQuotedMessage()
        }
        step("THEN user is scrolled up to the quote") {
            userRobot
                .assertGiphyImage(isDisplayed = false)
                .assertQuotedMessage(text = quoteReply, quote = sampleText, isDisplayed = false)
                .assertScrollToBottomButton(isDisplayed = true)
        }
    }

    @AllureId("5896")
    @Test
    fun test_userAddsQuotedReplyWithInvalidCommand_InThread() {
        val invalidCommand = "invalid command"

        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1, repliesCount = 1)
            userRobot.login().openChannel()
        }
        step("WHEN user quotes a message with invalid command in thread") {
            userRobot
                .openThread()
                .quoteMessage("/$invalidCommand")
        }
        step("THEN user observes invalid command alert") {
            userRobot
                .assertInvalidCommandMessage(invalidCommand)
                .assertQuotedMessage(text = quoteReply, isDisplayed = false)
        }
    }

    @AllureId("5897")
    @Test
    fun test_threadRepliesCount_One() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("WHEN participant sends one thread reply") {
            participantRobot.sendMessageInThread(sampleText)
        }
        step("THEN user observes one thread reply label in the channel") {
            userRobot
                .assertThreadReplyLabel(replies = 1)
                .assertThreadReplyLabelAvatars(count = 1)
        }
        step("WHEN user opens the thread") {
            userRobot.openThread()
        }
        step("THEN user observes one reply in the thread") {
            userRobot.assertThreadReplyLabel(replies = 1, inThread = true)
        }
    }

    @AllureId("6789")
    @Test
    fun test_threadRepliesCount_Multiple() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("WHEN participant sends multiple thread replies") {
            repeat(5) {
                participantRobot.sendMessageInThread(sampleText)
            }
        }
        step("THEN user observes one thread reply label in the channel") {
            userRobot
                .assertThreadReplyLabel(replies = 5)
                .assertThreadReplyLabelAvatars(count = 1)
        }
        step("WHEN user opens the thread") {
            userRobot.openThread()
        }
        step("THEN user observes all replies in the thread") {
            userRobot.assertThreadReplyLabel(replies = 5, inThread = true)
        }
    }

    @AllureId("5898")
    @Test
    fun test_quotedReplyInThreadAndAlsoInChannel() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(
                channelsCount = 1,
                messagesCount = 1,
                messagesText = sampleText,
                repliesCount = messagesCount,
            )
            userRobot.login().openChannel()
        }
        step("WHEN participant adds a quoted reply in thread and also in channel") {
            participantRobot.quoteMessageInThread(quoteReply, alsoSendInChannel = true, last = false)
        }
        step("THEN user observes the quoted reply in channel") {
            userRobot
                .assertQuotedMessage(text = quoteReply, quote = sampleText)
                .assertScrollToBottomButton(isDisplayed = false)
        }
        step("AND user observes the quoted reply also in thread") {
            userRobot
                .openThread()
                .assertQuotedMessage(text = quoteReply, quote = sampleText)
                .assertScrollToBottomButton(isDisplayed = false)
        }
    }

    @AllureId("5899")
    @Test
    fun test_quotedReplyIsDeletedByParticipant_deletedMessageIsShown_InThread() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("AND participant adds a quoted reply") {
            participantRobot.quoteMessageInThread(quoteReply)
        }
        step("WHEN participant deletes a quoted message") {
            participantRobot.deleteMessage()
        }
        step("THEN user observes Message deleted in thread") {
            userRobot
                .openThread()
                .assertDeletedMessage(quoteReply)
        }
    }

    @AllureId("6790")
    @Test
    fun test_originalQuoteIsDeletedByParticipant_deletedMessageIsShown_InThread() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("AND participant sends a message") {
            participantRobot.sendMessageInThread(sampleText)
        }
        step("AND user adds a quoted reply") {
            userRobot
                .openThread()
                .quoteMessage(quoteReply)
        }
        step("WHEN participant deletes an original message") {
            participantRobot.deleteMessage()
        }
        step("THEN deleted message is shown") {
            userRobot.assertDeletedMessage(sampleText)
        }
        step("AND deleted message is shown in quoted reply bubble") {
            userRobot.assertQuotedMessage(
                text = quoteReply,
                quote = appContext.getString(R.string.stream_ui_message_list_message_deleted),
            )
        }
    }

    @AllureId("5900")
    @Test
    fun test_quotedReplyIsDeletedByUser_deletedMessageIsShown_InThread() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1, messagesText = sampleText)
            userRobot.login().openChannel()
        }
        step("AND user adds a quoted reply in thread") {
            userRobot
                .openThread()
                .quoteMessage(quoteReply)
        }
        step("WHEN user deletes a quoted message") {
            userRobot.deleteMessage()
        }
        step("THEN deleted message is shown") {
            userRobot
                .assertDeletedMessage(quoteReply)
                .assertQuotedMessage(text = sampleText, isDisplayed = false)
        }
    }

    @AllureId("6791")
    @Ignore("https://linear.app/stream/issue/AND-272")
    @Test
    fun test_originalQuoteIsDeletedByUser_deletedMessageIsShown_InThread() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1, messagesText = sampleText)
            userRobot.login().openChannel()
        }
        step("AND user adds a quoted reply in thread") {
            userRobot
                .openThread()
                .quoteMessage(quoteReply)
        }
        step("WHEN user deletes an original message") {
            userRobot.deleteMessage(messageCellIndex = 1)
        }
        step("THEN deleted message is shown") {
            userRobot.assertDeletedMessage(sampleText)
        }
        step("AND deleted message is shown in quoted reply bubble") {
            userRobot.assertQuotedMessage(
                text = quoteReply,
                quote = appContext.getString(R.string.stream_ui_message_list_message_deleted),
            )
        }
    }

    @AllureId("5912")
    @Test
    fun test_rootMessageShouldOnlyBeVisibleInTheLastPageInThread() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(
                channelsCount = 1,
                messagesCount = 1,
                messagesText = sampleText,
                repliesCount = messagesCount,
            )
            userRobot.login().openChannel()
        }
        step("WHEN user opens the thread") {
            userRobot.openThread()
        }
        step("THEN parent message is not loaded") {
            userRobot.assertMessages(text = sampleText, count = 0)
        }
        step("WHEN user scrolls up to load one more page") {
            userRobot.scrollMessageListUp(times = 5)
        }
        step("THEN parent message is loaded") {
            userRobot.assertMessages(text = sampleText, count = 1)
        }
    }

    @AllureId("5915")
    @Test
    fun test_quoteReplyRootMessageWhenNotInTheList() {
        step("GIVEN user opens the thread") {
            backendRobot.generateChannels(
                channelsCount = 1,
                messagesCount = 1,
                messagesText = sampleText,
                repliesCount = messagesCount,
            )
            userRobot.login().openChannel().openThread()
        }
        step("WHEN user quote replies root message") {
            userRobot
                .scrollMessageListUp(times = 5)
                .quoteMessage(quoteReply, messageCellIndex = messagesCount)
        }
        step("AND user reenters the thread") {
            userRobot
                .tapOnBackButton()
                .openThread()
        }
        step("AND user jumps to root message") {
            userRobot.tapOnQuotedMessage()
        }
        step("THEN parent message is loaded") {
            userRobot
                .assertQuotedMessage(text = quoteReply, quote = sampleText, isDisplayed = false)
                .assertScrollToBottomButton(isDisplayed = true)
        }
    }
}
