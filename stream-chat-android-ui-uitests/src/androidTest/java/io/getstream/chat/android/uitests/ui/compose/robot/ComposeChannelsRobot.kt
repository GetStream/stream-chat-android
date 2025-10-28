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

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.rules.TestRule

/**
 * A factory function for [ComposeChannelsRobot].
 */
internal fun ComposeTestRule.composeChannelsRobot(
    block: ComposeChannelsRobot.() -> Unit,
): ComposeChannelsRobot = ComposeChannelsRobot(this).apply { block() }

/**
 * A robot that simulates user behavior on the channels screen.
 *
 * @param composeTestRule A [TestRule] that provides the main entry point into testing.
 */
internal class ComposeChannelsRobot(
    composeTestRule: ComposeTestRule,
) : BaseComposeTestRobot(composeTestRule) {

    /**
     * Clicks the first channel item in the list.
     */
    fun clickChannelItem() {
        waitForChannelItem().performClick()
    }

    /**
     * Assert that any channel is displayed on the screen.
     */
    fun assertChannelIsDisplayed() {
        waitForChannelItem().assertIsDisplayed()
    }

    private fun waitForChannelItem(): SemanticsNodeInteraction {
        val contentDescription = "Channel item"
        composeTestRule.waitUntil(DEFAULT_WAIT_TIMEOUT) {
            composeTestRule
                .onAllNodesWithContentDescription(contentDescription)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        return composeTestRule.onAllNodesWithContentDescription(contentDescription)[0]
    }

    companion object {
        /**
         * The default time to wait for the component to appear.
         */
        private const val DEFAULT_WAIT_TIMEOUT = 5000L
    }
}
