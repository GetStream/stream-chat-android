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

package io.getstream.chat.android.ai.compose.parts

import io.getstream.chat.android.ai.compose.ui.component.AIToolApprover
import io.getstream.chat.android.ai.compose.ui.component.AIToolAuthorizationState
import io.getstream.chat.android.ai.compose.ui.component.AIToolAuthorizer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
internal class AIToolAuthorizationTest {

    private fun call(json: String): AIToolCallPart = AIMessagePart.fromJson("ai_tool_call", json)!!.toolCall!!

    @Test
    fun `a call waiting for a sign-in carries it for its person`() {
        val waiting = call(
            """
                {"id":"call-1","name":"linear__list_tools","display_title":"Using Linear","status":"awaiting_authorization",
                "executor":"server","target_user_id":"u_1","authorization":{"type":"oauth","provider":"linear",
                "provider_name":"Linear","title":"Connect Linear","message":"You sign in at Linear.",
                "url":"https://router.example/v1/agents/connectors/oauth/launch/a1","handoff_token":"tok_-1",
                "expires_at":"2026-10-20T12:10:00.123456Z"}}
            """.trimIndent(),
        )

        assertEquals(AIToolCallPart.Status.AwaitingAuthorization, waiting.status)
        val read = waiting.authorization!!
        assertEquals(1_792_498_200_123L, read.expiresAt?.time)
        assertEquals(
            AIToolAuthorization(
                provider = "linear",
                providerName = "Linear",
                title = "Connect Linear",
                message = "You sign in at Linear.",
                url = "https://router.example/v1/agents/connectors/oauth/launch/a1",
                handoffToken = "tok_-1",
            ),
            read.copy(expiresAt = null),
        )
        assertTrue(waiting.isAwaitingAuthorization(userId = "u_1"))
        assertFalse("only its person signs in", waiting.isAwaitingAuthorization(userId = "u_2"))
        assertFalse(waiting.isAwaitingApproval(userId = "u_1", clientId = "android-1"))
        assertFalse(waiting.status.isFinished)
    }

    @Test
    fun `the sign-in page carries the handoff token in its fragment`() {
        val waiting = call(
            """
                {"id":"call-1","status":"awaiting_authorization","target_user_id":"u_1","authorization":{
                "title":"Connect Linear","url":"https://router.example/launch/a1","handoff_token":"tok_-1"}}
            """.trimIndent(),
        )
        val authorization = waiting.authorization!!

        assertEquals("https://router.example/launch/a1#handoff_token=tok_-1", authorization.launchUrl)
        assertEquals(
            "a sign-in that names no type is a consent at the provider",
            AIToolAuthorization.Kind.OAuth,
            authorization.kind,
        )
        val spent = authorization.copy(handoffToken = null)
        assertEquals(spent.url, spent.launchUrl)
        assertEquals(
            "the page reads its fragment as form parameters",
            "https://router.example/launch/a1#handoff_token=a%2Bb%26c%3Dd",
            authorization.copy(handoffToken = "a+b&c=d").launchUrl,
        )
        assertEquals(
            "the token replaces a fragment the page had",
            "https://router.example/launch/a1#handoff_token=tok_-1",
            authorization.copy(url = "https://router.example/launch/a1#old").launchUrl,
        )
    }

    @Test
    fun `an ended sign-in says how and a declined one is over`() {
        val connected = call(
            """
                {"id":"call-1","status":"running","target_user_id":"u_1","authorization":{"title":"Connect Linear",
                "url":"https://router.example/launch/a1","expires_at":"2026-10-20T12:10:00Z","status":"connected"}}
            """.trimIndent(),
        )
        assertEquals(AIToolAuthorization.Status.Connected, connected.authorization?.status)
        assertEquals("a time with no fractional seconds", Date(1_792_498_200_000L), connected.authorization?.expiresAt)
        assertNull(connected.authorization?.handoffToken)
        assertFalse(connected.isAwaitingAuthorization(userId = "u_1"))
        assertFalse(connected.isDeclined)

        val declined = call(
            """
                {"id":"call-2","status":"cancelled","summary":"Not connected","authorization":{"title":"Connect Linear",
                "url":"https://router.example/launch/a2","status":"declined"}}
            """.trimIndent(),
        )
        assertTrue(declined.isDeclined)
        assertTrue(declined.status.isFinished)

        val expired = call(
            """
                {"id":"call-3","status":"cancelled","authorization":{"title":"Connect Linear",
                "url":"https://router.example/launch/a3","status":"expired"}}
            """.trimIndent(),
        )
        assertEquals(AIToolAuthorization.Status.Expired, expired.authorization?.status)
        assertFalse("an expired sign-in is cancelled, not declined", expired.isDeclined)
    }

