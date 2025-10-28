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

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.rules.TestRule

/**
 * A factory function for [ComposeMessagesRobot].
 */
internal fun ComposeTestRule.composeMessagesRobot(
    block: ComposeMessagesRobot.() -> Unit,
): ComposeMessagesRobot = ComposeMessagesRobot(this).apply { block() }

/**
 * A robot that simulates user behavior on the messages screen.
 *
 * @param composeTestRule A [TestRule] that provides the main entry point into testing.
 */
internal class ComposeMessagesRobot(
    composeTestRule: ComposeTestRule,
) : BaseComposeTestRobot(composeTestRule) {

    /**
     * Types certain text in the message composer.
     *
     * @param text The text what will be typed in the message input.
     */
    fun typeMessageText(text: String) {
        composeTestRule
            .onNodeWithContentDescription("Message input")
            .performTextInput(text)
    }

    /**
     * Clicks the "send message" button.
     */
    fun clickSendButton() {
        composeTestRule
            .onNodeWithContentDescription("Send button")
            .performClick()
    }

    /**
     * Assert that any message is displayed.
     */
    fun assertMessageIsDisplayed() {
        val contentDescription = "Message item"

        composeTestRule.waitUntil(DEFAULT_WAIT_TIMEOUT) {
            composeTestRule
                .onAllNodesWithContentDescription(contentDescription)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onAllNodesWithContentDescription(contentDescription)[0]
            .assertIsDisplayed()
    }

    companion object {
        /**
         * The default time to wait for the component to appear.
         */
        private const val DEFAULT_WAIT_TIMEOUT = 5000L
    }
}
