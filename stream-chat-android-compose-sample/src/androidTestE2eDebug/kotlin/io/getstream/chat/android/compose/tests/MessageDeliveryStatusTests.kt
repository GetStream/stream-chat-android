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

import io.getstream.chat.android.compose.robots.assertMessageDeliveryStatus
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.getstream.chat.android.e2e.test.mockserver.MessageDeliveryStatus
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Ignore
import org.junit.Test

class MessageDeliveryStatusTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin
    private val sampleText = "Test"

    @AllureId("5741")
    @Test
    fun test_singleCheckmarkShown_whenMessageIsSent() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends a new message") {
            userRobot.sendMessage(sampleText)
        }
        step("THEN user spots single checkmark below the message") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT)
        }
    }

    @AllureId("5742")
    @Test
    fun test_deliveryStatusShowsClocks_whenMessageIsInPendingState() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends a new message that is gonna freeze") {
            backendRobot.freezeNewMessages()
            userRobot.sendMessage("pending message")
        }
        step("THEN message delivery status shows clocks") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.PENDING)
        }
    }

    @AllureId("5743")
    @Test
    fun test_errorIndicatorShown_whenMessageFailedToBeSent() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends a new message that is gonna fail") {
            backendRobot.failNewMessages()
            userRobot.sendMessage("failed message")
        }
        step("THEN error indicator is shown for the message") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.FAILED)
        }
    }

    @AllureId("5744")
    @Test
    fun test_doubleCheckmarkShown_whenMessageReadByParticipant() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user successfully sends a new message") {
            userRobot.sendMessage(sampleText)
        }
        step("WHEN participant reads the user's message") {
            participantRobot.readMessage()
        }
        step("THEN user spots double checkmark below the message") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.READ)
        }
    }

    @AllureId("5747")
    @Test
    fun test_deliveryStatusShownForTheLastMessageInGroup() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user successfully sends a new message") {
            userRobot.sendMessage(sampleText)
        }
        step("AND delivery status shows single checkmark") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT, count = 1)
        }
        step("WHEN user sends another message") {
            userRobot.sendMessage(sampleText)
        }
        step("THEN delivery status for the previous message is hidden") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT, count = 1)
        }
    }

    @AllureId("5748")
    @Test
    fun test_deliveryStatusHidden_whenMessageIsDeleted() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user successfully sends a new message") {
            userRobot.sendMessage(sampleText)
        }
        step("AND delivery status shows single checkmark") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT)
        }
        step("WHEN user removes the message") {
            userRobot.deleteMessage()
        }
        step("THEN delivery status is hidden") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.NIL)
        }
    }

    @AllureId("5749")
    @Test
    fun test_singleCheckmarkShown_whenMessageIsSent_andPreviewedInThread() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user successfully sends a new message") {
            userRobot.sendMessage("message")
        }
        step("WHEN user previews thread for the message") {
            userRobot.openThread()
        }
        step("THEN user spots single checkmark below the message") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT)
        }
        step("WHEN participant reads the message") {
            participantRobot.readMessage()
        }
        step("THEN user spots double checkmark below the message") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.READ)
        }
    }

    @AllureId("5752")
    @Test
    fun test_singleCheckmarkShown_whenThreadReplyIsSent() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant sends a new message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND user replies to the message in thread") {
            userRobot.openThread().sendMessage("thread reply")
        }
        step("THEN user spots single checkmark below the thread reply") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT)
        }
    }

    @AllureId("5753")
    @Test
    fun test_errorIndicatorShown_whenThreadReplyFailedToBeSent() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("WHEN user sends a message in thread that is gonna fail") {
            backendRobot.failNewMessages()
            userRobot.openThread().sendMessage(sampleText)
        }
        step("THEN error indicator is shown for the thread reply") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.FAILED)
        }
    }

    @AllureId("5754")
    @Test
    fun test_doubleCheckmarkShown_whenThreadReplyReadByParticipant() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("WHEN user replies to message in thread") {
            userRobot.openThread().sendMessage(sampleText)
        }
        step("AND participant reads the user's thread reply") {
            participantRobot.readMessage()
        }
        step("THEN user spots double checkmark below the message") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.READ)
        }
    }

    @AllureId("5757")
    @Test
    fun test_deliveryStatusShownForTheLastThreadReplyInGroup() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("WHEN user replies to message in thread") {
            userRobot.openThread().sendMessage(sampleText)
        }
        step("AND delivery status shows single checkmark") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT, count = 2)
        }
        step("AND user sends another message") {
            userRobot.sendMessage(sampleText)
        }
        step("THEN delivery status for the previous message is hidden") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT, count = 2)
        }
    }

    @AllureId("5758")
    @Test
    fun test_deliveryStatusHidden_whenThreadReplyIsDeleted() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("AND user successfully sends a new message in thread") {
            userRobot.openThread().sendMessage(sampleText)
        }
        step("AND delivery status shows single checkmark") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT, count = 2)
        }
        step("WHEN user removes the message") {
            userRobot.deleteMessage()
        }
        step("THEN delivery status is hidden") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT, count = 1)
        }
    }

    @AllureId("5784")
    @Test
    fun test_deliveryStatusShownForPreviousMessage_whenErrorMessageShown() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("WHEN user sends message that is gonna fail") {
            backendRobot.failNewMessages()
            userRobot.sendMessage(sampleText)
        }
        step("THEN delivery status is shown for the previous message") {
            userRobot
                .assertMessageDeliveryStatus(MessageDeliveryStatus.FAILED, count = 1)
                .assertMessageDeliveryStatus(MessageDeliveryStatus.SENT, count = 1)
        }
    }

    @AllureId("6747")
    @Test
    fun test_deliveryStatusShownForPreviousMessage_whenSystemMessageShown() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND successfully sends a new message") {
            userRobot.sendMessage(sampleText)
        }
        step("WHEN user sends message with invalid command") {
            userRobot.sendMessage("/command")
        }
        step("THEN delivery status is shown for first message") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT, count = 1)
        }
    }

    @AllureId("5767")
    @Test
    fun test_deliveryStatusClocksShownInPreview_whenTheLastMessageIsInPendingState() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends a new message that is gonna freeze") {
            backendRobot.freezeNewMessages()
            userRobot.sendMessage(sampleText)
        }
        step("AND user returns to the channel list") {
            userRobot.moveToChannelListFromMessageList()
        }
        step("THEN last message delivery status in the channel preview shows clocks") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.PENDING)
        }
    }

    @AllureId("5768")
    @Test
    fun test_singleCheckmarkShownInPreview_whenTheLastMessageIsSent() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user sends a new message") {
            userRobot.sendMessage(sampleText)
        }
        step("WHEN user returns to the channel list") {
            userRobot.moveToChannelListFromMessageList()
        }
        step("THEN last message delivery status in the channel preview shows single checkmark") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT)
        }
    }

    @AllureId("5769")
    @Ignore("https://linear.app/stream/issue/AND-256")
    @Test
    fun test_errorIndicatorShownInPreview_whenMessageFailedToBeSent() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }

        step("WHEN user sends a new message that is gonna fail") {
            backendRobot.failNewMessages()
            userRobot.sendMessage(sampleText)
        }
        step("AND user returns to the channel list") {
            userRobot.moveToChannelListFromMessageList()
        }
        step("THEN last message delivery status in the channel preview shows failed icon") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.FAILED)
        }
    }

    @AllureId("5770")
    @Test
    fun test_doubleCheckmarkShownInPreview_whenMessageReadByParticipant() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user sends a new message") {
            userRobot.sendMessage(sampleText)
        }
        step("WHEN user returns to the channel list") {
            userRobot.moveToChannelListFromMessageList()
        }
        step("AND participant reads the message") {
            participantRobot.readMessage()
        }
        step("THEN last message delivery status in the channel preview shows double checkmark") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.READ)
        }
    }

    @AllureId("6748")
    @Test
    fun test_deliveryStatusHiddenInPreview_whenMessageIsSentByParticipant() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant sends a new message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND user returns to the channel list") {
            userRobot.moveToChannelListFromMessageList()
        }
        step("THEN delivery status is hidden") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.NIL)
        }
    }

    @AllureId("6749")
    @Test
    fun test_noCheckmarkShownForMessageInPreview_whenThreadReplyIsSent() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends a new message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND user replies to the message in thread") {
            userRobot.openThread().sendMessage(sampleText)
        }
        step("WHEN user returns to the channel list") {
            userRobot.moveToChannelListFromThread()
        }
        step("THEN delivery status is hidden") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.NIL)
        }
    }

    @AllureId("5774")
    @Ignore("https://linear.app/stream/issue/AND-255")
    @Test
    fun test_singleCheckmarkShownForMessageInPreview_whenThreadReplyFailedToBeSent() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user sends a new message") {
            userRobot.sendMessage("1")
        }
        step("AND user sends a new message in thread that is gonna freeze") {
            userRobot.openThread()
            backendRobot.freezeNewMessages()
            userRobot.sendMessage("2")
        }
        step("WHEN user returns to the channel list") {
            userRobot.moveToChannelListFromThread()
        }
        step("THEN delivery status shows a single checkmark") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT)
        }
    }

    @AllureId("6750")
    @Test
    fun test_noCheckmarkShownForMessageInPreview_whenThreadReplyReadByParticipant() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends a new message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND user replies to this message in thread") {
            userRobot.openThread().sendMessage(sampleText)
        }
        step("AND participant reads the user's thread reply") {
            participantRobot.readMessage()
        }
        step("WHEN user returns to the channel list") {
            userRobot.moveToChannelListFromThread()
        }
        step("THEN delivery status is hidden") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.NIL)
        }
    }

    @AllureId("6751")
    @Test
    fun test_noCheckmarkShownForMessageInPreview_whenThreadReplyIsSentByParticipant() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user sends a new message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND participant replies to message in thread") {
            participantRobot.sendMessageInThread(sampleText)
        }
        step("WHEN user returns to the channel list") {
            userRobot.moveToChannelListFromMessageList()
        }
        step("THEN delivery status is hidden") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.NIL)
        }
    }
}
