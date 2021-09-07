package io.getstream.chat.android.client.utils

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

internal class ThreadLocalDelegateTest {

    private val delegate by threadLocal { Object() }

    @Test
    fun `Should create separate instance for each thread`() {
        val results = ConcurrentHashMap<Thread, Any>()

        (1..100)
            .map { thread { results.put(Thread.currentThread(), delegate) } }
            .forEach(Thread::join)

        val delegateValues = results.values
        delegateValues.size shouldBeEqualTo 100
        delegateValues.distinct().size shouldBeEqualTo 100
    }

    @Test
    fun `Should reuse same instance for a given thread`() {
        val results = mutableListOf<Any>()

        repeat(100) {
            results += delegate
        }

        results.size shouldBeEqualTo 100
        results.distinct().size shouldBeEqualTo 1
    }
}
