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

package io.getstream.chat.android.extensions

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class StringExtensionsTest {

    @Test
    fun testStringExtensionsSnakeToLowerCamelCase() {
        val text = "created_at_some_time"
        val expected = "createdAtSomeTime"
        text.snakeToLowerCamelCase() `should be equal to` expected
    }

    @Test
    fun testStringExtensionsLowerCamelCaseToGetter() {
        val text = "cammelCase"
        val expected = "getCammelCase"
        text.lowerCamelCaseToGetter() `should be equal to` expected
    }

    @Test
    fun testStringExtensionsCamelCaseToSnakeCase() {
        val text = "createdAtSomeTime"
        val expected = "created_at_some_time"
        text.camelCaseToSnakeCase() `should be equal to` expected
    }
}
