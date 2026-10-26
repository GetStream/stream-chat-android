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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.R
import io.getstream.chat.android.ai.compose.parts.AIToolAuthorization
import io.getstream.chat.android.ai.compose.parts.AIToolCallPart

/**
 * How the person signs in on this device when a tool call waits for their account at a provider,
 * and how their "not now" reaches your backend, which holds the call until one or the other.
 *
 * The SDK doesn't open the sign-in page itself: Connect hands it to [connect], and your app opens
 * it in a browser, such as a Custom Tab. The page goes back to the return URL your backend gives
 * its sign-ins, with `status=connected` in its query once the account is connected; your backend
 * then updates the call's step, and the call goes on. Give it to [AIToolApprover] to offer
 * sign-ins:
 *
 * ```
 * val authorizer = AIToolAuthorizer(
 *     connect = { call, url -> signIns.open(url) }, // true once the page came back connected
 *     decline = { call -> backend.declineSignIn(call.id) },
 * )
 * val approver = AIToolApprover(userId, installId, viewModelScope, authorizer) { call, allowed -> /* … */ }
 * ```
 *
 * Both run in the approver's scope.
 *
 * @param connect Opens the sign-in page, the call's [AIToolAuthorization.launchUrl], and returns
 * once the person is back: `true` when the page came back saying the account is connected, which
 * keeps the card connecting until the call's step changes, or `false` when they came back without
 * connecting, which lets them connect again. Return `false` as soon as the person is back when your
 * app can't tell. A thrown exception says the page couldn't be opened, and the person can try
 * again.
 * @param decline Tells your backend the person won't sign in now. Your backend ends the call, which
 * never runs. A thrown exception lets the person answer again.
 */
public class AIToolAuthorizer(
    connect: suspend (call: AIToolCallPart, url: String) -> Boolean,
    decline: suspend (call: AIToolCallPart) -> Unit,
) {
    internal val connect = connect
    internal val decline = decline
}

/**
 * Where the person's sign-in is.
 *
 * @param isConnecting The sign-in is open, or done and the call's step hasn't changed yet.
 * @param isDeclining Their "not now" is on its way, or arrived and the call's step hasn't changed
 * yet.
 * @param connectFailed The sign-in couldn't be opened, and the person can try again.
 * @param declineFailed Their "not now" couldn't be sent, and the person can answer again.
 */
public data class AIToolAuthorizationState(
    val isConnecting: Boolean = false,
    val isDeclining: Boolean = false,
    val connectFailed: Boolean = false,
    val declineFailed: Boolean = false,
) {
    /** Whether an answer is under way, so the buttons wait. */
    public val isBusy: Boolean get() = isConnecting || isDeclining
}

/**
 * Asks the person a tool call waits for to sign in at the provider it needs: the sign-in its
 * `ai_tool_call` step carries (status `awaiting_authorization`, with `authorization`). Connect
 * hands the sign-in page to the approver's [AIToolAuthorizer], which opens it. Once the provider
 * has answered, your backend updates the step and the call goes on. It shows only to that person,
 * only while the call waits, and only with an [AIToolAuthorizer], so it can sit under every tool
 * call:
 *
 * ```
 * AIToolCall(part = call)
 * AIToolAuthorizationPrompt(call = call, approver = approver)
 * ```
 *
 * By default it asks with [ChatAiComponentFactory.AIToolAuthorizationContent], an
 * [AIToolAuthorizationCard]. To ask in your own design, return yours there, or pass the content. It
 * gets the sign-in, where the answer is, and functions that connect and decline:
 *
 * ```
 * AIToolAuthorizationPrompt(call = call, approver = approver) { authorization, state, connect, decline ->
 *     MySignInCard(authorization.title, busy = state.isBusy, onConnect = connect, onNotNow = decline)
 * }
 * ```
 *
 * @param call The tool call.
 * @param approver Who answers on this device, with the [AIToolAuthorizer] the sign-in needs.
 * @param modifier The modifier to apply to the sign-in.
 * @param content Shows the sign-in. Defaults to [ChatAiComponentFactory.AIToolAuthorizationContent].
 */
@Composable
public fun AIToolAuthorizationPrompt(
    call: AIToolCallPart,
    approver: AIToolApprover,
    modifier: Modifier = Modifier,
    content: @Composable (
        authorization: AIToolAuthorization,
        state: AIToolAuthorizationState,
        connect: () -> Unit,
        decline: () -> Unit,
    ) -> Unit = { authorization, state, connect, decline ->
        with(LocalChatAiComponentFactory.current) {
            AIToolAuthorizationContent(
                AIToolAuthorizationContentParams(
                    call = call,
                    authorization = authorization,
                    state = state,
                    connect = connect,
                    decline = decline,
                ),
            )
        }
    },
) {
    val authorization = approver.signIn(call) ?: return
    Box(modifier = modifier) {
        content(
            authorization,
            approver.authorizationState(call),
            { approver.connect(call) },
            { approver.declineAuthorization(call) },
        )
    }
}

/**
 * A tool call's sign-in: which provider it needs and what signing in means, with buttons to
 * connect or to say not now.
 *
 * @param authorization The sign-in.
 * @param state Where the person's answer is. Connect waits while the sign-in is open or a "not now"
 * is sent; Not now stays, so a sign-in that never comes back leaves the person a way out.
 * @param connect Opens the sign-in.
 * @param decline Sends the person's "not now".
 * @param modifier The modifier to apply to the card.
 * @param textStyle The style of the question. Its message uses a smaller style.
 * @param colors The palette of the card, shared with [AIToolApprovalCard].
 */
@Suppress("LongParameterList") // The card's content, its two answers, and its look.
@Composable
public fun AIToolAuthorizationCard(
    authorization: AIToolAuthorization,
    state: AIToolAuthorizationState,
    connect: () -> Unit,
    decline: () -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    colors: AIToolApprovalColors = AIToolApprovalDefaults.colors(),
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background, shape)
            .border(1.dp, colors.border, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = authorization.title,
                style = textStyle.copy(fontWeight = FontWeight.SemiBold),
                color = colors.title,
            )
            authorization.message?.let { message ->
                Text(text = message, style = MaterialTheme.typography.bodySmall, color = colors.message)
            }
        }
        AuthorizationButtons(state, connect, decline, colors)
        authorizationFailure(state)?.let { failure ->
            Text(text = failure, style = MaterialTheme.typography.labelSmall, color = colors.failure)
        }
    }
}

/** What went wrong, such as the sign-in page not opening or the "not now" not reaching your backend. */
@Composable
private fun authorizationFailure(state: AIToolAuthorizationState): String? = when {
    state.connectFailed -> stringResource(R.string.stream_ai_compose_tool_authorization_not_opened)
    state.declineFailed -> stringResource(R.string.stream_ai_compose_tool_approval_not_sent)
    else -> null
}

@Composable
private fun AuthorizationButtons(
    state: AIToolAuthorizationState,
    connect: () -> Unit,
    decline: () -> Unit,
    colors: AIToolApprovalColors,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = connect,
            enabled = !state.isBusy,
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
            contentPadding = AUTHORIZATION_BUTTON_PADDING,
        ) {
            Text(
                stringResource(
                    if (state.isConnecting) {
                        R.string.stream_ai_compose_tool_authorization_connecting
                    } else {
                        R.string.stream_ai_compose_tool_authorization_connect
                    },
                ),
            )
        }
        OutlinedButton(
            onClick = decline,
            enabled = !state.isDeclining,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accent),
            border = BorderStroke(1.dp, colors.border),
            contentPadding = AUTHORIZATION_BUTTON_PADDING,
        ) {
            Text(stringResource(R.string.stream_ai_compose_tool_authorization_decline))
        }
        if (state.isBusy) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = colors.accent, strokeWidth = 2.dp)
        }
    }
}

private val AUTHORIZATION_BUTTON_PADDING = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
