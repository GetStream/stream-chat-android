package io.getstream.chat.android.client.utils

import com.google.common.truth.Truth.assertThat
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
        assertThat(delegateValues.size).isEqualTo(100)
        assertThat(delegateValues.distinct().size).isEqualTo(100)
    }

    @Test
    fun `Should reuse same instance for a given thread`() {
        val results = mutableListOf<Any>()

        (1..100).forEach {
            results += delegate
        }

        assertThat(results.size).isEqualTo(100)
        assertThat(results.distinct().size).isEqualTo(1)
    }
}
