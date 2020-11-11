@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.internal.DispatcherProvider
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.rules.TestWatcher
import org.junit.runner.Description

internal class TestCoroutineRule : TestWatcher() {

    val dispatcher = TestCoroutineDispatcher()
    val scope = TestCoroutineScope(dispatcher)

    override fun starting(description: Description?) {
        super.starting(description)
        DispatcherProvider.set(
            mainDispatcher = dispatcher,
            ioDispatcher = dispatcher
        )
    }

    override fun finished(description: Description?) {
        super.finished(description)
        scope.cleanupTestCoroutines()
        DispatcherProvider.reset()
    }
}
