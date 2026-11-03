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

package io.getstream.chat.android.ai.openui.lang

import java.net.URI
import java.net.URISyntaxException

/** Built-in action types dispatched to the host (`BuiltinActionType`). */
public object BuiltinActionType {
    /** Send the user's intent back to the model (`@ToAssistant`, follow-ups, plain buttons). */
    public const val CONTINUE_CONVERSATION: String = "continue_conversation"

    /** Open a URL (`@OpenUrl`). */
    public const val OPEN_URL: String = "open_url"
}

/**
 * A structured action fired by an interactive component (mirrors `ActionEvent`).
 *
 * `@Run`, `@Set` and `@Reset` steps are handled by the runtime; conversation and navigation
 * steps, and the library's custom actions, are forwarded to the host.
 */
public data class ActionEvent internal constructor(
    /**
     * [BuiltinActionType.CONTINUE_CONVERSATION], [BuiltinActionType.OPEN_URL], or the name of one
     * of the library's custom actions.
     */
    val type: String,
    /** Action-specific params: `url` for open-url, `context` for continue-conversation, a custom action's args. */
    val params: OpenUIObject = OpenUIObject.Empty,
    /** Human readable label (e.g. the button label), shown as the user's message in a chat. */
    val humanFriendlyMessage: String,
    /** Raw form state when the step ran. */
    val formState: OpenUIObject? = null,
    /** The form that scoped the action, if any. */
    val formName: String? = null,
) {
    /** Whether this sends the user's intent back to the model. */
    val isContinueConversation: Boolean get() = type == BuiltinActionType.CONTINUE_CONVERSATION

    /** Whether this opens a URL. */
    val isOpenUrl: Boolean get() = type == BuiltinActionType.OPEN_URL

    /**
     * The URL of an open-url action, when it's a web or email link. The model writes it, so a
     * link to another app or a phone number is `null`.
     */
    val url: String? get() = params["url"].stringValue?.takeIf(::isOpenable)

    /** The `context` of a `@ToAssistant(message, context)` step: any value, passed through unchanged. */
    val context: OpenUIValue? get() = params["context"]

    /**
     * The user message sent back to the model for a continue-conversation action, in the OpenUI
     * message protocol: the visible text, plus a context of what was clicked, the form state and
     * the step's context (upstream `buildActionUserMessage`).
     */
    val assistantMessage: String
        get() {
            val items = buildList {
                add(OpenUIValue.Str("User clicked: $humanFriendlyMessage"))
                if (formState != null || context != null) add(OpenUIValue.Obj(formState ?: OpenUIObject.Empty))
                context?.let(::add)
            }
            val message = buildMessage(content = humanFriendlyMessage, context = OpenUIValue.Arr(items))
            // No message: stored without the content line.
            if (humanFriendlyMessage.isNotEmpty()) return message
            return message.substringAfter('\n')
        }

    private companion object {
        // The model writes every link, so only the schemes react-markdown keeps by default for web
        // links and email open; a link to another app or a phone number doesn't.
        private val openableSchemes = setOf("http", "https", "mailto")

        fun isOpenable(url: String): Boolean = try {
            URI(url).scheme?.lowercase() in openableSchemes
        } catch (_: URISyntaxException) {
            false
        }
    }
}
