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

package io.getstream.chat.android.ai.openui.library

import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.stringValue

/** The library of the OpenUI 1.0 specification's fixtures, defined with this module's API. */
internal object SpecLibrary {
    val upper = DefinedFunction(
        name = "Upper",
        description = "The text in upper case",
        params = listOf(PropField.required("text", PropType.string)),
        returns = PropType.string,
    ) { args -> OpenUIValue.Str(args["text"].stringValue.orEmpty().uppercase()) }

    val copyToClipboard = DefinedAction(
        name = "CopyToClipboard",
        description = "Copies text to the clipboard",
        params = listOf(PropField.required("text", PropType.string)),
    )

    private fun schema(name: String, description: String, vararg props: PropField) =
        ComponentSchema(name, description, props.toList())

    val library = LibraryDefinition(
        root = "Stack",
        components = listOf(
            schema(
                "Stack",
                "Lays out children in a column or a row",
                required("children", array(any)),
                optional("direction", enumeration(listOf("row", "column"))),
            ),
            schema("Card", "A titled container", required("title", string), optional("children", array(any))),
            schema(
                "Text",
                "A run of text",
                required("text", string),
                optional("variant", enumeration(listOf("body", "heading"))),
            ),
            schema(
                "Tag",
                "A small colored label",
                required("text", string),
                optional("color", enumeration(listOf("gray", "green", "red"))),
            ),
            schema("Metric", "A labeled number", required("label", string), required("value", number)),
            schema(
                "Button",
                "A clickable button",
                required("label", string),
                optional("action", action),
                optional("variant", enumeration(listOf("primary", "secondary"))),
                optional("share", PropType.actions(listOf("CopyToClipboard", "OpenUrl"))),
            ),
            schema(
                "Form",
                "Groups its inputs under a form name",
                required("name", string),
                required("children", array(any)),
            ),
            schema(
                "Input",
                "A text field",
                required("name", string),
                optional("placeholder", string),
                binding("value", string),
                optional("rules", obj(emptyList())),
            ),
            schema(
                "Select",
                "A dropdown",
                required("name", string),
                required("items", array(any)),
                binding("value", string),
            ),
            schema("SelectItem", "One option of a Select", required("value", string), required("label", string)),
            schema("Table", "A table built from columns", required("columns", array(any))),
            schema(
                "Col",
                "One table column: a header and its cell values",
                required("label", string),
                optional("data", array(any)),
            ),
        ),
        functions = listOf(upper),
        customActions = listOf(copyToClipboard),
    )
}
