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

package io.getstream.chat.android.compose.ui.channels.list

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.ui.PIXEL_2_HDPI
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.previewdata.PreviewChannelData
import org.junit.Rule
import org.junit.Test

internal class DefaultChannelSwipeActionsTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = PIXEL_2_HDPI,
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun `swipe actions for unmuted channel`() {
        snapshotWithDarkMode {
            SwipeActionsRow(
                channelItem = ItemState.ChannelItemState(channel = ChannelWithMuteCapability),
            )
        }
    }

    @Test
    fun `swipe actions for muted channel`() {
        snapshotWithDarkMode {
            SwipeActionsRow(
                channelItem = ItemState.ChannelItemState(
                    channel = ChannelWithMuteCapability,
                    isMuted = true,
                ),
            )
        }
    }

    @Composable
    private fun SwipeActionsRow(channelItem: ItemState.ChannelItemState) {
        CompositionLocalProvider(
            LocalSwipeActionHandler provides {},
            LocalChannelMoreClickHandler provides {},
        ) {
            Row(modifier = Modifier.height(72.dp)) {
                DefaultChannelSwipeActions(channelItem = channelItem)
            }
        }
    }
}

private val ChannelWithMuteCapability = PreviewChannelData.channelWithImage.copy(
    ownCapabilities = setOf(ChannelCapabilities.MUTE_CHANNEL),
)
