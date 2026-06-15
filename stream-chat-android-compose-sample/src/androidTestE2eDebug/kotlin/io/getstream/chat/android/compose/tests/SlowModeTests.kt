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

import io.getstream.chat.android.compose.robots.assertComposerIsDisabledInSlowMode
import io.getstream.chat.android.compose.robots.assertCooldownIsShown
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.qameta.allure.kotlin.Allure.step
import org.junit.Test

class SlowModeTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin

    private val cooldownDuration = 15
    private val message = "message"

    @Test
    fun test_cooldownIsShownWhenNewMessageIsSent() {
        step("GIVEN slow mode is enabled on the channel") {
            backendRobot.setCooldown(enabled = true, duration = cooldownDuration)
        }
        step("AND user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends a new message") {
            userRobot.sendMessage(message)
        }
        step("THEN slow mode is active and the cooldown is shown") {
            userRobot.assertCooldownIsShown()
        }
    }

    @Test
    fun test_composerIsDisabledWhenSlowModeIsActive() {
        step("GIVEN slow mode is enabled on the channel") {
            backendRobot.setCooldown(enabled = true, duration = cooldownDuration)
        }
        step("AND user opens the channel") {
            userRobot.login().openChannel()
        }
        step("WHEN user sends a new message") {
            userRobot.sendMessage(message)
        }
        step("THEN the cooldown is shown and the composer is locked") {
            userRobot
                .assertCooldownIsShown()
                .assertComposerIsDisabledInSlowMode()
        }
    }
}
