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

import io.getstream.chat.android.compose.robots.assertGiphyButtons
import io.getstream.chat.android.compose.robots.assertGiphyImage
import io.getstream.chat.android.compose.robots.assertInvalidCommandMessage
import io.getstream.chat.android.compose.robots.assertMessageDeliveryStatus
import io.getstream.chat.android.compose.robots.assertMessageInChannelPreview
import io.getstream.chat.android.compose.robots.assertMessageTimestamps
import io.getstream.chat.android.compose.robots.assertSystemMessage
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Ignore
import org.junit.Test

class GiphyTests : StreamTestCase() {

    @AllureId("5698")
    @Test
    fun test_userObservesAnimatedGiphy_whenUserAddsGiphyMessage() {
        step("GIVEN user opens a channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends a giphy using giphy command") {
            userRobot.uploadGiphy()
        }
        step("THEN user observes the animated gif") {
            userRobot.assertGiphyImage()
        }
    }

    @AllureId("5699")
    @Test
    fun test_userObservesAnimatedGiphy_whenParticipantAddsGiphyMessage() {
        step("GIVEN user opens a channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant sends a giphy") {
            participantRobot.uploadGiphy()
        }
        step("THEN user observes the animated gif") {
            userRobot.assertGiphyImage()
        }
    }

    @AllureId("5783")
    @Test
    fun test_userObservesAnimatedGiphy_whenUserAddsGiphyMessageInThread() {
        step("GIVEN user opens a channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("WHEN user runs a giphy command in thread") {
            userRobot
                .openThread()
                .uploadGiphy()
        }
        step("THEN user observes the animated gif in thread") {
            userRobot.assertGiphyImage()
        }
    }

    @AllureId("6714")
    @Test
    fun test_userObservesAnimatedGiphy_whenParticipantAddsGiphyMessageInThread() {
        step("GIVEN user opens a channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("WHEN participant sends a giphy") {
            participantRobot.uploadGiphyInThread()
        }
        step("THEN user observes the animated gif in thread") {
            userRobot
                .openThread()
                .assertGiphyImage()
        }
    }

    @AllureId("5707")
    @Test
    fun test_messageIsNotSent_whenUserSendsInvalidCommand() {
        val invalidCommand = "invalid command"

        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("WHEN user sends a message with invalid command") {
            userRobot.sendMessage("/$invalidCommand")
        }
        step("THEN user observes error message") {
            userRobot.assertInvalidCommandMessage(invalidCommand)
        }
        step("AND the previous message has timestamp and delivery status shown") {
            userRobot
                .assertMessageDeliveryStatus(isDisplayed = true)
                .assertMessageTimestamps(count = 1)
        }
    }

    @AllureId("5787")
    @Ignore("https://linear.app/stream/issue/AND-218")
    @Test
    fun test_channelListNotModified_whenEphemeralMessageShown() {
        step("GIVEN user opens a channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user runs a giphy command") {
            userRobot.uploadGiphy(send = false)
        }
        step("WHEN user goes back to channel list") {
            userRobot.tapOnBackButton()
        }
        step("THEN message is not added to the channel list") {
            userRobot.assertMessageInChannelPreview("No messages", fromCurrentUser = false)
        }
    }

    @AllureId("5782")
    @Ignore("https://linear.app/stream/issue/AND-245")
    @Test
    fun test_deliveryStatusHidden_whenEphemeralMessageShown() {
        step("GIVEN user opens a channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user runs a giphy command") {
            userRobot.uploadGiphy(send = false)
        }
        step("THEN delivery status is hidden for ephemeral messages") {
            userRobot
                .assertMessageDeliveryStatus(false)
                .assertSystemMessage("Only visible to you")
        }
    }

    @AllureId("5823")
    @Test
    fun test_userObservesAnimatedGiphy_afterAddingGiphyThroughComposerMenu() {
        step("GIVEN user opens a channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends a giphy using giphy command") {
            userRobot.uploadGiphy(useComposerCommand = true)
        }
        step("THEN user observes the animated gif") {
            userRobot.assertGiphyImage()
        }
    }

    @AllureId("5824")
    @Test
    fun test_messageIsNotSent_whenUserCancelsEphemeralMessage() {
        step("GIVEN user opens a channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user cancels a giphy") {
            userRobot
                .uploadGiphy(send = false)
                .sleep(1000)
                .tapOnCancelGiphyButton()
        }
        step("THEN user does not observe the animated gif") {
            userRobot
                .assertGiphyImage(isDisplayed = false)
                .assertGiphyButtons(areDisplayed = false)
        }
    }

    @AllureId("5815")
    @Test
    fun test_userObservesAnimatedGiphy_whenUserAddsGiphyMessage_AfterShuffling() {
        step("GIVEN user opens a channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user shuffles a giphy") {
            userRobot
                .uploadGiphy(send = false)
                .tapOnShuffleGiphyButton()
        }
        step("THEN the giphy is shuffled but not sent") {
            userRobot
                .assertGiphyImage(isDisplayed = true)
                .assertGiphyButtons(areDisplayed = true)
        }
        step("WHEN user sends a giphy") {
            userRobot.tapOnSendGiphyButton()
        }
        step("THEN user observes the animated gif") {
            userRobot
                .assertGiphyImage(isDisplayed = true)
                .assertGiphyButtons(areDisplayed = false)
        }
    }
}
