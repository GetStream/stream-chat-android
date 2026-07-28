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

import io.getstream.chat.android.compose.robots.assertChannelUnreadCount
import io.getstream.chat.android.compose.robots.assertMessage
import io.getstream.chat.android.compose.robots.assertMessageDeliveryStatus
import io.getstream.chat.android.compose.robots.assertScrollToFirstUnreadButton
import io.getstream.chat.android.compose.robots.assertUnreadSeparator
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.getstream.chat.android.e2e.test.mockserver.MessageDeliveryStatus
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

class UnreadMessagesTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin
    private val sampleText = "Test"

    @AllureId("11453")
    @Test
    fun test_unreadSeparatorIsShown_whenParticipantSendsMessagesWhileUserIsAway() {
        val unreadCount = 2
        step("GIVEN user opens the channel and sends the message") {
            userRobot.login().openChannel().sendMessage(sampleText)
        }
        step("AND the message is delivered") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT)
        }
        step("AND user moves back to the channel list") {
            userRobot.moveToChannelListFromMessageList()
        }
        step("WHEN participant sends new messages") {
            participantRobot.sendMultipleMessages(text = "New", count = unreadCount)
        }
        step("AND the channel preview shows the unread count") {
            userRobot.assertChannelUnreadCount(unreadCount)
        }
        step("AND user reopens the channel") {
            userRobot.openChannel()
        }
        step("THEN the unread separator is shown with the unread count") {
            userRobot.assertUnreadSeparator(unreadCount = unreadCount)
        }
    }

    @AllureId("11454")
    @Test
    fun test_userScrollsToFirstUnreadMessage() {
        val unreadCount = 25
        step("GIVEN user opens the channel and sends the message") {
            userRobot.login().openChannel().sendMessage(sampleText)
        }
        step("AND the message is delivered") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT)
        }
        step("AND user moves back to the channel list") {
            userRobot.moveToChannelListFromMessageList()
        }
        step("AND participant sends new messages") {
            participantRobot.sendMultipleMessages(text = "New", count = unreadCount)
        }
        step("AND the channel preview shows the unread count") {
            userRobot.assertChannelUnreadCount(unreadCount)
        }
        step("WHEN user reopens the channel") {
            userRobot.openChannel()
        }
        step("THEN the scroll to first unread button is shown with the unread count") {
            userRobot.assertScrollToFirstUnreadButton(unreadCount = unreadCount)
        }
        step("WHEN user taps on the scroll to first unread button") {
            userRobot.tapOnScrollToFirstUnreadButton()
        }
        step("THEN the list scrolls to the unread separator") {
            userRobot.assertUnreadSeparator(unreadCount = unreadCount)
        }
    }

    @AllureId("6073")
    @Test
    fun test_userMarksMessageAsUnread() {
        val unreadCount = 2
        step("GIVEN user opens the channel and sends the message") {
            userRobot.login().openChannel().sendMessage(sampleText)
        }
        step("AND the message is delivered") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT)
        }
        step("AND participant sends messages") {
            participantRobot.sendMultipleMessages(text = "New", count = unreadCount)
            userRobot.assertMessage("New-$unreadCount")
        }
        step("WHEN user marks the first participant message as unread") {
            userRobot.markMessageAsUnread(messageCellIndex = 1)
        }
        step("THEN the unread separator is shown with the unread count") {
            userRobot.assertUnreadSeparator(unreadCount = unreadCount)
        }
        step("AND the channel preview shows the unread count") {
            userRobot.moveToChannelListFromMessageList().assertChannelUnreadCount(unreadCount)
        }
    }

    @AllureId("11455")
    @Test
    fun test_userDismissesTheUnreadIndicator() {
        val unreadCount = 25
        step("GIVEN user opens the channel and sends the message") {
            userRobot.login().openChannel().sendMessage(sampleText)
        }
        step("AND the message is delivered") {
            userRobot.assertMessageDeliveryStatus(MessageDeliveryStatus.SENT)
        }
        step("AND user moves back to the channel list") {
            userRobot.moveToChannelListFromMessageList()
        }
        step("AND participant sends new messages") {
            participantRobot.sendMultipleMessages(text = "New", count = unreadCount)
        }
        step("AND user reopens the channel") {
            userRobot.openChannel()
        }
        step("WHEN user dismisses the unread indicator") {
            userRobot.dismissUnreadIndicator()
        }
        step("THEN the scroll to first unread button is hidden") {
            userRobot.assertScrollToFirstUnreadButton(isDisplayed = false)
        }
    }
}
