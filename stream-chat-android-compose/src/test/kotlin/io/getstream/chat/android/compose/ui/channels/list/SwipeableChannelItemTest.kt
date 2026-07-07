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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.PIXEL_2_HDPI
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.previewdata.PreviewChannelData
import org.junit.Rule
import org.junit.Test

internal class SwipeableChannelItemTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = PIXEL_2_HDPI,
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun `closed swipeable channel item hides the actions behind the content`() {
        snapshotWithDarkMode {
            SwipeableChannelItem(
                channelCid = PreviewChannelData.channelWithImage.cid,
                backgroundColor = ChatTheme.colors.backgroundCoreApp,
                swipeActions = {
                    SwipeActionItem(
                        icon = painterResource(R.drawable.stream_design_ic_more),
                        label = "More",
                        onClick = {},
                        style = SwipeActionStyle.Secondary,
                    )
                },
                content = {
                    Text(
                        text = "Channel item content",
                        color = ChatTheme.colors.textPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                    )
                },
            )
        }
    }
}
