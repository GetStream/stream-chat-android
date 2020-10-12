package com.getstream.sdk.chat.utils

import com.getstream.sdk.chat.randomString
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class StartStopBufferTest {

    @Test
    fun `data should only be propagated when enqueueData and subscribe are done`() {
        StartStopBuffer<String>().run {
            val data = randomString()
            var resultText = ""

            enqueueData(data)
            active()
            subscribe {
                resultText = it
            }

            resultText `should be equal to` data
        }
    }

    @Test
    @Disabled("Flaky test because we are using `GloablScope.launch()` when enqueue new data")
    fun `data should not be propagated if hold is called before subscribe`() {
        StartStopBuffer<String>().run {
            val initialValue = randomString()
            val data = randomString()
            var resultText = initialValue

            enqueueData(data)
            hold()
            subscribe {
                resultText = it
            }

            resultText `should be equal to` initialValue
        }
    }
}
