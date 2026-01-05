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

package io.getstream.chat.android.client.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

/**
 * Test interface that helps to synchronize test coroutine scope and test scope. Use only when [runTest] is not
 * possible.
 */
@ExperimentalCoroutinesApi
public interface SynchronizedCoroutineTest {

    /** Returns test scope. */
    public fun getTestScope(): TestScope

    /** Helper function that synchronize test scope and run your test in another scope blocking. */
    public fun coroutineTest(block: suspend CoroutineScope.() -> Unit): Unit = runTest {
        getTestScope().launch(block = block).join()
    }
}
