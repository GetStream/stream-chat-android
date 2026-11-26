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

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performFirstLinkClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class StreamingReasoningLinkTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `a tapped web link in the reasoning opens`() {
        val opened = tapLink("Read [Docs](https://example.com/docs) first", "Docs")

        assertEquals(listOf("https://example.com/docs"), opened)
    }

    @Test
    fun `a tapped link of another scheme in the reasoning does not open`() {
        val opened = tapLink("Open [Settings](myapp://settings/delete-account) now", "Settings")

        assertEquals(emptyList<String>(), opened)
    }

    private fun tapLink(text: String, label: String): List<String> {
        val opened = mutableListOf<String>()
        val uriHandler = object : UriHandler {
            override fun openUri(uri: String) {
                opened += uri
            }
        }
        rule.setContent {
            CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                StreamingReasoning(text = text, isThinking = false, initiallyExpanded = true)
            }
        }

        val node = rule.onNodeWithText(label, substring = true, useUnmergedTree = true).assertExists()
        // A link that may not open has no link to click.
        runCatching { node.performFirstLinkClick() }
        rule.waitForIdle()
        return opened
    }
}
