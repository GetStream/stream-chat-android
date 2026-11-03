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

package io.getstream.chat.android.ai.a2ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

/** Draws one component type of a surface, from what [A2UIComponentScope] reads of it. */
public typealias A2UIComponentRenderer = @Composable (scope: A2UIComponentScope) -> Unit

/**
 * How each component type of a surface looks, by type, such as `Text`.
 *
 * [Basic] draws every component of the A2UI 1.0 basic catalog. Start from it to draw a type your
 * own way; the agent still writes the type as the catalog describes it:
 *
 * ```
 * val catalog = A2UICatalog.Basic.with("Card") { scope ->
 *     BrandCard(modifier = scope.modifier) { scope.child()?.let { scope.Child(it) } }
 * }
 * ```
 *
 * A type the catalog has no renderer for shows nothing.
 *
 * @param renderers The renderer of each type.
 */
@Immutable
public class A2UICatalog(public val renderers: Map<String, A2UIComponentRenderer>) {

    /** The renderer of [type], or `null` when the catalog has none. */
    public operator fun get(type: String): A2UIComponentRenderer? = renderers[type]

    /** A copy that draws [type] with [renderer]. */
    public fun with(type: String, renderer: A2UIComponentRenderer): A2UICatalog =
        A2UICatalog(renderers + (type to renderer))

    /** A catalog with the renderers of both; the ones of [other] win. */
    public operator fun plus(other: A2UICatalog): A2UICatalog = A2UICatalog(renderers + other.renderers)

    public companion object {
        /** Every component of the A2UI 1.0 basic catalog. */
        public val Basic: A2UICatalog = A2UICatalog(
            mapOf(
                "Text" to { scope -> A2UIText(scope) },
                "Image" to { scope -> A2UIImage(scope) },
                "Icon" to { scope -> A2UIIcon(scope) },
                "Video" to { scope -> A2UIVideo(scope) },
                "AudioPlayer" to { scope -> A2UIAudioPlayer(scope) },
                "Row" to { scope -> A2UIRow(scope) },
                "Column" to { scope -> A2UIColumn(scope) },
                "List" to { scope -> A2UIList(scope) },
                "Card" to { scope -> A2UICard(scope) },
                "Tabs" to { scope -> A2UITabs(scope) },
                "Modal" to { scope -> A2UIModal(scope) },
                "Divider" to { scope -> A2UIDivider(scope) },
                "Button" to { scope -> A2UIButton(scope) },
                "TextField" to { scope -> A2UITextField(scope) },
                "CheckBox" to { scope -> A2UICheckBox(scope) },
                "ChoicePicker" to { scope -> A2UIChoicePicker(scope) },
                "Slider" to { scope -> A2UISlider(scope) },
                "DateTimeInput" to { scope -> A2UIDateTimeInput(scope) },
            ),
        )
    }
}
