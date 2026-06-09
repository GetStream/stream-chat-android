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

package io.getstream.chat.android.client.internal.offline.repository.database.converter

import io.getstream.chat.android.client.internal.offline.repository.database.converter.internal.UserGroupConverter
import io.getstream.chat.android.randomUserGroup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource

internal class UserGroupConverterTest {
    private val sut = UserGroupConverter()

    @Test
    fun `userGroupListToString with null returns null`() {
        assertNull(sut.userGroupListToString(null))
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = ["", "null"])
    fun `stringToUserGroupList with blank input returns empty list`(input: String?) {
        assertEquals(emptyList<Nothing>(), sut.stringToUserGroupList(input))
    }

    @Test
    fun `round-trip preserves user groups including dates`() {
        val groups = listOf(randomUserGroup(), randomUserGroup())

        val json = sut.userGroupListToString(groups)
        val converted = sut.stringToUserGroupList(json)

        assertEquals(groups, converted)
    }
}
