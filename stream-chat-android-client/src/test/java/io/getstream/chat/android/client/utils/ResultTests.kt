package io.getstream.chat.android.client.utils

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test

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

    private interface Action {
        fun doAction(string: String)
    }

    private interface SuspendAction {
        suspend fun doAction(string: String)
    }
}
