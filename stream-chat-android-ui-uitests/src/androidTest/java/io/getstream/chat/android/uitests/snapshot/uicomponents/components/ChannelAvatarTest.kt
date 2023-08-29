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

package io.getstream.chat.android.uitests.snapshot.uicomponents.components

import android.view.LayoutInflater
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.widgets.avatar.ChannelAvatarView
import io.getstream.chat.android.uitests.R
import io.getstream.chat.android.uitests.snapshot.uicomponents.UiComponentsScreenshotTest
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Test

class ChannelAvatarTest : UiComponentsScreenshotTest() {

    @Test
    fun channelAvatarForChannelWithFiveMembers() {
        renderChannelAvatarView(
            channel = TestData.channel1().copy(
                members = listOf(
                    TestData.member1(),
                    TestData.member2(),
                    TestData.member3(),
                    TestData.member4(),
                    TestData.member5(),
                ),
            ),
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithFourMembers() {
        renderChannelAvatarView(
            channel = TestData.channel1().copy(
                members = listOf(
                    TestData.member1(),
                    TestData.member2(),
                    TestData.member3(),
                    TestData.member4(),
                ),
            ),
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithThreeMembers() {
        renderChannelAvatarView(
            channel = TestData.channel1().copy(
                members = listOf(
                    TestData.member1(),
                    TestData.member2(),
                    TestData.member3(),
                ),
            ),
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithTwoMembers() {
        renderChannelAvatarView(
            channel = TestData.channel1().copy(
                members = listOf(
                    TestData.member1(),
                    TestData.member2(),
                ),
            ),
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithOneMember() {
        renderChannelAvatarView(
            channel = TestData.channel1().copy(
                members = listOf(
                    TestData.member1(),
                ),
            ),
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithFiveMembersWithoutImages() {
        renderChannelAvatarView(
            channel = TestData.channel1().copy(
                members = listOf(
                    TestData.member1().let { it.copy(user = it.user.copy(image = "")) },
                    TestData.member2().let { it.copy(user = it.user.copy(image = "")) },
                    TestData.member3().let { it.copy(user = it.user.copy(image = "")) },
                    TestData.member4().let { it.copy(user = it.user.copy(image = "")) },
                    TestData.member5().let { it.copy(user = it.user.copy(image = "")) },
                ),
            ),
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithFourMembersWithoutImages() {
        renderChannelAvatarView(
            channel = TestData.channel1().copy(
                members = listOf(
                    TestData.member1().let { it.copy(user = it.user.copy(image = "")) },
                    TestData.member2().let { it.copy(user = it.user.copy(image = "")) },
                    TestData.member3().let { it.copy(user = it.user.copy(image = "")) },
                    TestData.member4().let { it.copy(user = it.user.copy(image = "")) },
                ),
            ),
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithThreeMembersWithoutImages() {
        renderChannelAvatarView(
            channel = TestData.channel1().copy(
                members = listOf(
                    TestData.member1().let { it.copy(user = it.user.copy(image = "")) },
                    TestData.member2().let { it.copy(user = it.user.copy(image = "")) },
                    TestData.member3().let { it.copy(user = it.user.copy(image = "")) },
                ),
            ),
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithTwoMembersWithoutImages() {
        renderChannelAvatarView(
            channel = TestData.channel1().copy(
                members = listOf(
                    TestData.member1().let { it.copy(user = it.user.copy(image = "")) },
                    TestData.member2().let { it.copy(user = it.user.copy(image = "")) },
                ),
            ),
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithOneMemberWithoutImage() {
        renderChannelAvatarView(
            channel = TestData.channel1().copy(
                members = listOf(
                    TestData.member1().let { it.copy(user = it.user.copy(image = "")) },
                ),
            ),
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithoutCurrentUser() {
        renderChannelAvatarView(
            channel = TestData.channel1().copy(
                members = listOf(
                    TestData.member1(),
                    TestData.member2(),
                ),
            ),
            currentUser = null,
        )
    }

    private fun renderChannelAvatarView(channel: Channel, currentUser: User?) {
        val channelAvatarView = LayoutInflater
            .from(context)
            .inflate(R.layout.view_channel_avatar, null, false) as ChannelAvatarView

        channelAvatarView.setChannel(channel, currentUser)

        compareScreenshot(view = channelAvatarView, widthInPx = SCREENSHOT_SIZE, heightInPx = SCREENSHOT_SIZE)
    }

    companion object {
        private const val SCREENSHOT_SIZE = 128
    }
}
