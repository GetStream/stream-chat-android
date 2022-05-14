package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
internal class CacheAwareCallTest {

    private val scope = TestScope()

    @Test
    fun `given a call was called many times in a roll it should be enqueued only once when finished`() {
        var hasFinished = false

        val mockedCall = mock<Call<String>>()
        val mockedCallWithClone = mock<Call<String>> {
            on(it.clone()) doReturn mockedCall
            on(it.enqueue(any())) doAnswer { invocation ->
                hasFinished = true
                invocation.getArgument<Call.Callback<String>>(0).onResult(Result(""))
            }
        }

        val call = CacheAwareCall(
            mockedCallWithClone,
            System.currentTimeMillis(),
            100,
            mutableListOf()
        )

        repeat(3) {
            call.enqueue()
        }

        verify(mockedCallWithClone, times(1)).enqueue(any())
        verify(mockedCall, never()).enqueue(any())
        assertTrue(hasFinished)
    }

    @Test
    fun `given a call was called many times in a roll it should be enqueued only once when running`() {
        var hasFinished = false

        val mockedCall = mock<Call<String>>()
        val mockedCallWithClone = mock<Call<String>> {
            on(it.clone()) doReturn mockedCall
            on(it.enqueue(any())) doAnswer { invocation ->
                scope.launch {
                    delay(3000)
                    hasFinished = true
                    invocation.getArgument<Call.Callback<String>>(0).onResult(Result(""))
                }

                Unit
            }
        }

        val call = CacheAwareCall(
            mockedCallWithClone,
            System.currentTimeMillis(),
            100,
            mutableListOf()
        )

        repeat(3) {
            call.enqueue()
        }

        verify(mockedCallWithClone, times(1)).enqueue(any())
        verify(mockedCall, never()).enqueue(any())
        assertFalse(hasFinished)
    }

    @Test
    fun `given a call was called many times in a roll it should be enqueued only once even if it has finished`() {
        var hasFinished = false

        val mockedCall = mock<Call<String>>()
        val mockedCallWithClone = mock<Call<String>> {
            on(it.clone()) doReturn mockedCall
            on(it.enqueue(any())) doAnswer { invocation ->
                hasFinished = true
                invocation.getArgument<Call.Callback<String>>(0).onResult(Result(""))
            }
        }

        val call = CacheAwareCall(
            mockedCallWithClone,
            System.currentTimeMillis(),
            100,
            mutableListOf()
        )

        repeat(3) {
            call.enqueue()
        }

        verify(mockedCallWithClone, times(1)).enqueue(any())
        verify(mockedCall, never()).enqueue(any())
        assertTrue(hasFinished)
    }

    @Test
    fun `given a call is called after cache is expired, new request to api should be made`() {
        val mockedCall = mock<Call<String>>()
        val mockedCallWithClone = mock<Call<String>> {
            on(it.clone()) doReturn mockedCall
            on(it.enqueue(any())) doAnswer { invocation ->
                invocation.getArgument<Call.Callback<String>>(0).onResult(Result(""))
            }
        }

        val call = CacheAwareCall(
            mockedCallWithClone,
            System.currentTimeMillis(),
            100,
            mutableListOf()
        )

        repeat(3) {
            call.enqueue()
            Thread.sleep(200)
        }

        verify(mockedCallWithClone, times(1)).enqueue(any())
        verify(mockedCall, times(2)).enqueue(any())
    }

    @Test
    fun `given a call is called after cache is expired, new request to api should be made even when not completed`() {
        var hasFinished = false

        val mockedCall = mock<Call<String>>()
        val mockedCallWithClone = mock<Call<String>> {
            on(it.clone()) doReturn mockedCall
            on(it.enqueue(any())) doAnswer { invocation ->
                scope.launch {
                    delay(3000)
                    hasFinished = true
                    invocation.getArgument<Call.Callback<String>>(0).onResult(Result(""))
                }

                Unit
            }
        }

        val call = CacheAwareCall(
            mockedCallWithClone,
            System.currentTimeMillis(),
            100,
            mutableListOf()
        )

        repeat(3) {
            call.enqueue()
            Thread.sleep(200)
        }

        verify(mockedCallWithClone, times(1)).enqueue(any())
        verify(mockedCall, times(2)).enqueue(any())
        assertFalse(hasFinished)
    }
}
