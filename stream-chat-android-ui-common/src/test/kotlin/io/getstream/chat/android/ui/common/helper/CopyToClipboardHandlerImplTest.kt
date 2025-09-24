/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.helper

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class CopyToClipboardHandlerImplTest {

    @Test
    fun `copy to clipboard`() {
        val fixture = Fixture()
        val sut = fixture.get()

        val text = "text to copy"
        sut.copy(text)

        fixture.verifyCopy(text)
    }

    private class Fixture {
        val mockClipboardManager = mock<ClipboardManager>()
        val mockContext = mock<Context> {
            on { applicationContext } doReturn it
            on { getSystemService(Context.CLIPBOARD_SERVICE) } doReturn mockClipboardManager
        }
        val clipboardManager: ClipboardManager = mock()

        fun verifyCopy(text: String) {
            verify(mockClipboardManager).setPrimaryClip(ClipData.newPlainText("plain text", text))
        }

        fun get() = CopyToClipboardHandler(
            context = mockContext,
        )
    }
}
