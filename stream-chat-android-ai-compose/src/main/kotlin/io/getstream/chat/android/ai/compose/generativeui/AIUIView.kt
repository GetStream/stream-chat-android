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

package io.getstream.chat.android.ai.compose.generativeui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A reply's generated UI, from the first of [host]'s renderers that reads its format. It comes in
 * as it first shows while the agent writes it, and its parts as they show after it (see
 * [AIUIEntrance]). It shows nothing when no renderer shows something of its own for the reply.
 *
 * `AIConversation` shows it in place of a reply's text when it is given the host. Showing a reply
 * yourself, call [AIUIHost.receive] with the conversation's messages first, then show it after the
 * reply's steps when [AIUIHost.canShow], and the reply's text otherwise.
 *
 * @param host The conversation's host.
 * @param document The reply's document.
 * @param messageId The reply's id.
 * @param modifier The modifier to apply.
 */
@Composable
public fun AIUIView(host: AIUIHost, document: AIUIDocument, messageId: String, modifier: Modifier = Modifier) {
    val content = host.content(document, messageId) ?: return
    AIUIEntranceStage(isStreaming = document.isStreaming, modifier = modifier.fillMaxWidth()) {
        content()
    }
}
