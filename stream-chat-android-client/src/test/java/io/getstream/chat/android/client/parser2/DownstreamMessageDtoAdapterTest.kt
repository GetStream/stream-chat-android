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

package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.api2.mapping.EventMapping
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.parser2.testdata.MessageDtoTestData.downstreamJson
import io.getstream.chat.android.client.parser2.testdata.MessageDtoTestData.downstreamJsonWithChannelInfo
import io.getstream.chat.android.client.parser2.testdata.MessageDtoTestData.downstreamJsonWithoutExtraData
import io.getstream.chat.android.client.parser2.testdata.MessageDtoTestData.downstreamMessage
import io.getstream.chat.android.client.parser2.testdata.MessageDtoTestData.downstreamMessageWithChannelInfo
import io.getstream.chat.android.client.parser2.testdata.MessageDtoTestData.downstreamMessageWithoutExtraData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

internal class DownstreamMessageDtoAdapterTest {
    private val parser = MoshiChatParser(
        EventMapping(
            DomainMapping(
                { "" },
                NoOpChannelTransformer,
                NoOpMessageTransformer,
            )
        ),
        DtoMapping(
            NoOpMessageTransformer
        ),
    )

    @Test
    fun `Deserialize JSON message with custom fields`() {
        val message = parser.fromJson(downstreamJson, DownstreamMessageDto::class.java)
        message shouldBeEqualTo downstreamMessage
    }

    @Test
    fun `Deserialize JSON message with channel info`() {
        val message = parser.fromJson(downstreamJsonWithChannelInfo, DownstreamMessageDto::class.java)
        message shouldBeEqualTo downstreamMessageWithChannelInfo
    }

    @Test
    fun `Deserialize JSON message without custom fields`() {
        val message = parser.fromJson(downstreamJsonWithoutExtraData, DownstreamMessageDto::class.java)
        message shouldBeEqualTo downstreamMessageWithoutExtraData
    }

    @Test
    fun `Can't serialize downstream dto`() {
        invoking {
            parser.toJson(downstreamMessage)
        }.shouldThrow(RuntimeException::class)
    }
}
