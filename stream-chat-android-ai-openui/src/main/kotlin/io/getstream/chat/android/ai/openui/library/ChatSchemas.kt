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

// Schemas specific to the chat library (`openuiChatLibrary`).
internal object ChatSchemas {
    val card: ComponentSchema = ComponentSchema(
        "Card",
        description = "Vertical container for all content in a chat response. Children stack top to bottom " +
            "automatically. Optional sources ([{ title, sourceName, url }]) render as a Sources strip at the bottom " +
            "and back inline [n] citations in TextContent.",
        props = listOf(
            required(
                "children",
                array(components(ContentUnions.chatNested + listOf("SectionBlock", "Tabs", "Carousel"))),
            ),
            optional(
                "sources",
                array(obj(listOf(optional("url", string), required("title", string), required("sourceName", string)))),
            ),
        ),
    )

    val followUpBlock: ComponentSchema = ComponentSchema(
        "FollowUpBlock",
        description = "List of clickable follow-up suggestions placed at the end of a response",
        props = listOf(required("items", array(component("FollowUpItem")))),
    )

    val followUpItem: ComponentSchema = ComponentSchema(
        "FollowUpItem",
        description = "Clickable follow-up suggestion — when clicked, sends text as user message",
        props = listOf(required("text", string)),
    )

    val sectionBlock: ComponentSchema = ComponentSchema(
        "SectionBlock",
        description = "Collapsible accordion sections. Auto-opens sections as they stream in. Use SectionItem for " +
            "each section.",
        props = listOf(required("sections", array(component("SectionItem"))), optional("isFoldable", boolean)),
    )

    val sectionItem: ComponentSchema = ComponentSchema(
        "SectionItem",
        description = "Section with a label and collapsible content — used inside SectionBlock",
        props = listOf(
            required("value", string),
            required("trigger", string),
            required("content", array(components(ContentUnions.chatNested + listOf("Tabs", "Accordion")))),
        ),
    )

    val tabItem: ComponentSchema = ComponentSchema(
        "TabItem",
        description = "value is unique id, trigger is tab label, content is array of components",
        props = listOf(
            required("value", string),
            required("trigger", string),
            required("content", array(components(ContentUnions.chatNested + listOf("Accordion")))),
        ),
    )

    val accordionItem: ComponentSchema = ComponentSchema(
        "AccordionItem",
        description = "value is unique id, trigger is section title",
        props = listOf(
            required("value", string),
            required("trigger", string),
            required("content", array(components(ContentUnions.chatNested))),
        ),
    )

    val carousel: ComponentSchema = ComponentSchema(
        "Carousel",
        description = "Horizontal scrollable carousel",
        props = listOf(
            required("children", array(array(components(ContentUnions.chatNested)))),
            optional("variant", enumeration(listOf("card", "sunk"))),
        ),
    )
}
