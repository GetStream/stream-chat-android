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

import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto
import io.getstream.chat.android.client.parser2.testdata.ReactionDtoTestData
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

internal class UpstreamReactionDtoAdapterTest {

    private val parser = MoshiChatParser()

    @Test
    fun `Serialize JSON reaction with custom fields`() {
        val jsonString = parser.toJson(ReactionDtoTestData.upstreamReaction)
        jsonString shouldBeEqualTo ReactionDtoTestData.upstreamJson
    }

    @Test
    fun `Serialize JSON reaction without custom fields`() {
        val jsonString = parser.toJson(ReactionDtoTestData.upstreamReactionWithoutExtraData)
        jsonString shouldBeEqualTo ReactionDtoTestData.upstreamJsonWithoutExtraData
    }

    @Test
    fun `Can't parse upstream reaction`() {
        invoking {
            parser.fromJson(ReactionDtoTestData.upstreamJson, DownstreamReactionDto::class.java)
        }.shouldThrow(RuntimeException::class)
    }
}
