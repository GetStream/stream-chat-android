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

import io.getstream.chat.android.compose.robots.assertMessage
import io.getstream.chat.android.compose.robots.assertMessageAuthor
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

class MessageListTests : StreamTestCase() {

    private val sampleText = "Test"

    @AllureId("5661")
    @Test
    fun test_messageListUpdates_whenParticipantSendsMessage() {
        step("GIVEN user opens a channel") {
            userRobot
                .login()
                .openChannel()
        }
        step("WHEN participant sends a message") {
            participantRobot.sendMessage(sampleText)
        }
        step("THEN user receives a message") {
            userRobot
                .assertMessage(sampleText)
                .assertMessageAuthor(isCurrentUser = false)
        }
    }

    @AllureId("5660")
    @Test
    fun test_messageListUpdates_whenUserSendsMessage() {
        step("GIVEN user opens a channel") {
            userRobot
                .login()
                .openChannel()
        }
        step("WHEN user sends a message") {
            userRobot.sendMessage("Test")
        }
        step("THEN message list updates") {
            userRobot
                .assertMessage(sampleText)
                .assertMessageAuthor(isCurrentUser = true)
        }
    }
}
