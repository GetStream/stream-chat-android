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

import io.getstream.chat.android.compose.robots.assertReaction
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.getstream.chat.android.compose.uiautomator.device
import io.getstream.chat.android.compose.uiautomator.disableInternetConnection
import io.getstream.chat.android.compose.uiautomator.enableInternetConnection
import io.getstream.chat.android.compose.uiautomator.seconds
import io.getstream.chat.android.e2e.test.mockserver.ReactionType
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

class ReactionsTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin
    private val sampleText = "Test"

    @AllureId("5675")
    @Test
    fun test_addsReaction() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends the message") {
            userRobot.sendMessage(sampleText)
        }
        step("AND user adds the reaction") {
            userRobot.addReaction(type = ReactionType.LIKE)
        }
        step("THEN the reaction is added") {
            userRobot.assertReaction(type = ReactionType.LIKE, isDisplayed = true)
        }
    }

    @AllureId("5679")
    @Test
    fun test_deletesReaction() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends the message") {
            userRobot.sendMessage(sampleText)
        }
        step("AND user adds the reaction") {
            userRobot.addReaction(type = ReactionType.WOW)
        }
        step("AND user removes the reaction") {
            userRobot.deleteReaction(type = ReactionType.WOW)
        }
        step("THEN the reaction is removed") {
            userRobot.assertReaction(type = ReactionType.WOW, isDisplayed = false)
        }
    }

    @AllureId("5676")
    @Test
    fun test_reactionIsAdded_whenReactingToParticipantsMessage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant sends the message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND user adds the reaction") {
            userRobot.addReaction(type = ReactionType.LOVE)
        }
        step("THEN the reaction is added") {
            userRobot.assertReaction(type = ReactionType.LOVE, isDisplayed = true)
        }
    }

    @AllureId("5680")
    @Test
    fun test_removesReaction_whenUnReactingToParticipantsMessage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant sends the message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND user adds the reaction") {
            userRobot.addReaction(type = ReactionType.LOL)
        }
        step("AND user removes the reaction") {
            userRobot.deleteReaction(type = ReactionType.LOL).sleep(5000)
        }
        step("THEN the reaction is removed") {
            userRobot.assertReaction(type = ReactionType.LOL, isDisplayed = false)
        }
    }

    @AllureId("5677")
    @Test
    fun test_reactionIsAddedByParticipant_whenReactingToUsersMessage() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("AND participant adds the reaction to user's message") {
            participantRobot
                .readMessage()
                .addReaction(type = ReactionType.LIKE)
        }
        step("THEN the reaction is added") {
            userRobot.assertReaction(type = ReactionType.LIKE, isDisplayed = true)
        }
    }

    @AllureId("5681")
    @Test
    fun test_reactionIsRemovedByParticipant_whenUnReactingToUsersMessage() {
        step("GIVEN user opens the channel") {
            backendRobot.generateChannels(channelsCount = 1, messagesCount = 1)
            userRobot.login().openChannel()
        }
        step("AND participant adds the reaction to user's message") {
            participantRobot
                .readMessage()
                .addReaction(type = ReactionType.LOL)
        }
        step("AND participant removes the reaction") {
            participantRobot.deleteReaction(type = ReactionType.LOL)
        }
        step("THEN the reaction is removed") {
            userRobot.assertReaction(type = ReactionType.LOL, isDisplayed = false)
        }
    }

    @AllureId("5678")
    @Test
    fun test_reactionIsAddedByParticipant_whenReactingToOwnMessage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant sends the message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND participant adds the reaction") {
            participantRobot.addReaction(type = ReactionType.WOW)
        }
        step("THEN the reaction is added") {
            userRobot.assertReaction(type = ReactionType.WOW, isDisplayed = true)
        }
    }

    @AllureId("5682")
    @Test
    fun test_reactionIsRemovedByParticipant_whenUnReactingToOwnMessage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN participant sends the message") {
            participantRobot.sendMessage(sampleText)
        }
        step("AND participant adds the reaction") {
            participantRobot.addReaction(type = ReactionType.SAD)
        }
        step("AND participant removes the reaction") {
            participantRobot.deleteReaction(type = ReactionType.SAD)
        }
        step("THEN the reaction is removed") {
            userRobot.assertReaction(type = ReactionType.SAD, isDisplayed = false)
        }
    }

    @AllureId("5714")
    @Test
    fun test_userAddsReactionWhileOffline() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user sends a message") {
            userRobot.sendMessage(sampleText)
        }
        step("AND user becomes offline") {
            device.disableInternetConnection()
        }
        step("WHEN user adds a reaction") {
            userRobot.addReaction(type = ReactionType.LIKE)
        }
        step("THEN user observes a new reaction") {
            userRobot.assertReaction(type = ReactionType.LIKE, isDisplayed = true)
        }
        step("WHEN user becomes online") {
            device.enableInternetConnection()
        }
        step("THEN user still observes a new reaction") {
            userRobot.assertReaction(type = ReactionType.LIKE, isDisplayed = true)
        }
    }

    @AllureId("6713")
    @Test
    fun test_participantAddsReactionWhileUserIsOffline() {
        val delay = 3
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user sends a message") {
            userRobot
                .sendMessage(sampleText)
                .sleep(500) // to fix flakiness on CI
        }
        step("AND user becomes offline") {
            participantRobot.addReaction(type = ReactionType.LIKE, delay)
            device.disableInternetConnection()
            userRobot.sleep((delay + 1).seconds)
        }
        step("WHEN participant adds a reaction") {
            // this action has been completed above with given delay,
            // because we can't send requests to the mock server being offline.
        }
        step("AND user becomes online") {
            device.enableInternetConnection()
        }
        step("THEN user observes a new reaction") {
            userRobot.assertReaction(type = ReactionType.LIKE, isDisplayed = true)
        }
    }
}
