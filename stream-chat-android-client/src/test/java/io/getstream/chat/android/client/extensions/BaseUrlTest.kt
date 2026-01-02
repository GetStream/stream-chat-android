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

package io.getstream.chat.android.client.extensions

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class BaseUrlTest {

    @ParameterizedTest
    @CsvSource(
        "domain.lan,domain.lan",
        "domain.lan/,domain.lan",
        "domain.lan//,domain.lan",
        "http://domain.lan,domain.lan",
        "http://domain.lan/,domain.lan",
        "http://domain.lan//,domain.lan",
        "https://domain.lan,domain.lan",
        "https://domain.lan/,domain.lan",
        "https://domain.lan//,domain.lan",
        "10.0.0.10,10.0.0.10",
        "10.0.0.10/,10.0.0.10",
        "http://10.0.0.10/,10.0.0.10",
        "https://10.0.0.10/,10.0.0.10",
    )
    fun test(fullUrl: String, baseUrl: String) {
        fullUrl.extractBaseUrl() `should be equal to` baseUrl
    }
}
