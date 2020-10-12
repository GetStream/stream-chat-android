package com.getstream.sdk.chat.utils

import com.getstream.sdk.chat.randomString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.`should be equal to`
import org.junit.After
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class StartStopBufferTest {

    private val testCoroutineDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testCoroutineDispatcher)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testCoroutineDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testCoroutineScope.cleanupTestCoroutines()
    }

    @Test
    fun `data should only be propagated when enqueueData and subscribe are done`() {
        StartStopBuffer<String>(testCoroutineDispatcher).run {
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
    fun `data should not be propagated if hold is called before subscribe`() = testCoroutineScope.runBlockingTest {
        StartStopBuffer<String>(testCoroutineDispatcher).run {
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
