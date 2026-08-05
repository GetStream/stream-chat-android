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

package io.getstream.chat.android.compose.ui.components.poll

import com.android.ide.common.rendering.api.SessionParams
import io.getstream.chat.android.compose.ui.PIXEL_2_HDPI
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import org.junit.Rule
import org.junit.Test

internal class PollOptionVotesDialogTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = createPaparazzi(
        deviceConfig = PIXEL_2_HDPI,
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun loading() = snapshot { PollOptionVotesSheetLoading() }

    @Test
    fun `loading in dark mode`() = snapshot(isInDarkMode = true) { PollOptionVotesSheetLoading() }

    @Test
    fun content() = snapshot { PollOptionVotesSheetContent() }

    @Test
    fun `content in dark mode`() = snapshot(isInDarkMode = true) { PollOptionVotesSheetContent() }

    @Test
    fun `loading more`() = snapshot { PollOptionVotesSheetLoadingMore() }

    @Test
    fun `loading more in dark mode`() = snapshot(isInDarkMode = true) {
        PollOptionVotesSheetLoadingMore()
    }
}
