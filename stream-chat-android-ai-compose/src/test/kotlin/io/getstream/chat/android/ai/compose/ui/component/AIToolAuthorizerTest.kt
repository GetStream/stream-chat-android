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

import io.getstream.chat.android.ai.compose.parts.AIToolAuthorization
import io.getstream.chat.android.ai.compose.parts.AIToolCallPart
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
internal class AIToolAuthorizerTest {

    private val scope = TestScope(UnconfinedTestDispatcher())
    private val call = AIToolCallPart(
        id = "call-1",
        name = "linear__list_tools",
        status = AIToolCallPart.Status.AwaitingAuthorization,
        targetUserId = "u_1",
        authorization = AIToolAuthorization(
            provider = "linear",
            title = "Connect Linear",
            url = "https://router.example/launch/a1",
            handoffToken = "tok_-1",
        ),
    )

    private fun approver(authorizer: AIToolAuthorizer) =
        AIToolApprover("u_1", "android-1", scope, authorizer) { _, _ -> }

    @Test
    fun `connect hands the app the page with its handoff token, once at a time`() {
        val returned = CompletableDeferred<Boolean>()
        val opened = mutableListOf<Pair<String, String>>()
        val approver = approver(
            AIToolAuthorizer(
                connect = { call, url ->
                    opened += call.id to url
                    returned.await()
                },
                decline = {},
            ),
        )

        approver.connect(call)
        approver.connect(call)

        assertEquals(listOf("call-1" to "https://router.example/launch/a1#handoff_token=tok_-1"), opened)
        assertEquals(AIToolAuthorizationState(isConnecting = true), approver.authorizationState(call))
    }

    @Test
    fun `a connected sign-in keeps connecting for a while, then offers Connect again if the step stays`() {
        var opens = 0
        val approver = approver(
            AIToolAuthorizer(
                connect = { _, _ ->
                    opens++
                    true
                },
                decline = {},
            ),
        )

        approver.connect(call)
        scope.advanceTimeBy(ConnectedSettleMillis - 1)
        assertEquals(AIToolAuthorizationState(isConnecting = true), approver.authorizationState(call))

        scope.advanceTimeBy(1)
        scope.runCurrent()
        assertEquals(AIToolAuthorizationState(), approver.authorizationState(call))
        approver.connect(call)
        assertEquals(2, opens)
    }

    @Test
    fun `a sign-in issued again on the same step starts over`() {
        val returned = CompletableDeferred<Boolean>()
        val opened = mutableListOf<String>()
        val approver = approver(
            AIToolAuthorizer(
                connect = { _, url ->
                    opened += url
                    if (opened.size == 1) returned.await() else true
                },
                decline = {},
            ),
        )
        val reissued = call.copy(authorization = call.authorization!!.copy(handoffToken = "tok_-2"))

        approver.connect(call)
        assertEquals(AIToolAuthorizationState(), approver.authorizationState(reissued))

        approver.connect(reissued)
        returned.complete(false)

        assertEquals(
            listOf(
                "https://router.example/launch/a1#handoff_token=tok_-1",
                "https://router.example/launch/a1#handoff_token=tok_-2",
            ),
            opened,
        )
        assertEquals(AIToolAuthorizationState(isConnecting = true), approver.authorizationState(reissued))
    }

    @Test
    fun `a failed not now while the sign-in is open keeps it the only one open`() {
        val returned = CompletableDeferred<Boolean>()
        var opens = 0
        val approver = approver(
            AIToolAuthorizer(
                connect = { _, _ ->
                    opens++
                    returned.await()
                },
                decline = { error("offline") },
            ),
        )

        approver.connect(call)
        approver.declineAuthorization(call)
        assertEquals(
            AIToolAuthorizationState(isConnecting = true, declineFailed = true),
            approver.authorizationState(call),
        )
        assertFalse(approver.authorizationState(call).canConnect)

        approver.connect(call)
        assertEquals(1, opens)

        returned.complete(false)
        assertEquals(AIToolAuthorizationState(declineFailed = true), approver.authorizationState(call))
        approver.connect(call)
        assertEquals(2, opens)
    }

    @Test
    fun `an expired sign-in is no longer offered`() {
        var opens = 0
        val approver = approver(
            AIToolAuthorizer(
                connect = { _, _ ->
                    opens++
                    false
                },
                decline = {},
            ),
        )
        val expired = call.copy(authorization = call.authorization!!.copy(expiresAt = Date(0)))
        val later = call.copy(
            authorization = call.authorization!!.copy(expiresAt = Date(System.currentTimeMillis() + 60_000)),
        )

        approver.connect(expired)
        assertEquals(0, opens)
        assertEquals(AIToolAuthorizationState(isExpired = true), approver.authorizationState(expired))
        assertFalse(approver.authorizationState(expired).canConnect)

        approver.connect(later)
        assertEquals(1, opens)
        assertEquals(AIToolAuthorizationState(), approver.authorizationState(later))
    }

    @Test
    fun `coming back without connecting lets the person connect again`() {
        var opens = 0
        val approver = approver(
            AIToolAuthorizer(
                connect = { _, _ ->
                    opens++
                    false
                },
                decline = {},
            ),
        )

        approver.connect(call)
        assertEquals(AIToolAuthorizationState(), approver.authorizationState(call))

        approver.connect(call)
        assertEquals(2, opens)
    }

    @Test
    fun `a page that couldn't be opened can be tried again`() {
        val approver = approver(AIToolAuthorizer(connect = { _, _ -> error("no browser") }, decline = {}))

        approver.connect(call)

        assertEquals(AIToolAuthorizationState(connectFailed = true), approver.authorizationState(call))
    }

    @Test
    fun `not now is sent once and a failed one can be sent again`() {
        val declined = mutableListOf<String>()
        val approver = approver(
            AIToolAuthorizer(
                connect = { _, _ -> true },
                decline = { call ->
                    declined += call.id
                    if (declined.size == 1) error("offline")
                },
            ),
        )

        approver.declineAuthorization(call)
        assertEquals(AIToolAuthorizationState(declineFailed = true), approver.authorizationState(call))

        approver.declineAuthorization(call)
        approver.declineAuthorization(call)
        assertEquals(listOf("call-1", "call-1"), declined)
        assertEquals(AIToolAuthorizationState(isDeclining = true), approver.authorizationState(call))
    }

    @Test
    fun `not now while the sign-in is open stays when the person comes back`() {
        val returned = CompletableDeferred<Boolean>()
        val approver = approver(AIToolAuthorizer(connect = { _, _ -> returned.await() }, decline = {}))

        approver.connect(call)
        approver.declineAuthorization(call)
        returned.complete(false)

        assertEquals(AIToolAuthorizationState(isDeclining = true), approver.authorizationState(call))
    }

    @Test
    fun `nothing is asked of a call that doesn't wait for this person`() {
        var opens = 0
        val approver = approver(
            AIToolAuthorizer(
                connect = { _, _ ->
                    opens++
                    true
                },
                decline = {},
            ),
        )

        approver.connect(call.copy(targetUserId = "u_2"))
        approver.connect(call.copy(status = AIToolCallPart.Status.Running))

        assertEquals(0, opens)
    }
}
