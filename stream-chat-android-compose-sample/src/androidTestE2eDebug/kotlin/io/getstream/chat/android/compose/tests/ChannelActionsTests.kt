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

import io.getstream.chat.android.compose.robots.assertChannelActionsSheetForGroupChannel
import io.getstream.chat.android.compose.robots.assertChannelIsMuted
import io.getstream.chat.android.compose.robots.assertChannelListIsEmpty
import io.getstream.chat.android.compose.robots.assertGroupChannelInfoScreen
import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

class ChannelActionsTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin

    @AllureId("11456")
    @Test
    fun test_channelActionsSheetIsShown_whenUserLongPressesTheChannel() {
        step("GIVEN user logs in") {
            userRobot.login().waitForChannelListToLoad()
        }
        step("WHEN user long presses the channel") {
            userRobot.openChannelMenu()
        }
        step("THEN the channel actions sheet is shown") {
            userRobot.assertChannelActionsSheetForGroupChannel()
        }
    }

    @AllureId("11457")
    @Test
    fun test_channelActionsSheetIsShown_whenUserSwipesTheChannel() {
        step("GIVEN user logs in") {
            userRobot.login().waitForChannelListToLoad()
        }
        step("WHEN user swipes the channel and taps on the more action") {
            userRobot.swipeChannel().tapOnMoreSwipeAction()
        }
        step("THEN the channel actions sheet is shown") {
            userRobot.assertChannelActionsSheetForGroupChannel()
        }
    }

    @AllureId("11458")
    @Test
    fun test_userOpensChannelInfoFromTheChannelActionsSheet() {
        step("GIVEN user logs in") {
            userRobot.login().waitForChannelListToLoad()
        }
        step("AND user long presses the channel") {
            userRobot.openChannelMenu()
        }
        step("WHEN user taps on the view info option") {
            userRobot.tapOnViewChannelInfo()
        }
        step("THEN the group channel info screen is shown") {
            userRobot.assertGroupChannelInfoScreen()
        }
    }

    @Test
    fun test_userLeavesGroupChannel() {
        step("GIVEN user logs in") {
            userRobot.login().waitForChannelListToLoad()
        }
        step("AND user long presses the channel") {
            userRobot.openChannelMenu()
        }
        step("WHEN user leaves the group") {
            userRobot
                .tapOnLeaveGroup()
                .confirmChannelAction()
        }
        step("THEN the channel is gone from the channel list") {
            userRobot.assertChannelListIsEmpty()
        }
    }

    @Test
    fun test_userDeletesGroupChannel() {
        step("GIVEN user logs in") {
            userRobot.login().waitForChannelListToLoad()
        }
        step("AND user long presses the channel") {
            userRobot.openChannelMenu()
        }
        step("WHEN user deletes the group") {
            userRobot
                .tapOnDeleteGroup()
                .confirmChannelAction()
        }
        step("THEN the channel is gone from the channel list") {
            userRobot.assertChannelListIsEmpty()
        }
    }

    @Test
    fun test_userMutesChannelFromTheSwipeAction() {
        step("GIVEN user logs in") {
            userRobot.login().waitForChannelListToLoad()
        }
        step("WHEN user swipes the channel and taps on the mute action") {
            userRobot
                .swipeChannel()
                .tapOnMuteSwipeAction()
        }
        step("THEN the channel shows the muted icon") {
            userRobot.assertChannelIsMuted(isMuted = true)
        }
    }

    @Test
    fun test_userUnmutesChannelFromTheSwipeAction() {
        step("GIVEN user logs in") {
            userRobot.login().waitForChannelListToLoad()
        }
        step("AND user mutes the channel from the swipe action") {
            userRobot
                .swipeChannel()
                .tapOnMuteSwipeAction()
                .assertChannelIsMuted(isMuted = true)
        }
        step("WHEN user swipes the channel and taps on the unmute action") {
            userRobot
                .swipeChannel()
                .tapOnUnmuteSwipeAction()
        }
        step("THEN the channel shows no muted icon") {
            userRobot.assertChannelIsMuted(isMuted = false)
        }
    }
}
