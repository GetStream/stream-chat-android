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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.compose.parts.AIToolApproval
import io.getstream.chat.android.ai.compose.parts.AIToolCallPart
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
internal class AIToolApprovalPromptTest {

    @get:Rule
    val rule = createComposeRule()

    private val call = AIToolCallPart(
        id = "toolu_01A",
        name = "send_email",
        status = AIToolCallPart.Status.AwaitingApproval,
        targetUserId = "u_1",
        approval = AIToolApproval(title = "Send this email?"),
    )

    @Test
    fun `an answer on its way survives the prompt leaving the screen`() {
        val pending = CompletableDeferred<Unit>()
        var sends = 0
        val approver = AIToolApprover("u_1", "android-1", TestScope(UnconfinedTestDispatcher())) { _, _ ->
            sends++
            pending.await()
        }
        var shown by mutableStateOf(true)
        rule.setContent {
            if (shown) AIToolApprovalPrompt(call = call, approver = approver)
        }

        rule.onNodeWithText("Allow").performClick()
        rule.runOnIdle { shown = false }
        rule.runOnIdle { shown = true }

        rule.onNodeWithText("Allow").assertIsNotEnabled()
        rule.onNodeWithText("Don't allow").assertIsNotEnabled()
        rule.runOnIdle {
            assertEquals(1, sends)
            assertFalse("the send goes on", pending.isCancelled)
        }
    }
}
