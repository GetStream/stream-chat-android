/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.channel.info

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import io.getstream.chat.android.compose.ui.SnapshotTest
import org.junit.Rule
import org.junit.Test

internal class ChannelInfoMemberInfoConfirmationModalTest : SnapshotTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_2,
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun `ban confirmation`() {
        snapshot {
            ChannelInfoMemberInfoConfirmationModalBan()
        }
    }

    @Test
    fun `ban confirmation in dark mode`() {
        snapshot(isInDarkMode = true) {
            ChannelInfoMemberInfoConfirmationModalBan()
        }
    }

    @Test
    fun `remove confirmation`() {
        snapshot {
            ChannelInfoMemberInfoConfirmationModalRemove()
        }
    }

    @Test
    fun `remove confirmation in dark mode`() {
        snapshot(isInDarkMode = true) {
            ChannelInfoMemberInfoConfirmationModalRemove()
        }
    }
}
