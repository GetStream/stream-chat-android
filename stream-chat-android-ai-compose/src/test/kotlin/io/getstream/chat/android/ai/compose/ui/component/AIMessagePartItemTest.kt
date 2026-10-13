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

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.compose.parts.AIMessagePart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
internal class AIMessagePartItemTest {

    @get:Rule
    val rule = createComposeRule()

    private val waitingCall = AIMessagePart.from(
        type = "ai_tool_call",
        payload = mapOf(
            "id" to "toolu_01A",
            "name" to "get_location",
            "display_title" to "Checking your location",
            "status" to "awaiting_approval",
            "target_user_id" to "u_1",
            "approval" to mapOf("title" to "Share your location?", "allow_title" to "Share"),
        ),
    )!!

    @Test
    fun `a call waiting for this person shows its question under it`() {
        val answers = mutableListOf<Boolean>()
        val approver = AIToolApprover("u_1", "android-1", TestScope(UnconfinedTestDispatcher())) { _, allowed ->
            answers += allowed
        }
        rule.setContent { AIMessageParts(parts = listOf(waitingCall), approver = approver) }

        rule.onNodeWithText("Checking your location").assertExists()
        rule.onNodeWithText("Share your location?").assertExists()
        rule.onNodeWithText("Share").performClick()

        rule.runOnIdle { assertEquals(listOf(true), answers) }
    }

    @Test
    fun `without an approver the call shows no question`() {
        rule.setContent { AIMessageParts(parts = listOf(waitingCall)) }

        rule.onNodeWithText("Checking your location").assertExists()
        rule.onNodeWithText("Share your location?").assertDoesNotExist()
    }

    @Test
    fun `a step this version cannot read shows a placeholder`() {
        val citation = AIMessagePart.from("ai_citation", mapOf("id" to "c1"))!!

        rule.setContent { AIMessageParts(parts = listOf(citation)) }

        rule.onNodeWithText("A step this app version can't show").assertExists()
    }

    @Test
    fun `a round of reasoning shows as reasoning`() {
        val reasoning = AIMessagePart.from(
            type = "ai_reasoning",
            payload = mapOf("id" to "r1", "status" to "completed", "text" to "Weighing the options"),
        )!!

        rule.setContent { AIMessageParts(parts = listOf(reasoning)) }

        rule.onNodeWithText("Thought", substring = true, useUnmergedTree = true).assertExists()
    }
}
