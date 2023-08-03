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
import io.getstream.chat.android.compose.state.OnlineIndicatorAlignment
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.uitests.snapshot.compose.ComposeScreenshotTest
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Test

class UserAvatarTest : ComposeScreenshotTest() {

    @Test
    fun userAvatarForUserWithoutImage() = runScreenshotTest {
        UserAvatar(
            modifier = Modifier.size(40.dp),
            user = TestData.user1().copy(image = ""),
            showOnlineIndicator = false,
            onlineIndicatorAlignment = OnlineIndicatorAlignment.TopEnd,
        )
    }

    @Test
    fun userAvatarForUserWithImage() = runScreenshotTest {
        UserAvatar(
            modifier = Modifier.size(40.dp),
            user = TestData.user1(),
            showOnlineIndicator = false,
            onlineIndicatorAlignment = OnlineIndicatorAlignment.TopEnd,
        )
    }

    @Test
    fun userAvatarForOnlineUserWithTopEndIndicator() = runScreenshotTest {
        UserAvatar(
            modifier = Modifier.size(40.dp),
            user = TestData.user1(),
            showOnlineIndicator = true,
            onlineIndicatorAlignment = OnlineIndicatorAlignment.TopEnd,
        )
    }

    @Test
    fun userAvatarForOnlineUserWithBottomEndIndicator() = runScreenshotTest {
        UserAvatar(
            modifier = Modifier.size(40.dp),
            user = TestData.user1(),
            showOnlineIndicator = true,
            onlineIndicatorAlignment = OnlineIndicatorAlignment.BottomEnd,
        )
    }

    @Test
    fun userAvatarForOnlineUserWithTopStartIndicator() = runScreenshotTest {
        UserAvatar(
            modifier = Modifier.size(40.dp),
            user = TestData.user1(),
            showOnlineIndicator = true,
            onlineIndicatorAlignment = OnlineIndicatorAlignment.TopStart,
        )
    }

    @Test
    fun userAvatarForOnlineUserWithBottomStartIndicator() = runScreenshotTest {
        UserAvatar(
            modifier = Modifier.size(40.dp),
            user = TestData.user1(),
            showOnlineIndicator = true,
            onlineIndicatorAlignment = OnlineIndicatorAlignment.BottomStart,
        )
    }
}
