/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.message.input.internal

import org.amshove.kluent.`should be`
import org.junit.jupiter.api.Test

internal class MessageTextValidatorTest {

    @Test
    fun `should not be possible to send empty giphys`() {
        val message = "/giphy"
        val result = MessageTextValidator.isMessageTextValid(message)

        result `should be` (false)
    }

    @Test
    fun `should not be possible to send blank giphys`() {
        val message = "/giphy      "
        val result = MessageTextValidator.isMessageTextValid(message)

        result `should be` (false)
    }

    @Test
    fun `should be possible to send giphys with content`() {
        val message = "/giphy hi"
        val result = MessageTextValidator.isMessageTextValid(message)

        result `should be` (true)
    }

    @Test
    fun `should be possible to send text`() {
        val message = "hi"
        val result = MessageTextValidator.isMessageTextValid(message)

        result `should be` (true)
    }
}
