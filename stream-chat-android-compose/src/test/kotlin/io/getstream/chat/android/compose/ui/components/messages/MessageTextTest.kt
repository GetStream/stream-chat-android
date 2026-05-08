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
import io.getstream.chat.android.compose.ui.util.AnnotationTagEmail
import io.getstream.chat.android.compose.ui.util.AnnotationTagMention
import io.getstream.chat.android.compose.ui.util.AnnotationTagUrl
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class MessageTextTest {

    @ParameterizedTest
    @MethodSource("interactiveTagCases")
    fun `isInteractiveTag returns true for URL, email and mention tags`(tag: String, expected: Boolean) {
        val range = AnnotatedString.Range(item = "value", start = 0, end = 5, tag = tag)

        range.isInteractiveTag() `should be equal to` expected
    }

    @Test
    fun `hasInteractiveAt returns false for empty annotation list`() {
        val annotations = emptyList<AnnotatedString.Range<String>>()

        annotations.hasInteractiveAt(offset = 0) `should be equal to` false
    }

    @Test
    fun `hasInteractiveAt returns true when offset falls inside a URL annotation`() {
        val annotations = listOf(urlAt(start = 5, end = 15))

        annotations.hasInteractiveAt(offset = 7) `should be equal to` true
    }

    @Test
    fun `hasInteractiveAt returns true at the inclusive start boundary`() {
        val annotations = listOf(urlAt(start = 5, end = 15))

        annotations.hasInteractiveAt(offset = 5) `should be equal to` true
    }

    @Test
    fun `hasInteractiveAt returns false at the exclusive end boundary`() {
        val annotations = listOf(urlAt(start = 5, end = 15))

        annotations.hasInteractiveAt(offset = 15) `should be equal to` false
    }

    @Test
    fun `hasInteractiveAt returns false just before a URL annotation`() {
        val annotations = listOf(urlAt(start = 5, end = 15))

        annotations.hasInteractiveAt(offset = 4) `should be equal to` false
    }

    @Test
    fun `hasInteractiveAt returns true for a mention annotation`() {
        val annotations = listOf(
            AnnotatedString.Range(item = "user", start = 0, end = 5, tag = AnnotationTagMention),
        )

        annotations.hasInteractiveAt(offset = 2) `should be equal to` true
    }

    @Test
    fun `hasInteractiveAt returns true for an email annotation`() {
        val annotations = listOf(
            AnnotatedString.Range(item = "x@y.z", start = 0, end = 5, tag = AnnotationTagEmail),
        )

        annotations.hasInteractiveAt(offset = 2) `should be equal to` true
    }

    @Test
    fun `hasInteractiveAt ignores annotations with non-interactive tags at the same offset`() {
        val annotations = listOf(
            AnnotatedString.Range(item = "style", start = 0, end = 10, tag = "STYLE"),
        )

        annotations.hasInteractiveAt(offset = 5) `should be equal to` false
    }

    @Test
    fun `hasInteractiveAt returns true when at least one of multiple annotations matches`() {
        val annotations = listOf(
            AnnotatedString.Range(item = "style", start = 0, end = 100, tag = "STYLE"),
            urlAt(start = 5, end = 15),
        )

        annotations.hasInteractiveAt(offset = 7) `should be equal to` true
    }

    private fun urlAt(start: Int, end: Int) =
        AnnotatedString.Range(item = "https://example.com", start = start, end = end, tag = AnnotationTagUrl)

    companion object {

        @JvmStatic
        fun interactiveTagCases(): List<Arguments> = listOf(
            Arguments.of(AnnotationTagUrl, true),
            Arguments.of(AnnotationTagEmail, true),
            Arguments.of(AnnotationTagMention, true),
            Arguments.of("STYLE", false),
            Arguments.of("UNKNOWN", false),
            Arguments.of("", false),
        )
    }
}
