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
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent
import org.junit.Rule
import org.junit.Test

internal class ChannelInfoScreenModelTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_2,
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun `hide direct channel`() {
        snapshot {
            ChannelInfoScreenModal(
                modal = ChannelInfoViewEvent.HideChannelModal,
                isGroupChannel = false,
            )
        }
    }

    @Test
    fun `hide direct channel in dark mode`() {
        snapshot(isInDarkMode = true) {
            ChannelInfoScreenModal(
                modal = ChannelInfoViewEvent.HideChannelModal,
                isGroupChannel = false,
            )
        }
    }

    @Test
    fun `hide group channel`() {
        snapshot {
            ChannelInfoScreenModal(
                modal = ChannelInfoViewEvent.HideChannelModal,
                isGroupChannel = true,
            )
        }
    }

    @Test
    fun `hide group channel in dark mode`() {
        snapshot(isInDarkMode = true) {
            ChannelInfoScreenModal(
                modal = ChannelInfoViewEvent.HideChannelModal,
                isGroupChannel = true,
            )
        }
    }

    @Test
    fun `leave direct channel`() {
        snapshot {
            ChannelInfoScreenModal(
                modal = ChannelInfoViewEvent.LeaveChannelModal,
                isGroupChannel = false,
            )
        }
    }

    @Test
    fun `leave direct channel in dark mode`() {
        snapshot(isInDarkMode = true) {
            ChannelInfoScreenModal(
                modal = ChannelInfoViewEvent.LeaveChannelModal,
                isGroupChannel = false,
            )
        }
    }

    @Test
    fun `leave group channel`() {
        snapshot {
            ChannelInfoScreenModal(
                modal = ChannelInfoViewEvent.LeaveChannelModal,
                isGroupChannel = true,
            )
        }
    }

    @Test
    fun `leave group channel in dark mode`() {
        snapshot(isInDarkMode = true) {
            ChannelInfoScreenModal(
                modal = ChannelInfoViewEvent.LeaveChannelModal,
                isGroupChannel = true,
            )
        }
    }

    @Test
    fun `delete direct channel`() {
        snapshot {
            ChannelInfoScreenModal(
                modal = ChannelInfoViewEvent.DeleteChannelModal,
                isGroupChannel = false,
            )
        }
    }

    @Test
    fun `delete direct channel in dark mode`() {
        snapshot(isInDarkMode = true) {
            ChannelInfoScreenModal(
                modal = ChannelInfoViewEvent.DeleteChannelModal,
                isGroupChannel = false,
            )
        }
    }

    @Test
    fun `delete group channel`() {
        snapshot {
            ChannelInfoScreenModal(
                modal = ChannelInfoViewEvent.DeleteChannelModal,
                isGroupChannel = true,
            )
        }
    }

    @Test
    fun `delete group channel in dark mode`() {
        snapshot(isInDarkMode = true) {
            ChannelInfoScreenModal(
                modal = ChannelInfoViewEvent.DeleteChannelModal,
                isGroupChannel = true,
            )
        }
    }

    @Test
    fun `ban member`() {
        snapshot {
            ChannelInfoScreenModalBanMember()
        }
    }

    @Test
    fun `ban member in dark mode`() {
        snapshot(isInDarkMode = true) {
            ChannelInfoScreenModalBanMember()
        }
    }

    @Test
    fun `remove member`() {
        snapshot {
            ChannelInfoScreenModalRemoveMember()
        }
    }

    @Test
    fun `remove member in dark mode`() {
        snapshot(isInDarkMode = true) {
            ChannelInfoScreenModalRemoveMember()
        }
    }
}
