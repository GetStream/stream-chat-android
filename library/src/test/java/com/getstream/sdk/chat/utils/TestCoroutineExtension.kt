@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.getstream.sdk.chat.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class TestCoroutineExtension : BeforeAllCallback, AfterEachCallback, AfterAllCallback {

    val dispatcher = TestCoroutineDispatcher()
    val scope = TestCoroutineScope(dispatcher)

    override fun beforeAll(context: ExtensionContext) {
        Dispatchers.setMain(dispatcher)
    }

    override fun afterEach(context: ExtensionContext) {
        scope.cleanupTestCoroutines()
    }

    override fun afterAll(context: ExtensionContext) {
        Dispatchers.resetMain()
    }
}
