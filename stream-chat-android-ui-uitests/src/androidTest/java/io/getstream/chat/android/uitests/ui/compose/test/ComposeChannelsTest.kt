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

package io.getstream.chat.android.uitests.ui.compose.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import io.getstream.chat.android.uitests.ui.MockWebServerTest
import io.getstream.chat.android.uitests.ui.compose.robot.composeChannelsRobot
import io.getstream.chat.android.uitests.ui.compose.robot.composeLoginRobot
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
internal class ComposeChannelsTest : MockWebServerTest() {

    @Test
    fun testChannelList() {
        with(composeTestRule) {
            composeLoginRobot {
                selectComposeSdk()
                loginWithUser("Jc Miñarro")
            }

            composeChannelsRobot {
                assertChannelIsDisplayed()
            }
        }
    }
}
