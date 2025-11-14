/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.compose.robots.assertDeletedMessage
import io.getstream.chat.android.compose.robots.assertMessage
import io.getstream.chat.android.compose.robots.assertReaction
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.getstream.chat.android.e2e.test.mockserver.ReactionType
import io.qameta.allure.kotlin.Allure.step
import org.junit.Test

class BackendTests : StreamTestCase() {

    override var useMockServer = false
    override fun initTestActivity() = InitTestActivity.UserLogin

    @Test
    fun test_message() {
        val originalMessage = "hi"
        val editedMessage = "hello"

        step("GIVEN user opens a channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends a message") {
            userRobot.sendMessage(originalMessage)
        }
        step("THEN message appears") {
            userRobot.assertMessage(originalMessage)
        }
        step("WHEN user edits the message") {
            userRobot.editMessage(editedMessage)
        }
        step("THEN the message is edited") {
            userRobot.assertMessage(editedMessage)
        }
        step("WHEN user deletes the message") {
            userRobot.deleteMessage()
        }
        step("THEN the message is deleted") {
            userRobot.assertDeletedMessage()
        }
    }

    @Test
    fun test_reaction() {
        val message = "test"

        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends the message") {
            userRobot.sendMessage(message)
        }
        step("AND user adds the reaction") {
            userRobot.addReaction(type = ReactionType.LIKE)
        }
        step("THEN the reaction is added") {
            userRobot.assertReaction(type = ReactionType.LIKE, isDisplayed = true)
        }
        step("WHEN user removes the reaction") {
            userRobot.deleteReaction(type = ReactionType.LIKE)
        }
        step("THEN the reaction is removed") {
            userRobot.assertReaction(type = ReactionType.LIKE, isDisplayed = false)
        }
    }
}
