@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.getstream.chat.android.test

import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

public class TestCoroutineRule : TestWatcher() {

    public val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
    public val scope: TestScope = TestScope(dispatcher)

    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
        DispatcherProvider.set(
            mainDispatcher = dispatcher,
            ioDispatcher = dispatcher
        )
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
        DispatcherProvider.reset()
    }
}
