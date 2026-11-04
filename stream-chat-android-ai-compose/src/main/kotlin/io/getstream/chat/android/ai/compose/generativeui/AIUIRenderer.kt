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

import androidx.compose.runtime.Composable
import io.getstream.chat.android.ai.compose.parts.AIClientTool

/**
 * Reads one format of generated UI and shows it. The SDK knows no format of its own: give a
 * conversation's [AIUIHost] the renderer of each format your agent writes, such as OpenUI's.
 *
 * A renderer places the host's components by name, among the ones its format has.
 */
public interface AIUIRenderer {
    /** The format it reads. */
    public val format: AIUIFormat

    /**
     * Whether it reads documents of [version], or of no version when `null`. It reads every
     * version by default.
     *
     * @param version A document's version, or `null` when it has none.
     */
    public fun supports(version: String?): Boolean = true

    /**
     * What the agent's backend needs to have its model write UI this renderer shows with
     * [components], and runs [tools] from, such as the model's instructions that list the tools, as
     * plain Kotlin values. None by default.
     *
     * @param components The app's components the agent can place.
     * @param tools The app's tools a reply's UI can run. Each one's [AIClientTool.definition] has its
     * name, description, argument schema and read-only hint, ready to list in the instructions.
     */
    public fun capabilities(components: List<AIUIComponent>, tools: List<AIClientTool>): Map<String, Any?> =
        emptyMap()

    /**
     * Makes what shows the documents of one conversation, for [host].
     *
     * @param host The conversation's host.
     */
    public fun makeEngine(host: AIUIHost): AIUIEngine
}

/**
 * Shows the documents of one renderer in one conversation. The host makes one per renderer and
 * keeps it while it is kept, so an engine can hold each reply's state, and a format whose later
 * replies change an earlier one's UI can follow the conversation.
 *
 * The host calls it on the main thread.
 */
public interface AIUIEngine {
    /**
     * Takes a reply's document as it arrives or changes, oldest reply first, whether or not the
     * reply is on screen, so a format whose later replies change an earlier one's UI can follow
     * them all. It comes while the conversation composes, before any reply's content is asked
     * for: keep what is on screen in snapshot state so it follows. Nothing by default.
     *
     * @param document The reply's document.
     * @param messageId The reply's id.
     */
    public fun receive(document: AIUIDocument, messageId: String) {
        // Each reply's UI is its own.
    }

    /**
     * Forgets a reply that left the conversation, or whose UI did, such as one that was deleted,
     * so the engine can let go of what it kept for it. Nothing by default.
     *
     * @param messageId The reply's id.
     */
    public fun remove(messageId: String) {
        // Nothing kept for the reply.
    }

    /**
     * The content of a reply's document, or `null` when it shows nothing of its own, such as a
     * reply that only changed an earlier one's UI, so the reply's text shows.
     *
     * @param document The reply's document.
     * @param messageId The reply's id.
     */
    public fun content(document: AIUIDocument, messageId: String): (@Composable () -> Unit)?
}
