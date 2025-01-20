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
import io.getstream.chat.android.client.api2.model.dto.UpstreamUserDto
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

internal class UpstreamUserDtoAdapterTest {
    private val parser = MoshiChatParser(
        EventMapping(
            DomainMapping(
                { "" },
                NoOpChannelTransformer,
                NoOpMessageTransformer,
            ),
        ),
        DtoMapping(
            NoOpMessageTransformer,
        ),
    )

    @Test
    fun `Serialize JSON user with custom fields`() {
        val jsonString = parser.toJson(UserDtoTestData.upstreamUser)
        jsonString shouldBeEqualTo UserDtoTestData.upstreamJson
    }

    @Test
    fun `Serialize JSON user without custom fields`() {
        val jsonString = parser.toJson(UserDtoTestData.upstreamUserWithoutExtraData)
        jsonString shouldBeEqualTo UserDtoTestData.upstreamJsonWithoutExtraData
    }

    @Test
    fun `Can't parse upstream user`() {
        invoking {
            parser.fromJson(UserDtoTestData.upstreamJson, UpstreamUserDto::class.java)
        }.shouldThrow(RuntimeException::class)
    }
}
