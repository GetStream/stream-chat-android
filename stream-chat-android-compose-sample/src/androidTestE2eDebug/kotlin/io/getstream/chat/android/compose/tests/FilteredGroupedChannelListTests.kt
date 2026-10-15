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
import io.getstream.chat.android.compose.robots.assertFirstChannelWithName
import io.getstream.chat.android.compose.robots.assertGroupUnreadBadge
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

/**
 * The groups of `GroupedChannelsActivity` are queried with per-group filters and a recent cutoff of 7 days:
 * the all group shows the new channels and the recent current ones, and the current group only its recent channels.
 * The old group has no filter.
 *
 * A channel event can only add a channel the client already has, so the tests that bring a channel into a filtered
 * tab use one loaded through the old tab.
 */
class FilteredGroupedChannelListTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.FilteredGroupedChannels

    private val sampleText = "Test"

    @AllureId("12315")
    @Test
    fun test_tabsShowOnlyTheChannelsThatMatchTheirFilter() {
        step("GIVEN backend has channels in the new, current and old groups") {
            seedGroupedChannels()
        }
        step("WHEN user opens the grouped channel list") {
            userRobot.login().waitForChannelListToLoad()
        }
        step("THEN the all tab shows the new channel and the recent current channel") {
            userRobot
                .assertChannelCount(2)
                .assertChannelWithName("new-1")
                .assertChannelWithName("current-1")
        }
        step("WHEN user opens the current tab") {
            userRobot.openGroupTab("current")
        }
        step("THEN only the recent current channel is shown") {
            userRobot
                .assertChannelCount(1)
                .assertChannelWithName("current-1")
        }
    }

    @AllureId("12316")
    @Test
    fun test_olderChannelMovedToFilteredGroup_entersTab_whenParticipantSendsMessage() {
        step("GIVEN backend has channels in the new, current and old groups") {
            seedGroupedChannels()
        }
        step("AND user opens the old tab") {
            userRobot.login().waitForChannelListToLoad().openGroupTab("old")
            userRobot.assertChannelWithName("old-1")
        }
        step("AND participant moves the older old channel to the current group") {
            moveOldChannelToCurrentGroup()
        }
        step("WHEN user opens the current tab") {
            userRobot.openGroupTab("current")
        }
        step("THEN the moved channel is not shown, because it is older than the cutoff") {
            userRobot.assertChannelCount(1)
        }
        step("WHEN participant sends a message to the moved channel") {
            participantRobot.sendMessage(sampleText, channelName = "old-1")
        }
        step("THEN the channel is shown in the current tab") {
            userRobot
                .assertChannelWithName("old-1")
                .assertChannelCount(2)
        }
        step("AND the current and all tabs show one unread channel") {
            userRobot
                .assertGroupUnreadBadge("current", 1)
                .assertGroupUnreadBadge("all", 1)
        }
    }

    @AllureId("12317")
    @Test
    fun test_olderChannelMovedToFilteredGroup_entersTabFirst_whenParticipantTouchesIt() {
        step("GIVEN backend has channels in the new, current and old groups") {
            seedGroupedChannels()
        }
        step("AND user opens the old tab") {
            userRobot.login().waitForChannelListToLoad().openGroupTab("old")
            userRobot.assertChannelWithName("old-1")
        }
        step("AND participant moves the older old channel to the current group") {
            moveOldChannelToCurrentGroup()
        }
        step("WHEN user opens the current tab") {
            userRobot.openGroupTab("current")
        }
        step("THEN the moved channel is not shown, because it is older than the cutoff") {
            userRobot.assertChannelCount(1)
        }
        step("WHEN participant sets the touched_at date of the moved channel") {
            participantRobot.touchChannel("old-1")
        }
        step("THEN the channel is shown first in the current tab, ordered by its touched_at date") {
            userRobot
                .assertChannelCount(2)
                .assertFirstChannelWithName("old-1")
        }
    }

    @AllureId("12318")
    @Test
    fun test_filteredBadgeLeavesOutUnreadChannelsItsFilterHides() {
        step("GIVEN backend has channels in the new, current and old groups") {
            seedGroupedChannels()
        }
        step("AND user opens the grouped channel list") {
            userRobot.login().waitForChannelListToLoad()
        }
        step("WHEN participant sends a message to the old channel, which the all tab leaves out") {
            participantRobot.sendMessage(sampleText, channelName = "old-1")
        }
        step("THEN the old tab shows one unread channel") {
            userRobot.assertGroupUnreadBadge("old", 1)
        }
        step("AND the all tab shows no badge") {
            userRobot.assertGroupUnreadBadge("all", 0)
        }
    }

    @AllureId("12319")
    @Test
    fun test_filteredBadgesCountChannelsNotLoadedYet_andClear_whenUserMarksAllRead() {
        val currentChannelsCount = 12
        val currentChannelNames = List(currentChannelsCount) { "current-${it + 1}" }
        step("GIVEN backend has more recent current channels than one page, and an old channel") {
            backendRobot.generateChannels(
                channelsCount = currentChannelsCount + 1,
                channelNames = currentChannelNames + "old-1",
                channelGroups = List(currentChannelsCount) { "current" } + "old",
                channelDaysAgo = List(currentChannelsCount + 1) { 1 },
            )
        }
        step("AND participant sends a message to every channel before user opens the app") {
            (currentChannelNames + "old-1").forEach { name -> participantRobot.sendMessage(sampleText, channelName = name) }
        }
        step("WHEN user opens the grouped channel list") {
            userRobot.login().waitForChannelListToLoad()
        }
        step("THEN the filtered tabs count the unread channels of the next page too") {
            // GroupedChannelsActivity asks for pages of 10, so 2 unread current channels are not loaded yet.
            userRobot
                .assertGroupUnreadBadge("all", currentChannelsCount)
                .assertGroupUnreadBadge("current", currentChannelsCount)
                .assertGroupUnreadBadge("old", 1)
        }
        step("WHEN user reads the newest current channel") {
            userRobot
                .openChannel("current-$currentChannelsCount")
                .waitForMessageListToLoad()
                .tapOnBackButton()
        }
        step("THEN the filtered tabs count one unread channel less, and the all tab still leaves out the old channel") {
            userRobot
                .assertGroupUnreadBadge("all", currentChannelsCount - 1)
                .assertGroupUnreadBadge("current", currentChannelsCount - 1)
        }
        step("WHEN user marks all channels read") {
            userRobot.tapOnMarkAllRead()
        }
        step("THEN no tab shows a badge, including for the channels that are not loaded") {
            userRobot
                .assertGroupUnreadBadge("all", 0)
                .assertGroupUnreadBadge("current", 0)
                .assertGroupUnreadBadge("old", 0)
        }
    }

    /**
     * Seeds a recent channel in each group, plus an older current channel and an older old channel.
     * The old group has no filter, so its older channel is loaded and watched.
     */
    private fun seedGroupedChannels() {
        backendRobot.generateChannels(
            channelsCount = 4,
            channelNames = listOf("new-1", "current-1", "current-2", "old-1"),
            channelGroups = listOf("new", "current", "current", "old"),
            channelDaysAgo = listOf(1, 1, 10, 10),
        )
    }

    /**
     * Moves the old channel to the current group while the old tab is open. Waiting for the channel to leave the old
     * tab makes sure the client has handled the move.
     */
    private fun moveOldChannelToCurrentGroup() {
        participantRobot.moveChannelToGroup(channelName = "old-1", group = "current")
        userRobot.assertChannelWithName("old-1", isDisplayed = false)
    }
}
