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

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
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
}
