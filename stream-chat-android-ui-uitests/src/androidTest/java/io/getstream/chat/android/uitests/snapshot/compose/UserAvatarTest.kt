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

package io.getstream.chat.android.uitests.snapshot.compose

import android.content.Context
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.karumi.shot.ScreenshotTest
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.ui.components.avatar.ChannelAvatar
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.uitests.util.FakeImageLoader
import org.junit.Rule
import org.junit.Test

class UserAvatarTest : ScreenshotTest {

    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun testUserAvatar() {
        composeRule.setContent {
            ChatTheme(imageLoaderFactory = { FakeImageLoader(context) }) {
                UserAvatar(
                    modifier = Modifier.size(40.dp),
                    user = user1,
                    showOnlineIndicator = true,
                )
            }
        }

        compareScreenshot(composeRule.onRoot())
    }

    @Test
    fun testChannelAvatar() {
        composeRule.setContent {
            ChatTheme(imageLoaderFactory = { FakeImageLoader(context) }) {
                ChannelAvatar(
                    modifier = Modifier.size(40.dp),
                    currentUser = user1,
                    channel = channel,
                    showOnlineIndicator = true,
                )
            }
        }

        compareScreenshot(composeRule.onRoot())
    }

    companion object {
        private val user1: User = User().apply {
            id = "jc1"
            name = "Jc Miñarro"
            image = FakeImageLoader.AVATAR_JC
            online = true
        }

        private val user2: User = User().apply {
            id = "amit"
            name = "Amit Kumar"
            image = FakeImageLoader.AVATAR_AMIT
        }

        private val user3: User = User().apply {
            id = "filip"
            name = "Filip Babić"
            image = FakeImageLoader.AVATAR_FILIP
        }

        private val channel = Channel(
            cid = "messaging:123",
            members = listOf(
                Member(user = user1),
                Member(user = user2),
                Member(user = user3),
            )
        )
    }
}
