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

package io.getstream.chat.android.uitests.snapshot.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.components.avatar.ChannelAvatar
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.uitests.snapshot.compose.ComposeScreenshotTest
import io.getstream.chat.android.uitests.util.FakeImageLoader
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Test

class DocsAvatarTest : ComposeScreenshotTest() {

    @Test
    fun userAvatarShowcase() = runScreenshotTest {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // With image, no indicator
            UserAvatar(
                modifier = Modifier.size(56.dp),
                user = TestData.elena(),
                showIndicator = false,
            )
            // Initials only (no image)
            UserAvatar(
                modifier = Modifier.size(56.dp),
                user = TestData.elena().copy(image = ""),
                showIndicator = false,
            )
            // With image + online indicator
            UserAvatar(
                modifier = Modifier.size(56.dp),
                user = TestData.elena().copy(online = true),
                showIndicator = true,
            )
        }
    }

    @Test
    fun channelAvatarShowcase() = runScreenshotTest {
        val currentUser = TestData.alex()
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Channel with custom image
            ChannelAvatar(
                modifier = Modifier.size(56.dp),
                channel = Channel().copy(
                    type = "messaging",
                    id = "ch_image",
                    image = FakeImageLoader.AVATAR_MARCO,
                    members = listOf(TestData.memberAlex()),
                ),
                currentUser = currentUser,
                showIndicator = false,
            )
            // DM channel with online indicator
            ChannelAvatar(
                modifier = Modifier.size(56.dp),
                channel = Channel().copy(
                    type = "messaging",
                    id = "dm1",
                    members = listOf(
                        TestData.memberAlex(),
                        Member(user = TestData.elena().copy(online = true)),
                    ),
                ),
                currentUser = currentUser,
                showIndicator = true,
            )
            // Group channel — 2 other members
            ChannelAvatar(
                modifier = Modifier.size(56.dp),
                channel = Channel().copy(
                    type = "messaging",
                    id = "group2",
                    members = listOf(
                        TestData.memberAlex(),
                        TestData.memberElena(),
                        TestData.memberSarah(),
                    ),
                ),
                currentUser = currentUser,
                showIndicator = false,
            )
            // Group channel — 4 other members
            ChannelAvatar(
                modifier = Modifier.size(56.dp),
                channel = Channel().copy(
                    type = "messaging",
                    id = "group4",
                    members = listOf(
                        TestData.memberAlex(),
                        TestData.memberElena(),
                        TestData.memberSarah(),
                        TestData.memberMarco(),
                        TestData.memberPriya(),
                    ),
                ),
                currentUser = currentUser,
                showIndicator = false,
            )
        }
    }
}
