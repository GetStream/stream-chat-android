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

package io.getstream.chat.android.ai.compose.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import io.getstream.chat.android.ai.compose.ui.PaparazziTest
import org.junit.Rule
import org.junit.Test

internal class AISuggestionsTest : PaparazziTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        renderingMode = RenderingMode.SHRINK,
    )

    @Test
    fun default() {
        snapshot {
            AISuggestionsSample()
        }
    }

    @Test
    fun `wider items`() {
        snapshot {
            AISuggestionsSample(itemMaxWidth = 240.dp)
        }
    }

    @Test
    fun `large font`() {
        paparazzi.unsafeUpdateConfig(deviceConfig = DeviceConfig.PIXEL_5.copy(fontScale = 2f))
        snapshot {
            AISuggestionsSample()
        }
    }

    @Test
    fun `custom chip without size modifiers`() {
        snapshot {
            CompositionLocalProvider(LocalChatAiComponentFactory provides PlainChipFactory) {
                AISuggestionsSample()
            }
        }
    }
}

// A chip that applies none of the row's size modifiers itself.
private object PlainChipFactory : ChatAiComponentFactory {
    @Composable
    override fun AISuggestionsChip(params: AISuggestionsChipParams) {
        Box(Modifier.background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp)) {
            Text(params.text, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
