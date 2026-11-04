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

package io.getstream.chat.android.ai.openui.rendering

import androidx.compose.ui.text.LinkAnnotation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class InlineMarkdownTest {

    @Test
    fun `bare web links are tappable`() {
        val text = inlineMarkdown("Docs at https://getstream.io/chat today")
        val links = text.getLinkAnnotations(0, text.length).map { (it.item as LinkAnnotation.Url).url }
        assertEquals(listOf("https://getstream.io/chat"), links)
    }

    @Test
    fun `plain text stays plain`() {
        val text = inlineMarkdown("Just a title")
        assertEquals("Just a title", text.text)
        assertTrue(text.getLinkAnnotations(0, text.length).isEmpty())
    }

    @Test
    fun `links that can't open show their text`() {
        val text = inlineMarkdown("[call](tel:123) or [site](https://getstream.io)")
        assertEquals("call or site", text.text)
        val links = text.getLinkAnnotations(0, text.length).map { (it.item as LinkAnnotation.Url).url }
        assertEquals(listOf("https://getstream.io"), links)
    }
}
