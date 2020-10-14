package com.getstream.sdk.chat.utils

import com.getstream.sdk.chat.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
class StartStopBufferTest {

    @JvmField
    @RegisterExtension
    val testCoroutines = TestCoroutineExtension()

    @Test
    fun `data should only be propagated when enqueueData and subscribe are done`() {
        StartStopBuffer<String>(testCoroutines.dispatcher).run {
            val data = randomString()
            var resultText = ""

            enqueueData(data)
            active()
            subscribe { result ->
                resultText = result
            }

            resultText `should be equal to` data
        }
    }

    @Test
    fun `data should not be propagated if hold is called before subscribe`() =
        testCoroutines.scope.runBlockingTest {
            StartStopBuffer<String>(testCoroutines.dispatcher).run {
                val initialValue = randomString()
                val data = randomString()
                var resultText = initialValue

                enqueueData(data)
                hold()
                subscribe { result ->
                    resultText = result
                }

                resultText `should be equal to` initialValue
            }
        }
}
