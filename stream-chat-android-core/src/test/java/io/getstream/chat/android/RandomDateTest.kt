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

package io.getstream.chat.android

import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test
import java.util.Date

/**
 * The two properties the rest of the suite leans on. Because the generator is random, breaking either surfaces as an
 * unrelated test failing in a fraction of CI runs, far from the cause, rather than as a failure here.
 */
internal class RandomDateTest {

    @Test
    fun `a generated date is always in the future`() {
        // Production code that holds on to future dates only, live locations for one, drops a fixture date that
        // sits behind the clock, leaving the test asserting on an empty result.
        val now = Date()

        val past = List(ROUNDS) { randomDate() }.filter { it.before(now) }

        past.firstOrNull().shouldBeNull()
    }

    @Test
    fun `a generated date is always serializable`() {
        // The ISO-8601 formatter writes a four digit year, so a later date is written as a different date, or as one
        // that cannot be read back at all.
        val lastSerializable = Date(LAST_SERIALIZABLE_DATE_MILLIS)

        val unserializable = List(ROUNDS) { randomDate() }.filter { it.after(lastSerializable) }

        unserializable.firstOrNull().shouldBeNull()
    }

    @Test
    fun `a date generated after another one is serializable too`() {
        val lastSerializable = Date(LAST_SERIALIZABLE_DATE_MILLIS)

        val unserializable = List(ROUNDS) { randomDateAfter(randomDate()) }.filter { it.after(lastSerializable) }

        unserializable.firstOrNull().shouldBeNull()
    }

    private companion object {
        const val ROUNDS = 100_000
        const val LAST_SERIALIZABLE_DATE_MILLIS = 253_402_300_799_999L // 9999-12-31T23:59:59.999Z
    }
}
