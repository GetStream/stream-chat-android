package io.getstream.chat.android.client.utils

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.getstream.chat.android.client.errors.ChatError
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ResultTests {

    @Test
    fun `Should execute side effects for onSuccess and onSuccessSuspend`() = runBlockingTest {
        val action = mock<Action>()
        val suspendAction = mock<SuspendAction>()

        Result.success("123")
            .onSuccess { string -> action.doAction(string) }
            .onSuccessSuspend { string -> suspendAction.doAction(string) }

        verify(action).doAction("123")
        verify(suspendAction).doAction("123")
    }

    @Test
    fun `Should create corresponding types of Result from of`() {
        val success = Result.success("123")
        success `should be equal to result` Result.of("123")

        val exception = IllegalArgumentException("error")
        val errorByThrowable = Result.error<String>(exception)
        errorByThrowable `should be equal to result` Result.of(exception)

        val chatError = ChatError("123", exception)
        val errorByChatError = Result.error<String>(chatError)
        errorByChatError `should be equal to result` Result.of(chatError)

        val differentTypeOfData = 123
        val illegalArgumentException =
            assertThrows<IllegalArgumentException>("Should throw an exception") {
                Result.of<String>(differentTypeOfData)
            }
        assertEquals("Unexpected type of the data payload: $differentTypeOfData", illegalArgumentException.message)
    }

    @Test
    fun `Should create corresponding types of Result by from with lambda`() {
        val success = Result.success("123")
        success `should be equal to result` Result.of { "123" }

        val exception = IllegalArgumentException("error")
        val errorByThrowable = Result.error<String>(exception)
        errorByThrowable `should be equal to result` Result.of { exception }

        val chatError = ChatError("123", exception)
        val errorByChatError = Result.error<String>(chatError)
        errorByChatError `should be equal to result` Result.of { chatError }

        val differentTypeOfData = 123
        val illegalArgumentException =
            assertThrows<IllegalArgumentException>("Should throw an exception") {
                Result.of<String>(differentTypeOfData)
            }
        assertEquals("Unexpected type of the data payload: $differentTypeOfData", illegalArgumentException.message)
    }

    private interface Action {
        fun doAction(string: String)
    }

    private interface SuspendAction {
        suspend fun doAction(string: String)
    }
}
