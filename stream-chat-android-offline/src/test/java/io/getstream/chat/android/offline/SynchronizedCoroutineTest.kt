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
