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

import io.getstream.chat.android.compose.robots.assertComposerText
import io.getstream.chat.android.compose.robots.assertMessageInChannelPreview
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.getstream.chat.android.compose.uiautomator.device
import io.getstream.chat.android.compose.uiautomator.disableInternetConnection
import io.getstream.chat.android.compose.uiautomator.enableInternetConnection
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

class DraftMessagesTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin

    private val firstMessage = "message"
    private val draftMessage = "alright"

    @AllureId("10106")
    @Test
    fun test_updateChannelDraftMessage() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user sends a message") {
            userRobot.sendMessage(firstMessage)
        }
        step("WHEN user inputs some text in the composer") {
            userRobot.typeText(draftMessage)
        }
        step("AND user leaves the Channel") {
            userRobot.tapOnBackButton()
        }
        step("THEN the draft message is in preview") {
            userRobot.assertMessageInChannelPreview("Draft: $draftMessage")
        }
        step("WHEN user comes back to the channel") {
            userRobot.openChannel()
        }
        step("THEN the draft message is in the composer") {
            userRobot.assertComposerText(draftMessage)
        }
        step("WHEN user deletes the draft message") {
            userRobot.clearComposer()
        }
        step("AND user leaves the Channel") {
            userRobot.tapOnBackButton()
        }
        step("THEN there is no draft message in preview") {
            userRobot.assertMessageInChannelPreview(firstMessage, fromCurrentUser = true)
        }
        step("WHEN user comes back to the channel") {
            userRobot.openChannel()
        }
        step("THEN there is no draft message in the composer") {
            userRobot.assertComposerText("")
        }
    }

    @AllureId("10107")
    @Test
    fun test_updateThreadDraftMessage() {
        step("GIVEN user opens a thread") {
            backendRobot.generateChannels(
                channelsCount = 1,
                messagesCount = 1,
                repliesCount = 1,
                repliesText = firstMessage,
            )
            userRobot
                .login()
                .openChannel()
                .openThread()
        }
        step("WHEN user inputs some text in the composer") {
            userRobot.typeText(draftMessage)
        }
        step("AND user leaves the Thread and comes back to the Thread") {
            userRobot.tapOnBackButton().openThread()
        }
        step("THEN the draft message is in the composer") {
            userRobot.assertComposerText(draftMessage)
        }
        step("WHEN user deletes the draft message") {
            userRobot.clearComposer()
        }
        step("AND user leaves the Thread and comes back to the Thread") {
            userRobot.tapOnBackButton().openThread()
        }
        step("THEN there is no draft message in the composer") {
            userRobot.assertComposerText("")
        }
    }

    @AllureId("10108")
    @Test
    fun test_updateDraftMessageBeingOffline() {
        step("GIVEN user opens the channel") {
            userRobot.login().openChannel()
        }
        step("AND user becomes offline") {
            device.disableInternetConnection()
        }
        step("WHEN user inputs some text in the composer") {
            userRobot.typeText(draftMessage)
        }
        step("AND user leaves the Channel") {
            userRobot.tapOnBackButton()
        }
        step("THEN the draft message is in preview") {
            userRobot.assertMessageInChannelPreview("Draft: $draftMessage")
        }
        step("WHEN user comes back to the channel") {
            userRobot.openChannel()
        }
        step("THEN the draft message is in the composer") {
            userRobot.assertComposerText(draftMessage)
        }

        // To be able to close the mock server
        device.enableInternetConnection()
    }
}
