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

// Schemas specific to the standard library (`openuiLibrary`).
internal object StandardSchemas {
    public val card: ComponentSchema = ComponentSchema(
        "Card",
        description = "Styled container. variant: \"card\" (default, elevated) | \"sunk\" (recessed) | \"clear\" " +
            "(transparent). Always full width. Accepts all Stack flex params (default: direction \"column\"). Cards " +
            "flex to share space in row/wrap layouts.",
        props = listOf(
            required("children", array(components(ContentUnions.base + listOf("Tabs", "Carousel", "Stack")))),
            optional("variant", enumeration(listOf("card", "sunk", "clear"))),
            optional("direction", enumeration(listOf("row", "column"))),
            optional("gap", enumeration(listOf("none", "xs", "s", "m", "l", "xl", "2xl"))),
            optional("align", enumeration(listOf("start", "center", "end", "stretch", "baseline"))),
            optional("justify", enumeration(listOf("start", "center", "end", "between", "around", "evenly"))),
            optional("wrap", boolean),
        ),
    )

    public val stack: ComponentSchema = ComponentSchema(
        "Stack",
        description = "Flex container. direction: \"row\"|\"column\" (default \"column\"). gap: " +
            "\"none\"|\"xs\"|\"s\"|\"m\"|\"l\"|\"xl\"|\"2xl\" (default \"m\"). align: " +
            "\"start\"|\"center\"|\"end\"|\"stretch\"|\"baseline\". justify: " +
            "\"start\"|\"center\"|\"end\"|\"between\"|\"around\"|\"evenly\".",
        props = listOf(
            required("children", array(any)),
            optional("direction", enumeration(listOf("row", "column"))),
            optional("gap", enumeration(listOf("none", "xs", "s", "m", "l", "xl", "2xl"))),
            optional("align", enumeration(listOf("start", "center", "end", "stretch", "baseline"))),
            optional("justify", enumeration(listOf("start", "center", "end", "between", "around", "evenly"))),
            optional("wrap", boolean),
        ),
    )

    public val tabItem: ComponentSchema = ComponentSchema(
        "TabItem",
        description = "value is unique id, trigger is tab label, content is array of components",
        props = listOf(
            required("value", string),
            required("trigger", string),
            required("content", array(components(ContentUnions.base))),
        ),
    )

    public val accordionItem: ComponentSchema = ComponentSchema(
        "AccordionItem",
        description = "value is unique id, trigger is section title",
        props = listOf(
            required("value", string),
            required("trigger", string),
            required("content", array(components(ContentUnions.base))),
        ),
    )

    public val carousel: ComponentSchema = ComponentSchema(
        "Carousel",
        description = "Horizontal scrollable carousel",
        props = listOf(
            required("children", array(array(components(ContentUnions.base)))),
            optional("variant", enumeration(listOf("card", "sunk"))),
        ),
    )

    public val modal: ComponentSchema = ComponentSchema(
        "Modal",
        description = "Modal dialog. open is a reactive \$boolean binding — set to true to open, X/Escape/backdrop " +
            "auto-closes. Put Form with buttons inside children.",
        props = listOf(
            required("title", string),
            binding("open", boolean),
            required("children", array(components(ContentUnions.base))),
            optional("size", enumeration(listOf("sm", "md", "lg"))),
        ),
    )
}
