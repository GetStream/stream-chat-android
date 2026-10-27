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

package io.getstream.chat.android.ai.compose.ui.component.internal

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AIConversationScrollPaddingTest {

    @get:Rule
    val rule = createComposeRule()

    private lateinit var state: LazyListState
    private lateinit var scope: CoroutineScope
    private lateinit var density: Density

    private fun setContent(contentPadding: PaddingValues) {
        rule.setContent {
            state = rememberLazyListState()
            scope = rememberCoroutineScope()
            density = LocalDensity.current
            Box(modifier = Modifier.size(width = 300.dp, height = 400.dp)) {
                LazyColumn(state = state, contentPadding = contentPadding) {
                    items(count = 5) {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp))
                    }
                }
            }
        }
    }

    private fun scrollToEnd() {
        rule.runOnIdle { scope.launch { state.scrollToEnd(animated = false) } }
        rule.waitForIdle()
    }

    private fun distanceToEnd(): Int = rule.runOnIdle { state.layoutInfo.distanceToEnd() }

    @Test
    fun `the end is the bottom padding fully in view`() {
        setContent(PaddingValues(top = 56.dp, bottom = 96.dp))
        scrollToEnd()

        assertEquals(0, distanceToEnd())
        // The last item ends where the bottom padding starts.
        rule.runOnIdle {
            val info = state.layoutInfo
            val last = info.visibleItemsInfo.last()
            val bottomPadding = with(density) { 96.dp.roundToPx() }
            assertEquals(info.viewportEndOffset - info.afterContentPadding, last.offset + last.size)
            assertEquals(bottomPadding, info.afterContentPadding)
        }
    }

    @Test
    fun `the last item ending under the bottom padding is not the end`() {
        setContent(PaddingValues(bottom = 96.dp))
        scrollToEnd()
        rule.runOnIdle { scope.launch { state.scrollBy(-with(density) { 40.dp.toPx() }) } }
        rule.waitForIdle()

        val distance = distanceToEnd()
        assertTrue(distance > 0 && distance < Int.MAX_VALUE)
        assertEquals(with(density) { 40.dp.roundToPx() }.toFloat(), distance.toFloat(), 1f)
    }

    @Test
    fun `the last item out of the layout is far from the end`() {
        setContent(PaddingValues(bottom = 96.dp))

        assertEquals(Int.MAX_VALUE, distanceToEnd())
    }
}
