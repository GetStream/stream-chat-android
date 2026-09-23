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

import io.getstream.chat.android.client.parser2.testdata.MemberInfoDtoTestData
import io.getstream.chat.android.network.models.ChannelMemberPartialResponse
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class ChannelMemberPartialResponseAdapterTest {

    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Deserialize JSON member info`() {
        val member = parser.fromJson(MemberInfoDtoTestData.downstreamJson, ChannelMemberPartialResponse::class.java)
        member shouldBeEqualTo MemberInfoDtoTestData.downstreamMemberInfo
    }

    @Test
    fun `Deserialize JSON member info with member custom inlined by API v1`() {
        val member = parser.fromJson(
            MemberInfoDtoTestData.downstreamJsonWithInlineCustom,
            ChannelMemberPartialResponse::class.java,
        )
        member shouldBeEqualTo MemberInfoDtoTestData.downstreamMemberInfoWithInlineCustom
    }

    @Test
    fun `Deserialize JSON member info with member custom nested by API v2`() {
        val member = parser.fromJson(
            MemberInfoDtoTestData.downstreamJsonWithNestedCustom,
            ChannelMemberPartialResponse::class.java,
        )
        member shouldBeEqualTo MemberInfoDtoTestData.downstreamMemberInfoWithNestedCustom
    }
}
