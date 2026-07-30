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

package io.getstream.chat.android.compose.ui.messages.list

import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class ScrollToFocusedItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var listState: LazyListState
    private lateinit var scope: CoroutineScope

    private fun setListContent() {
        composeTestRule.setContent {
            listState = rememberLazyListState()
            scope = rememberCoroutineScope()
            LazyColumn(state = listState, modifier = Modifier.size(200.dp)) {
                items(count = 200) {
                    Box(modifier = Modifier.size(50.dp))
                }
            }
        }
    }

    @Test
    fun `given no focused item, the list does not scroll`() {
        setListContent()

        val jump = scrollToFocusedItemAsync(focusedItemIndex = -1)

        composeTestRule.waitUntil(timeoutMillis = 5_000) { jump.isCompleted }
        assertEquals(0, listState.firstVisibleItemIndex)
    }

    @Test
    fun `given an idle list, scrolls to the focused item`() {
        setListContent()

        val jump = scrollToFocusedItemAsync(focusedItemIndex = 100)

        composeTestRule.waitUntil(timeoutMillis = 5_000) { jump.isCompleted }
        assertEquals(100, listState.firstVisibleItemIndex)
    }

    @Test
    fun `given a scroll in progress, waits it out and then scrolls to the focused item`() {
        setListContent()
        // Pause the clock so the animation genuinely overlaps the jump. With the clock
        // auto-advancing, runOnIdle would finish the animation before the jump even starts and
        // the test would pass without exercising the wait. Assertions use runOnUiThread because
        // runOnIdle cannot settle while the clock is paused mid-animation.
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.runOnUiThread {
            scope.launch {
                listState.animateScrollBy(value = 1_000f, animationSpec = tween(durationMillis = 2_000))
            }
        }
        composeTestRule.mainClock.advanceTimeBy(500)
        composeTestRule.runOnUiThread { assertTrue(listState.isScrollInProgress) }

        val jump = composeTestRule.runOnUiThread {
            scope.async { listState.scrollToFocusedItem(focusedItemIndex = 100, offset = 0) }
        }

        composeTestRule.mainClock.advanceTimeBy(500)
        composeTestRule.runOnUiThread {
            assertTrue(listState.isScrollInProgress)
            assertFalse(jump.isCompleted)
            assertTrue(listState.firstVisibleItemIndex < 100)
        }

        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitUntil(timeoutMillis = 5_000) { jump.isCompleted }
        assertEquals(100, listState.firstVisibleItemIndex)
    }

    private fun scrollToFocusedItemAsync(focusedItemIndex: Int): Deferred<Unit> =
        composeTestRule.runOnIdle {
            scope.async {
                listState.scrollToFocusedItem(focusedItemIndex = focusedItemIndex, offset = 0)
            }
        }
}
