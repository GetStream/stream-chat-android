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

package io.getstream.chat.android.models

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class NoOpChannelTransformerTest {

    @Test
    fun testNoOpChannelTransformer() {
        // given
        val channel = Channel(id = "cid1", type = "messaging")
        // when
        val transformed = NoOpChannelTransformer.transform(channel)
        // then
        transformed `should be equal to` channel
    }

    @Test
    fun testCustomChannelTransformed() {
        // given
        val channel = Channel(id = "cid1", type = "messaging", name = "channel")
        val customTransformer = ChannelTransformer { it.copy(name = "transformed") }
        // when
        val transformed = customTransformer.transform(channel)
        // then
        val expected = Channel(id = "cid1", type = "messaging", name = "transformed")
        transformed `should be equal to` expected
    }
}
