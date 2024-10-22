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

import io.getstream.chat.android.compose.robots.assertTest
import io.getstream.chat.android.compose.uiautomator.sleep
import io.qameta.allure.kotlin.Allure.step
import org.junit.Test

class SampleTest : StreamTestCase() {

    @Test
    fun test_messageListUpdates_whenParticipantSendsMessage() {
        step("GIVEN user opens a channel") {
            userRobot
                .login()
                .openChannel()
        }
        step("WHEN participant sends a message") {
            participantRobot.sendMessage("Testme33")
        }
        step("THEN user receives a message") {
            userRobot.assertTest()
        }
    }

    @Test
    fun test_messageListUpdates_whenUserSendsMessage() {
        step("GIVEN user opens a channel") {
            userRobot
                .login()
                .openChannel()
        }
        step("WHEN user sends a message") {
            participantRobot.sendMessage("Testme44")
        }
        step("THEN message list updates") {
            userRobot.assertTest()
        }
    }
}
