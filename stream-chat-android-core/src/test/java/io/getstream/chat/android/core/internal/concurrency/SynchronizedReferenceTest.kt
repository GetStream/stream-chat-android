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

package io.getstream.chat.android.core.internal.concurrency

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class SynchronizedReferenceTest {

    @Test
    fun testGet() {
        val ref = SynchronizedReference(value = "sync")
        ref.get() `should be equal to` "sync"
    }

    @Test
    fun testGetOrCreateWhenInitialized() {
        val ref = SynchronizedReference(value = "sync")
        val value = ref.getOrCreate { "new" }
        value `should be equal to` "sync"
    }

    @Test
    fun testGetOrCreateWhenNotInitialized() {
        val ref = SynchronizedReference<String>()
        val value = ref.getOrCreate { "new" }
        value `should be equal to` "new"
    }

    @Test
    fun testReset() {
        val ref = SynchronizedReference(value = "sync")
        ref.reset() `should be equal to` true
    }

    @Test
    fun testSet() {
        val ref = SynchronizedReference<String>()
        ref.set("sync") `should be equal to` null
        ref.get() `should be equal to` "sync"
    }
}
