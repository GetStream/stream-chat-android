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

/**
 * The language a reply's generated UI is written in, such as OpenUI. It is an open set: each
 * renderer says which one it reads.
 *
 * @param rawValue The format's name, as the agent writes it in `ai_ui.format`.
 */
@JvmInline
public value class AIUIFormat(public val rawValue: String) {
    override fun toString(): String = rawValue

    public companion object {
        /** [OpenUI](https://github.com/thesysdev/openui) Lang, a compact program. */
        public val OpenUI: AIUIFormat = AIUIFormat("openui")

        /** [A2UI](https://a2ui.org), JSON messages that build surfaces. */
        public val A2UI: AIUIFormat = AIUIFormat("a2ui")
    }
}
