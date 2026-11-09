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

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class FollowTopInsertionsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val items = mutableStateOf(List(30) { "item $it" })
    private lateinit var listState: LazyListState

    @Test
    fun `an item inserted on top while at the top is scrolled into view`() {
        setList()

        composeTestRule.runOnIdle { items.value = listOf("new") + items.value }
        composeTestRule.waitForIdle()

        assertEquals("new", firstVisibleKey())
    }

    @Test
    fun `an item inserted on top while scrolled down keeps the current position`() {
        setList()
        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(10) } }

        composeTestRule.runOnIdle { items.value = listOf("new") + items.value }
        composeTestRule.waitForIdle()

        assertEquals("item 10", firstVisibleKey())
    }

    private fun setList() {
        composeTestRule.setContent {
            listState = rememberLazyListState()
            FollowTopInsertions(listState = listState, topKey = items.value.firstOrNull())
            LazyColumn(state = listState, modifier = Modifier.height(300.dp)) {
                items(items = items.value, key = { it }) { Text(text = it, modifier = Modifier.height(50.dp)) }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun firstVisibleKey(): Any? = composeTestRule.runOnIdle {
        listState.layoutInfo.visibleItemsInfo.firstOrNull()?.key
    }
}
