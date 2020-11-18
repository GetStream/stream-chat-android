@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.getstream.chat.test

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
        scope.cleanupTestCoroutines()
        DispatcherProvider.reset()
    }
}
