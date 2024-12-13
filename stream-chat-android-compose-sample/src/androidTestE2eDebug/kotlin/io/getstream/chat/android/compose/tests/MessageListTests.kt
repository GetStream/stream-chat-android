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

import io.getstream.chat.android.compose.robots.assertAttachmentsMenu
import io.getstream.chat.android.compose.robots.assertComposerCommandsMenu
import io.getstream.chat.android.compose.robots.assertComposerMentionsMenu
import io.getstream.chat.android.compose.robots.assertComposerSize
import io.getstream.chat.android.compose.robots.assertEditedMessage
import io.getstream.chat.android.compose.robots.assertMessage
import io.getstream.chat.android.compose.robots.assertMessageAuthor
import io.getstream.chat.android.compose.robots.assertMessageFailedIcon
import io.getstream.chat.android.compose.robots.assertMessageReadStatus
import io.getstream.chat.android.compose.robots.assertMessageSizeChangesAfterEditing
import io.getstream.chat.android.compose.robots.assertScrollToBottomButton
import io.getstream.chat.android.compose.robots.assertTypingIndicator
import io.getstream.chat.android.compose.uiautomator.device
import io.getstream.chat.android.compose.uiautomator.disableInternetConnection
import io.getstream.chat.android.compose.uiautomator.enableInternetConnection
import io.getstream.chat.android.compose.uiautomator.goToBackground
import io.getstream.chat.android.compose.uiautomator.goToForeground
import io.getstream.chat.android.e2e.test.mockserver.MessageReadStatus
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Ignore
import org.junit.Test

class MessageListTests : StreamTestCase() {

    private val sampleText = "Test"

    @AllureId("5661")
    @Test
    fun test_messageListUpdates_whenParticipantSendsMessage() {
        step("GIVEN user opens a channel") {
            userRobot
                .login()
                .openChannel()
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
            userRobot
                .login()
                .openChannel()
        }
        step("WHEN user sends a message") {
            userRobot.sendMessage(sampleText)
        }
        step("THEN message list updates") {
            userRobot
                .assertMessage(sampleText)
                .assertMessageAuthor(isCurrentUser = true)
                .assertMessageReadStatus(MessageReadStatus.SENT)
        }
        step("WHEN participant reads the message") {
            participantRobot.readMessage()
        }
        step("THEN the message is read") {
            userRobot.assertMessageReadStatus(MessageReadStatus.READ)
        }
    }

    @AllureId("")
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
                .assertMessageReadStatus(MessageReadStatus.SENT)
        }
    }

    @AllureId("")
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
                .assertMessageReadStatus(MessageReadStatus.SENT)
        }
    }

    @AllureId("")
    @Test
    fun test_userEditsMessage() {
        val message = "test message"
        val editedMessage = "hello"

        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends the message: $message") {
            userRobot.sendMessage(message)
        }
        step("AND user edits the message: $editedMessage") {
            userRobot.editMessage(editedMessage)
        }
        step("THEN the message is edited") {
            userRobot
                .assertEditedMessage(editedMessage)
                .assertMessageReadStatus(MessageReadStatus.SENT)
        }
    }

    @AllureId("")
    @Test
    fun test_participantEditsMessage() {
        val message = "test message"
        val editedMessage = "hello"

        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant sends the message: $message") {
            participantRobot.sendMessage(message)
            userRobot.assertMessage(message)
        }
        step("AND participant edits the message: $editedMessage") {
            participantRobot.editMessage(editedMessage)
        }
        step("THEN the message is edited") {
            userRobot.assertEditedMessage(editedMessage)
        }
    }

    @AllureId("")
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

    @AllureId("")
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

    @AllureId("")
    @Test
    fun test_composerSizeChange() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("THEN user verifies that composer does not grow more than 5 lines") {
            userRobot.assertComposerSize(isChangeable = true)
        }
    }

    @AllureId("")
    @Test
    fun test_composerSizeLimit() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("THEN user verifies that composer size changes") {
            userRobot.assertComposerSize(isChangeable = false)
        }
    }

    @AllureId("")
    @Test
    fun test_typingIndicator() {
        step("GIVEN user opens the channel") {
            userRobot
                .login()
                .openChannel()
                .sendMessage(sampleText)
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

    @AllureId("")
    @Test
    fun test_attachmentsMenuCloses_whenUserTapsOnMessageList() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user opens attachments menu") {
            userRobot
                // .openAttachmentsMenu()
                .assertAttachmentsMenu(isDisplayed = true)
        }
        step("WHEN user taps on message list") {
            userRobot.tapOnMessageList()
        }
        step("THEN command suggestions disappear") {
            userRobot.assertAttachmentsMenu(isDisplayed = false)
        }
    }

    @Ignore("https://linear.app/stream/issue/AND-181")
    @AllureId("")
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

    @AllureId("")
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


    @AllureId("")
    @Test
    fun test_addMessageWhileOffline() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user becomes offline") {
            device.disableInternetConnection()
        }
        step("WHEN user sends a new message") {
            userRobot.sendMessage(sampleText)
        }
        step("THEN error indicator is shown for the message") {
            userRobot.assertMessageFailedIcon(isDisplayed = true)
        }
        step("WHEN user becomes online") {
            device.enableInternetConnection()
        }
        step("AND user resends the message") {
            userRobot.resendMessage()
        }
        step("THEN new message is delivered") {
            userRobot
                .assertMessageReadStatus(MessageReadStatus.SENT)
                .assertMessageFailedIcon(isDisplayed = false)
        }
    }

    @AllureId("")
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

    @Ignore("https://linear.app/stream/issue/AND-76")
    @AllureId("")
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

    @AllureId("")
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

    @AllureId("")
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
    @AllureId("")
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

