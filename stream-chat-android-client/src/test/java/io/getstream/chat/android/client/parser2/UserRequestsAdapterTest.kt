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

import io.getstream.chat.android.client.parser2.testdata.UserRequestsTestData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class UserRequestsAdapterTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Serialize UpdateUsersPartialRequest`() {
        val json = parser.toJson(UserRequestsTestData.updateUsersPartialRequest)
        Assertions.assertEquals(UserRequestsTestData.updateUsersPartialRequestJson, json)
    }

    @Test
    fun `Serialize CreateGuestRequest with custom fields`() {
        val json = parser.toJson(UserRequestsTestData.createGuestRequest)
        Assertions.assertEquals(UserRequestsTestData.createGuestRequestJson, json)
    }

    @Test
    fun `Serialize CreateGuestRequest without custom fields`() {
        val json = parser.toJson(UserRequestsTestData.createGuestRequestWithoutCustom)
        Assertions.assertEquals(UserRequestsTestData.createGuestRequestJsonWithoutCustom, json)
    }

    @Test
    fun `Serialize UpdateUsersRequest`() {
        val json = parser.toJson(UserRequestsTestData.updateUsersRequest)
        Assertions.assertEquals(UserRequestsTestData.updateUsersRequestJson, json)
    }
}
