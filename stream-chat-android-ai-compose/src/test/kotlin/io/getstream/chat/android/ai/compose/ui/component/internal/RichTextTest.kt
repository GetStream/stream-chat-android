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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class RichTextTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `each revealed text shows at once`() {
        val words = List(20) { "word$it" }
        var text by mutableStateOf(words.first())
        rule.setContent { RichText(text = text) }
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false

        words.indices.drop(1).forEach { last ->
            text = words.take(last + 1).joinToString(" ")
            Snapshot.sendApplyNotifications()
            // One frame recomposes, the next one shows the result.
            repeat(2) { rule.mainClock.advanceTimeByFrame() }
            rule.onNodeWithText(words[last], substring = true, useUnmergedTree = true).assertExists()
        }
    }
}
