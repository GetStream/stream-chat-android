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

package io.getstream.chat.android.uitests.snapshot.uicomponents.channels

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.karumi.shot.ScreenshotTest
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.images.internal.StreamCoil
import io.getstream.chat.android.ui.feature.channels.header.ChannelListHeaderView
import io.getstream.chat.android.uitests.util.FakeImageLoader
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Before
import org.junit.Test

class ChannelListHeaderViewTest : ScreenshotTest {

    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @OptIn(InternalStreamChatApi::class)
    @Before
    fun setup() {
        StreamCoil.setImageLoader { FakeImageLoader(context) }
    }

    @Test
    fun channelListHeaderForConnectedState() {
        renderChannelListHeader {
            setUser(TestData.user1())
            showOnlineTitle()
        }
    }

    @Test
    fun channelListHeaderForConnectingState() {
        renderChannelListHeader {
            setUser(TestData.user1())
            showConnectingTitle()
        }
    }

    @Test
    fun channelListHeaderForOfflineState() {
        renderChannelListHeader {
            setUser(TestData.user1())
            showOfflineTitle()
        }
    }

    private fun renderChannelListHeader(block: ChannelListHeaderView.() -> Unit) {
        val channelListHeaderView = ChannelListHeaderView(context)

        channelListHeaderView.block()

        compareScreenshot(channelListHeaderView)
    }
}
