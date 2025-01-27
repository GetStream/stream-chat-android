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

package io.getstream.chat.android.core.internal.coroutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class TubeTest {

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
    private val scope = TestScope(testDispatcher)

    @Test
    fun testTubeCollectionAndEmission() = runTest {
        // given
        var collected1 = false
        var collected2 = false

        val tube = Tube<Unit>()
        scope.launch {
            tube.collect {
                collected1 = true
            }
        }
        scope.launch {
            tube.collect {
                collected2 = true
            }
        }
        // when
        tube.emit(Unit)
        // then
        collected1 `should be equal to` true
        collected2 `should be equal to` true
        // cleanup
        scope.cancel()
    }
}
