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
import io.getstream.chat.android.e2e.test.robots.ParticipantRobot
import io.qameta.allure.kotlin.Allure.step
import io.qameta.allure.kotlin.AllureId
import org.junit.Test

/**
 * Covers a channel search customized through `ChannelListSearchOptions`. It needs the app started with the channel
 * search and member name search flags, which make the sample also match channels by the name of a member. The
 * default channel search is covered by [ChannelSearchTests].
 */
class MemberNameSearchTests : StreamTestCase() {

    override fun initTestActivity() = InitTestActivity.UserLogin
    override var useChannelSearch = true
    override var useMemberNameSearch = true

    @AllureId("12485")
    @Test
    fun test_userSearchesForChannelsByMemberName() {
        step("GIVEN group channels with a third member and a direct message channel without them exist") {
            backendRobot.generateChannels(channelsCount = 3, withDirectMessageChannel = true)
        }
        step("AND user logs in") {
            userRobot
                .login()
                .waitForChannelListToLoad()
                .assertChannelCount(4)
        }
        step("WHEN user searches for the third member's name") {
            userRobot.search(MEMBER_NAME_QUERY)
        }
        step("THEN only the channels with that member are shown") {
            userRobot
                .assertChannelCount(3)
                .assertChannelWithName(ParticipantRobot.name, isDisplayed = false)
        }
    }

    @AllureId("12486")
    @Test
    fun test_userSearchesForChannelByNameWithMemberNameSearch() {
        step("GIVEN channels exist, one with a searchable name") {
            backendRobot.generateChannels(
                channelsCount = 3,
                channelNames = listOf(CHANNEL_NAME),
                withDirectMessageChannel = true,
            )
        }
        step("AND user logs in") {
            userRobot
                .login()
                .waitForChannelListToLoad()
                .assertChannelCount(4)
        }
        step("WHEN user searches for the channel by its name") {
            userRobot.search(CHANNEL_NAME_QUERY)
        }
        step("THEN only the matching channel is shown") {
            userRobot
                .assertChannelCount(1)
                .assertChannelWithName(CHANNEL_NAME)
        }
    }

    private companion object {
        /** A member of the seeded group channels only: not the app user, and not part of the direct message channel. */
        const val MEMBER_NAME_QUERY = "Lando"
        const val CHANNEL_NAME = "Blue Team"
        const val CHANNEL_NAME_QUERY = "Blue"
    }
}