//
//         func test_userFillsTheComposerMentioningParticipantThroughMentionsView() {
//             linkToScenario(withId: 62)
//
//             GIVEN("user opens the channel") {
//                 userRobot.login().openChannel()
//             }
//             WHEN("user taps on participants name") {
//                 userRobot.mentionParticipant()
//             }
//             THEN("composer fills in participants name") {
//                 userRobot.assertMentionWasApplied()
//             }
//         }
//     }
//
// // MARK: Links preview
//
//     extension MessageList_Tests {
//
//         func test_addMessageWithLinkToUnsplash() {
//             linkToScenario(withId: 59)
//
//             let message = "https://unsplash.com/photos/1_2d3MRbI9c"
//
//             GIVEN("user opens the channel") {
//                 userRobot.login().openChannel()
//             }
//             WHEN("user sends a message with YouTube link") {
//                 userRobot
//                     .sendMessage(message)
//                     .scrollMessageListDown() // to hide the keyboard
//             }
//             THEN("user observes a preview of the image with description") {
//                 userRobot.assertLinkPreview()
//             }
//         }
//
//         func test_addMessageWithLinkToYoutube() {
//             linkToScenario(withId: 60)
//
//             let message = "https://youtube.com/watch?v=xOX7MsrbaPY"
//
//             GIVEN("user opens the channel") {
//                 userRobot.login().openChannel()
//             }
//             WHEN("user sends a message with YouTube link") {
//                 userRobot
//                     .sendMessage(message)
//                     .scrollMessageListDown() // to hide the keyboard
//             }
//             THEN("user observes a preview of the video with description") {
//                 userRobot.assertLinkPreview()
//             }
//         }
//
//         func test_participantAddsMessageWithLinkToUnsplash() {
//             linkToScenario(withId: 280)
//
//             let message = "https://unsplash.com/photos/1_2d3MRbI9c"
//
//             GIVEN("user opens the channel") {
//                 userRobot.login().openChannel()
//             }
//             WHEN("participant sends a message with Unsplash link") {
//                 participantRobot.sendMessage(message)
//                 userRobot.scrollMessageListDown() // to hide the keyboard
//             }
//             THEN("user observes a preview of the image with description") {
//                 userRobot.assertLinkPreview()
//             }
//         }
//
//         func test_participantAddsMessageWithLinkToYoutube() {
//             linkToScenario(withId: 281)
//
//             let message = "https://youtube.com/watch?v=xOX7MsrbaPY"
//
//             GIVEN("user opens the channel") {
//                 userRobot.login().openChannel()
//             }
//             WHEN("participant sends a message with YouTube link") {
//                 participantRobot.sendMessage(message)
//                 userRobot.scrollMessageListDown() // to hide the keyboard
//             }
//             THEN("user observes a preview of the video with description") {
//                 userRobot.assertLinkPreview()
//             }
//         }
//
//         func test_messageWithLinkOpensSafari() {
//             linkToScenario(withId: 3119)
//
//             let message = "Some link: https://youtube.com"
//
//             GIVEN("user opens the channel") {
//                 userRobot.login().openChannel()
//             }
//             WHEN("user sends a message with YouTube link") {
//                 userRobot
//                     .sendMessage(message)
//                     .scrollMessageListDown() // to hide the keyboard
//             }
//             THEN("user observes safari opening") {
//                 userRobot.assertLinkOpensSafari()
//             }
//         }
//
//         func test_messageWithLinkOpensSafari_whenNoHttpScheme() {
//             linkToScenario(withId: 3120)
//
//             let message = "Some link: youtube.com"
//
//             GIVEN("user opens the channel") {
//                 userRobot.login().openChannel()
//             }
//             WHEN("user sends a message with YouTube link without https://") {
//                 userRobot
//                     .sendMessage(message)
//                     .scrollMessageListDown() // to hide the keyboard
//             }
//             THEN("user observes safari opening") {
//                 userRobot.assertLinkOpensSafari()
//             }
//         }
//     }
//
// // MARK: - Thread replies
//     extension MessageList_Tests {
//         func test_threadReplyAppearsInThread_whenParticipantAddsThreadReply() {
//             linkToScenario(withId: 50)
//
//             let threadReply = "thread reply"
//
//             GIVEN("user opens the channel") {
//                 backendRobot.generateChannels(count: 1, messagesCount: 1)
//                 userRobot.login().openChannel()
//             }
//             WHEN("participant adds a thread reply") {
//                 participantRobot.replyToMessageInThread(threadReply)
//             }
//             AND("user enters thread") {
//                 userRobot.openThread()
//             }
//             THEN("user observes the thread reply in thread") {
//                 userRobot.assertThreadReply(threadReply)
//             }
//         }
//
//         func test_threadReplyAppearsInChannelAndThread_whenParticipantAddsThreadReplySentAlsoToChannel() {
//             linkToScenario(withId: 110)
//
//             let threadReply = "thread reply"
//
//             GIVEN("user opens the channel") {
//                 backendRobot.generateChannels(count: 1, messagesCount: 1)
//                 userRobot.login().openChannel()
//             }
//             WHEN("participant adds a thread reply") {
//                 participantRobot.replyToMessageInThread(threadReply, alsoSendInChannel: true)
//             }
//             THEN("user observes the thread reply in channel") {
//                 userRobot.assertMessage(threadReply)
//             }
//             WHEN("user enters thread") {
//                 userRobot.openThread()
//             }
//             THEN("user observes the thread reply in thread") {
//                 userRobot.assertThreadReply(threadReply)
//             }
//         }
//
//         func test_threadReplyAppearsInChannelAndThread_whenUserAddsThreadReplySentAlsoToChannel() {
//             linkToScenario(withId: 111)
//
//             let threadReply = "thread reply"
//
//             GIVEN("user opens the channel") {
//                 backendRobot.generateChannels(count: 1, messagesCount: 1)
//                 userRobot.login().openChannel()
//             }
//             WHEN("user adds a thread reply and sends it also to main channel") {
//                 userRobot.replyToMessageInThread(threadReply, alsoSendInChannel: true)
//             }
//             THEN("user observes the thread reply in thread") {
//                 userRobot.assertThreadReply(threadReply)
//             }
//             AND("user observes the thread reply in channel") {
//                 userRobot
//                     .tapOnBackButton()
//                     .assertMessage(threadReply)
//             }
//         }
//
//         func test_threadTypingIndicatorHidden_whenParticipantStopsTyping() {
//             linkToScenario(withId: 243)
//
//             GIVEN("user opens the channel") {
//                 backendRobot.generateChannels(count: 1, messagesCount: 1)
//                 userRobot.login().openChannel()
//             }
//             AND("user opens the thread") {
//                 userRobot.openThread()
//             }
//             WHEN("participant starts typing in thread") {
//                 participantRobot.wait(2).startTypingInThread()
//             }
//             THEN("user observes typing indicator is shown") {
//                 let typingUserName = UserDetails.userName(for: participantRobot.currentUserId)
//                 userRobot.assertTypingIndicatorShown(typingUserName: typingUserName)
//             }
//             WHEN("participant stops typing in thread") {
//                 participantRobot.wait(2).stopTypingInThread()
//             }
//             THEN("user observes typing indicator has disappeared") {
//                 userRobot.assertTypingIndicatorHidden()
//             }
//         }
//     }
//
// // MARK: - Message grouping
//
//     extension MessageList_Tests {
//         func test_messageEndsGroup_whenFollowedByErrorMessage() {
//             linkToScenario(withId: 218)
//
//             let message = "Hey there"
//             let messageWithForbiddenContent = server.forbiddenWords.first ?? ""
//
//             GIVEN("user opens the channel") {
//                 userRobot
//                     .login()
//                     .openChannel()
//             }
//             AND("user sends the 1st message") {
//                 userRobot.sendMessage(message)
//             }
//             AND("the timestamp is shown under the 1st message") {
//                 userRobot.assertMessageHasTimestamp()
//             }
//             WHEN("user sends a message that does not pass moderation") {
//                 userRobot.sendMessage(messageWithForbiddenContent, waitForAppearance: false)
//             }
//             THEN("messages are not grouped, 1st message shows the timestamp") {
//                 userRobot.assertMessageHasTimestamp(at: 1)
//             }
//         }
//
//         func test_messageEndsGroup_whenFollowedByEphemeralMessage() {
//             linkToScenario(withId: 221)
//
//             let message = "Hey there"
//
//             GIVEN("user opens the channel") {
//                 userRobot
//                     .login()
//                     .openChannel()
//             }
//             AND("user sends the 1st message") {
//                 userRobot.sendMessage(message)
//             }
//             AND("the timestamp is shown under the 1st message") {
//                 userRobot.assertMessageHasTimestamp()
//             }
//             WHEN("user sends an ephemeral message") {
//                 userRobot
//                     .sendGiphy(send: false)
//                 .scrollMessageListDown() // to hide the keyboard
//             }
//             THEN("messages are not grouped, 1st message shows the timestamp") {
//                 userRobot
//                     .assertMessageCount(2)
//                     .assertMessageHasTimestamp(at: 1)
//             }
//         }
//
//         func test_messageRendersTimestampAgain_whenMessageLastInGroupIsHardDeleted() {
//             linkToScenario(withId: 288)
//
//             GIVEN("user opens the channel") {
//                 backendRobot
//                     .generateChannels(count: 1, messagesCount: 1)
//                 userRobot
//                     .login()
//                     .openChannel()
//             }
//             AND("user inserts 3 group messages") {
//                 userRobot.sendMessage("Hey")
//                 userRobot.sendMessage("Hey2")
//                 userRobot.sendMessage("Hey3")
//                 userRobot.assertMessageHasTimestamp()
//             }
//             WHEN("user deletes last message") {
//                 userRobot.deleteMessage(hard: true)
//             }
//             THEN("previous message should re-render timestamp") {
//                 userRobot.assertMessageHasTimestamp(at: 0)
//             }
//         }
//     }
//
// // MARK: Deleted messages
//
//     extension MessageList_Tests {
//         func test_deletesMessage() throws {
//             linkToScenario(withId: 37)
//
//             let message = "test message"
//
//             GIVEN("user opens the channel") {
//                 userRobot.login().openChannel()
//             }
//             WHEN("user sends the message: '\(message)'") {
//                 userRobot.sendMessage(message)
//             }
//             AND("user deletes the message: '\(message)'") {
//                 userRobot.deleteMessage()
//             }
//             THEN("the message is deleted") {
//                 userRobot.assertDeletedMessage()
//             }
//         }
//
//         func test_messageDeleted_whenParticipantDeletesMessage() throws {
//             linkToScenario(withId: 38)
//
//             let message = "test message"
//
//             GIVEN("user opens the channel") {
//                 userRobot.login().openChannel()
//             }
//             WHEN("participant sends the message: '\(message)'") {
//                 participantRobot.sendMessage(message)
//             }
//             AND("participant deletes the message: '\(message)'") {
//                 participantRobot.deleteMessage()
//             }
//             THEN("the message is deleted") {
//                 userRobot.assertDeletedMessage()
//             }
//         }
//
//         func test_threadReplyIsRemovedEverywhere_whenParticipantRemovesItFromChannel() {
//             linkToScenario(withId: 112)
//
//             let threadReply = "thread reply"
//
//             GIVEN("user opens the channel") {
//                 backendRobot.generateChannels(count: 1, messagesCount: 1)
//                 userRobot.login().openChannel()
//             }
//             AND("participant adds a thread reply and sends it also to main channel") {
//                 participantRobot.replyToMessageInThread(threadReply, alsoSendInChannel: true)
//             }
//             WHEN("participant removes the thread reply from channel") {
//                 participantRobot.deleteMessage()
//             }
//             THEN("user observes the thread reply removed in channel") {
//                 userRobot.assertDeletedMessage()
//             }
//             AND("user observes the thread reply removed in thread") {
//                 userRobot
//                     .openThread(messageCellIndex: 1)
//                 .assertDeletedMessage()
//             }
//         }
//
//         func test_threadReplyIsRemovedEverywhere_whenUserRemovesItFromChannel() throws {
//             linkToScenario(withId: 114)
//
//             let threadReply = "thread reply"
//
//             GIVEN("user opens the channel") {
//                 backendRobot.generateChannels(count: 1, messagesCount: 1)
//                 userRobot.login().openChannel()
//             }
//             AND("user adds a thread reply and sends it also to main channel") {
//                 userRobot.replyToMessageInThread(threadReply, alsoSendInChannel: true)
//             }
//             WHEN("user removes thread reply from thread") {
//                 userRobot.deleteMessage()
//             }
//             THEN("user observes the thread reply removed in thread") {
//                 userRobot.assertDeletedMessage()
//             }
//             AND("user observes the thread reply removed in channel") {
//                 userRobot
//                     .tapOnBackButton()
//                     .assertDeletedMessage()
//             }
//         }
//
//         func test_participantRemovesThreadReply() {
//             linkToScenario(withId: 54)
//
//             let threadReply = "thread reply"
//
//             GIVEN("user opens the channel") {
//                 backendRobot.generateChannels(count: 1, messagesCount: 1)
//                 userRobot.login().openChannel()
//             }
//             AND("participant adds a thread reply") {
//                 participantRobot.replyToMessageInThread(threadReply, alsoSendInChannel: false)
//             }
//             WHEN("participant removes the thread reply") {
//                 participantRobot.deleteMessage()
//             }
//             THEN("user observes a thread reply count button in channel") {
//                 userRobot.assertThreadReplyCountButton()
//             }
//             THEN("user observes the thread reply removed in thread") {
//                 userRobot.openThread().assertDeletedMessage()
//             }
//         }
//
//         func test_threadReplyIsRemovedEverywhere_whenUserRemovesItFromThread() {
//             linkToScenario(withId: 115)
//
//             let threadReply = "thread reply"
//
//             GIVEN("user opens the channel") {
//                 backendRobot.generateChannels(count: 1, messagesCount: 1)
//                 userRobot.login().openChannel()
//             }
//             AND("user adds a thread reply and sends it also to main channel") {
//                 userRobot.replyToMessageInThread(threadReply, alsoSendInChannel: true)
//             }
//             WHEN("user goes back to channel and removes thread reply") {
//                 userRobot
//                     .tapOnBackButton()
//                     .deleteMessage()
//             }
//             THEN("user observes the thread reply removed in channel") {
//                 userRobot.assertDeletedMessage()
//             }
//             AND("user observes the thread reply removed in thread") {
//                 userRobot
//                     .openThread(messageCellIndex: 1)
//                 .assertDeletedMessage()
//             }
//         }
//
//         func test_userRemovesThreadReply() throws {
//             linkToScenario(withId: 53)
//
//             let threadReply = "thread reply"
//
//             GIVEN("user opens the channel") {
//                 backendRobot.generateChannels(count: 1, messagesCount: 1)
//                 userRobot.login().openChannel()
//             }
//             AND("user adds a thread reply") {
//                 userRobot.replyToMessageInThread(threadReply, alsoSendInChannel: false)
//             }
//             WHEN("user removes the thread reply") {
//                 userRobot.deleteMessage()
//             }
//             THEN("user observes the thread reply removed in thread") {
//                 userRobot.assertDeletedMessage()
//             }
//             AND("user observes a thread reply count button in channel") {
//                 userRobot
//                     .tapOnBackButton()
//                     .assertThreadReplyCountButton()
//             }
//         }
//
//         func test_hardDeletesMessage() throws {
//             linkToScenario(withId: 234)
//
//             let message = "test message"
//
//             GIVEN("user opens the channel") {
//                 backendRobot.generateChannels(count: 1, messagesCount: 1)
//                 userRobot.login().openChannel()
//             }
//             WHEN("user sends the message: '\(message)'") {
//                 userRobot.sendMessage(message)
//             }
//             AND("user hard-deletes the message: '\(message)'") {
//                 userRobot.deleteMessage(hard: true)
//             }
//             THEN("the message is hard-deleted") {
//                 userRobot.assertHardDeletedMessage(withText: message)
//             }
//         }
//
//         func test_messageDeleted_whenParticipantHardDeletesMessage() throws {
//             linkToScenario(withId: 235)
//
//             let message = "test message"
//
//             GIVEN("user opens the channel") {
//                 backendRobot.generateChannels(count: 1, messagesCount: 1)
//                 userRobot.login().openChannel()
//             }
//             WHEN("participant sends the message: '\(message)'") {
//                 participantRobot.sendMessage(message)
//             }
//             AND("the message is delivered") {
//                 userRobot.assertMessage(message)
//             }
//             AND("participant hard-deletes the message: '\(message)'") {
//                 participantRobot.wait(2).deleteMessage(hard: true)
//             }
//             THEN("the message is hard-deleted") {
//                 userRobot.assertHardDeletedMessage(withText: message)
//             }
//         }

}
