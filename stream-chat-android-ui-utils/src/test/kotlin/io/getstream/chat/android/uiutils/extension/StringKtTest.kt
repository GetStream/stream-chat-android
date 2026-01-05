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

package io.getstream.chat.android.uiutils.extension

import io.getstream.chat.android.randomString
import org.amshove.kluent.`should be`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class StringKtTest {

    @ParameterizedTest
    @MethodSource("containsLinksData")
    fun `containsLinks returns expected result`(input: String, expected: Boolean) {
        input.containsLinks() `should be` expected
    }

    companion object {

        @JvmStatic
        fun containsLinksData() =
            stringsWithoutLink().map { Arguments.of(it, false) } +
                stringsWithLink().map { Arguments.of(it, true) }

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
