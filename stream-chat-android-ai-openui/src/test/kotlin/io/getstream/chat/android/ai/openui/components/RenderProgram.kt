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

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import io.getstream.chat.android.ai.openui.lang.ActionEvent
import io.getstream.chat.android.ai.openui.rendering.Library
import io.getstream.chat.android.ai.openui.rendering.Renderer
import io.getstream.chat.android.ai.openui.rendering.openuiLibrary

/** Renders an OpenUI Lang [program] with [library] and waits until it settles. */
internal fun ComposeContentTestRule.renderProgram(
    program: String,
    library: Library = openuiLibrary,
    isStreaming: Boolean = false,
    onAction: ((ActionEvent) -> Unit)? = null,
    onOpenUrl: ((String) -> Unit)? = null,
) {
    setContent {
        Renderer(
            response = program,
            library = library,
            isStreaming = isStreaming,
            onAction = onAction,
            onOpenUrl = onOpenUrl,
        )
    }
    waitForIdle()
}
