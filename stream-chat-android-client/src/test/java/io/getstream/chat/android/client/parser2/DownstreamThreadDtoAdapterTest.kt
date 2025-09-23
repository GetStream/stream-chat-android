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

import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadDto
import io.getstream.chat.android.client.parser2.testdata.ThreadDtoTestData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class DownstreamThreadDtoAdapterTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Deserialize JSON thread with custom fields`() {
        val thread = parser.fromJson(
            ThreadDtoTestData.downstreamThreadJson,
            DownstreamThreadDto::class.java,
        )
        Assertions.assertEquals(ThreadDtoTestData.downstreamThread, thread)
    }

    @Test
    fun `Deserialize JSON thread without custom fields`() {
        val thread = parser.fromJson(
            ThreadDtoTestData.downstreamThreadJsonWithoutExtraData,
            DownstreamThreadDto::class.java,
        )
        Assertions.assertEquals(ThreadDtoTestData.downstreamThreadWithoutExtraData, thread)
    }
}
