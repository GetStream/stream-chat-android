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
import io.getstream.chat.android.compose.robots.assertGroupUnreadBadge
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.getstream.chat.android.e2e.test.uiautomator.device
import io.getstream.chat.android.e2e.test.uiautomator.disableInternetConnection
import io.getstream.chat.android.e2e.test.uiautomator.enableInternetConnection
import io.getstream.chat.android.e2e.test.uiautomator.seconds
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

class GroupedChannelListTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.GroupedChannels

    private val sampleText = "Test"

    @AllureId("12276")
    @Test
    fun test_eachTabShowsOnlyItsGroupChannels() {
        step("GIVEN backend has channels in the new, current and old groups") {
            seedGroupedChannels()
        }
        step("WHEN user opens the grouped channel list") {
            userRobot.login().waitForChannelListToLoad()
        }
        step("THEN the all tab shows every channel") {
            userRobot.assertChannelCount(4)
        }
        step("WHEN user opens the new tab") {
            userRobot.openGroupTab("new")
        }
        step("THEN only the new channels are shown") {
            userRobot
                .assertChannelCount(2)
                .assertChannelWithName("new-1")
                .assertChannelWithName("new-2")
        }
        step("WHEN user opens the old tab") {
            userRobot.openGroupTab("old")
        }
        step("THEN only the old channel is shown") {
            userRobot
                .assertChannelCount(1)
                .assertChannelWithName("old-1")
        }
    }

    @AllureId("12277")
    @Test
    fun test_channelMovesToAnotherTab_whenItsGroupChanges() {
        step("GIVEN backend has channels in the new, current and old groups") {
            seedGroupedChannels()
        }
        step("AND user opens the old tab") {
            userRobot.login().waitForChannelListToLoad().openGroupTab("old")
            userRobot.assertChannelWithName("old-1")
        }
        step("WHEN participant moves the old channel to the new group") {
            participantRobot.moveChannelToGroup(channelName = "old-1", group = "new")
        }
        step("THEN the channel leaves the old tab") {
            userRobot.assertChannelWithName("old-1", isDisplayed = false)
        }
        step("AND the channel is shown in the new tab") {
            userRobot
                .openGroupTab("new")
                .assertChannelCount(3)
                .assertChannelWithName("old-1")
        }
    }

    @AllureId("12278")
    @Test
    fun test_groupBadgeIncreases_whenParticipantSendsMessage() {
        step("GIVEN backend has channels in the new, current and old groups") {
            seedGroupedChannels()
        }
        step("AND user opens the grouped channel list") {
            userRobot.login().waitForChannelListToLoad()
        }
        step("WHEN participant sends a message to a new channel") {
            participantRobot.sendMessage(sampleText, channelName = "new-1")
        }
        step("THEN the new and all tabs show one unread channel") {
            userRobot
                .assertGroupUnreadBadge("new", 1)
                .assertGroupUnreadBadge("all", 1)
        }
        step("AND the other tabs show no badge") {
            userRobot
                .assertGroupUnreadBadge("current", 0)
                .assertGroupUnreadBadge("old", 0)
        }
    }

    @AllureId("12279")
    @Test
    fun test_groupBadgeDecreases_whenUserReadsChannel() {
        step("GIVEN backend has channels in the new, current and old groups") {
            seedGroupedChannels()
        }
        step("AND user opens the grouped channel list") {
            userRobot.login().waitForChannelListToLoad()
        }
        step("AND participant sends a message to both new channels") {
            participantRobot
                .sendMessage(sampleText, channelName = "new-1")
                .sendMessage(sampleText, channelName = "new-2")
            userRobot.assertGroupUnreadBadge("new", 2)
        }
        step("WHEN user reads one of the channels") {
            userRobot
                .openChannel("new-1")
                .waitForMessageListToLoad()
                .tapOnBackButton()
        }
        step("THEN the new and all tabs show one unread channel") {
            userRobot
                .assertGroupUnreadBadge("new", 1)
                .assertGroupUnreadBadge("all", 1)
        }
    }

    @AllureId("12280")
    @Test
    fun test_allGroupBadgesClear_whenUserMarksAllRead() {
        step("GIVEN backend has channels in the new, current and old groups") {
            seedGroupedChannels()
        }
        step("AND user opens the grouped channel list") {
            userRobot.login().waitForChannelListToLoad()
        }
        step("AND participant sends a message to a new and an old channel") {
            participantRobot
                .sendMessage(sampleText, channelName = "new-1")
                .sendMessage(sampleText, channelName = "old-1")
            userRobot
                .assertGroupUnreadBadge("new", 1)
                .assertGroupUnreadBadge("old", 1)
                .assertGroupUnreadBadge("all", 2)
        }
        step("WHEN user marks all channels read") {
            userRobot.tapOnMarkAllRead()
        }
        step("THEN no tab shows a badge") {
            userRobot
                .assertGroupUnreadBadge("all", 0)
                .assertGroupUnreadBadge("new", 0)
                .assertGroupUnreadBadge("old", 0)
        }
    }

    @AllureId("12281")
    @Test
    fun test_tabLoadsMoreChannels_whenUserScrollsToTheEnd() {
        val channelsCount = 15
        step("GIVEN backend has more new channels than one page") {
            backendRobot.generateChannels(
                channelsCount = channelsCount,
                channelGroups = List(channelsCount) { "new" },
            )
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
        val channelsCount = 20
        step("GIVEN backend has two pages of new channels") {
            backendRobot.generateChannels(
                channelsCount = channelsCount,
                channelGroups = List(channelsCount) { "new" },
            )
        }
        step("AND user logs in and reconnects once, so the app has a last sync date") {
            // After a fresh login, the first sync date is stored by the first reconnection. Pages are kept only after
            // a sync that starts from a known date.
            userRobot.login().waitForChannelListToLoad()
            device.disableInternetConnection()
            userRobot.sleep(2.seconds)
            device.enableInternetConnection()
            userRobot.sleep(3.seconds)
        }
        step("AND user scrolls to the end of the second page of the new tab") {
            userRobot
                // Opened online: a tab asks for its next page when it is shown, and a request rejected while offline
                // is not sent again.
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

    @AllureId("12282")
    @Test
    fun test_tabsAndBadgesAreCorrect_afterReconnection() {
        val delay = 3
        step("GIVEN backend has channels in the new, current and old groups") {
            seedGroupedChannels()
        }
        step("AND user opens the new tab") {
            userRobot.login().waitForChannelListToLoad().openGroupTab("new")
        }
        step("AND user goes offline") {
            participantRobot
                .sendMessage(sampleText, delay = delay, channelName = "new-1")
                .moveChannelToGroup(channelName = "current-1", group = "new", delay = delay)
            device.disableInternetConnection()
            userRobot.sleep((delay + 1).seconds)
        }
        step("WHEN participant sends a message to a new channel and moves the current channel to the new group") {
            // These actions have been completed above with a delay,
            // because the mock server can't be reached while offline.
        }
        step("AND user goes back online") {
            device.enableInternetConnection()
        }
        step("THEN the new and all tabs show one unread channel") {
            userRobot
                .assertGroupUnreadBadge("new", 1)
                .assertGroupUnreadBadge("all", 1)
        }
        step("AND the new tab shows the moved channel") {
            userRobot
                .assertChannelCount(3)
                .assertChannelWithName("current-1")
        }
    }

    private fun seedGroupedChannels() {
        backendRobot.generateChannels(
            channelsCount = 4,
            channelNames = listOf("new-1", "new-2", "current-1", "old-1"),
            channelGroups = listOf("new", "new", "current", "old"),
        )
    }
}