    @Test
    fun `a sign-in without a title or a page asks nothing`() {
        val untitled = call(
            """{"id":"call-1","status":"awaiting_authorization","target_user_id":"u_1","authorization":{"url":"https://router.example/launch/a1"}}""",
        )
        assertNull(untitled.authorization)
        assertFalse(untitled.isAwaitingAuthorization(userId = "u_1"))

        val nowhere = call(
            """{"id":"call-1","status":"awaiting_authorization","target_user_id":"u_1","authorization":{"title":"Connect Linear"}}""",
        )
        assertNull(nowhere.authorization)

        listOf(
            "/v1/agents/connectors/oauth/launch/a1",
            "javascript:alert(1)",
            "com.example.app:/connected",
            "https:///launch/a1",
        ).forEach { page ->
            val notAPage = call(
                """{"id":"call-1","status":"awaiting_authorization","target_user_id":"u_1","authorization":{"title":"Connect Linear","url":"$page"}}""",
            )
            assertNull("$page is no web page to open", notAPage.authorization)
        }
    }

    @Test
    fun `a sign-in is asked only of its person while the call waits and only where they can sign in`() {
        val waiting = call(
            """
                {"id":"call-1","status":"awaiting_authorization","target_user_id":"u_1","authorization":{"type":"oauth",
                "title":"Connect Linear","url":"https://router.example/launch/a1"}}
            """.trimIndent(),
        )
        val authorizer = AIToolAuthorizer(connect = { _, _ -> true }, decline = {})
        val approver = approver("u_1", authorizer)

        assertEquals("Connect Linear", approver.signIn(waiting)?.title)
        assertNull("another person is not asked", approver("u_2", authorizer).signIn(waiting))
        assertNull("without an authorizer this device cannot open it", approver("u_1", null).signIn(waiting))

        val connected = call(
            """
                {"id":"call-1","status":"running","target_user_id":"u_1","authorization":{"title":"Connect Linear",
                "url":"https://router.example/launch/a1","status":"connected"}}
            """.trimIndent(),
        )
        assertNull("a sign-in that is over asks nothing", approver.signIn(connected))

        val passkey = call(
            """
                {"id":"call-1","status":"awaiting_authorization","target_user_id":"u_1","authorization":{"type":"passkey",
                "title":"Use your passkey","url":"https://router.example/launch/a1"}}
            """.trimIndent(),
        )
        assertNull("a kind this SDK does not open is left to the app", approver.signIn(passkey))
    }

    @Test
    fun `an expiry that does not read leaves the sign-in as it is`() {
        listOf("in ten minutes", "2026-13-20T12:10:00Z", "2026-10-20T12:10:00").forEach { expiry ->
            val waiting = call(
                """
                    {"id":"call-1","status":"awaiting_authorization","target_user_id":"u_1","authorization":{
                    "title":"Connect Linear","url":"https://router.example/launch/a1","expires_at":"$expiry"}}
                """.trimIndent(),
            )
            assertEquals("Connect Linear", waiting.authorization?.title)
            assertNull(expiry, waiting.authorization?.expiresAt)
        }
    }

    @Test
    fun `an expiry with an offset reads as the same moment`() {
        val waiting = call(
            """
                {"id":"call-1","status":"awaiting_authorization","target_user_id":"u_1","authorization":{
                "title":"Connect Linear","url":"https://router.example/launch/a1","expires_at":"2026-10-20T14:10:00.5+02:00"}}
            """.trimIndent(),
        )
        assertEquals(Date(1_792_498_200_500L), waiting.authorization?.expiresAt)
    }

    @Test
    fun `the buttons wait while an answer is under way`() {
        assertFalse(AIToolAuthorizationState().isBusy)
        assertTrue(AIToolAuthorizationState(isConnecting = true).isBusy)
        assertTrue(AIToolAuthorizationState(isDeclining = true).isBusy)
        assertFalse("a failure lets the person try again", AIToolAuthorizationState(connectFailed = true).isBusy)
        assertFalse(AIToolAuthorizationState(declineFailed = true).isBusy)
    }

    private fun approver(userId: String, authorizer: AIToolAuthorizer?) =
        AIToolApprover(userId, "android-1", TestScope(UnconfinedTestDispatcher()), authorizer) { _, _ -> }
}
