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

package io.getstream.chat.android.ai.compose.ui.component

import io.getstream.chat.android.ai.compose.parts.AIToolApproval
import io.getstream.chat.android.ai.compose.parts.AIToolCallPart
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class AIToolApproverTest {

    private val scope = TestScope(UnconfinedTestDispatcher())
    private val call = AIToolCallPart(
        id = "toolu_01A",
        name = "send_email",
        status = AIToolCallPart.Status.AwaitingApproval,
        targetUserId = "u_1",
        approval = AIToolApproval(title = "Send this email?"),
    )

    @Test
    fun `a second answer while the first is sent is ignored`() {
        val pending = CompletableDeferred<Unit>()
        val answers = mutableListOf<Boolean>()
        val approver = AIToolApprover("u_1", "android-1", scope) { _, allowed ->
            answers += allowed
            pending.await()
        }

        approver.decide(call, allowed = true)
        approver.decide(call, allowed = false)

        assertEquals(listOf(true), answers)
        assertEquals(AIToolApprovalState(isSending = true), approver.state(call))
    }

    @Test
    fun `a sent answer keeps the buttons disabled until the step changes`() {
        var sends = 0
        val approver = AIToolApprover("u_1", "android-1", scope) { _, _ -> sends++ }

        approver.decide(call, allowed = true)
        approver.decide(call, allowed = true)

        assertEquals(1, sends)
        assertEquals(AIToolApprovalState(isSending = true), approver.state(call))
    }

    @Test
    fun `a failed answer can be given again`() {
        val answers = mutableListOf<Boolean>()
        val approver = AIToolApprover("u_1", "android-1", scope) { _, allowed ->
            answers += allowed
            if (answers.size == 1) error("offline")
        }

        approver.decide(call, allowed = true)
        assertEquals(AIToolApprovalState(failed = true), approver.state(call))

        approver.decide(call, allowed = false)
        assertEquals(listOf(true, false), answers)
        assertEquals(AIToolApprovalState(isSending = true), approver.state(call))
    }

    @Test
    fun `each call has its own answer`() {
        val approver = AIToolApprover("u_1", "android-1", scope) { _, _ -> }

        approver.decide(call, allowed = true)

        assertEquals(AIToolApprovalState(), approver.state(call.copy(id = "toolu_01B")))
    }

    @Test
    fun `an approver is made the ways it was before sign-ins`() {
        val answers = mutableListOf<Boolean>()
        suspend fun send(call: AIToolCallPart, allowed: Boolean) {
            answers += allowed
        }
        val decide: suspend (AIToolCallPart, Boolean) -> Unit = ::send
        val authorizer = AIToolAuthorizer(connect = { _, _ -> false }, decline = {})

        val approvers = listOf(
            AIToolApprover("u_1", "android-1", scope) { _, allowed -> answers += allowed },
            AIToolApprover("u_1", "android-1", scope, ::send),
            AIToolApprover("u_1", "android-1", scope, decide),
            AIToolApprover("u_1", "android-1", scope, decide = decide),
            AIToolApprover(userId = "u_1", clientId = "android-1", scope = scope, decide = decide),
            AIToolApprover("u_1", "android-1", scope, authorizer) { _, allowed -> answers += allowed },
            AIToolApprover("u_1", "android-1", scope, authorizer, decide),
        )
        approvers.forEach { it.decide(call, allowed = true) }

        assertEquals(List(approvers.size) { true }, answers)
        approvers.take(5).forEach { assertNull(it.authorizer) }
        approvers.drop(5).forEach { assertEquals(authorizer, it.authorizer) }
    }

    @Test
    fun `the constructor without an authorizer links as it did`() {
        val constructor = AIToolApprover::class.java.getConstructor(
            String::class.java,
            String::class.java,
            CoroutineScope::class.java,
            Function3::class.java,
        )

        assertFalse(constructor.isSynthetic)
    }
}
