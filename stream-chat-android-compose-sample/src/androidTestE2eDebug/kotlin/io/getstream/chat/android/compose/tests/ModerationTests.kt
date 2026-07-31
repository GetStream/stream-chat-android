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

import io.getstream.chat.android.compose.robots.assertFlagMessageDialog
import io.getstream.chat.android.compose.robots.assertMessage
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

/**
 * Covers flagging another user's message from the message menu. The option is absent on the user's
 * own messages.
 */
class ModerationTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin
    private val sampleText = "Test"

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
}
