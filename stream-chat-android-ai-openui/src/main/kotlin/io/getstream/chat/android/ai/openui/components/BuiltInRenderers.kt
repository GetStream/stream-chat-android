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

package io.getstream.chat.android.ai.openui.components

import io.getstream.chat.android.ai.openui.rendering.ComponentRenderer

/** Registry of the built-in renderers (data-only components such as `Series` have none). */
internal object BuiltInRenderers {
    enum class Flavor { Chat, Standard }

    /** Renderers shared by both libraries. */
    private val shared: Map<String, ComponentRenderer>
        get() = contentRenderers + tableRenderers + layoutRenderers

    fun forLibrary(flavor: Flavor): Map<String, ComponentRenderer?> = when (flavor) {
        Flavor.Chat -> shared + chatContentRenderers + chatLayoutRenderers
        Flavor.Standard -> shared + standardContentRenderers + standardLayoutRenderers
    }
}
