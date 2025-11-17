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

package io.getstream.chat.android.client.parser2.testdata

import com.squareup.moshi.Moshi
import org.junit.jupiter.api.Test

internal class DtoTestDataTest {

    @Test
    fun `Check test data is valid JSON`() {
        val moshi = Moshi.Builder().build()
        val anyAdapter = moshi.adapter(Any::class.java)

        anyAdapter.fromJson(MessageDtoTestData.downstreamJson)
        anyAdapter.fromJson(MessageDtoTestData.downstreamJsonWithoutExtraData)
        anyAdapter.fromJson(MessageDtoTestData.upstreamJson)
        anyAdapter.fromJson(MessageDtoTestData.upstreamJsonWithoutExtraData)

        anyAdapter.fromJson(ReactionDtoTestData.downstreamJson)
        anyAdapter.fromJson(ReactionDtoTestData.downstreamJsonWithoutExtraData)
        anyAdapter.fromJson(ReactionDtoTestData.upstreamJson)
        anyAdapter.fromJson(ReactionDtoTestData.upstreamJsonWithoutExtraData)

        anyAdapter.fromJson(UserDtoTestData.downstreamJson)
        anyAdapter.fromJson(UserDtoTestData.downstreamJsonWithoutExtraData)
        anyAdapter.fromJson(UserDtoTestData.upstreamJson)
        anyAdapter.fromJson(UserDtoTestData.upstreamJsonWithoutExtraData)

        anyAdapter.fromJson(ChannelDtoTestData.configJson)
        anyAdapter.fromJson(ChannelDtoTestData.downstreamJson)
        anyAdapter.fromJson(ChannelDtoTestData.downstreamJsonWithoutExtraData)

        anyAdapter.fromJson(AttachmentDtoTestData.json)
        anyAdapter.fromJson(AttachmentDtoTestData.jsonWithoutExtraData)
    }
}
