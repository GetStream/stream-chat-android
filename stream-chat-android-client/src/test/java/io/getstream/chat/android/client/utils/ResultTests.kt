/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.utils

import io.getstream.result.Result
import io.getstream.result.onSuccessSuspend
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
internal class ResultTests {
    @Test
    fun `Should execute side effects for onSuccess and onSuccessSuspend`() = runTest {
        val action = mock<Action>()
        val suspendAction = mock<SuspendAction>()

        Result.Success("123")
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
