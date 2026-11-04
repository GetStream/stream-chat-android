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

package io.getstream.chat.android.ai.a2ui.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/** The A2UI regex dialect and its static safety check. */
internal class A2UIRegexTest {

    @Test
    fun `the protocol's accepted examples compile and match`() {
        assertTrue(A2UIRegex.compile("^[a-z]+@[a-z]+\\.com$").containsMatchIn("ada@example.com"))
        assertTrue(A2UIRegex.compile("^(?=.*\\d).{8,}$").containsMatchIn("password1"))
        assertFalse(A2UIRegex.compile("^(?=.*\\d).{8,}$").containsMatchIn("password"))
        assertTrue(A2UIRegex.compile("\\w+\\s+\\w+").containsMatchIn("hello world"))
    }

    @Test
    fun `patterns that could backtrack catastrophically are rejected`() {
        // Nested quantifiers, a quantified alternation, adjacent repeats and a backreference.
        listOf("(a+)+$", "(a|ab)*c", "\\d+\\s*\\d+", "(\\w)\\1", "(?:a*){2}", "a*a*").forEach(::assertRejected)
    }

    @Test
    fun `constructs outside the dialect are rejected`() {
        listOf(
            "(?<=a)b", "(?<!a)b", "(?<name>a)", "(?i)a", "\\p{L}", "\\P{L}", "\\cA", "\\01", "\\k<a>",
            "a**", "a{2", "{", "]", "\\z", "\\Qa\\E", "a++", "^*", "[\\d-z]", "[invalid(", "a)",
        ).forEach(::assertRejected)
    }

    @Test
    fun `a pattern longer than 256 code points is rejected`() {
        assertRejected("a".repeat(A2UIRegex.MAX_PATTERN_LENGTH + 1))
        A2UIRegex.compile("a".repeat(A2UIRegex.MAX_PATTERN_LENGTH))
    }

    @Test
    fun `matching follows the dialect rather than the platform`() {
        assertFalse(A2UIRegex.compile("^a$").containsMatchIn("a\n"))
        assertFalse(A2UIRegex.compile("^.$").containsMatchIn("\r"))
        assertTrue(A2UIRegex.compile("^.$").containsMatchIn("\u0085"))
        assertFalse(A2UIRegex.compile("\\d").containsMatchIn("\u0663"))
        assertTrue(A2UIRegex.compile("^\\s$").containsMatchIn("\u00A0"))
        assertTrue(A2UIRegex.compile("^\\u{1F600}$").containsMatchIn("\uD83D\uDE00"))
        assertTrue(A2UIRegex.compile("^\\uD83D\\uDE00$").containsMatchIn("\uD83D\uDE00"))
        assertTrue(A2UIRegex.compile("^[^]$").containsMatchIn("\n"))
        assertFalse(A2UIRegex.compile("[]").containsMatchIn("a"))
        assertFalse(A2UIRegex.compile("\\bé").containsMatchIn("é"))
        assertTrue(A2UIRegex.compile("^[\\w-]{2,3}?x$").containsMatchIn("a-x"))
        assertEquals(true, A2UIRegex.compile("^\\x41\\$\\/$").containsMatchIn("A$/"))
    }

    @Test
    fun `a quantifier over a group with a quantifier or an alternation is rejected`() {
        listOf(
            "(a?){25}a{25}",
            "(a?){25}",
            "(a+)+",
            "(a|aa)*",
            "(?:a*)*",
            "(?:a|b)?",
            "(?:(a)b?)+",
            "(?:ab{2})?",
        ).forEach(::assertRejected)
    }

    @Test
    fun `backreferences are rejected`() {
        listOf("(a)\\1", "(a)(b)\\2", "(?<x>a)\\k<x>").forEach(::assertRejected)
    }

    @Test
    fun `a count above 1000 is rejected`() {
        listOf("a{1001}", "a{0,1001}", "a{1001,}", "a{99999999999}").forEach(::assertRejected)
        A2UIRegex.compile("a{0,${A2UIRegex.MAX_REPEAT_COUNT}}")
    }

    @Test
    fun `common validation patterns are accepted`() {
        val email = A2UIRegex.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
        assertTrue(A2UIRegex.containsMatch(email, "ada@example.com"))
        assertFalse(A2UIRegex.containsMatch(email, "ada@example"))
        val phone = A2UIRegex.compile("^\\+?[0-9]{10,15}$")
        assertTrue(A2UIRegex.containsMatch(phone, "+46701234567"))
        assertFalse(A2UIRegex.containsMatch(phone, "12345"))
        val code = A2UIRegex.compile("^[A-Z]{2}\\d{4}$")
        assertTrue(A2UIRegex.containsMatch(code, "AB1234"))
        assertFalse(A2UIRegex.containsMatch(code, "AB123"))
        listOf("^#[0-9a-fA-F]{6}$", "^@([^@]|$)", "^(?:abc)?x$", "^(a)+$").forEach { A2UIRegex.compile(it) }
    }

    @Test
    fun `a value longer than the limit doesn't match`() {
        val regex = A2UIRegex.compile("a")
        assertTrue(A2UIRegex.containsMatch(regex, "a".repeat(A2UIRegex.MAX_VALUE_LENGTH)))
        assertFalse(A2UIRegex.containsMatch(regex, "a".repeat(A2UIRegex.MAX_VALUE_LENGTH + 1)))
    }

    private fun assertRejected(pattern: String) {
        try {
            A2UIRegex.compile(pattern)
            fail("'$pattern' was accepted")
        } catch (error: FunctionException) {
            assertEquals(RendererError.EXPRESSION_ERROR, error.code)
        }
    }
}
