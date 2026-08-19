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

import io.getstream.chat.android.compose.robots.assertBlockMessageAuthorOption
import io.getstream.chat.android.compose.robots.assertChannelCount
import io.getstream.chat.android.compose.robots.assertChannelWithName
import io.getstream.chat.android.compose.robots.assertFlagMessageDialog
import io.getstream.chat.android.compose.robots.assertMessage
import io.getstream.chat.android.compose.robots.assertMuteMessageAuthorOption
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.getstream.chat.android.e2e.test.robots.ParticipantRobot
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

/**
 * Covers the moderation options the message menu offers for another user's message: flagging the
 * message, and muting or blocking its author. The options are absent on the user's own messages.
 */
class ModerationTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin
    private val sampleText = "Test"

    // Seeded messages alternate authors starting with the user, so with two seeded
    // messages the one with text "2" belongs to the participant.
    private val participantMessageText = "2"
    private val groupChannelName = "1"

    @AllureId("11572")
    @Test
    fun test_userFlagsMessage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends the message") {
            participantRobot.sendMessage(sampleText)
        }
        step("WHEN user taps on the flag option of the message") {
            userRobot.flagMessage(sampleText)
        }
        step("THEN the flag confirmation is shown") {
            userRobot.assertFlagMessageDialog(isDisplayed = true)
        }
        step("WHEN user confirms flagging the message") {
            userRobot.confirmFlagMessage()
        }
        step("THEN the confirmation is dismissed and the message stays in the message list") {
            userRobot
                .assertFlagMessageDialog(isDisplayed = false)
                .assertMessage(sampleText)
        }
    }

    @AllureId("11563")
    @Test
    fun test_userMutesMessageAuthor() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends the message") {
            participantRobot.sendMessage(sampleText)
        }
        step("WHEN user mutes the message author") {
            userRobot.muteMessageAuthor(sampleText)
        }
        step("THEN the message menu offers to unmute the author") {
            userRobot.assertMuteMessageAuthorOption(sampleText, isAuthorMuted = true)
        }
    }

    @AllureId("11566")
    @Test
    fun test_userUnmutesMessageAuthor() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends the message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND user mutes the message author") {
            userRobot.muteMessageAuthor(sampleText)
        }
        step("WHEN user unmutes the message author") {
            userRobot.unmuteMessageAuthor(sampleText)
        }
        step("THEN the message menu offers to mute the author again") {
            userRobot.assertMuteMessageAuthorOption(sampleText, isAuthorMuted = false)
        }
    }

    @AllureId("6071")
    @Test
    fun test_userBlocksMessageAuthor() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends the message") {
            participantRobot.sendMessage(sampleText)
        }
        step("WHEN user blocks the message author") {
            userRobot.blockMessageAuthor(sampleText)
        }
        step("THEN the message menu offers to unblock the author") {
            userRobot.assertBlockMessageAuthorOption(sampleText, isAuthorBlocked = true)
        }
    }

    @AllureId("11575")
    @Test
    fun test_userUnblocksMessageAuthor() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND participant sends the message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND user blocks the message author") {
            userRobot.blockMessageAuthor(sampleText)
        }
        step("WHEN user unblocks the message author") {
            userRobot.unblockMessageAuthor(sampleText)
        }
        step("THEN the message menu offers to block the author again") {
            userRobot.assertBlockMessageAuthorOption(sampleText, isAuthorBlocked = false)
        }
    }

    @AllureId("6070")
    @Test
    fun test_userBlocksUserInDirectMessageChannel() {
        step("GIVEN a direct message channel with the participant exists") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 2, withDirectMessageChannel = true)
        }
        step("AND user opens the direct message channel") {
            userRobot.login().openChannel(ParticipantRobot.name)
        }
        step("WHEN user blocks the participant") {
            userRobot.blockMessageAuthor(participantMessageText)
        }
        step("THEN the message menu offers to unblock the participant") {
            userRobot.assertBlockMessageAuthorOption(participantMessageText, isAuthorBlocked = true)
        }
    }

    @AllureId("6072")
    @Test
    fun test_directMessageChannelDisappears_whenUserBlocksParticipant() {
        step("GIVEN a direct message channel with the participant exists") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 2, withDirectMessageChannel = true)
        }
        step("AND user sees it in the channel list") {
            userRobot
                .login()
                .assertChannelCount(2)
                .assertChannelWithName(ParticipantRobot.name)
        }
        step("WHEN user blocks the participant in the direct message channel") {
            userRobot
                .openChannel(ParticipantRobot.name)
                .blockMessageAuthor(participantMessageText)
                .moveToChannelListFromMessageList()
        }
        step("THEN the direct message channel disappears from the channel list") {
            userRobot
                .assertChannelCount(1)
                .assertChannelWithName(ParticipantRobot.name, isDisplayed = false)
        }
        step("WHEN user unblocks the participant from the group channel") {
            userRobot
                .openChannel(groupChannelName)
                .unblockMessageAuthor(participantMessageText)
                .moveToChannelListFromMessageList()
        }
        step("THEN the direct message channel is shown in the channel list again") {
            userRobot
                .assertChannelCount(2)
                .assertChannelWithName(ParticipantRobot.name)
        }
    }
}
