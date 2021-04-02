package io.getstream.chat.android.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.jvm.Throws

@Throws
public fun <T : Any?> Flow<T>.collectWithTimeout(timeoutInMillis: Long, consumer: (T) -> Unit) {
    runBlocking {
        val countDownLatch = CountDownLatch(1)
        flowOn(Dispatchers.IO)
            .collect { value ->
                countDownLatch.countDown()
                consumer(value)
            }

        if (!countDownLatch.await(timeoutInMillis, TimeUnit.MILLISECONDS)) {
            throw TimeoutException("Timeout! Value has not been sent")
        }
    }
}