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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import io.getstream.chat.android.compose.ui.util.AnnotationTagBlockQuote
import io.getstream.chat.android.compose.ui.util.AnnotationTagEmail
import io.getstream.chat.android.compose.ui.util.AnnotationTagLiteral
import io.getstream.chat.android.compose.ui.util.AnnotationTagMention
import io.getstream.chat.android.compose.ui.util.AnnotationTagUrl
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class MessageTextClickableTagTest {

    @Test
    fun `treats links, emails and mentions as clickable`() {
        listOf(AnnotationTagUrl, AnnotationTagEmail, AnnotationTagMention).forEach { tag ->
            range(tag).isClickableTag() shouldBeEqualTo true
        }
    }

    @Test
    fun `treats the markers markdown leaves behind as not clickable`() {
        listOf(AnnotationTagBlockQuote, AnnotationTagLiteral).forEach { tag ->
            range(tag).isClickableTag() shouldBeEqualTo false
        }
    }

    @Test
    fun `resolves a tap inside a quote to the link, not the quote's depth`() {
        // A quote's annotation spans its whole text and holds the depth, and it is recorded before
        // the entity pass finds the link, so an unfiltered lookup answers with the depth.
        val text = buildAnnotatedString {
            append("see https://getstream.io")
            addStringAnnotation(AnnotationTagBlockQuote, "1", 0, length)
            addStringAnnotation(AnnotationTagUrl, "https://getstream.io", 4, length)
        }
        val position = text.text.indexOf("https")

        val annotations = text.getStringAnnotations(0, text.length)
        val resolved = annotations.firstOrNull {
            it.isClickableTag() && position in it.start until it.end
        }

        resolved?.tag shouldBeEqualTo AnnotationTagUrl
        resolved?.item shouldBeEqualTo "https://getstream.io"
        // What the lookup used to return.
        annotations.firstOrNull { position in it.start until it.end }
            ?.tag shouldBeEqualTo AnnotationTagBlockQuote
    }

    @Test
    fun `does not answer a tap one past the end of an annotation`() {
        val text = buildAnnotatedString {
            append("hi https://getstream.io there")
            addStringAnnotation(AnnotationTagUrl, "https://getstream.io", 3, 23)
        }

        val annotations = text.getStringAnnotations(0, text.length)
        annotations.firstOrNull { it.isClickableTag() && 23 in it.start until it.end } shouldBeEqualTo null
    }

    private fun range(tag: String) = AnnotatedString.Range(item = "value", start = 0, end = 1, tag = tag)
}
