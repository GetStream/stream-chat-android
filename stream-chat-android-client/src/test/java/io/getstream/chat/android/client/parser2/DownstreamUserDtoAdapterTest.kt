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
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData.downstreamJson
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData.downstreamJsonWithoutExtraData
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData.downstreamJsonWithoutImageAndName
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData.downstreamUser
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData.downstreamUserWithoutExtraData
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData.downstreamUserWithoutImageAndName
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

internal class DownstreamUserDtoAdapterTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Deserialize JSON user without custom fields`() {
        val user = parser.fromJson(downstreamJsonWithoutExtraData, DownstreamUserDto::class.java)
        user shouldBeEqualTo downstreamUserWithoutExtraData
    }

    @Test
    fun `Deserialize JSON user without image and name fields`() {
        val user = parser.fromJson(downstreamJsonWithoutImageAndName, DownstreamUserDto::class.java)
        user shouldBeEqualTo downstreamUserWithoutImageAndName
    }

    @Test
    fun `Deserialize JSON user with custom fields`() {
        val user = parser.fromJson(downstreamJson, DownstreamUserDto::class.java)
        user shouldBeEqualTo downstreamUser
    }

    @Test
    fun `Can't serialize downstream dto`() {
        invoking {
            parser.toJson(downstreamUser)
        }.shouldThrow(RuntimeException::class)
    }
}
