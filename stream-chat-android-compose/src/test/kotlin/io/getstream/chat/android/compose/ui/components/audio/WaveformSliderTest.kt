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

package io.getstream.chat.android.compose.ui.components.audio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import org.junit.Rule
import org.junit.Test

internal class WaveformSliderTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_2,
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun `slider playing at start`() {
        snapshotWithDarkModeRow { StaticWaveformSliderAtStart() }
    }

    @Test
    fun `slider playing midway`() {
        snapshotWithDarkModeRow { StaticWaveformSliderMidway() }
    }

    @Test
    fun `slider paused`() {
        snapshotWithDarkModeRow { StaticWaveformSliderPaused() }
    }

    @Test
    fun `slider without thumb`() {
        snapshotWithDarkModeRow { StaticWaveformSliderWithoutThumb() }
    }

    @Test
    fun `full track`() {
        snapshotWithDarkModeRow { FullWaveformTrack() }
    }

    @Test
    fun `slider playing midway rtl`() {
        snapshotWithDarkModeRow {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                StaticWaveformSliderMidway()
            }
        }
    }

    @Test
    fun `slider renders at sub-handle width`() {
        // Regression test for when the parent is narrower than handleSize
        snapshotWithDarkModeRow {
            Box(modifier = Modifier.width(3.dp).height(36.dp)) {
                StaticWaveformSliderMidway()
            }
        }
    }
}
