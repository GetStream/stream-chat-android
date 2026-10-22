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

@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.getstream.chat.android.test

import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.result.call.dispatcher.CallDispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Gives each test its own [dispatcher] and [scope], set as the main and IO dispatchers while the test runs.
 * The scope is cancelled after each test, so no coroutine or virtual time carries over to the next one.
 * Register it on a static field; [dispatcher] and [scope] are available from `beforeEach` until `afterEach`.
 */
public class TestCoroutineExtension : BeforeEachCallback, BeforeAllCallback, AfterEachCallback {

    private var _dispatcher: TestDispatcher? = null
    public val dispatcher: TestDispatcher
        get() = requireNotNull(_dispatcher)
    private var _scope: TestScope? = null
    public val scope: TestScope
        get() = requireNotNull(_scope)
    private var beforeAllCalled: Boolean = false

    override fun beforeAll(context: ExtensionContext) {
        TestLoggingHelper.initialize()
        beforeAllCalled = true
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun beforeEach(context: ExtensionContext?) {
        val dispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
        _dispatcher = dispatcher
        _scope = TestScope(dispatcher)
        Dispatchers.setMain(dispatcher)
        DispatcherProvider.set(
            mainDispatcher = dispatcher,
            ioDispatcher = dispatcher,
        )
        CallDispatcherProvider.set(
            mainDispatcher = dispatcher,
            ioDispatcher = dispatcher,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun afterEach(context: ExtensionContext) {
        check(beforeAllCalled) { "TestCoroutineExtension field must be static" }
        scope.cancel()
        Dispatchers.resetMain()
        DispatcherProvider.reset()
        CallDispatcherProvider.reset()
        _scope = null
        _dispatcher = null
    }
}
