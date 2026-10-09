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

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import io.getstream.chat.android.ai.compose.parts.AIToolApproval
import io.getstream.chat.android.ai.compose.ui.PaparazziTest
import org.junit.Rule
import org.junit.Test

internal class AIToolApprovalCardTest : PaparazziTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        renderingMode = RenderingMode.SHRINK,
    )

    private val approval = AIToolApproval(
        title = "Share your location?",
        message = "Only your city is shared.",
        reason = "to check the local weather",
        allowTitle = "Share location",
        declineTitle = "Don't share",
    )

    @Test
    fun idle() {
        snapshot {
            AIToolApprovalCard(approval = approval, state = AIToolApprovalState(), decide = {}, modifier = Modifier.padding(16.dp))
        }
    }

    @Test
    fun sending() {
        snapshot {
            AIToolApprovalCard(
                approval = approval,
                state = AIToolApprovalState(isSending = true),
                decide = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    @Test
    fun long_titles_at_large_font() {
        snapshot {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                AIToolApprovalCard(
                    approval = approval.copy(allowTitle = "Share precise location"),
                    state = AIToolApprovalState(),
                    decide = {},
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }

    @Test
    fun failed() {
        snapshot {
            AIToolApprovalCard(
                approval = AIToolApproval(title = "Send this email?"),
                state = AIToolApprovalState(failed = true),
                decide = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
