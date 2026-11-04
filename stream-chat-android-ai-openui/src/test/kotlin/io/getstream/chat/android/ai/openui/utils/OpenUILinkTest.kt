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

package io.getstream.chat.android.ai.openui.utils

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class OpenUILinkTest {

    @Test
    fun `web and email links open`() {
        assertTrue(OpenUILink.isOpenable("https://getstream.io/chat"))
        assertTrue(OpenUILink.isOpenable("HTTP://example.com"))
        assertTrue(OpenUILink.isOpenable("mailto:support@getstream.io"))
    }

    @Test
    fun `links to other apps, scripts and phone numbers don't open`() {
        assertFalse(OpenUILink.isOpenable("javascript:alert(1)"))
        assertFalse(OpenUILink.isOpenable("tel:+15550100"))
        assertFalse(OpenUILink.isOpenable("intent://scan/#Intent;scheme=zxing;end"))
        assertFalse(OpenUILink.isOpenable("file:///data/data/app/secret"))
        assertFalse(OpenUILink.isOpenable("/relative/path"))
        assertFalse(OpenUILink.isOpenable(""))
        assertFalse(OpenUILink.isOpenable(null))
    }

    @Test
    fun `links with spaces or control characters don't open`() {
        assertFalse(OpenUILink.isOpenable("https://example.com/a b"))
        assertFalse(OpenUILink.isOpenable("https://example.com/\u0000"))
    }

    @Test
    fun `a host handler gets any well-formed link`() {
        assertTrue(OpenUILink.isWellFormed("tel:+15550100"))
        assertTrue(OpenUILink.isWellFormed("geo:0,0?q=Amsterdam"))
        assertTrue(OpenUILink.isWellFormed("https://getstream.io"))
        assertFalse(OpenUILink.isWellFormed("/relative/path"))
        assertFalse(OpenUILink.isWellFormed("1tel:123"))
        assertFalse(OpenUILink.isWellFormed("tel:+1 555"))
        assertFalse(OpenUILink.isWellFormed(null))
    }
}
