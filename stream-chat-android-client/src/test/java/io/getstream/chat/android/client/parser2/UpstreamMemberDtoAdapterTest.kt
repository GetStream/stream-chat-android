/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.client.parser2.testdata.MemberDtoTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class UpstreamMemberDtoAdapterTest {

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
    fun `Serialize JSON member with custom data`() {
        val json = parser.toJson(MemberDtoTestData.upstreamMemberWithExtraData)
        json shouldBeEqualTo MemberDtoTestData.upstreamJsonWithExtraData
    }

    @Test
    fun `Serialize JSON member without custom data`() {
        val json = parser.toJson(MemberDtoTestData.upstreamMemberWithoutExtraData)
        json shouldBeEqualTo MemberDtoTestData.upstreamJsonWithoutExtraData
    }
}
