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

package io.getstream.chat.android.ai.a2ui.generativeui

import io.getstream.chat.android.ai.a2ui.core.RendererError

/**
 * A problem with a reply's A2UI, as A2UI reports it to the agent: a message that doesn't read, a
 * component or function its catalog doesn't have, or a value its data model can't take.
 *
 * @param code The error's code in A2UI 1.0, such as `VALIDATION_FAILED`.
 * @param message What went wrong, written for the agent.
 * @param surfaceId The surface it happened on, when it belongs to one.
 * @param path A JSON Pointer to the part of the agent's message at fault, when there is one.
 */
public data class A2UIError internal constructor(
    public val code: String,
    public val message: String,
    public val surfaceId: String? = null,
    public val path: String? = null,
) {
    internal constructor(error: RendererError) : this(error.code, error.message, error.surfaceId, error.path)

    override fun toString(): String = "$code: $message"
}
