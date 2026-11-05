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

/** The A2UI regex dialect, matched with RE2J. */
internal class A2UIRegexTest {

    @Test
    fun `the protocol's accepted examples compile and match`() {
        assertTrue(A2UIRegex.compile("^[a-z]+@[a-z]+\\.com$").containsMatchIn("ada@example.com"))
        assertTrue(A2UIRegex.compile("\\w+\\s+\\w+").containsMatchIn("hello world"))
    }

    @Test(timeout = 10_000)
    fun `patterns that backtrack catastrophically elsewhere match in linear time`() {
        val longest = A2UIRegex.MAX_VALUE_LENGTH
        assertFalse(A2UIRegex.containsMatch(A2UIRegex.compile("x.*x.*y"), "x".repeat(longest)))
        assertFalse(A2UIRegex.containsMatch(A2UIRegex.compile("^(a+)+$"), "a".repeat(longest - 1) + "!"))
        assertFalse(A2UIRegex.containsMatch(A2UIRegex.compile("^(a|aa)*$"), "a".repeat(longest - 1) + "!"))
        assertFalse(A2UIRegex.containsMatch(A2UIRegex.compile("^[a-z]*[a-z]*[a-z]*[a-z]*1$"), "a".repeat(longest)))
        assertTrue(A2UIRegex.containsMatch(A2UIRegex.compile("^(a?){25}a{25}$"), "a".repeat(25)))
        assertTrue(A2UIRegex.containsMatch(A2UIRegex.compile("\\d+\\s*\\d+"), "12 34"))
        assertTrue(A2UIRegex.containsMatch(A2UIRegex.compile("^((a|b)c)+$"), "acbc"))
    }

    @Test
    fun `lookahead is rejected`() {
        listOf("^(?=.*\\d).{8,}$", "^(?!.*\\s).+$", "(?=a)b", "a(?!b)").forEach(::assertRejected)
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
    fun `optional and counted groups match`() {
        listOf(
            "^[0-9]{5}(?:-[0-9]{4})?$",
            "(Mr|Mrs)?",
            "(?:-[0-9]{4})?",
            "(ab){3}",
            "(?:a|b)?",
            "(?:ab{2})?",
            "(?:a+){1}",
            "(?:a*){0,1}",
            "(?:ab{2})+",
            "^(\\+\\d{1,3}\\s?)?\\d{10}$",
        ).forEach { A2UIRegex.compile(it) }
        val zip = A2UIRegex.compile("^[0-9]{5}(?:-[0-9]{4})?$")
        assertTrue(A2UIRegex.containsMatch(zip, "12345"))
        assertTrue(A2UIRegex.containsMatch(zip, "12345-6789"))
        assertFalse(A2UIRegex.containsMatch(zip, "12345-67"))
        val title = A2UIRegex.compile("^(Mr|Mrs)?\\.? [A-Z][a-z]+$")
        assertTrue(A2UIRegex.containsMatch(title, "Mrs. Smith"))
        assertTrue(A2UIRegex.containsMatch(title, " Smith"))
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
    fun `nested counts whose product is above 1000 are rejected`() {
        listOf("(?:a{1000}){2}", "((a{10}){10}){11}", "(?:(?:a{1000}){1000}){1000}", "(?:a{2,}){501}").forEach(::assertRejected)
        listOf("(?:a{10}){100}", "(?:a{1000})*", "(?:a{500})+(?:b{1000})?").forEach { A2UIRegex.compile(it) }
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
        val fullEmail = A2UIRegex.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        assertTrue(A2UIRegex.containsMatch(fullEmail, "ada.lovelace+news@example.co.uk"))
        val usPhone = A2UIRegex.compile("^\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}$")
        assertTrue(A2UIRegex.containsMatch(usPhone, "(555) 123-4567"))
        val intlPhone = A2UIRegex.compile("^(\\+\\d{1,3}\\s?)?\\d{10}$")
        assertTrue(A2UIRegex.containsMatch(intlPhone, "+389 7012345678"))
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
