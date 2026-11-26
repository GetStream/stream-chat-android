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

package io.getstream.chat.android.ai.compose.util.internal

import androidx.compose.ui.platform.UriHandler
import org.junit.Assert.assertEquals
import org.junit.Test

internal class OpenableLinksTest {

    private val opened = mutableListOf<String>()
    private val handler = OpenableLinkUriHandler(
        delegate = object : UriHandler {
            override fun openUri(uri: String) {
                opened += uri
            }
        },
    )

    @Test
    fun `web and email links open`() {
        val links = listOf("https://example.com", "HTTP://example.com/path", "mailto:ana@example.com")

        links.forEach(handler::openUri)

        assertEquals(links, opened)
    }

    @Test
    fun `other links are ignored`() {
        listOf(
            "myapp://settings/delete-account",
            "intent://scan/#Intent;scheme=zxing;end",
            "javascript:alert(1)",
            "file:///sdcard/secret.txt",
            "tel:+15550100",
            "https:example.com",
            "/relative",
        ).forEach(handler::openUri)

        assertEquals(emptyList<String>(), opened)
    }
}
