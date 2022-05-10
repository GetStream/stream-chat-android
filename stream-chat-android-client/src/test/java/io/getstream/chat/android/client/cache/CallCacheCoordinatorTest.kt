/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.cache

import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomString
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.Test

internal class CallCacheCoordinatorTest {

    private val cacheCoordinator = CallCacheCoordinator(300)

    @Test
    fun `Given the same hash code, the cached call result should not change if forceRefresh false`() {
        val testCall = TestCall(Result(randomString()))
        val testCall2 = TestCall(Result(randomString()))
        val hashCode = randomInt()

        cacheCoordinator.cachedCall(hashCode, forceRefresh = false, testCall)

        repeat(5) {
            val cachedCall = cacheCoordinator.cachedCall(hashCode, forceRefresh = false, testCall2)

            cachedCall `should be` testCall
        }
    }

    @Test
    fun `Given the same hash code, the cached call result should change if forceRefresh true`() {
        val testCall = TestCall(Result(randomString()))
        val testCall2 = TestCall(Result(randomString()))
        val hashCode = randomInt()

        cacheCoordinator.cachedCall(hashCode, forceRefresh = true, testCall)

        repeat(5) {
            val cachedCall = cacheCoordinator.cachedCall(hashCode, forceRefresh = true, testCall2)

            cachedCall `should be` testCall2
        }
    }

    @Test
    fun `Given the same hash code, the cached call result should not with many requests`() {
        val testCall = TestCall(Result(randomString()))
        val testCall2 = TestCall(Result(randomString()))
        val testCall3 = TestCall(Result(randomString()))

        val hashCode = randomInt()
        val hashCode2 = randomInt()
        val hashCode3 = randomInt()

        val ignoredCall = TestCall(Result(randomString()))
        val ignoredCall2 = TestCall(Result(randomString()))
        val ignoredCall3 = TestCall(Result(randomString()))

        cacheCoordinator.cachedCall(hashCode, forceRefresh = true, testCall)
        cacheCoordinator.cachedCall(hashCode2, forceRefresh = true, testCall2)
        cacheCoordinator.cachedCall(hashCode3, forceRefresh = true, testCall3)

        repeat(5) {
            val cachedCall = cacheCoordinator.cachedCall(hashCode, forceRefresh = false, ignoredCall)
            val cachedCall2 = cacheCoordinator.cachedCall(hashCode2, forceRefresh = false, ignoredCall2)
            val cachedCall3 = cacheCoordinator.cachedCall(hashCode3, forceRefresh = false, ignoredCall3)

            cachedCall `should be` testCall
            cachedCall2 `should be` testCall2
            cachedCall3 `should be` testCall3
        }
    }
}
