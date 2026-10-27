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

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.compose.parts.AIMessagePart
import io.getstream.chat.android.ai.compose.parts.AIToolAuthorization
import io.getstream.chat.android.ai.compose.parts.AIToolCallPart
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
internal class AIToolAuthorizationPromptTest {

    @get:Rule
    val rule = createComposeRule()

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

    private fun approver(authorizer: AIToolAuthorizer?) =
        AIToolApprover("u_1", "android-1", TestScope(UnconfinedTestDispatcher()), authorizer) { _, _ -> }

    @Test
    fun `connect hands the sign-in page to the app and waits for it`() {
        val returned = CompletableDeferred<Boolean>()
        val opened = mutableListOf<String>()
        val approver = approver(
            AIToolAuthorizer(
                connect = { _, url ->
                    opened += url
                    returned.await()
                },
                decline = {},
            ),
        )
        rule.setContent { AIToolAuthorizationPrompt(call = call, approver = approver) }

        rule.onNodeWithText("Connect Linear").assertExists()
        rule.onNodeWithText("Connect").performClick()

        rule.onNodeWithText("Connecting…").assertIsNotEnabled()
        rule.onNodeWithText("Not now").assertIsEnabled()
        rule.runOnIdle { assertEquals(listOf("https://router.example/launch/a1#handoff_token=tok_-1"), opened) }

        returned.complete(false)
        rule.onNodeWithText("Connect").assertIsEnabled()
    }

    @Test
    fun `an expired sign-in says so and offers only not now`() {
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
        val expired = call.copy(authorization = call.authorization!!.copy(expiresAt = Date(0)))
        rule.setContent { AIToolAuthorizationPrompt(call = expired, approver = approver) }

        rule.onNodeWithText("This sign-in expired.").assertExists()
        rule.onNodeWithText("Connect").assertIsNotEnabled()
        rule.onNodeWithText("Not now").assertIsEnabled()
        rule.runOnIdle { assertEquals(0, opens) }
    }

    @Test
    fun `not now can be tapped while the sign-in is open`() {
        val returned = CompletableDeferred<Boolean>()
        val declined = mutableListOf<String>()
        val approver = approver(
            AIToolAuthorizer(
                connect = { _, _ -> returned.await() },
                decline = { call -> declined += call.id },
            ),
        )
        rule.setContent { AIToolAuthorizationPrompt(call = call, approver = approver) }

        rule.onNodeWithText("Connect").performClick()
        rule.onNodeWithText("Not now").performClick()

        rule.onNodeWithText("Connecting…").assertIsNotEnabled()
        rule.onNodeWithText("Not now").assertIsNotEnabled()
        rule.runOnIdle { assertEquals(listOf("call-1"), declined) }
    }

    @Test
    fun `only the person a sign-in is for reads that it waits for them`() {
        val approver = approver(AIToolAuthorizer(connect = { _, _ -> true }, decline = {}))
        fun step(id: String, targetUserId: String) = AIMessagePart.from(
            type = "ai_tool_call",
            payload = mapOf(
                "id" to id,
                "display_title" to "Using Linear $id",
                "status" to "awaiting_authorization",
                "target_user_id" to targetUserId,
                "authorization" to mapOf("title" to "Connect Linear", "url" to "https://router.example/launch/$id"),
            ),
        )!!
        rule.setContent {
            Column {
                AIMessagePartItem(part = step("a1", targetUserId = "u_1"), approver = approver)
                AIMessagePartItem(part = step("a2", targetUserId = "u_2"), approver = approver)
                AIMessagePartItem(part = step("a3", targetUserId = "u_1"))
                AIToolCall(part = step("a4", targetUserId = "u_1").toolCall!!)
            }
        }

        rule.onAllNodesWithText("Waiting for you to sign in").assertCountEquals(1)
        rule.onAllNodesWithText("Waiting for sign-in").assertCountEquals(3)
    }

    @Test
    fun `without an authorizer no sign-in is offered`() {
        rule.setContent { AIToolAuthorizationPrompt(call = call, approver = approver(null)) }

        rule.onNodeWithText("Connect Linear").assertDoesNotExist()
    }

    @Test
    fun `the sign-in asks with the factory's content`() {
        val factory = object : ChatAiComponentFactory {
            @Composable
            override fun AIToolAuthorizationContent(params: AIToolAuthorizationContentParams) {
                Text("Sign in to ${params.authorization.provider} for ${params.call.id}")
            }
        }
        val approver = approver(AIToolAuthorizer(connect = { _, _ -> true }, decline = {}))
        rule.setContent {
            CompositionLocalProvider(LocalChatAiComponentFactory provides factory) {
                AIToolAuthorizationPrompt(call = call, approver = approver)
            }
        }

        rule.onNodeWithText("Sign in to linear for call-1").assertExists()
        rule.onNodeWithText("Connect").assertDoesNotExist()
    }

    @Test
    fun `a reply's sign-in gets the style and palette of its question`() {
        val textStyle = TextStyle(fontSize = 19.sp)
        val colors = AIToolApprovalColors(
            title = Color.Red,
            message = Color.Green,
            background = Color.Blue,
            border = Color.Cyan,
            accent = Color.Magenta,
            failure = Color.Yellow,
        )
        val seen = mutableListOf<AIToolAuthorizationContentParams>()
        val factory = object : ChatAiComponentFactory {
            @Composable
            override fun AIToolAuthorizationContent(params: AIToolAuthorizationContentParams) {
                seen += params
            }
        }
        val approver = approver(AIToolAuthorizer(connect = { _, _ -> true }, decline = {}))
        rule.setContent {
            CompositionLocalProvider(LocalChatAiComponentFactory provides factory) {
                AIMessagePartItem(
                    part = AIMessagePart.from(
                        type = "ai_tool_call",
                        payload = mapOf(
                            "id" to "call-1",
                            "status" to "awaiting_authorization",
                            "target_user_id" to "u_1",
                            "authorization" to mapOf("title" to "Connect Linear", "url" to "https://router.example/launch/a1"),
                        ),
                    )!!,
                    approver = approver,
                    textStyle = textStyle,
                    toolApprovalColors = colors,
                )
            }
        }

        rule.runOnIdle {
            assertEquals(textStyle, seen.last().textStyle)
            assertEquals(colors, seen.last().colors)
        }
    }
}
