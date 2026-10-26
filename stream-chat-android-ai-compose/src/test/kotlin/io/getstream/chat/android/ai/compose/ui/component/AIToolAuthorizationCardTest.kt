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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import io.getstream.chat.android.ai.compose.parts.AIMessagePart
import io.getstream.chat.android.ai.compose.parts.AIToolAuthorization
import io.getstream.chat.android.ai.compose.ui.PaparazziTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class AIToolAuthorizationCardTest : PaparazziTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        renderingMode = RenderingMode.SHRINK,
    )

    private val authorization = AIToolAuthorization(
        provider = "linear",
        providerName = "Linear",
        title = "Connect Linear",
        message = "You sign in at Linear. The agent can then read and update your issues.",
        url = "https://router.example/launch/a1",
    )

    @Test
    fun idle() {
        card(AIToolAuthorizationState())
    }

    @Test
    fun connecting() {
        card(AIToolAuthorizationState(isConnecting = true))
    }

    @Test
    fun declining() {
        card(AIToolAuthorizationState(isDeclining = true))
    }

    @Test
    fun not_opened() {
        card(AIToolAuthorizationState(connectFailed = true), authorization.copy(message = null))
    }

    @Test
    fun not_sent() {
        card(AIToolAuthorizationState(declineFailed = true), authorization.copy(message = null))
    }

    @Test
    fun sign_in_steps() {
        val approver = AIToolApprover(
            userId = "u_1",
            clientId = "android-1",
            scope = TestScope(UnconfinedTestDispatcher()),
            authorizer = AIToolAuthorizer(connect = { _, _ -> true }, decline = {}),
        ) { _, _ -> }
        val authorization = """
            "authorization":{"provider":"linear","title":"Connect Linear","url":"https://router.example/launch/a1",
            "message":"You sign in at Linear. The agent can then read and update your issues."
        """.trimIndent()
        val steps = listOf(
            """{"id":"call-1","display_title":"Using Linear","status":"awaiting_authorization","target_user_id":"u_1",$authorization}}""",
            """{"id":"call-2","display_title":"Using Linear","status":"cancelled",$authorization,"status":"declined"}}""",
            """
                {"id":"call-3","display_title":"Using Linear","status":"cancelled","summary":"Linear wasn't connected in time",
                $authorization,"status":"expired"}}
            """.trimIndent(),
            """{"id":"call-4","display_title":"Using Linear","status":"completed","duration_ms":2300,$authorization,"status":"connected"}}""",
        ).map { AIMessagePart.fromJson("ai_tool_call", it)!! }
        snapshot {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                steps.forEach { step -> AIMessagePartItem(part = step, approver = approver) }
            }
        }
    }

    private fun card(state: AIToolAuthorizationState, authorization: AIToolAuthorization = this.authorization) {
        snapshot {
            AIToolAuthorizationCard(
                authorization = authorization,
                state = state,
                connect = {},
                decline = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
