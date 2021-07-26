package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch

internal class CallTests {

    @get:Rule
    val testCoroutines = TestCoroutineRule()

    @Test
    fun `123`() = runBlocking {
        val call = CoroutineCall(testCoroutines.scope) {
            println("In coroutine call")
            Result.success(2)
        }

        call
            .doOnStart(testCoroutines.scope) {
                println("On start")
            }
            .doOnResult(testCoroutines.scope) {
                println("On result")
            }
            .enqueue {
                println("In enqueue")
            }

        delay(10000)
    }

    @Test
    fun `Should invoke methods in right order`() {
        val countDownLatch = CountDownLatch(1)
        val mutableList = mutableListOf<Int>()
        val scope = CoroutineScope(Dispatchers.IO)
        val call = CoroutineCall(scope) {
            mutableList.add(2)
            return@CoroutineCall Result.success(2)
        }

        call.doOnStart(scope) {
            mutableList.add(1)
        }
            .doOnResult(scope) {
                mutableList.add(3)
            }
            .enqueue {
                countDownLatch.countDown()
            }

        countDownLatch.await()

        Thread.sleep(10000)

        Assert.assertEquals(listOf(1, 2, 3), mutableList)
    }
}
