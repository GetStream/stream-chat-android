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

package io.getstream.chat.android.uitests.ui.test

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.uitests.app.login.LoginActivity
import io.getstream.chat.android.uitests.ui.robot.compose.composeChannelsRobot
import io.getstream.chat.android.uitests.ui.robot.compose.composeLoginRobot
import io.getstream.chat.android.uitests.ui.robot.compose.composeMessagesRobot
import io.getstream.chat.android.uitests.ui.robot.uicomponents.channelsRobot
import io.getstream.chat.android.uitests.ui.robot.uicomponents.messagesRobot
import io.getstream.chat.android.uitests.ui.util.CoroutineTaskExecutorRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
internal class MessagesTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<LoginActivity>()

    @get:Rule
    @InternalStreamChatApi
    val coroutineTaskExecutorRule = CoroutineTaskExecutorRule()

    @Test
    fun testSendMessageUiComponents() {
        composeLoginRobot(composeTestRule) {
            selectedUiComponentsSdk()
            loginWithUser("Jc Miñarro")
        }

        channelsRobot {
            clickAnyChannel()
        }

        messagesRobot {
            typeMessageText("Test message")
            clickSendButton()
        }
    }

    @Test
    fun testSendMessageCompose() {
        composeLoginRobot(composeTestRule) {
            selectComposeSdk()
            loginWithUser("Jc Miñarro")
        }

        composeChannelsRobot(composeTestRule) {
            clickAnyChannel()
        }

        composeMessagesRobot(composeTestRule) {
            typeMessageText("Test message")
            clickSendButton()
        }
    }
}
