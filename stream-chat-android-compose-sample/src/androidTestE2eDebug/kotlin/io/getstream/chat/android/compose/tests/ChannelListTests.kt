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
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

class ChannelListTests : StreamTestCase() {

    private val sampleText = "Test"

    @AllureId("6343")
    @Test
    fun test_channelPreviewUpdates_whenParticipantSendsMessage() {
        step("GIVEN user opens a channel") {
            userRobot
                .login()
                .openChannel()
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
                .assertMessageDeliveryStatus(shouldBeVisible = false)
                .assertChannelAvatar()
        }
    }

    @AllureId("6344")
    @Test
    fun test_channelPreviewUpdates_whenUserSendsMessage() {
        step("GIVEN user opens a channel") {
            userRobot
                .login()
                .openChannel()
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
                .assertMessageDeliveryStatus(shouldBeVisible = true, shouldBeRead = false)
                .assertChannelAvatar()
        }
        step("WHEN participant reads the message") {
            participantRobot.readMessage()
        }
        step("THEN user observes the new message in preview") {
            userRobot.assertMessageDeliveryStatus(shouldBeVisible = true, shouldBeRead = true)
        }
    }
}
