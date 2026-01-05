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

package io.getstream.chat.android.uitests.ui.util

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.idling.CountingIdlingResource
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.coroutines.CoroutineContext

/**
 * A Junit Test Rule that helps to synchronize Espresso tests with the work that is
 * being done on the IO dispatcher.
 */
internal class CoroutineTaskExecutorRule : TestWatcher() {

    /**
     * The wrapped IO dispatcher
     */
    private val dispatcher = DelegatingIdlingResourceDispatcher(Dispatchers.IO)

    /**
     * Invoked when a test is about to start.
     */
    @OptIn(InternalStreamChatApi::class)
    override fun starting(description: Description) {
        super.starting(description)
        DispatcherProvider.set(
            mainDispatcher = Dispatchers.Main.immediate,
            ioDispatcher = dispatcher,
        )
    }

    /**
     * Invoked when a test method finishes (whether passing or failing).
     */
    @OptIn(InternalStreamChatApi::class)
    override fun finished(description: Description) {
        super.finished(description)
        DispatcherProvider.reset()
        dispatcher.reset()
    }
}

/**
 * A [CoroutineDispatcher] implementation that wraps a delegate [CoroutineDispatcher] and
 * helps Espresso to synchronize test execution with the jobs that are being executed on
 * the delegate dispatcher.
 *
 * @property delegate The inner dispatcher that will be wrapped.
 */
private class DelegatingIdlingResourceDispatcher(
    private val delegate: CoroutineDispatcher,
) : CoroutineDispatcher() {

    /**
     * An [IdlingResource] responsible for tracking jobs that are being executed on
     * the [delegate] dispatcher.
     */
    private val countingIdlingResource: CountingIdlingResource =
        CountingIdlingResource("DelegatingIdlingResourceDispatcher for $delegate")

    init {
        IdlingRegistry.getInstance().register(countingIdlingResource)
    }

    /**
     * Dispatches execution of a runnable [block] onto another thread in the given [context].
     * Also tracks running jobs with the help of [countingIdlingResource], so that Espresso
     * is able to do necessary synchronization during test execution.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        countingIdlingResource.increment()

        val wrappedBlock = Runnable {
            try {
                block.run()
            } finally {
                countingIdlingResource.decrement()
            }
        }
        delegate.dispatch(context, wrappedBlock)
    }

    /**
     * Unregisters the [IdlingResource] that was used to track ongoing jobs
     * executed on the [delegate] dispatcher.
     */
    fun reset() {
        IdlingRegistry.getInstance().unregister(countingIdlingResource)
    }
}
