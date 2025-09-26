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

package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadInfoDto
import io.getstream.chat.android.client.parser2.testdata.ThreadDtoTestData
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class DownstreamThreadInfoDtoAdapterTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Deserialize JSON thread info with custom fields`() {
        val threadInfo = parser.fromJson(
            ThreadDtoTestData.downstreamThreadInfoJson,
            DownstreamThreadInfoDto::class.java,
        )
        threadInfo shouldBeEqualTo ThreadDtoTestData.downstreamThreadInfo
    }

    @Test
    fun `Deserialize JSON thread info without custom fields`() {
        val threadInfo = parser.fromJson(
            ThreadDtoTestData.downstreamThreadInfoJsonWithoutExtraData,
            DownstreamThreadInfoDto::class.java,
        )
        threadInfo shouldBeEqualTo ThreadDtoTestData.downstreamThreadInfoWithoutExtraData
    }
}
