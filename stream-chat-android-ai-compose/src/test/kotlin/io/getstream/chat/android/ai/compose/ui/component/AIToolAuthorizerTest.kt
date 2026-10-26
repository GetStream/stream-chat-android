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
import org.junit.Assert.assertEquals
import org.junit.Test

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
    fun `a connected sign-in keeps connecting until the step changes`() {
        val approver = approver(AIToolAuthorizer(connect = { _, _ -> true }, decline = {}))

        approver.connect(call)

        assertEquals(AIToolAuthorizationState(isConnecting = true), approver.authorizationState(call))
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
