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

import io.getstream.chat.android.compose.robots.assertChannelAvatar
import io.getstream.chat.android.compose.robots.assertMessageDeliveryStatus
import io.getstream.chat.android.compose.robots.assertMessageInChannelPreview
import io.getstream.chat.android.compose.robots.assertMessagePreviewTimestamp
import io.getstream.chat.android.compose.uiautomator.device
import io.getstream.chat.android.compose.uiautomator.disableInternetConnection
import io.getstream.chat.android.compose.uiautomator.enableInternetConnection
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Ignore
import org.junit.Test

class ChannelListTests : StreamTestCase() {

    private val sampleText = "Test"

    @AllureId("6343")
    @Test
    fun test_channelPreviewUpdates_whenParticipantSendsMessage() {
        step("GIVEN user opens a channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant sends a message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND user goes back to the channel list") {
            userRobot.pressBack()
        }
        step("THEN user observes the new message in preview") {
            userRobot
                .assertMessageInChannelPreview(sampleText, false)
                .assertMessageDeliveryStatus(isDisplayed = false)
                .assertChannelAvatar()
        }
    }

    @AllureId("6344")
    @Test
    fun test_channelPreviewUpdates_whenUserSendsMessage() {
        step("GIVEN user opens a channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends a message") {
            userRobot.sendMessage("Test")
        }
        step("AND user goes back to the channel list") {
            userRobot.pressBack()
        }
        step("THEN user observes the new message in preview") {
            userRobot
                .assertMessageInChannelPreview(sampleText, true)
                .assertMessageDeliveryStatus(isDisplayed = true, shouldBeRead = false)
                .assertChannelAvatar()
        }
        step("WHEN participant reads the message") {
            participantRobot.readMessage()
        }
        step("THEN user observes the new message in preview") {
            userRobot.assertMessageDeliveryStatus(isDisplayed = true, shouldBeRead = true)
        }
    }

    @AllureId("6679")
    @Test
    fun test_channelPreviewUpdates_whenUserIsOfflineAndParticipantSendsMessage() {
        step("GIVEN user opens a channel list") {
            userRobot.login().waitForChannelListToLoad()
        }
        step("AND user goes offline") {
            device.disableInternetConnection()
        }
        step("WHEN participant sends a message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND user goes back online") {
            device.enableInternetConnection()
        }
        step("THEN user observes the new message in preview") {
            userRobot.assertMessageInChannelPreview(sampleText, false)
        }
    }

    @AllureId("5785")
    @Test
    fun test_errorMessageIsNotShownInChannelPreview() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends a message") {
            participantRobot.sendMessage(sampleText)
        }
        step("WHEN user sends a message with invalid command") {
            userRobot.sendMessage("/test")
        }
        step("AND user goes back to the channel list") {
            userRobot.tapOnBackButton()
        }
        step("THEN the error message is not shown in preview") {
            userRobot
                .assertMessageInChannelPreview(sampleText, false)
                .assertMessagePreviewTimestamp()
        }
    }

    @AllureId("5796")
    @Ignore("https://linear.app/stream/issue/AND-218")
    @Test
    fun test_channelPreviewShowsNoMessages_whenChannelIsEmpty() {
        step("WHEN user opens channel list") {
            userRobot.login()
        }
        step("AND the channel has no messages") {
            // No actions required as the channel is empty by default
        }
        step("THEN the channel preview shows No messages") {
            userRobot.assertMessageInChannelPreview("No messages", fromCurrentUser = false)
        }
        step("AND the message timestamp is hidden") {
            userRobot.assertMessagePreviewTimestamp(isDisplayed = false)
        }
    }

    @AllureId("5798")
    @Ignore("https://linear.app/stream/issue/AND-218")
    @Test
    fun test_channelPreviewShowsNoMessages_whenTheOnlyMessageInChannelIsDeleted() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends a message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND participant deletes the message") {
            participantRobot.deleteMessage()
        }
        step("WHEN user goes back to the channel list") {
            userRobot.tapOnBackButton()
        }
        step("THEN the channel preview shows No messages") {
            userRobot.assertMessageInChannelPreview("No messages", fromCurrentUser = false)
        }
        step("AND the message timestamp is hidden") {
            userRobot.assertMessagePreviewTimestamp(isDisplayed = false)
        }
    }

    @AllureId("5821")
    @Test
    fun test_channelPreviewShowsPreviousMessage_whenLastMessageIsDeleted() {
        val oldMessage = "Old"
        val newMessage = "New"

        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends 2 messages") {
            participantRobot
                .sendMessage(oldMessage)
                .sendMessage(newMessage)
        }
        step("AND participant deletes the last message") {
            participantRobot.deleteMessage()
        }
        step("WHEN user goes back to the channel list") {
            userRobot.tapOnBackButton()
        }
        step("THEN the channel preview shows previous message") {
            userRobot.assertMessageInChannelPreview(oldMessage, fromCurrentUser = false)
        }
        step("AND the message timestamp is shown") {
            userRobot.assertMessagePreviewTimestamp(isDisplayed = true)
        }
    }

    @AllureId("5799")
    @Test
    fun test_channelPreviewIsNotUpdated_whenThreadReplyIsSent() {
        val channelMessage = "Channel message"
        val threadReply = "Thread reply"

        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends a message") {
            participantRobot.sendMessage(channelMessage)
        }
        step("AND participant adds thread reply to this message") {
            participantRobot.sendMessageInThread(threadReply)
        }
        step("WHEN user goes back to the channel list") {
            userRobot.tapOnBackButton()
        }
        step("THEN the channel preview shows the channel message preview") {
            userRobot.assertMessageInChannelPreview(channelMessage, fromCurrentUser = false)
        }
        step("AND the message timestamp is shown") {
            userRobot.assertMessagePreviewTimestamp(isDisplayed = true)
        }
    }

    @AllureId("6680")
    @Test
    fun test_channelPreviewIsUpdated_whenThreadReplyIsSentAlsoInTheChannel() {
        val channelMessage = "Channel message"
        val threadReply = "Thread reply"

        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user sends a message") {
            userRobot.sendMessage(channelMessage)
        }
        step("AND user adds thread reply to this message also in the channel") {
            userRobot.openThread().sendMessageInThread(threadReply, alsoSendInChannel = true)
        }
        step("WHEN user goes back to the channel list") {
            userRobot.moveToChannelListFromThreadList()
        }
        step("THEN the channel preview shows the thread message preview") {
            userRobot.assertMessageInChannelPreview(threadReply, fromCurrentUser = true)
        }
        step("AND the message timestamp is shown") {
            userRobot.assertMessagePreviewTimestamp(isDisplayed = true)
        }
    }

    @AllureId("5820")
    @Test
    fun test_channelPreviewIsUpdated_whenPreviewMessageIsEdited() {
        val editedMessage = "edited message"

        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends a message") {
            participantRobot.sendMessage(sampleText)
        }
        step("WHEN participant edits the message") {
            participantRobot.editMessage(editedMessage)
        }
        step("AND user goes back to the channel list") {
            userRobot.tapOnBackButton()
        }
        step("THEN the channel preview shows edited message") {
            userRobot.assertMessageInChannelPreview(editedMessage, fromCurrentUser = false)
        }
        step("AND the message timestamp is shown") {
            userRobot.assertMessagePreviewTimestamp(isDisplayed = true)
        }
    }
}
