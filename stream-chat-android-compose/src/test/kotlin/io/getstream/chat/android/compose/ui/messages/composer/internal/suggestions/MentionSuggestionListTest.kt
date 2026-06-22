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

package io.getstream.chat.android.compose.ui.messages.composer.internal.suggestions

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention
import org.junit.Rule
import org.junit.Test

internal class MentionSuggestionListTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_2,
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun `mention suggestion list`() {
        snapshotWithDarkModeRow {
            MentionSuggestionList(
                mentions = listOf(
                    Mention.Channel,
                    Mention.Here,
                    Mention.User(PreviewUserData.user1),
                    Mention.User(PreviewUserData.user2),
                    Mention.User(PreviewUserData.user3),
                    Mention.Role("admin"),
                ),
            )
        }
    }
}
