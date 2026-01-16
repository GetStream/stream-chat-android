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

package io.getstream.chat.android.ui.common.utils.extensions

import io.getstream.chat.android.randomString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class StringExtensionsTests {

    @ParameterizedTest
    @MethodSource("containsLinksData")
    fun `containsLinks returns expected result`(input: String, expected: Boolean) {
        Assertions.assertEquals(expected, input.containsLinks())
    }

    @ParameterizedTest
    @MethodSource("addSchemeToUrlIfNeededData")
    fun `addSchemeToUrlIfNeeded returns expected result`(input: String, expected: String) {
        Assertions.assertEquals(expected, input.addSchemeToUrlIfNeeded())
    }

    companion object {

        @JvmStatic
        fun containsLinksData() =
            stringsWithoutLink().map { Arguments.of(it, false) } +
                stringsWithLink().map { Arguments.of(it, true) }

        @JvmStatic
        fun addSchemeToUrlIfNeededData() = listOf(
            // URLs that already have http:// scheme should remain unchanged
            Arguments.of("http://example.com", "http://example.com"),
            Arguments.of("http://www.example.com", "http://www.example.com"),
            Arguments.of("http://example.com/path", "http://example.com/path"),
            Arguments.of("http://example.com/path?query=value", "http://example.com/path?query=value"),
            Arguments.of("http://example.com:8080", "http://example.com:8080"),
            // URLs that already have https:// scheme should remain unchanged
            Arguments.of("https://example.com", "https://example.com"),
            Arguments.of("https://www.example.com", "https://www.example.com"),
            Arguments.of("https://example.com/path", "https://example.com/path"),
            Arguments.of("https://example.com/path?query=value", "https://example.com/path?query=value"),
            Arguments.of("https://example.com:443", "https://example.com:443"),
            // mailto: URLs should remain unchanged
            Arguments.of("mailto:user@example.com", "mailto:user@example.com"),
            Arguments.of("mailto:test@test.com", "mailto:test@test.com"),
            Arguments.of("mailto:contact@company.org?subject=Hello", "mailto:contact@company.org?subject=Hello"),
            // URLs without scheme should get http:// prepended
            Arguments.of("example.com", "http://example.com"),
            Arguments.of("www.example.com", "http://www.example.com"),
            Arguments.of("example.com/path", "http://example.com/path"),
            Arguments.of("www.example.com/path/to/page", "http://www.example.com/path/to/page"),
            Arguments.of("example.com?query=value", "http://example.com?query=value"),
            Arguments.of("subdomain.example.com", "http://subdomain.example.com"),
            Arguments.of("example.co.uk", "http://example.co.uk"),
            Arguments.of("192.168.1.1", "http://192.168.1.1"),
            Arguments.of("localhost:8080", "http://localhost:8080"),

            // Edge cases
            Arguments.of("ftp://example.com", "http://ftp://example.com"), // ftp is not handled, gets http://
            Arguments.of("", "http://"),
        )

        private fun stringsWithoutLink() = listOf(
            randomString(),
            "not a url",
            "http//missing.colon.com",
            "https:/missing.slash.com",
            "http://missing-tld",
            "http://.com",
            "http://..com",
        )

        private fun stringsWithLink() = listOf(
            "https://www.example.com",
            "http://example.com",
            "www.example.com",
            "example.com",
            "ftp://example.com",
            "example.com/path?query=param",
            "HTTPS://WWW.EXAMPLE.COM",
            "HTTP://EXAMPLE.COM",
            "WWW.EXAMPLE.COM",
            "EXAMPLE.COM",
            "FTP://EXAMPLE.COM",
            "EXAMPLE.COM/PATH?QUERY=PARAM",
            "${randomString()} https://www.example.com",
            "${randomString()} http://example.com",
            "${randomString()} www.example.com",
            "${randomString()} example.com",
            "${randomString()} ftp://example.com",
            "${randomString()} example.com/path?query=param",
            "${randomString()} HTTPS://WWW.EXAMPLE.COM",
            "${randomString()} HTTP://EXAMPLE.COM",
            "${randomString()} WWW.EXAMPLE.COM",
            "${randomString()} EXAMPLE.COM",
            "${randomString()} FTP://EXAMPLE.COM",
            "${randomString()} EXAMPLE.COM/PATH?QUERY=PARAM",
            "${randomString()} https://www.example.com ${randomString()}",
            "${randomString()} http://example.com ${randomString()}",
            "${randomString()} www.example.com ${randomString()}",
            "${randomString()} example.com ${randomString()}",
            "${randomString()} ftp://example.com ${randomString()}",
            "${randomString()} example.com/path?query=param ${randomString()}",
            "${randomString()} HTTPS://WWW.EXAMPLE.COM ${randomString()}",
            "${randomString()} HTTP://EXAMPLE.COM ${randomString()}",
            "${randomString()} WWW.EXAMPLE.COM ${randomString()}",
            "${randomString()} EXAMPLE.COM ${randomString()}",
            "${randomString()} FTP://EXAMPLE.COM ${randomString()}",
            "${randomString()} EXAMPLE.COM/PATH?QUERY=PARAM ${randomString()}",
            "https://www.example.com ${randomString()}",
            "http://example.com ${randomString()}",
            "www.example.com ${randomString()}",
            "example.com ${randomString()}",
            "ftp://example.com ${randomString()}",
            "example.com/path?query=param ${randomString()}",
            "HTTPS://WWW.EXAMPLE.COM ${randomString()}",
            "HTTP://EXAMPLE.COM ${randomString()}",
            "WWW.EXAMPLE.COM ${randomString()}",
            "EXAMPLE.COM ${randomString()}",
            "FTP://EXAMPLE.COM ${randomString()}",
            "EXAMPLE.COM/PATH?QUERY=PARAM ${randomString()}",
        )
    }
}
