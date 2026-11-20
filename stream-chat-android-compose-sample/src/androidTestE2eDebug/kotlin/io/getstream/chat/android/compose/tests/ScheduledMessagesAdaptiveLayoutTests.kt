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

import io.getstream.chat.android.compose.pages.ScheduledMessagesPage.Banner
import io.getstream.chat.android.compose.robots.assertMessage
import io.getstream.chat.android.compose.robots.assertScheduledMessage
import io.getstream.chat.android.compose.robots.assertScheduledMessageIsNotShown
import io.getstream.chat.android.compose.robots.assertScheduledMessagesBanner
import io.getstream.chat.android.compose.robots.assertScheduledMessagesScreen
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.qameta.allure.kotlin.Allure.step
import org.junit.Test

/**
 * Covers scheduled messages in the sample's adaptive layout, where the chats screen opens the
 * scheduled messages of a channel in its info pane.
 */
class ScheduledMessagesAdaptiveLayoutTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin
    override var useScheduledMessages = true
    override var useAdaptiveLayout = true

    @Test
    fun test_bannerOpensTheScheduledMessagesOfTheChannel() {
        val firstChannelText = "Scheduled in the first channel"
        val secondChannelText = "Scheduled in the second channel"
        step("GIVEN user schedules a message in a first channel") {
            backendRobot.generateChannels(channelsCount = 2)
            userRobot
                .login()
                .openChannel(channelCellIndex = 1)
                .scheduleMessage(firstChannelText)
                .assertScheduledMessagesBanner(Banner.scheduledFor)
                .pressBack()
        }
        step("AND user schedules a message in a second channel") {
            userRobot
                .openChannel(channelCellIndex = 0)
                .scheduleMessage(secondChannelText)
                .assertScheduledMessagesBanner(Banner.scheduledFor)
        }
        step("WHEN user opens the scheduled messages from the banner") {
            userRobot.openScheduledMessagesFromBanner()
        }
        step("THEN only the message of the second channel is listed") {
            userRobot
                .assertScheduledMessagesScreen()
                .assertScheduledMessage(secondChannelText)
                .assertScheduledMessageIsNotShown(firstChannelText)
        }
        step("WHEN user goes back") {
            userRobot.pressBack()
        }
        step("THEN the second channel shows the banner again") {
            userRobot
                .assertScheduledMessagesBanner(Banner.scheduledFor)
                .assertMessage(secondChannelText, isDisplayed = false)
        }
    }
}
