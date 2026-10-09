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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

/**
 * Builds a compound [ChatAiComponentFactory] by wrapping the current factory and provides it
 * to [content] through [LocalChatAiComponentFactory].
 *
 * Use this to override some components while keeping the rest, without rebuilding the whole
 * factory:
 *
 * ```
 * CompoundChatAiComponentFactory(
 *     factory = { current ->
 *         object : ChatAiComponentFactory by current {
 *             @Composable
 *             override fun RowScope.ComposerLeadingContent(params: ComposerLeadingContentParams) {
 *                 // Render nothing to hide the attachment button.
 *             }
 *         }
 *     },
 * ) {
 *     ChatComposer(/* ... */)
 * }
 * ```
 *
 * @param keys Keys that control recomposition. When any key changes, [factory] runs again to
 * build a new compound factory.
 * @param factory Builds a new [ChatAiComponentFactory] from the current one.
 * @param content The content that uses the compound factory.
 */
@Composable
public fun CompoundChatAiComponentFactory(
    vararg keys: Any?,
    factory: (current: ChatAiComponentFactory) -> ChatAiComponentFactory,
    content: @Composable () -> Unit,
) {
    val current = LocalChatAiComponentFactory.current
    val compound = remember(*keys) { factory(current) }
    CompositionLocalProvider(
        value = LocalChatAiComponentFactory provides compound,
        content = content,
    )
}
