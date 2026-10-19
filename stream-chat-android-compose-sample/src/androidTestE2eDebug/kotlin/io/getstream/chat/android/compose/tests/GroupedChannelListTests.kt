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

import io.getstream.chat.android.compose.robots.assertChannelWithName
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.getstream.chat.android.compose.uiautomator.device
import io.getstream.chat.android.compose.uiautomator.disableInternetConnection
import io.getstream.chat.android.compose.uiautomator.enableInternetConnection
import io.getstream.chat.android.compose.uiautomator.seconds
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

class GroupedChannelListTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.GroupedChannels

    @AllureId("12281")
    @Test
    fun test_tabLoadsMoreChannels_whenUserScrollsToTheEnd() {
        step("GIVEN backend has more new channels than one page") {
            seedNewChannels()
        }
        step("AND user opens the new tab") {
            userRobot.login().waitForChannelListToLoad().openGroupTab("new")
        }
        step("WHEN user scrolls to the end of the list") {
            userRobot.scrollChannelListDown()
        }
        step("THEN the first channel of the next page is shown") {
            // Channels are named by position, newest first, and GroupedChannelsActivity asks for pages of 10.
            userRobot.assertChannelWithName("11")
        }
    }

    @Test
    fun test_tabKeepsLoadedPagesAndScrollPosition_afterReconnection() {
        val delay = 3
        step("GIVEN backend has two pages of new channels") {
            seedNewChannels(channelsCount = 20)
        }
        step("AND user scrolls to the end of the second page of the new tab") {
            userRobot
                .login()
                .waitForChannelListToLoad()
                .openGroupTab("new")
                // The first scroll loads the second page, the second one reaches its end.
                .scrollChannelListDown()
                .scrollChannelListDown()
                .assertChannelWithName("20")
        }
        step("AND user goes offline") {
            participantRobot.moveChannelToGroup(channelName = "20", group = "old", delay = delay)
            device.disableInternetConnection()
            userRobot.sleep((delay + 1).seconds)
        }
        step("WHEN participant moves a channel of the second page to the old group") {
            // This action has been completed above with a delay,
            // because the mock server can't be reached while offline.
        }
        step("AND user goes back online") {
            device.enableInternetConnection()
        }
        step("THEN the moved channel, which was on screen, is no longer in the new tab") {
            userRobot.assertChannelWithName("20", isDisplayed = false)
        }
        step("AND the new tab still shows the end of the second page after the first page is refreshed") {
            // The first page is refreshed after the sync that moved the channel, without anything to wait for.
            // A list reset to the first page would scroll back up, and loading the second page again would not
            // bring the end of the list back on screen.
            userRobot
                .sleep(3.seconds)
                .assertChannelWithName("19")
        }
    }

    private fun seedNewChannels(channelsCount: Int = 15) {
        backendRobot.generateChannels(
            channelsCount = channelsCount,
            channelGroups = List(channelsCount) { "new" },
        )
    }
}
