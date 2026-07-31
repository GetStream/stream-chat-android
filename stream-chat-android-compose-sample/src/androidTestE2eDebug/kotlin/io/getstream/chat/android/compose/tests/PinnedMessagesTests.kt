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

package io.getstream.chat.android.compose.tests

import io.getstream.chat.android.compose.robots.assertMessageDeliveryStatus
import io.getstream.chat.android.compose.robots.assertMessageInPinnedMessages
import io.getstream.chat.android.compose.robots.assertMessagePinnedLabel
import io.getstream.chat.android.compose.robots.assertPinnedMessagesAreEmpty
import io.getstream.chat.android.compose.robots.assertPinnedMessagesScreen
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.getstream.chat.android.e2e.test.mockserver.MessageDeliveryStatus
import io.getstream.chat.android.e2e.test.robots.ParticipantRobot
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

class PinnedMessagesTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin
    private val sampleText = "Test"

    @AllureId("5953")
    @Test
    fun test_userPinsMessage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant sends the message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND user pins the message") {
            userRobot.pinMessage()
        }
        step("THEN the message shows the pinned by you label") {
            userRobot.assertMessagePinnedLabel()
        }
    }

    @AllureId("11383")
    @Test
    fun test_userUnpinsMessage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends the message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND user pins the message") {
            userRobot.pinMessage()
        }
        step("WHEN user unpins the message") {
            userRobot.unpinMessage()
        }
        step("THEN the message shows no pinned label") {
            userRobot.assertMessagePinnedLabel(isDisplayed = false)
        }
    }

    @AllureId("11384")
    @Test
    fun test_participantPinsMessage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends the message") {
            userRobot.sendMessage(sampleText)
        }
        step("AND the message is delivered") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT)
        }
        step("AND participant pins the message") {
            participantRobot.pinMessage()
        }
        step("THEN the message shows the pinned by participant label") {
            userRobot.assertMessagePinnedLabel(pinnedBy = ParticipantRobot.name)
        }
    }

    @AllureId("11568")
    @Test
    fun test_pinnedMessageIsShownOnThePinnedMessagesScreen() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends the message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND user pins the message") {
            userRobot.pinMessage()
        }
        step("WHEN user opens the pinned messages screen") {
            userRobot
                .moveToChannelListFromMessageList()
                .openChannelMenu()
                .tapOnViewChannelInfo()
                .tapOnPinnedMessagesOption()
        }
        step("THEN the pinned message is shown") {
            userRobot
                .assertPinnedMessagesScreen()
                .assertMessageInPinnedMessages(sampleText)
        }
    }

    @AllureId("11565")
    @Test
    fun test_unpinnedMessageIsNotShownOnThePinnedMessagesScreen() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends the message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND user pins the message") {
            userRobot.pinMessage()
        }
        step("WHEN user unpins the message") {
            userRobot.unpinMessage()
        }
        step("AND user opens the pinned messages screen") {
            userRobot
                .moveToChannelListFromMessageList()
                .openChannelMenu()
                .tapOnViewChannelInfo()
                .tapOnPinnedMessagesOption()
        }
        step("THEN no pinned message is shown") {
            userRobot
                .assertPinnedMessagesScreen()
                .assertPinnedMessagesAreEmpty()
        }
    }

    @AllureId("11385")
    @Test
    fun test_participantUnpinsMessage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user sends the message") {
            userRobot.sendMessage(sampleText)
        }
        step("AND the message is delivered") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT)
        }
        step("AND participant pins the message") {
            participantRobot.pinMessage()
        }
        step("AND the message shows the pinned by participant label") {
            userRobot.assertMessagePinnedLabel(pinnedBy = ParticipantRobot.name)
        }
        step("WHEN participant unpins the message") {
            participantRobot.unpinMessage()
        }
        step("THEN the message shows no pinned label") {
            userRobot.assertMessagePinnedLabel(pinnedBy = ParticipantRobot.name, isDisplayed = false)
        }
    }
}
