@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.internal.DispatcherProvider
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

internal class TestCoroutineExtension : BeforeAllCallback, AfterEachCallback, AfterAllCallback {

    val dispatcher = TestCoroutineDispatcher()
    val scope = TestCoroutineScope(dispatcher)

    override fun beforeAll(context: ExtensionContext) {
        DispatcherProvider.set(
            mainDispatcher = dispatcher,
            ioDispatcher = dispatcher
        )
    }

    override fun afterEach(context: ExtensionContext) {
        scope.cleanupTestCoroutines()
    }

    override fun afterAll(context: ExtensionContext) {
        DispatcherProvider.reset()
    }
}
