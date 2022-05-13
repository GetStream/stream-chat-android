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

package io.getstream.chat.android.uitests.snapshot.compose.components

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.karumi.shot.ScreenshotTest
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.ui.components.avatar.ChannelAvatar
import io.getstream.chat.android.uitests.snapshot.compose.TestChatTheme
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Rule
import org.junit.Test

class ChannelAvatarTest : ScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun channelAvatarForChannelWithFiveMembers() {
        renderChannelAvatar(
            channel = TestData.channel1().apply {
                members = listOf(
                    TestData.member1(),
                    TestData.member2(),
                    TestData.member3(),
                    TestData.member4(),
                    TestData.member5()
                )
            },
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithFourMembers() {
        renderChannelAvatar(
            channel = TestData.channel1().apply {
                members = listOf(
                    TestData.member1(),
                    TestData.member2(),
                    TestData.member3(),
                    TestData.member4(),
                )
            },
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithThreeMembers() {
        renderChannelAvatar(
            channel = TestData.channel1().apply {
                members = listOf(
                    TestData.member1(),
                    TestData.member2(),
                    TestData.member3(),
                )
            },
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithTwoMembers() {
        renderChannelAvatar(
            channel = TestData.channel1().apply {
                members = listOf(
                    TestData.member1(),
                    TestData.member2(),
                )
            },
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithOneMember() {
        renderChannelAvatar(
            channel = TestData.channel1().apply {
                members = listOf(
                    TestData.member1()
                )
            },
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithFiveMembersWithoutImages() {
        renderChannelAvatar(
            channel = TestData.channel1().apply {
                members = listOf(
                    TestData.member1().apply { user.image = "" },
                    TestData.member2().apply { user.image = "" },
                    TestData.member3().apply { user.image = "" },
                    TestData.member4().apply { user.image = "" },
                    TestData.member5().apply { user.image = "" },
                )
            },
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithFourMembersWithoutImages() {
        renderChannelAvatar(
            channel = TestData.channel1().apply {
                members = listOf(
                    TestData.member1().apply { user.image = "" },
                    TestData.member2().apply { user.image = "" },
                    TestData.member3().apply { user.image = "" },
                    TestData.member4().apply { user.image = "" },
                )
            },
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithThreeMembersWithoutImages() {
        renderChannelAvatar(
            channel = TestData.channel1().apply {
                members = listOf(
                    TestData.member1().apply { user.image = "" },
                    TestData.member2().apply { user.image = "" },
                    TestData.member3().apply { user.image = "" },
                )
            },
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithTwoMembersWithoutImages() {
        renderChannelAvatar(
            channel = TestData.channel1().apply {
                members = listOf(
                    TestData.member1().apply { user.image = "" },
                    TestData.member2().apply { user.image = "" },
                )
            },
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithOneMemberWithoutImage() {
        renderChannelAvatar(
            channel = TestData.channel1().apply {
                members = listOf(
                    TestData.member1().apply { user.image = "" },
                )
            },
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithoutCurrentUser() {
        renderChannelAvatar(
            channel = TestData.channel1().apply {
                members = listOf(
                    TestData.member1(),
                    TestData.member2(),
                )
            },
            currentUser = null,
        )
    }

    private fun renderChannelAvatar(
        channel: Channel,
        currentUser: User?,
    ) {
        composeRule.setContent {
            TestChatTheme {
                ChannelAvatar(
                    modifier = Modifier.size(40.dp),
                    channel = channel,
                    currentUser = currentUser,
                )
            }
        }
        compareScreenshot(composeRule)
    }
}
