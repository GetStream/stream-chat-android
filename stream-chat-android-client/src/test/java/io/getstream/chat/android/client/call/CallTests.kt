package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
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

        mutableList shouldBeEqualTo listOf(1, 2, 3, 4)
    }

    @Test
    fun `Should invoke methods in right order with precondition`() {
        val mutableList = mutableListOf<Int>()

        CoroutineCall(testCoroutines.scope) {
            mutableList.add(2)
            Result.success(2)
        }
            .doOnStart(testCoroutines.scope) { mutableList.add(1) }
            .doOnResult(testCoroutines.scope) { mutableList.add(3) }
            .withPrecondition(testCoroutines.scope) {
                mutableList.add(0)
                Result.success(Unit)
            }
            .enqueue {
                mutableList.add(4)
            }

        mutableList shouldBeEqualTo listOf(0, 1, 2, 3, 4)
    }

    @Test
    fun `Should return from onErrorReturn when original call gives error`() {
        runBlockingTest {
            val result = CoroutineCall(testCoroutines.scope) {
                Result(ChatError("Test error"))
            }.onErrorReturn(testCoroutines.scope) {
                Result(listOf(0, 1))
            }.await()
            result shouldBeEqualTo Result(listOf(0, 1))
        }
    }

    @Test
    fun `Should not return from onErrorReturn when original call gives success`() {
        runBlockingTest {
            val result = CoroutineCall(testCoroutines.scope) {
                Result(listOf(10, 20, 30))
            }.onErrorReturn(testCoroutines.scope) {
                Result(listOf(0, 1))
            }.await()
            result shouldBeEqualTo Result(listOf(10, 20, 30))
        }
    }
}
