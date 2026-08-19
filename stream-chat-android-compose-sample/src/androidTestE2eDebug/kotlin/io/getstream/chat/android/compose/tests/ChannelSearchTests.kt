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

import io.getstream.chat.android.compose.robots.assertChannelCount
import io.getstream.chat.android.compose.robots.assertChannelWithName
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

/**
 * Covers the channel search wired into the channel list header. It needs the app started with the
 * channel search flag, which configures the channel list with `SearchMode.Channels`. Message
 * search, the default mode, is covered by [SearchTests].
 */
class ChannelSearchTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin
    override var useChannelSearch = true

    private val channelName = "Blue Team"
    private val searchQuery = "Blue"

    @AllureId("5934")
    @Test
    fun test_userSearchesForChannel() {
        step("GIVEN channels exist, one with a searchable name") {
            backendRobot.generateChannels(channelsCount = 3, channelNames = listOf(channelName))
        }
        step("AND user logs in") {
            userRobot
                .login()
                .waitForChannelListToLoad()
        }
        step("WHEN user searches for the channel by its name") {
            userRobot.searchForChannel(searchQuery)
        }
        step("THEN only the matching channel is shown") {
            userRobot
                .assertChannelCount(1)
                .assertChannelWithName(channelName)
        }
    }
}
