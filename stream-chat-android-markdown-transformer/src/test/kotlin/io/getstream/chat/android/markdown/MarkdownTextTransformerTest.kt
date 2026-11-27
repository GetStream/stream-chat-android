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

package io.getstream.chat.android.markdown

import android.widget.TextView
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
internal class MarkdownTextTransformerTest {

    @Test
    fun `an image shows as its alt text`() {
        assertEquals("before a cat after", render("before ![a cat](https://example.com/cat.png) after"))
    }

    @Test
    fun `an image without alt text shows as its link`() {
        assertEquals("https://example.com/cat.png", render("![](https://example.com/cat.png)"))
    }

    private fun render(text: String): String {
        val context = RuntimeEnvironment.getApplication()
        val textView = TextView(context)
        val item = MessageListItem.MessageItem(message = Message(text = text))

        MarkdownTextTransformer(context).transformAndApply(textView, item)

        return textView.text.toString()
    }
}
