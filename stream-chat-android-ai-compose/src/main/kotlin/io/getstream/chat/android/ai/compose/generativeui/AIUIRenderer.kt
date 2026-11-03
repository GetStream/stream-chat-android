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
     */
    public fun supports(version: String?): Boolean = true

    /**
     * What the agent's backend needs to have its model write UI this renderer shows with
     * [components], such as the model's instructions, as plain Kotlin values. None by default.
     */
    public fun capabilities(components: List<AIUIComponent>): Map<String, Any?> = emptyMap()

    /** Makes what shows the documents of one conversation, for [host]. */
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
     */
    public fun receive(document: AIUIDocument, messageId: String) {
        // Each reply's UI is its own.
    }

    /**
     * The content of a reply's document, or `null` when it shows nothing of its own, such as a
     * reply that only changed an earlier one's UI, so the reply's text shows.
     */
    public fun content(document: AIUIDocument, messageId: String): (@Composable () -> Unit)?
}
