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

package io.getstream.chat.android.ui.feature.channels.header

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.PaparazziViewTest
import org.junit.Test

internal class ChannelListHeaderViewTest : PaparazziViewTest() {

    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `connected, no user`() {
        channelListHeader {
            showOnlineTitle()
        }
    }

    @Test
    fun `connected, with user`() {
        channelListHeader {
            setUser(PreviewUserData.user1)
            showOnlineTitle()
        }
    }

    @Test
    fun `connecting, no user`() {
        channelListHeader {
            showConnectingTitle()
        }
    }

    @Test
    fun `connecting, with user`() {
        channelListHeader {
            setUser(PreviewUserData.user1)
            showConnectingTitle()
        }
    }

    @Test
    fun `offline, no user`() {
        channelListHeader {
            showOfflineTitle()
        }
    }

    @Test
    fun `offline, with user`() {
        channelListHeader {
            setUser(PreviewUserData.user1)
            showOfflineTitle()
        }
    }

    private fun channelListHeader(block: ChannelListHeaderView.() -> Unit) {
        snapshotColumn { context -> ChannelListHeaderView(context).apply { block() } }
    }
}
