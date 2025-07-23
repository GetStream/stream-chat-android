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

import io.getstream.chat.android.compose.robots.assertAlsoInTheChannelLabelInChannel
import io.getstream.chat.android.compose.robots.assertAlsoInTheChannelLabelInThread
import io.getstream.chat.android.compose.robots.assertComposerCommandsMenu
import io.getstream.chat.android.compose.robots.assertComposerMentionsMenu
import io.getstream.chat.android.compose.robots.assertComposerSize
import io.getstream.chat.android.compose.robots.assertDeletedMessage
import io.getstream.chat.android.compose.robots.assertEditedMessage
import io.getstream.chat.android.compose.robots.assertMentionWasApplied
import io.getstream.chat.android.compose.robots.assertMessage
import io.getstream.chat.android.compose.robots.assertMessageAuthor
import io.getstream.chat.android.compose.robots.assertMessageDeliveryStatus
import io.getstream.chat.android.compose.robots.assertMessageFailedIcon
import io.getstream.chat.android.compose.robots.assertMessageSizeChangesAfterEditing
import io.getstream.chat.android.compose.robots.assertMessageTimestamps
import io.getstream.chat.android.compose.robots.assertScrollToBottomButton
import io.getstream.chat.android.compose.robots.assertThreadMessage
import io.getstream.chat.android.compose.robots.assertThreadReplyLabelOnParentMessage
import io.getstream.chat.android.compose.robots.assertThreadReplyLabelOnThreadMessage
import io.getstream.chat.android.compose.robots.assertTypingIndicator
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.getstream.chat.android.compose.uiautomator.device
import io.getstream.chat.android.compose.uiautomator.disableInternetConnection
import io.getstream.chat.android.compose.uiautomator.enableInternetConnection
import io.getstream.chat.android.compose.uiautomator.goToBackground
import io.getstream.chat.android.compose.uiautomator.goToForeground
import io.getstream.chat.android.e2e.test.mockserver.MessageDeliveryStatus
import io.getstream.chat.android.e2e.test.mockserver.forbiddenWord
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Ignore
import org.junit.Test

class MessageListTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin
    private val sampleText = "Test"

    // MARK: Message sending

    @AllureId("5661")
    @Test
    fun test_messageListUpdates_whenParticipantSendsMessage() {
        step("GIVEN user opens a channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant sends a message") {
            participantRobot.sendMessage(sampleText)
        }
        step("THEN user receives a message") {
            userRobot
                .assertMessage(sampleText)
                .assertMessageAuthor(isCurrentUser = false)
        }
    }

    @AllureId("5660")
    @Test
    fun test_messageListUpdates_whenUserSendsMessage() {
        step("GIVEN user opens a channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends a message") {
            userRobot.sendMessage(sampleText)
        }
        step("THEN message list updates") {
            userRobot
                .assertMessage(sampleText)
                .assertMessageAuthor(isCurrentUser = true)
                .assertMessageDeliveryStatus(MessageDeliveryStatus.SENT)
        }
    }

    @AllureId("5695")
    @Test
    fun test_userSendsMessageWithOneEmoji() {
        val message = "🤖"

        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends the emoji: $message") {
            userRobot.sendMessage(message)
        }
        step("THEN the message is delivered") {
            userRobot
                .assertMessage(message)
                .assertMessageDeliveryStatus(MessageDeliveryStatus.SENT)
        }
    }

    @AllureId("5697")
    @Test
    fun test_userSendsMessageWithMultipleEmojis() {
        val message = "🤖🔥✅"

        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends a message with multiple emojis: $message") {
            userRobot.sendMessage(message)
        }
        step("THEN the message is delivered") {
            userRobot
                .assertMessage(message)
                .assertMessageDeliveryStatus(MessageDeliveryStatus.SENT)
        }
    }

    // MARK: Message editing

    @AllureId("5673")
    @Test
    fun test_userEditsMessage() {
        val editedMessage = "hello"

        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends the message") {
            userRobot.sendMessage(sampleText)
        }
        step("AND user edits the message") {
            userRobot.editMessage(editedMessage)
        }
        step("THEN the message is edited") {
            userRobot
                .assertEditedMessage(editedMessage)
                .assertMessageDeliveryStatus(MessageDeliveryStatus.SENT)
        }
    }

    @AllureId("5674")
    @Test
    fun test_participantEditsMessage() {
        val editedMessage = "hello"

        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant sends the message") {
            participantRobot.sendMessage(sampleText)
            userRobot.assertMessage(sampleText)
        }
        step("AND participant edits the message") {
            participantRobot.editMessage(editedMessage)
        }
        step("THEN the message is edited") {
            userRobot.assertEditedMessage(editedMessage)
        }
    }

    // MARK: Message size

    @AllureId("5718")
    @Test
    fun test_messageIncreases_whenUserEditsMessageWithOneLineText() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user sends a one line message: $sampleText") {
            userRobot.sendMessage(sampleText)
        }
        step("THEN user verifies that message cell increases after editing") {
            userRobot.assertMessageSizeChangesAfterEditing(linesCountShouldBeIncreased = true)
        }
    }

    @AllureId("5719")
    @Test
    fun test_messageDecreases_whenUserEditsMessage() {
        val message = "test\nmessage"

        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user sends a two line message: $message") {
            userRobot.sendMessage(message)
        }
        step("THEN user verifies that message cell decreases after editing") {
            userRobot.assertMessageSizeChangesAfterEditing(linesCountShouldBeIncreased = false)
        }
    }

    // MARK: Composer

    @AllureId("5701")
    @Test
    fun test_composerSizeChange() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("THEN user verifies that composer does not grow more than 5 lines") {
            userRobot.assertComposerSize(isChangeable = true)
        }
    }

    @AllureId("5871")
    @Test
    fun test_composerSizeDoesNotChange() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("THEN user verifies that composer size changes") {
            userRobot.assertComposerSize(isChangeable = false)
        }
    }

    @Ignore("https://linear.app/stream/issue/AND-181")
    @AllureId("5717")
    @Test
    fun test_commandsMenuCloses_whenUserTapsOnMessageList() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user opens attachments menu") {
            userRobot
                .openComposerCommands()
                .assertComposerCommandsMenu(isDisplayed = true)
        }
        step("WHEN user taps on message list") {
            userRobot.tapOnMessageList()
        }
        step("THEN command suggestions disappear") {
            userRobot.assertComposerCommandsMenu(isDisplayed = false)
        }
    }

    // MARK: Typing indicator

    @AllureId("5702")
    @Test
    fun test_typingIndicator() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("WHEN participant starts typing") {
            participantRobot.startTyping()
        }
        step("THEN user observes typing indicator is shown") {
            userRobot.assertTypingIndicator(isDisplayed = true)
        }
        step("WHEN participant stops typing") {
            participantRobot.stopTyping()
        }
        step("THEN user observes typing indicator has disappeared") {
            userRobot.assertTypingIndicator(isDisplayed = false)
        }
    }

    @AllureId("5819")
    @Test
    fun test_threadTypingIndicatorHidden_whenParticipantStopsTyping() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("AND user opens the thread") {
            userRobot.openThread()
        }
        step("WHEN participant starts typing in thread") {
            participantRobot.startTypingInThread()
        }
        step("THEN user observes typing indicator is shown") {
            userRobot.assertTypingIndicator(isDisplayed = true)
        }
        step("WHEN participant stops typing in thread") {
            participantRobot.stopTypingInThread()
        }
        step("THEN user observes typing indicator has disappeared") {
            userRobot.assertTypingIndicator(isDisplayed = false)
        }
    }

    // MARK: Offline mode

    @AllureId("6609")
    @Test
    fun test_offlineMessageInTheMessageList() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user goes to background") {
            device.goToBackground()
        }
        step("WHEN participant sends a new message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND user becomes offline") {
            device.disableInternetConnection()
        }
        step("AND user comes back to foreground") {
            device.goToForeground()
        }
        step("THEN user does not observe a new message from participant") {
            userRobot.assertMessage(sampleText, isDisplayed = false)
        }
        step("AND user becomes online") {
            device.enableInternetConnection()
        }
        step("THEN user observes a new message from participant") {
            userRobot.assertMessage(sampleText)
        }
    }

    @AllureId("5670")
    @Test
    fun test_userAddsMessageWhileOffline() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user becomes offline") {
            device.disableInternetConnection()
        }
        step("WHEN user sends a new message") {
            userRobot.sendMessage(sampleText)
        }
        step("THEN pending indicator is shown for the message") {
            userRobot
                .assertMessageDeliveryStatus(status = MessageDeliveryStatus.PENDING, count = 1)
        }
        step("WHEN user becomes online") {
            device.enableInternetConnection()
        }
        step("THEN new message is delivered") {
            userRobot
                .assertMessageDeliveryStatus(MessageDeliveryStatus.SENT, count = 1)
                .assertMessageFailedIcon(isDisplayed = false)
        }
    }

    @AllureId("6610")
    @Test
    fun test_offlineRecoveryWithinSession() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user goes to the background") {
            device.goToBackground()
        }
        step("WHEN participant sends a new message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND user comes back to the foreground") {
            device.goToForeground()
        }
        step("THEN new message is delivered") {
            userRobot
                .assertMessage(sampleText)
                .assertMessageAuthor(isCurrentUser = false)
        }
    }

    // MARK: Scrolling

    @Ignore("https://linear.app/stream/issue/AND-76")
    @AllureId("5792")
    @Test
    fun test_messageListScrollsDown_whenMessageListIsScrolledUp_andUserSendsNewMessage() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 30)
            userRobot.login().openChannel()
        }
        step("WHEN user scrolls up") {
            userRobot.scrollMessageListUp()
        }
        step("AND user sends a new message") {
            userRobot.sendMessage(sampleText)
        }
        step("THEN message list is scrolled down") {
            userRobot
                .assertScrollToBottomButton(isDisplayed = false)
                .assertMessage(sampleText)
        }
    }

    @AllureId("5703")
    @Test
    fun test_messageListScrollsDown_whenMessageListIsScrolledDown_andUserReceivesNewMessage() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 30)
            userRobot.login().openChannel()
        }
        step("WHEN participant sends a message") {
            participantRobot.sendMessage(sampleText)
        }
        step("THEN message list is scrolled down") {
            userRobot
                .assertScrollToBottomButton(isDisplayed = false)
                .assertMessage(sampleText)
        }
    }

    @AllureId("5793")
    @Test
    fun test_messageListDoesNotScrollDown_whenMessageListIsScrolledUp_andUserReceivesNewMessage() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 30)
            userRobot.login().openChannel()
        }
        step("WHEN user scrolls up") {
            userRobot.scrollMessageListUp()
        }
        step("AND participant sends a message") {
            participantRobot.sendMessage(sampleText)
        }
        step("THEN message list is not scrolled down") {
            userRobot
                .assertMessage(sampleText, isDisplayed = false)
                .assertScrollToBottomButton(isDisplayed = true)
        }
        step("WHEN user taps on scroll to botton button") {
            userRobot.tapOnScrollToBottomButton()
        }
        step("THEN message list is scrolled down") {
            userRobot
                .assertMessage(sampleText)
                .assertScrollToBottomButton(isDisplayed = false)
        }
    }

    // MARK: Mentions

    @AllureId("5693")
    @Test
    fun test_mentionsView() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user types '@'") {
            userRobot.typeText("@")
        }
        step("THEN composer mention view appears") {
            userRobot.assertComposerMentionsMenu(isDisplayed = true)
        }
        step("WHEN user removes '@'") {
            userRobot.clearComposer()
        }
        step("THEN composer mention view disappears") {
            userRobot.assertComposerMentionsMenu(isDisplayed = false)
        }
    }

    @AllureId("5694")
    @Test
    fun test_userFillsTheComposerMentioningParticipantThroughMentionsView() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user taps on participant's name") {
            userRobot.mentionParticipant(send = false)
        }
        step("THEN composer fills in participant's name") {
            userRobot.assertMentionWasApplied()
        }
    }

    // MARK: - Thread replies

    @AllureId("5683")
    @Test
    fun test_threadReplyAppearsInThread_whenParticipantAddsThreadReply() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("WHEN participant adds a thread reply") {
            participantRobot.sendMessageInThread(sampleText, alsoSendInChannel = false)
        }
        step("AND user enters thread") {
            userRobot
                .assertThreadReplyLabelOnParentMessage()
                .openThread(usingContextMenu = false)
        }
        step("THEN user observes the thread reply in thread") {
            userRobot.assertThreadMessage(sampleText)
        }
    }

    @AllureId("5724")
    @Test
    fun test_threadReplyAppearsInChannelAndThread_whenParticipantAddsThreadReplySentAlsoToChannel() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("WHEN participant adds a thread reply and also sends it to the channel") {
            participantRobot.sendMessageInThread(sampleText, alsoSendInChannel = true)
        }
        step("THEN user observes the thread reply in channel") {
            userRobot
                .assertMessage(sampleText)
                .assertAlsoInTheChannelLabelInChannel()
                .assertThreadReplyLabelOnThreadMessage()
        }
        step("WHEN user enters thread") {
            userRobot.openThread(usingContextMenu = false)
        }
        step("THEN user observes the thread reply in thread") {
            userRobot
                .assertThreadMessage(sampleText)
                .assertAlsoInTheChannelLabelInThread()
        }
    }

    @AllureId("5725")
    @Test
    fun test_threadReplyAppearsInChannelAndThread_whenUserAddsThreadReplySentAlsoToChannel() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("WHEN user adds a thread reply and sends it also to the main channel") {
            userRobot.openThread().sendMessageInThread(sampleText, alsoSendInChannel = true)
        }
        step("THEN user observes the thread reply in thread") {
            userRobot
                .assertThreadMessage(sampleText)
                .assertAlsoInTheChannelLabelInThread()
        }
        step("AND user observes the thread reply in channel") {
            userRobot
                .tapOnBackButton()
                .assertMessage(sampleText)
                .assertAlsoInTheChannelLabelInChannel()
                .assertThreadReplyLabelOnThreadMessage()
        }
    }

    // MARK: Message deleting

    @AllureId("5671")
    @Test
    fun test_userDeletesMessage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends the message") {
            userRobot.sendMessage(sampleText)
        }
        step("AND user deletes the message") {
            userRobot.deleteMessage()
        }
        step("THEN the message is deleted") {
            userRobot
                .assertDeletedMessage(sampleText)
                .assertMessageAuthor(isCurrentUser = true)
        }
    }

    @AllureId("5672")
    @Test
    fun test_participantDeletesMessage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant sends the message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND participant deletes the message") {
            participantRobot.deleteMessage()
        }
        step("THEN the message is deleted") {
            userRobot
                .assertDeletedMessage(sampleText)
                .assertMessageAuthor(isCurrentUser = false)
        }
    }

    @AllureId("5813")
    @Ignore("https://linear.app/stream/issue/AND-211")
    @Test
    fun test_userHardDeletesMessage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends the message") {
            userRobot.sendMessage(sampleText)
        }
        step("AND user hard-deletes the message") {
            userRobot.deleteMessage(hard = true)
        }
        step("THEN the message is hard-deleted") {
            userRobot.assertDeletedMessage(sampleText, hard = true)
        }
    }

    @AllureId("5814")
    @Test
    fun test_participantHardDeletesMessage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant sends the message") {
            participantRobot.sendMessage(sampleText)
            userRobot.assertMessage(sampleText)
        }
        step("AND participant hard-deletes the message") {
            participantRobot.deleteMessage(hard = true)
        }
        step("THEN the message is hard-deleted") {
            userRobot.assertDeletedMessage(sampleText, hard = true)
        }
    }

    @AllureId("5726")
    @Test
    fun test_threadReplyIsRemovedEverywhere_whenParticipantRemovesItFromChannel() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("AND participant adds a thread reply and sends it also to main channel") {
            participantRobot.sendMessageInThread(sampleText, alsoSendInChannel = true)
        }
        step("WHEN participant removes the thread reply") {
            participantRobot.deleteMessage()
        }
        step("THEN user observes the thread reply removed in channel") {
            userRobot
                .assertDeletedMessage(sampleText)
                .assertAlsoInTheChannelLabelInChannel()
        }
        step("AND user observes the thread reply removed in thread") {
            userRobot
                .openThread(messageCellIndex = 1)
                .assertDeletedMessage(sampleText)
                .assertAlsoInTheChannelLabelInThread()
        }
    }

    @AllureId("5728")
    @Test
    fun test_threadReplyIsRemovedEverywhere_whenUserRemovesItFromChannel() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("AND user adds a thread reply and sends it also to main channel") {
            userRobot
                .openThread()
                .sendMessageInThread(sampleText, alsoSendInChannel = true)
        }
        step("WHEN user removes thread reply from channel") {
            userRobot
                .tapOnBackButton()
                .deleteMessage()
        }
        step("THEN user observes the thread reply removed in channel") {
            userRobot
                .assertDeletedMessage(sampleText)
                .assertAlsoInTheChannelLabelInChannel()
        }
        step("AND user observes the thread reply removed in thread") {
            userRobot
                .openThread(messageCellIndex = 1)
                .assertDeletedMessage(sampleText)
                .assertAlsoInTheChannelLabelInThread()
        }
    }

    @AllureId("5686")
    @Test
    fun test_participantRemovesThreadReply() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("AND participant adds a thread reply") {
            participantRobot.sendMessageInThread(sampleText, alsoSendInChannel = false)
        }
        step("WHEN participant removes the thread reply") {
            participantRobot.deleteMessage()
        }
        step("THEN user observes the thread reply removed in thread") {
            userRobot
                .assertThreadReplyLabelOnParentMessage()
                .openThread()
                .assertDeletedMessage(sampleText)
        }
    }

    @AllureId("5729")
    @Test
    fun test_threadReplyIsRemovedEverywhere_whenUserRemovesItFromThread() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("AND user adds a thread reply and sends it also to main channel") {
            userRobot
                .openThread()
                .sendMessageInThread(sampleText, alsoSendInChannel = true)
        }
        step("WHEN user removes thread reply from thread") {
            userRobot.deleteMessage()
        }
        step("THEN user observes the thread reply removed in thread") {
            userRobot
                .assertDeletedMessage(sampleText)
                .assertAlsoInTheChannelLabelInThread()
        }
        step("AND user observes the thread reply removed in channel") {
            userRobot
                .tapOnBackButton()
                .assertDeletedMessage(sampleText)
                .assertAlsoInTheChannelLabelInChannel()
        }
    }

    @AllureId("5686")
    @Test
    fun test_userRemovesThreadReply() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("AND user adds a thread reply") {
            userRobot
                .openThread()
                .sendMessageInThread(sampleText, alsoSendInChannel = false)
        }
        step("WHEN user removes the thread reply") {
            userRobot.deleteMessage()
        }
        step("THEN user observes the thread reply removed in thread") {
            userRobot.assertDeletedMessage(sampleText)
        }
        step("AND user observes a thread reply count button in channel") {
            userRobot
                .tapOnBackButton()
                .assertThreadReplyLabelOnParentMessage()
        }
    }

    @AllureId("6784")
    @Ignore("https://linear.app/stream/issue/AND-273")
    @Test
    fun test_threadIsNotLocked_afterParentMessageDeletedByUser() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("AND participant adds a message in thread") {
            participantRobot.sendMessageInThread(sampleText)
        }
        step("WHEN user deletes a parent message") {
            userRobot.deleteMessage()
        }
        step("THEN thread is not locked") {
            userRobot
                .openThread(usingContextMenu = false)
                .assertMessage(sampleText)
        }
    }

    @AllureId("6785")
    @Ignore("https://linear.app/stream/issue/AND-273")
    @Test
    fun test_threadIsNotLocked_afterParentMessageDeletedByParticipant() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends a message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND user sends a message in thread") {
            userRobot
                .openThread()
                .sendMessage(sampleText)
                .tapOnBackButton()
        }
        step("WHEN participant deletes a parent message") {
            participantRobot.deleteMessage()
        }
        step("THEN thread is not locked") {
            userRobot
                .openThread(usingContextMenu = false)
                .assertMessage(sampleText)
        }
    }

    // MARK: - Message grouping

    @AllureId("5803")
    @Test
    fun test_messageEndsGroup_whenFollowedByErrorMessage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user sends the 1st message") {
            userRobot.sendMessage(sampleText)
        }
        step("AND the timestamp is shown under the 1st message") {
            userRobot.assertMessageTimestamps(1)
        }
        step("WHEN user sends a message that does not pass moderation") {
            userRobot.sendMessage(forbiddenWord)
        }
        step("THEN messages are not grouped, 1st message shows the timestamp") {
            userRobot.assertMessageTimestamps(1)
        }
    }

    @AllureId("5830")
    @Test
    fun test_messageRendersTimestampAgain_whenMessageLastInGroupIsHardDeleted() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant inserts 3 group messages") {
            participantRobot
                .sendMessage("1")
                .sendMessage("2")
                .sendMessage("3")
            userRobot.assertMessageTimestamps(1)
        }
        step("WHEN participant hard deletes last message") {
            participantRobot.deleteMessage(hard = true)
        }
        step("THEN previous message should re-render timestamp") {
            userRobot.assertMessageTimestamps(1)
        }
    }

    @AllureId("6608")
    @Ignore("https://linear.app/stream/issue/AND-212")
    @Test
    fun test_messageRendersTimestampAgain_whenMessageLastInGroupIsSoftDeleted() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant inserts 3 group messages") {
            participantRobot
                .sendMessage("1")
                .sendMessage("2")
                .sendMessage("3")
            userRobot.assertMessageTimestamps(1)
        }
        step("WHEN participant hard deletes last message") {
            participantRobot.deleteMessage(hard = false)
        }
        step("THEN previous message should re-render timestamp") {
            userRobot.assertMessageTimestamps(1)
        }
    }
}
