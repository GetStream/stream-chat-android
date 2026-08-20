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

package io.getstream.chat.android.ui.common.utils

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class SearchDebounceTest {

    @Test
    fun `Given a short query When resolving the debounce Should return the short query debounce`() {
        SearchDebounce.debounceMsFor("a", DEBOUNCE_MS) shouldBeEqualTo SearchDebounce.SHORT_QUERY_DEBOUNCE_MS
        SearchDebounce.debounceMsFor("ab", DEBOUNCE_MS) shouldBeEqualTo SearchDebounce.SHORT_QUERY_DEBOUNCE_MS
    }

    @Test
    fun `Given a regular query When resolving the debounce Should return the configured debounce`() {
        SearchDebounce.debounceMsFor("abc", DEBOUNCE_MS) shouldBeEqualTo DEBOUNCE_MS
        SearchDebounce.debounceMsFor("abcd", DEBOUNCE_MS) shouldBeEqualTo DEBOUNCE_MS
    }

    @Test
    fun `Given an empty query When resolving the debounce Should return the configured debounce`() {
        SearchDebounce.debounceMsFor("", DEBOUNCE_MS) shouldBeEqualTo DEBOUNCE_MS
    }

    @Test
    fun `Given a debounce longer than the short query one When resolving the debounce Should keep it`() {
        val debounceMs = SearchDebounce.SHORT_QUERY_DEBOUNCE_MS + 300

        SearchDebounce.debounceMsFor("a", debounceMs) shouldBeEqualTo debounceMs
        SearchDebounce.debounceMsFor("abc", debounceMs) shouldBeEqualTo debounceMs
    }

    private companion object {
        private const val DEBOUNCE_MS = 300L
    }
}
