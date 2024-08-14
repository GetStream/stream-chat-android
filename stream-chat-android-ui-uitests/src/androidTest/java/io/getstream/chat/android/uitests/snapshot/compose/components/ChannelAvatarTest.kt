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
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.components.avatar.ChannelAvatar
import io.getstream.chat.android.uitests.snapshot.compose.ComposeScreenshotTest
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Test

class ChannelAvatarTest : ComposeScreenshotTest() {

    @Test
    fun channelAvatarForChannelWithFiveMembers() = runScreenshotTest {
        ChannelAvatar(
            modifier = Modifier.size(40.dp),
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
    fun channelAvatarForChannelWithFourMembers() = runScreenshotTest {
        ChannelAvatar(
            modifier = Modifier.size(40.dp),
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
    fun channelAvatarForChannelWithThreeMembers() = runScreenshotTest {
        ChannelAvatar(
            modifier = Modifier.size(40.dp),
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
    fun channelAvatarForChannelWithTwoMembers() = runScreenshotTest {
        ChannelAvatar(
            modifier = Modifier.size(40.dp),
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
    fun channelAvatarForChannelWithOneMember() = runScreenshotTest {
        ChannelAvatar(
            modifier = Modifier.size(40.dp),
            channel = TestData.channel1().copy(
                members = listOf(
                    TestData.member1(),
                ),
            ),
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithFiveMembersWithoutImages() = runScreenshotTest {
        ChannelAvatar(
            modifier = Modifier.size(40.dp),
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
    fun channelAvatarForChannelWithFourMembersWithoutImages() = runScreenshotTest {
        ChannelAvatar(
            modifier = Modifier.size(40.dp),
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
    fun channelAvatarForChannelWithThreeMembersWithoutImages() = runScreenshotTest {
        ChannelAvatar(
            modifier = Modifier.size(40.dp),
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
    fun channelAvatarForChannelWithTwoMembersWithoutImages() = runScreenshotTest {
        ChannelAvatar(
            modifier = Modifier.size(40.dp),
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
    fun channelAvatarForChannelWithOneMemberWithoutImage() = runScreenshotTest {
        ChannelAvatar(
            modifier = Modifier.size(40.dp),
            channel = TestData.channel1().copy(
                members = listOf(
                    TestData.member1().let { it.copy(user = it.user.copy(image = "")) },
                ),
            ),
            currentUser = TestData.user1(),
        )
    }

    @Test
    fun channelAvatarForChannelWithoutCurrentUser() = runScreenshotTest {
        ChannelAvatar(
            modifier = Modifier.size(40.dp),
            channel = TestData.channel1().copy(
                members = listOf(
                    TestData.member1(),
                    TestData.member2(),
                ),
            ),
            currentUser = null,
        )
    }
}
