package io.getstream.chat.android.core.lazy

import io.getstream.chat.android.core.internal.lazy.ParameterizedLazy
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

internal class ParameterizedLazyTest {

    @Test
    fun `test parameterized lazy`() = runBlocking {
        val counter = AtomicInteger()
        val initializer: suspend (Int) -> String = {
            counter.incrementAndGet()
            "test$it"
        }
        val loadStringForNumber = ParameterizedLazy(initializer)
        val string1a = loadStringForNumber(1)
        val string1b = loadStringForNumber(1)
        val string2a = loadStringForNumber(2)
        val string2b = loadStringForNumber(2)

        counter.get() `should be equal to` 2
        Assertions.assertSame(string1a, string1b)
        Assertions.assertSame(string2a, string2b)
        Assertions.assertNotSame(string1a, string2b)
    }

}