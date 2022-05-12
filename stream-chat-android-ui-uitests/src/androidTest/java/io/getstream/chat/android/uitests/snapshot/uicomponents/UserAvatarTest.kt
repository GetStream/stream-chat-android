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

package io.getstream.chat.android.uitests.snapshot.uicomponents

import android.content.Context
import android.view.LayoutInflater
import androidx.test.platform.app.InstrumentationRegistry
import com.getstream.sdk.chat.coil.StreamCoil
import com.karumi.shot.ScreenshotTest
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.avatar.AvatarView
import io.getstream.chat.android.uitests.R
import io.getstream.chat.android.uitests.util.FakeImageLoader
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Before
import org.junit.Test

class UserAvatarTest : ScreenshotTest {

    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @OptIn(InternalStreamChatApi::class)
    @Before
    fun setup() {
        StreamCoil.setImageLoader { FakeImageLoader(context) }
    }

    @Test
    fun testUserAvatar() {
        val avatarView = LayoutInflater
            .from(context)
            .inflate(R.layout.view_avatar, null, false) as AvatarView

        avatarView.setUserData(TestData.user1())

        compareScreenshot(view = avatarView, widthInPx = SCREENSHOT_SIZE, heightInPx = SCREENSHOT_SIZE)
    }

    @Test
    fun testChannelAvatar() {
        val avatarView = LayoutInflater
            .from(context)
            .inflate(R.layout.view_avatar, null, false) as AvatarView
        val channel = Channel(
            cid = "messaging:123",
            members = listOf(
                Member(user = TestData.user1()),
                Member(user = TestData.user2()),
                Member(user = TestData.user3()),
            )
        )

        avatarView.setChannelData(channel)

        compareScreenshot(view = avatarView, widthInPx = SCREENSHOT_SIZE, heightInPx = SCREENSHOT_SIZE)
    }

    companion object {
        private const val SCREENSHOT_SIZE = 128
    }
}
