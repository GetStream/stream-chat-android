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
 
package io.getstream.chat.android.offline

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope

/**
 * Test interface that helps to synchronize test coroutine scope and test scope. Use only when [runBlockingTest] is not
 * possible.
 */
@ExperimentalCoroutinesApi
internal interface SynchronizedCoroutineTest {

    /** Returns test scope. */
    fun getTestScope(): TestCoroutineScope

    /** Helper function that synchronize test scope and run your test in another scope blocking. */
    fun coroutineTest(block: suspend CoroutineScope.() -> Unit): Unit = runBlocking {
        getTestScope().launch(block = block).join()
    }
}
