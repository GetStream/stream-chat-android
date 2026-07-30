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

import io.getstream.chat.android.client.parser2.testdata.MessageAndReminderRequestTestData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class MessageAndReminderRequestAdapterTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Serialize UpdateMessagePartialRequest with all fields`() {
        val json = parser.toJson(MessageAndReminderRequestTestData.updateMessagePartialRequest)
        Assertions.assertEquals(MessageAndReminderRequestTestData.updateMessagePartialRequestJson, json)
    }

    @Test
    fun `Serialize UpdateMessagePartialRequest with default fields`() {
        val json = parser.toJson(MessageAndReminderRequestTestData.updateMessagePartialRequestWithDefaults)
        Assertions.assertEquals(MessageAndReminderRequestTestData.updateMessagePartialRequestJsonWithDefaults, json)
    }

    @Test
    fun `Serialize CreateReminderRequest with remind at`() {
        val json = parser.toJson(MessageAndReminderRequestTestData.createReminderRequest)
        Assertions.assertEquals(MessageAndReminderRequestTestData.createReminderRequestJson, json)
    }

    @Test
    fun `Serialize CreateReminderRequest without remind at`() {
        val json = parser.toJson(MessageAndReminderRequestTestData.createReminderRequestWithDefaults)
        Assertions.assertEquals(MessageAndReminderRequestTestData.createReminderRequestJsonWithDefaults, json)
    }

    @Test
    fun `Serialize UpdateReminderRequest with remind at`() {
        val json = parser.toJson(MessageAndReminderRequestTestData.updateReminderRequest)
        Assertions.assertEquals(MessageAndReminderRequestTestData.updateReminderRequestJson, json)
    }

    @Test
    fun `Serialize UpdateReminderRequest without remind at`() {
        val json = parser.toJson(MessageAndReminderRequestTestData.updateReminderRequestWithDefaults)
        Assertions.assertEquals(MessageAndReminderRequestTestData.updateReminderRequestJsonWithDefaults, json)
    }
}
