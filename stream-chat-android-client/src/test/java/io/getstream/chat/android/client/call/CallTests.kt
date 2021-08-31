package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.test.TestCoroutineRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

internal class CallTests {
    @get:Rule
    val testCoroutines: TestCoroutineRule = TestCoroutineRule()

    @Test
    fun `Should invoke methods in right order`() {
        val mutableList = mutableListOf<Int>()

        CoroutineCall(testCoroutines.scope) {
            mutableList.add(2)
            Result.success(2)
        }
            .doOnStart(testCoroutines.scope) { mutableList.add(1) }
            .doOnResult(testCoroutines.scope) { mutableList.add(3) }
            .enqueue {
                mutableList.add(4)
            }

        Assert.assertEquals(listOf(1, 2, 3, 4), mutableList)
    }
}
