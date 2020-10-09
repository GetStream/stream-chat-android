package com.getstream.sdk.chat.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class StartStopBufferTest {

    @Test
    fun `data should only be propagated when enqueueData and subscribe are done`() {
        StartStopBuffer<String>().run {
            var shouldFail = true

            enqueueData("this is data, I promise")
            active()
            subscribe {
                shouldFail = false
            }

            assertFalse(shouldFail)
        }
    }

    @Test
    fun `data should not be propagated if hold is called before subscribe`() {
        StartStopBuffer<String>().run {
            var shouldFail = false

            enqueueData("this is data, I promise")
            hold()
            subscribe {
                shouldFail = true
            }

            assertFalse(shouldFail)
        }
    }
}
