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

package io.getstream.chat.android.models

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class MessageTransformerTest {

    @Test
    fun testNoOpMessageTransformer() {
        // given
        val message = Message(id = "mid1", text = "message")
        // when
        val transformed = NoOpMessageTransformer.transform(message)
        // then
        transformed `should be equal to` message
    }

    @Test
    fun testCustomMessageTransformed() {
        // given
        val message = Message(id = "mid1", text = "message")
        val customTransformer = MessageTransformer { it.copy(text = "transformed") }
        // when
        val transformed = customTransformer.transform(message)
        // then
        val expected = Message(id = "mid1", text = "transformed")
        transformed `should be equal to` expected
    }
}
