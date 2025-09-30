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

package io.getstream.chat.android.compose.ui.pinned

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.SnapshotTest
import io.getstream.chat.android.previewdata.PreviewPinnedMessageData
import io.getstream.chat.android.ui.common.model.MessageResult
import io.getstream.chat.android.ui.common.state.pinned.PinnedMessageListState
import org.junit.Rule
import org.junit.Test

internal class PinnedMessageListTest : SnapshotTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `loading pinned messages`() {
        snapshotWithDarkMode {
            PinnedMessageList(
                state = PinnedMessageListState(),
                modifier = Modifier.fillMaxSize(),
                onPinnedMessageClick = { },
                onLoadMore = { },
            )
        }
    }

    @Test
    fun `empty pinned messages`() {
        snapshotWithDarkMode {
            PinnedMessageList(
                state = PinnedMessageListState(isLoading = false),
                modifier = Modifier.fillMaxSize(),
                onPinnedMessageClick = { },
                onLoadMore = { },
            )
        }
    }

    @Test
    fun `loaded pinned messages`() {
        snapshotWithDarkMode {
            PinnedMessageList(
                state = PinnedMessageListState(
                    results = PreviewPinnedMessageData
                        .pinnedMessageList
                        .map { MessageResult(it, null) },
                    isLoading = false,
                ),
                modifier = Modifier.fillMaxSize(),
                onPinnedMessageClick = { },
                onLoadMore = { },
            )
        }
    }

    @Test
    fun `loading more pinned messages`() {
        snapshotWithDarkMode {
            PinnedMessageList(
                state = PinnedMessageListState(
                    results = PreviewPinnedMessageData
                        .pinnedMessageListWithLoadingMore
                        .map { MessageResult(it, null) },
                ),
                modifier = Modifier.fillMaxSize(),
                onPinnedMessageClick = { },
                onLoadMore = { },
            )
        }
    }
}
