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

package io.getstream.chat.android.models.querysort

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class FieldSearcherTest {

    /**
     * Test subject class.
     *
     * @property comparableField a comparable field.
     */
    data class TestSubject(val comparableField: String)

    @Test
    fun `Should find comparable camel case member property`() {
        // Given
        val fieldName = "comparableField"
        val kClass = TestSubject::class

        // When
        val result = FieldSearcher().findComparableMemberProperty(fieldName, kClass)

        // Then
        Assertions.assertNotNull(result)
    }

    @Test
    fun `Should find comparable snake case member property`() {
        // Given
        val fieldName = "comparable_field"
        val kClass = TestSubject::class

        // When
        val result = FieldSearcher().findComparableMemberProperty(fieldName, kClass)

        // Then
        Assertions.assertNotNull(result)
    }

    @Test
    fun `Should not find comparable camel case member property`() {
        // Given
        val fieldName = "unknownField"
        val kClass = TestSubject::class

        // When
        val result = FieldSearcher().findComparableMemberProperty(fieldName, kClass)

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `Should not find comparable snake case member property`() {
        // Given
        val fieldName = "unknown_field"
        val kClass = TestSubject::class

        // When
        val result = FieldSearcher().findComparableMemberProperty(fieldName, kClass)

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `Should find comparable camel case`() {
        // Given
        val any = TestSubject("test")
        val fieldName = "comparableField"

        // When
        val result = FieldSearcher().findComparable(any, fieldName)

        // Then
        Assertions.assertNotNull(result)
    }

    @Test
    fun `Should find comparable snake case`() {
        // Given
        val any = TestSubject("test")
        val fieldName = "comparable_field"

        // When
        val result = FieldSearcher().findComparable(any, fieldName)

        // Then
        Assertions.assertNotNull(result)
    }

    @Test
    fun `Should not find comparable camel case`() {
        // Given
        val any = TestSubject("test")
        val fieldName = "unknownField"

        // When
        val result = FieldSearcher().findComparable(any, fieldName)

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `Should not find comparable snake case`() {
        // Given
        val any = TestSubject("test")
        val fieldName = "unknown_field"

        // When
        val result = FieldSearcher().findComparable(any, fieldName)

        // Then
        Assertions.assertNull(result)
    }
}
