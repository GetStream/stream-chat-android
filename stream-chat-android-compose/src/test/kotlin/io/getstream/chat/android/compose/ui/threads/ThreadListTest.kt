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

package io.getstream.chat.android.compose.ui.threads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.previewdata.PreviewThreadData
import io.getstream.chat.android.ui.common.state.threads.ThreadListState
import org.junit.Rule
import org.junit.Test

internal class ThreadListTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `loading threads`() {
        snapshotWithDarkMode {
            ThreadList(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChatTheme.colors.appBackground),
                state = ThreadListState(
                    threads = emptyList(),
                    isLoading = true,
                    isLoadingMore = false,
                    unseenThreadsCount = 0,
                ),
                onUnreadThreadsBannerClick = {},
                onThreadClick = { },
                onLoadMore = {},
            )
        }
    }

    @Test
    fun `empty threads`() {
        snapshotWithDarkMode {
            ThreadList(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChatTheme.colors.appBackground),
                state = ThreadListState(
                    threads = emptyList(),
                    isLoading = false,
                    isLoadingMore = false,
                    unseenThreadsCount = 0,
                ),
                onUnreadThreadsBannerClick = {},
                onThreadClick = { },
                onLoadMore = {},
            )
        }
    }

    @Test
    fun `loaded threads`() {
        snapshotWithDarkMode {
            ThreadList(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChatTheme.colors.appBackground),
                state = ThreadListState(
                    threads = PreviewThreadData.threadList,
                    isLoading = false,
                    isLoadingMore = false,
                    unseenThreadsCount = 0,
                ),
                onUnreadThreadsBannerClick = {},
                onThreadClick = { },
                onLoadMore = {},
            )
        }
    }

    @Test
    fun `loaded threads with unread banner`() {
        snapshotWithDarkMode {
            ThreadList(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChatTheme.colors.appBackground),
                state = ThreadListState(
                    threads = PreviewThreadData.threadList,
                    isLoading = false,
                    isLoadingMore = false,
                    unseenThreadsCount = 1,
                ),
                onUnreadThreadsBannerClick = {},
                onThreadClick = { },
                onLoadMore = {},
            )
        }
    }

    @Test
    fun `loading more threads`() {
        snapshotWithDarkMode {
            ThreadList(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChatTheme.colors.appBackground),
                state = ThreadListState(
                    threads = PreviewThreadData.threadList,
                    isLoading = false,
                    isLoadingMore = true,
                    unseenThreadsCount = 0,
                ),
                onUnreadThreadsBannerClick = {},
                onThreadClick = { },
                onLoadMore = {},
            )
        }
    }
}
