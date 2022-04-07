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

@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.getstream.chat.android.test

import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.rules.TestWatcher
import org.junit.runner.Description

public class TestCoroutineRule : TestWatcher() {

    public val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
    public val scope: TestCoroutineScope = TestCoroutineScope(dispatcher)

    override fun starting(description: Description) {
        super.starting(description)
        DispatcherProvider.set(
            mainDispatcher = dispatcher,
            ioDispatcher = dispatcher
        )
    }

    override fun finished(description: Description) {
        super.finished(description)
        DispatcherProvider.reset()
    }
}
