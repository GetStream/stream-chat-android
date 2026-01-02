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

package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelDto
import io.getstream.chat.android.client.parser2.testdata.ChannelDtoTestData
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

internal class DownstreamChannelDtoAdapterTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Deserialize JSON channel with custom fields`() {
        val channel = parser.fromJson(
            ChannelDtoTestData.downstreamJson,
            DownstreamChannelDto::class.java,
        )
        channel shouldBeEqualTo ChannelDtoTestData.downstreamChannel
    }

    @Test
    fun `Deserialize JSON channel without custom fields`() {
        val channel = parser.fromJson(
            ChannelDtoTestData.downstreamJsonWithoutExtraData,
            DownstreamChannelDto::class.java,
        )
        channel shouldBeEqualTo ChannelDtoTestData.downstreamChannelWithoutExtraData
    }

    @Test
    fun `Deserialize JSON channel without name and image fields`() {
        val channel = parser.fromJson(
            ChannelDtoTestData.downstreamJsonWithoutNameAndImage,
            DownstreamChannelDto::class.java,
        )
        channel shouldBeEqualTo ChannelDtoTestData.downstreamChannelWithoutNameAndImage
    }

    @Test
    fun `Can't serialize downstream dto`() {
        invoking {
            parser.toJson(ChannelDtoTestData.downstreamChannel)
        }.shouldThrow(RuntimeException::class)
    }
}
