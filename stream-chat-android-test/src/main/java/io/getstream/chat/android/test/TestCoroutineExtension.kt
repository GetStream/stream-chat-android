@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.getstream.chat.android.test

import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

public class TestCoroutineExtension : BeforeAllCallback, AfterEachCallback, AfterAllCallback {

    public val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
    public val scope: TestCoroutineScope = TestCoroutineScope(dispatcher)

    private var beforeAllCalled: Boolean = false

    override fun beforeAll(context: ExtensionContext) {
        DispatcherProvider.set(
            mainDispatcher = dispatcher,
            ioDispatcher = dispatcher
        )
        beforeAllCalled = true
    }

    override fun afterEach(context: ExtensionContext) {
        check(beforeAllCalled) { "TestCoroutineExtension field must be static" }
        scope.cleanupTestCoroutines()
    }

    override fun afterAll(context: ExtensionContext) {
        DispatcherProvider.reset()
    }
}
