/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.uitests.ui.compose.robot

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.rules.TestRule

/**
 * A factory function for [ComposeLoginRobot].
 */
internal fun ComposeTestRule.composeLoginRobot(
    block: ComposeLoginRobot.() -> Unit,
): ComposeLoginRobot = ComposeLoginRobot(this).apply { block() }

/**
 * A robot that simulates user behavior on the login screen.
 *
 *  @param composeTestRule A [TestRule] that provides the main entry point into testing.
 */
internal class ComposeLoginRobot(
    composeTestRule: ComposeTestRule,
) : BaseComposeTestRobot(composeTestRule) {

    /**
     * Use UI Components SDK after login.
     */
    fun selectedUiComponentsSdk() {
        composeTestRule
            .onNodeWithContentDescription("UI Components")
            .performClick()
    }

    /**
     * Use Compose SDK after login.
     */
    fun selectComposeSdk() {
        composeTestRule
            .onNodeWithContentDescription("Compose")
            .performClick()
    }

    /**
     * Clicks on the login button that should take the user to the channels screen.
     */
    fun loginWithUser(name: String) {
        composeTestRule
            .onNodeWithText(name)
            .performClick()
    }
}
