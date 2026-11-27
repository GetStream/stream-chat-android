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

package io.getstream.chat.android.compose.ui.components.avatar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import io.getstream.chat.android.compose.ui.PIXEL_4A_HDPI
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewChannelData
import org.junit.Rule
import org.junit.Test

internal class ChannelAvatarTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        // Extra-tall canvas to fit all avatar variant rows: 4000px at the native 440dpi, converted to hdpi.
        deviceConfig = PIXEL_4A_HDPI.copy(screenHeight = 2182),
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun `channel avatar in light mode`() {
        snapshot {
            ChannelAvatarContent()
        }
    }

    @Test
    fun `channel avatar in dark mode`() {
        snapshot(isInDarkMode = true) {
            ChannelAvatarContent()
        }
    }

    @Test
    fun `channel avatar with more members than the ones loaded`() {
        snapshot {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Loaded members, then the member count of the channel.
                listOf(1 to 6, 2 to 6, 4 to 6, 4 to 200).forEach { (loaded, total) ->
                    ChannelAvatar(
                        channel = PreviewChannelData.makeChannelWithMembers(loaded).copy(memberCount = total),
                        currentUser = null,
                        modifier = Modifier.size(AvatarSize.ExtraLarge),
                    )
                }
                // The current user is one of the 2 loaded members, so it stays in the avatar.
                ChannelAvatar(
                    channel = PreviewChannelData.makeChannelWithMembers(2).copy(memberCount = 6),
                    currentUser = User(id = "user0"),
                    modifier = Modifier.size(AvatarSize.ExtraLarge),
                )
            }
        }
    }
}
