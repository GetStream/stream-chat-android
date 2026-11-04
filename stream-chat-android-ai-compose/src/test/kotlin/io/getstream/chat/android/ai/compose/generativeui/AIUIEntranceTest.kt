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

package io.getstream.chat.android.ai.compose.generativeui

import android.graphics.Bitmap
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
internal class AIUIEntranceTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `a part of a streaming reply comes in and shows`() {
        assertShowsAfterEntrance(isStreaming = true)
    }

    @Test
    fun `a part of a finished reply shows at once`() {
        assertShowsAfterEntrance(isStreaming = false)
    }

    @Test
    fun `a part brought in by a modifier keeps the weight its row gives it`() {
        lateinit var view: View
        rule.setContent {
            view = LocalView.current
            Row(modifier = Modifier.width((Size * 2).dp).background(Color.White)) {
                AIUIEntranceStage(isStreaming = true) {
                    Row {
                        AIUIPartEntrance { entrance ->
                            Box(modifier = Modifier.weight(1f).then(entrance).height(Size.dp).background(Color.Red))
                        }
                        AIUIPartEntrance { entrance ->
                            Box(modifier = Modifier.weight(1f).then(entrance).height(Size.dp).background(Color.Blue))
                        }
                    }
                }
            }
        }
        rule.mainClock.advanceTimeBy(EntranceMillis)

        val pixels = rule.runOnIdle {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(bitmap))
            bitmap.getPixel(Size / 2, Size / 2) to bitmap.getPixel(Size * 3 / 2, Size / 2)
        }
        assertEquals(Color.Red.toArgb() to Color.Blue.toArgb(), pixels)
    }

    private fun assertShowsAfterEntrance(isStreaming: Boolean) {
        lateinit var view: View
        rule.setContent {
            view = LocalView.current
            Box(modifier = Modifier.background(Color.White)) {
                AIUIEntranceStage(isStreaming = isStreaming) {
                    AIUIEntrance { Box(modifier = Modifier.size(Size.dp).background(Color.Red)) }
                }
            }
        }
        rule.mainClock.advanceTimeBy(EntranceMillis)

        val pixel = rule.runOnIdle {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(bitmap))
            bitmap.getPixel(Size / 2, Size / 2)
        }
        assertEquals(Color.Red.toArgb(), pixel)
    }

    private companion object {
        const val EntranceMillis = 5_000L
        const val Size = 20
    }
}
