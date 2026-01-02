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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.ChatEventDto
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class EventMappingTest {

    private val eventMapping = EventMapping(
        domainMapping = DomainMapping(
            currentUserIdProvider = { "" },
            channelTransformer = NoOpChannelTransformer,
            messageTransformer = NoOpMessageTransformer,
            userTransformer = NoOpUserTransformer,
        ),
    )

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.api2.mapping.EventMappingTestArguments#arguments")
    fun testChatEventMapping(dto: ChatEventDto, expected: ChatEvent) {
        val actual = with(eventMapping) {
            dto.toDomain()
        }
        assertEquals(expected, actual)
    }
}
