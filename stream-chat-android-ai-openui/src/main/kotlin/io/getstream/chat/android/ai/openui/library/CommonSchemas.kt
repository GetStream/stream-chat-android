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

// Component content unions (mirrors `unions.ts`).
internal object ContentUnions {
    // Components allowed inside layout containers.
    public val base: List<String> = listOf(
        "TextContent",
        "MarkDownRenderer",
        "CardHeader",
        "Callout",
        "TextCallout",
        "CodeBlock",
        "Image",
        "ImageBlock",
        "ImageGallery",
        "Separator",
        "HorizontalBarChart",
        "RadarChart",
        "PieChart",
        "RadialChart",
        "SingleStackedBarChart",
        "ScatterChart",
        "AreaChart",
        "BarChart",
        "LineChart",
        "Table",
        "TagBlock",
        "Form",
        "Buttons",
        "IconButton",
        "Steps",
        "InlineHeader",
        "EntityList",
        "EditableTable",
        "SnippetCardBlock",
        "OverviewCardBlock",
        "ContextCardBlock",
        "CompositeCardBlock",
        "VisualCardBlock",
    )

    // Everything a nested chat container may hold.
    public val chatNested: List<String> = base + listOf("ListBlock", "FollowUpBlock")
}

/** Schemas shared by the chat and standard libraries. */
@Suppress("LargeClass") // The definitions of upstream's components, one value each.
internal object CommonSchemas {
    public val cardHeader: ComponentSchema = ComponentSchema(
        "CardHeader",
        description = "Header with optional title and subtitle",
        props = listOf(optional("title", string), optional("subtitle", string)),
    )

    public val textContent: ComponentSchema = ComponentSchema(
        "TextContent",
        description = "Text block. Supports markdown. Optional size: \"small\" | \"default\" | \"large\" | " +
            "\"small-heavy\" | \"large-heavy\".",
        props = listOf(
            required("text", string),
            optional("size", enumeration(listOf("small", "default", "large", "small-heavy", "large-heavy"))),
        ),
    )

    public val markDownRenderer: ComponentSchema = ComponentSchema(
        "MarkDownRenderer",
        description = "Renders markdown text with optional container variant",
        props = listOf(
            required("textMarkdown", string),
            optional("variant", enumeration(listOf("clear", "card", "sunk"))),
        ),
    )

    public val callout: ComponentSchema = ComponentSchema(
        "Callout",
        description = "Callout banner. Optional visible is a reactive \$boolean — auto-dismisses after 3s by setting " +
            "\$visible to false.",
        props = listOf(
            required("variant", enumeration(listOf("info", "warning", "error", "success", "neutral"))),
            required("title", string),
            required("description", string),
            binding("visible", boolean),
        ),
    )

    public val textCallout: ComponentSchema = ComponentSchema(
        "TextCallout",
        description = "Text callout with variant, title, and description",
        props = listOf(
            optional("variant", enumeration(listOf("neutral", "info", "warning", "success", "danger"))),
            optional("title", string),
            optional("description", string),
        ),
    )

    public val image: ComponentSchema = ComponentSchema(
        "Image",
        description = "Image with alt text and optional URL",
        props = listOf(required("alt", string), optional("src", string)),
    )

    public val imageBlock: ComponentSchema = ComponentSchema(
        "ImageBlock",
        description = "Image block with loading state",
        props = listOf(required("src", string), optional("alt", string)),
    )

    public val imageGallery: ComponentSchema = ComponentSchema(
        "ImageGallery",
        description = "Gallery grid of images with modal preview",
        props = listOf(
            required(
                "images",
                array(obj(listOf(required("src", string), optional("alt", string), optional("details", string)))),
            ),
        ),
    )

    public val codeBlock: ComponentSchema = ComponentSchema(
        "CodeBlock",
        description = "Syntax-highlighted code block",
        props = listOf(required("language", string), required("codeString", string)),
    )

    public val separator: ComponentSchema = ComponentSchema(
        "Separator",
        description = "Visual divider between content sections",
        props = listOf(
            optional("orientation", enumeration(listOf("horizontal", "vertical"))),
            optional("decorative", boolean),
        ),
    )

    public val table: ComponentSchema = ComponentSchema(
        "Table",
        description = "Data table — column-oriented. Each Col holds its own data array.",
        props = listOf(required("columns", array(component("Col")))),
    )

    public val col: ComponentSchema = ComponentSchema(
        "Col",
        description = "Column definition — holds label + data array",
        props = listOf(
            required("label", string),
            required("data", any),
            optional("type", enumeration(listOf("string", "number", "action"))),
        ),
    )

    public val barChart: ComponentSchema = ComponentSchema(
        "BarChart",
        description = "Vertical bars; use for comparing values across categories with one or more series",
        props = listOf(
            required("labels", array(string)),
            required("series", array(component("Series"))),
            optional("variant", enumeration(listOf("grouped", "stacked"))),
            optional("xLabel", string),
            optional("yLabel", string),
            optional("height", number),
        ),
    )

    public val lineChart: ComponentSchema = ComponentSchema(
        "LineChart",
        description = "Lines over categories; use for trends and continuous data over time",
        props = listOf(
            required("labels", array(string)),
            required("series", array(component("Series"))),
            optional("variant", enumeration(listOf("linear", "natural", "step"))),
            optional("xLabel", string),
            optional("yLabel", string),
            optional("height", number),
        ),
    )

    public val areaChart: ComponentSchema = ComponentSchema(
        "AreaChart",
        description = "Filled area under lines; use for cumulative totals or volume trends over time",
        props = listOf(
            required("labels", array(string)),
            required("series", array(component("Series"))),
            optional("variant", enumeration(listOf("linear", "natural", "step"))),
            optional("xLabel", string),
            optional("yLabel", string),
            optional("height", number),
        ),
    )

    public val radarChart: ComponentSchema = ComponentSchema(
        "RadarChart",
        description = "Spider/web chart; use for comparing multiple variables across one or more entities",
        props = listOf(required("labels", array(string)), required("series", array(component("Series")))),
    )

    public val horizontalBarChart: ComponentSchema = ComponentSchema(
        "HorizontalBarChart",
        description = "Horizontal bars; prefer when category labels are long or for ranked lists",
        props = listOf(
            required("labels", array(string)),
            required("series", array(component("Series"))),
            optional("variant", enumeration(listOf("grouped", "stacked"))),
            optional("xLabel", string),
            optional("yLabel", string),
        ),
    )

    public val series: ComponentSchema = ComponentSchema(
        "Series",
        description = "One data series",
        props = listOf(required("category", string), required("values", array(number))),
    )

    public val pieChart: ComponentSchema = ComponentSchema(
        "PieChart",
        description = "Circular slices; use plucked arrays: PieChart(data.categories, data.values)",
        props = listOf(
            required("labels", array(string)),
            required("values", array(number)),
            optional("variant", enumeration(listOf("pie", "donut"))),
            optional("appearance", enumeration(listOf("circular", "semiCircular"))),
        ),
    )

    public val radialChart: ComponentSchema = ComponentSchema(
        "RadialChart",
        description = "Radial bars; use plucked arrays: RadialChart(data.categories, data.values)",
        props = listOf(required("labels", array(string)), required("values", array(number))),
    )

    public val singleStackedBarChart: ComponentSchema = ComponentSchema(
        "SingleStackedBarChart",
        description = "Single horizontal stacked bar; use plucked arrays: SingleStackedBarChart(data.categories, " +
            "data.values)",
        props = listOf(required("labels", array(string)), required("values", array(number))),
    )

    public val slice: ComponentSchema = ComponentSchema(
        "Slice",
        description = "One slice with label and numeric value",
        props = listOf(required("category", string), required("value", number)),
    )

    public val scatterChart: ComponentSchema = ComponentSchema(
        "ScatterChart",
        description = "X/Y scatter plot; use for correlations, distributions, and clustering",
        props = listOf(
            required("datasets", array(component("ScatterSeries"))),
            optional("xLabel", string),
            optional("yLabel", string),
        ),
    )

    public val scatterSeries: ComponentSchema = ComponentSchema(
        "ScatterSeries",
        description = "Named dataset",
        props = listOf(required("name", string), required("points", array(component("Point")))),
    )

    public val point: ComponentSchema = ComponentSchema(
        "Point",
        description = "Data point with numeric coordinates",
        props = listOf(required("x", number), required("y", number), optional("z", number)),
    )

    public val form: ComponentSchema = ComponentSchema(
        "Form",
        description = "Form container with fields and explicit action buttons",
        props = listOf(
            required("name", string),
            required("buttons", component("Buttons")),
            defaulted("fields", array(component("FormControl")), emptyList<Any>()),
        ),
    )

    public val formControl: ComponentSchema = ComponentSchema(
        "FormControl",
        description = "Field with label, input component, and optional hint text",
        props = listOf(
            required("label", string),
            required(
                "input",
                components(
                    listOf(
                        "Input", "TextArea", "Select", "DatePicker", "Slider", "CheckBoxGroup", "RadioGroup", "Chips",
                        "OptionCards",
                    ),
                ),
            ),
            optional("hint", string),
        ),
    )

    public val label: ComponentSchema = ComponentSchema(
        "Label",
        description = "Text label",
        props = listOf(required("text", string)),
    )

    public val input: ComponentSchema = ComponentSchema(
        "Input",
        description = "",
        props = listOf(
            required("name", string),
            optional("placeholder", string),
            optional("type", enumeration(listOf("text", "email", "password", "number", "url"))),
            optional("rules", validationRules),
            binding("value", string),
        ),
    )

    public val textArea: ComponentSchema = ComponentSchema(
        "TextArea",
        description = "",
        props = listOf(
            required("name", string),
            optional("placeholder", string),
            optional("rows", number),
            optional("rules", validationRules),
            binding("value", string),
        ),
    )

    public val select: ComponentSchema = ComponentSchema(
        "Select",
        description = "",
        props = listOf(
            required("name", string),
            required("items", array(component("SelectItem"))),
            optional("placeholder", string),
            optional("rules", validationRules),
            binding("value", string),
            optional("size", enumeration(listOf("small", "medium", "large"))),
        ),
    )

    public val selectItem: ComponentSchema = ComponentSchema(
        "SelectItem",
        description = "Option for Select",
        props = listOf(required("value", string), required("label", string)),
    )

    public val datePicker: ComponentSchema = ComponentSchema(
        "DatePicker",
        description = "",
        props = listOf(
            required("name", string),
            optional("mode", enumeration(listOf("single", "range"))),
            optional("rules", validationRules),
            binding("value", any),
        ),
    )

    public val slider: ComponentSchema = ComponentSchema(
        "Slider",
        description = "Numeric slider input; supports continuous and discrete (stepped) variants",
        props = listOf(
            required("name", string),
            required("variant", enumeration(listOf("continuous", "discrete"))),
            required("min", number),
            required("max", number),
            optional("step", number),
            optional("defaultValue", array(number)),
            optional("label", string),
            optional("rules", validationRules),
            binding("value", array(number)),
        ),
    )

    public val checkBoxGroup: ComponentSchema = ComponentSchema(
        "CheckBoxGroup",
        description = "",
        props = listOf(
            required("name", string),
            required("items", array(component("CheckBoxItem"))),
            optional("rules", validationRules),
            binding("value", record(boolean)),
        ),
    )

    public val checkBoxItem: ComponentSchema = ComponentSchema(
        "CheckBoxItem",
        description = "",
        props = listOf(
            required("label", string),
            required("description", string),
            required("name", string),
            optional("defaultChecked", boolean),
        ),
    )

    public val radioGroup: ComponentSchema = ComponentSchema(
        "RadioGroup",
        description = "",
        props = listOf(
            required("name", string),
            required("items", array(component("RadioItem"))),
            optional("defaultValue", string),
            optional("rules", validationRules),
            binding("value", string),
        ),
    )

    public val radioItem: ComponentSchema = ComponentSchema(
        "RadioItem",
        description = "",
        props = listOf(required("label", string), required("description", string), required("value", string)),
    )

    public val switchGroup: ComponentSchema = ComponentSchema(
        "SwitchGroup",
        description = "Group of switch toggles",
        props = listOf(
            required("name", string),
            required("items", array(component("SwitchItem"))),
            optional("variant", enumeration(listOf("clear", "card", "sunk"))),
            binding("value", record(boolean)),
        ),
    )

    public val switchItem: ComponentSchema = ComponentSchema(
        "SwitchItem",
        description = "Individual switch toggle",
        props = listOf(
            optional("label", string),
            optional("description", string),
            required("name", string),
            optional("defaultChecked", boolean),
        ),
    )

    public val button: ComponentSchema = ComponentSchema(
        "Button",
        description = "Clickable button",
        props = listOf(
            required("label", string),
            optional("action", action),
            optional("variant", enumeration(listOf("primary", "secondary", "tertiary"))),
            optional("type", enumeration(listOf("normal", "destructive"))),
            optional("size", enumeration(listOf("extra-small", "small", "medium", "large"))),
        ),
    )

    public val buttons: ComponentSchema = ComponentSchema(
        "Buttons",
        description = "Group of Button components. direction: \"row\" (default) | \"column\".",
        props = listOf(
            required("buttons", array(component("Button"))),
            optional("direction", enumeration(listOf("row", "column"))),
        ),
    )

    public val listBlock: ComponentSchema = ComponentSchema(
        "ListBlock",
        description = "A list of items with number or image indicators. Each item can optionally have an action. " +
            "size small renders a compact list.",
        props = listOf(
            required("items", array(component("ListItem"))),
            optional("variant", enumeration(listOf("number", "image"))),
            optional("size", enumeration(listOf("default", "small"))),
        ),
    )

    public val listItem: ComponentSchema = ComponentSchema(
        "ListItem",
        description = "Item in a ListBlock — displays a title with an optional subtitle and image. When action is " +
            "provided, the item becomes clickable.",
        props = listOf(
            required("title", string),
            optional("subtitle", string),
            optional("image", obj(listOf(required("src", string), required("alt", string)))),
            optional("actionLabel", string),
            optional("action", action),
        ),
    )

    public val tabs: ComponentSchema = ComponentSchema(
        "Tabs",
        description = "Tabbed container",
        props = listOf(required("items", array(component("TabItem")))),
    )

    public val accordion: ComponentSchema = ComponentSchema(
        "Accordion",
        description = "Collapsible sections",
        props = listOf(required("items", array(component("AccordionItem")))),
    )

    public val steps: ComponentSchema = ComponentSchema(
        "Steps",
        description = "Step-by-step guide",
        props = listOf(required("items", array(component("StepsItem")))),
    )

    public val stepsItem: ComponentSchema = ComponentSchema(
        "StepsItem",
        description = "title and details text for one step",
        props = listOf(required("title", string), required("details", string)),
    )

    public val tagBlock: ComponentSchema = ComponentSchema(
        "TagBlock",
        description = "tags is an array of strings; optional size sm | md | lg",
        props = listOf(required("tags", array(string)), optional("size", enumeration(listOf("sm", "md", "lg")))),
    )

    public val tag: ComponentSchema = ComponentSchema(
        "Tag",
        description = "Styled tag/badge with optional Icon and variant",
        props = listOf(
            required("text", string),
            optional("icon", component("Icon")),
            optional("size", enumeration(listOf("sm", "md", "lg"))),
            optional("variant", enumeration(listOf("neutral", "info", "success", "warning", "danger"))),
        ),
    )

    public val entityList: ComponentSchema = ComponentSchema(
        "EntityList",
        description = "Two-column key/value rows (left label, right value). size 'default' supports optional header " +
            "and footer rows; rightVariant 'number' uses tabular numbers.",
        props = listOf(
            defaulted(
                "rows",
                array(
                    obj(
                        listOf(
                            required("left", string),
                            required("right", string),
                            defaulted("rightVariant", enumeration(listOf("text", "number")), "text"),
                        ),
                    ),
                ),
                emptyList<Any>(),
            ),
            defaulted("size", enumeration(listOf("small", "default")), "default"),
            optional(
                "header",
                obj(
                    listOf(
                        required("left", string),
                        required("right", string),
                        defaulted("rightVariant", enumeration(listOf("text", "number")), "text"),
                    ),
                ),
            ),
            optional(
                "footer",
                obj(
                    listOf(
                        required("left", string),
                        required("right", string),
                        defaulted("rightVariant", enumeration(listOf("text", "number")), "text"),
                    ),
                ),
            ),
        ),
    )

    public val inlineHeader: ComponentSchema = ComponentSchema(
        "InlineHeader",
        description = "Compact section heading with an optional one-line description, for use inside cards.",
        props = listOf(required("heading", string), optional("description", string)),
    )

    public val icon: ComponentSchema = ComponentSchema(
        "Icon",
        description = "A lucide icon by kebab-case name (e.g. 'circle-check'). Optional category picks a topical " +
            "fallback when the name doesn't resolve.",
        props = listOf(required("name", string), optional("category", string)),
    )

    public val iconButton: ComponentSchema = ComponentSchema(
        "IconButton",
        description = "Icon-only button. name is the accessible label and the action label; icon is an Icon; action " +
            "fires on click.",
        props = listOf(
            required("name", string),
            required("icon", component("Icon")),
            optional("action", action),
            optional("variant", enumeration(listOf("primary", "secondary", "tertiary"))),
            optional("size", enumeration(listOf("extra-small", "small", "medium", "large"))),
            optional("shape", enumeration(listOf("square", "circle"))),
        ),
    )

    public val editableTable: ComponentSchema = ComponentSchema(
        "EditableTable",
        description = "Spreadsheet-like table whose cells the user can edit inline (text, number, url, date, select " +
            "columns); edits are saved back as a form field",
        props = listOf(
            defaulted("name", string, ""),
            defaulted(
                "columns",
                array(
                    obj(
                        listOf(
                            required("type", enumeration(listOf("text", "number", "date-single", "select", "url"))),
                            defaulted("key", string, "default"),
                            defaulted("header", string, ""),
                            optional("width", number),
                            optional(
                                "options",
                                array(obj(listOf(required("value", string), required("label", string)))),
                            ),
                        ),
                    ),
                ),
                emptyList<Any>(),
            ),
            defaulted(
                "data",
                array(obj(listOf(required("id", string), required("values", array(union(listOf(string, number))))))),
                emptyList<Any>(),
            ),
        ),
    )

    public val chipItem: ComponentSchema = ComponentSchema(
        "ChipItem",
        description = "A single selectable chip inside a Chips group, with a value, label and optional icon.",
        props = listOf(
            required("value", string),
            required("label", string),
            optional("icon", component("Icon")),
            optional("disabled", boolean),
        ),
    )

    public val chips: ComponentSchema = ComponentSchema(
        "Chips",
        description = "A form field of compact selectable chips for choosing one or many short options; the " +
            "selection is stored under `name`.",
        props = listOf(
            required("name", string),
            defaulted("type", enumeration(listOf("single", "multiple")), "multiple"),
            defaulted("items", array(component("ChipItem")), emptyList<Any>()),
            optional("rules", validationRules),
            optional("defaultValue", union(listOf(string, array(string)))),
        ),
    )

    public val optionCard: ComponentSchema = ComponentSchema(
        "OptionCard",
        description = "A single selectable card inside an OptionCards group, with a value, title, optional subtitle " +
            "and an optional Icon or Image on top.",
        props = listOf(
            required("value", string),
            required("title", string),
            optional("subtitle", string),
            optional("topContent", components(listOf("Icon", "Image"))),
            optional("disabled", boolean),
        ),
    )

    public val optionCards: ComponentSchema = ComponentSchema(
        "OptionCards",
        description = "A form field of selectable cards (title, optional subtitle, optional icon or image) laid out " +
            "in a responsive grid for choosing one or many options; the selection is stored under `name`.",
        props = listOf(
            required("name", string),
            defaulted("type", enumeration(listOf("single", "multiple")), "single"),
            defaulted("items", array(component("OptionCard")), emptyList<Any>()),
            optional("rules", validationRules),
            optional("defaultValue", union(listOf(string, array(string)))),
        ),
    )

    public val text: ComponentSchema = ComponentSchema(
        "Text",
        description = "Plain text line with optional subtext. variant 'number' uses tabular number styling; " +
            "subtextVariant 'metric' colors a leading +/- subtext green/red.",
        props = listOf(
            defaulted("variant", enumeration(listOf("text", "number")), "text"),
            required("value", string),
            optional("subtext", string),
            defaulted("subtextVariant", enumeration(listOf("text", "number", "metric")), "text"),
            defaulted("size", enumeration(listOf("xs", "sm", "md", "lg")), "sm"),
        ),
    )

    public val boldText: ComponentSchema = ComponentSchema(
        "BoldText",
        description = "Emphasized (bold) text line with optional subtext. variant 'number' uses tabular number " +
            "styling; subtextVariant 'metric' colors a leading +/- subtext green/red.",
        props = listOf(
            defaulted("variant", enumeration(listOf("text", "number")), "text"),
            required("value", string),
            optional("subtext", string),
            defaulted("subtextVariant", enumeration(listOf("text", "number", "metric")), "text"),
            defaulted("size", enumeration(listOf("xs", "sm", "md", "lg")), "sm"),
        ),
    )

    public val iconText: ComponentSchema = ComponentSchema(
        "IconText",
        description = "An icon badge with a title and optional subtitle, laid out horizontally or vertically. " +
            "iconVariant sets the badge color.",
        props = listOf(
            required("icon", component("Icon")),
            defaulted(
                "iconVariant",
                enumeration(listOf("neutral", "info", "success", "warning", "danger", "inverted", "filled", "soft")),
                "neutral",
            ),
            defaulted("iconSize", enumeration(listOf("xs", "s", "m", "l", "xl", "sm", "md", "lg")), "m"),
            required("title", string),
            optional("subtitle", string),
            defaulted("bold", boolean, false),
            defaulted("layout", enumeration(listOf("horizontal", "vertical")), "horizontal"),
        ),
    )

    public val imageText: ComponentSchema = ComponentSchema(
        "ImageText",
        description = "A small square image (thumbnail/avatar) with a title and optional subtitle. src must be a " +
            "real image URL.",
        props = listOf(
            required("src", string),
            optional("alt", string),
            required("title", string),
            optional("subtitle", string),
            defaulted("bold", boolean, false),
            defaulted("layout", enumeration(listOf("horizontal", "vertical")), "horizontal"),
            optional("imageSize", number),
        ),
    )

    public val imageTextLarge: ComponentSchema = ComponentSchema(
        "ImageTextLarge",
        description = "A full-width banner image above a bold title and optional subtitle. src must be a real image " +
            "URL.",
        props = listOf(
            required("src", string),
            optional("alt", string),
            required("title", string),
            optional("subtitle", string),
            defaulted("bold", boolean, false),
        ),
    )

    public val metricIndicatorInline: ComponentSchema = ComponentSchema(
        "MetricIndicatorInline",
        description = "Headline metric value with an optional +/- percentage trend and subtext, all on one line.",
        props = listOf(
            required("value", string),
            optional("subtext", string),
            optional(
                "trend",
                obj(listOf(required("direction", enumeration(listOf("up", "down"))), required("value", number))),
            ),
        ),
    )

    public val metricIndicatorWithStrikethrough: ComponentSchema = ComponentSchema(
        "MetricIndicatorWithStrikethrough",
        description = "Headline metric value with an optional struck-through previousValue, a +/- percentage trend, " +
            "and subtext below.",
        props = listOf(
            required("value", string),
            optional("subtext", string),
            optional("previousValue", string),
            optional(
                "trend",
                obj(listOf(required("direction", enumeration(listOf("up", "down"))), required("value", number))),
            ),
        ),
    )

    public val snippetCardItem: ComponentSchema = ComponentSchema(
        "SnippetCardItem",
        description = "One row-style snippet card: a label on the left (IconText or ImageText) and an optional value " +
            "on the right (Text or BoldText).",
        props = listOf(
            optional("id", string),
            required("lhs", components(listOf("IconText", "ImageText"))),
            optional("rhs", components(listOf("Text", "BoldText"))),
        ),
    )

    public val snippetCardBlock: ComponentSchema = ComponentSchema(
        "SnippetCardBlock",
        description = "A responsive grid of compact label/value cards (2 per row) for showing several short facts " +
            "side by side; optionally clickable with a shared action.",
        props = listOf(
            required("items", array(component("SnippetCardItem"), minItems = 2)),
            defaulted("layout", enumeration(listOf("grid")), "grid"),
            defaulted("responsive", boolean, true),
            optional("action", action),
            optional("gap", union(listOf(number, string))),
        ),
    )

    public val overviewCardItem: ComponentSchema = ComponentSchema(
        "OverviewCardItem",
        description = "One overview card: a heading slot at the top (IconText, ImageText or Text) and an optional " +
            "MetricIndicatorInline at the bottom.",
        props = listOf(
            optional("id", string),
            required("top", components(listOf("IconText", "ImageText", "Text"))),
            optional("bottom", component("MetricIndicatorInline")),
        ),
    )

    public val overviewCardBlock: ComponentSchema = ComponentSchema(
        "OverviewCardBlock",
        description = "A grid or horizontal carousel of compact overview cards, each with a heading " +
            "(icon/image/text) on top and an inline metric below; optionally clickable with a shared action.",
        props = listOf(
            required("items", array(component("OverviewCardItem"), minItems = 2)),
            defaulted("layout", enumeration(listOf("grid", "carousel")), "grid"),
            defaulted("responsive", boolean, true),
            optional("action", action),
            optional("gap", union(listOf(number, string))),
        ),
    )

    public val contextCardItem: ComponentSchema = ComponentSchema(
        "ContextCardItem",
        description = "A single card inside a ContextCardBlock: a title (plain string or Tag), an optional markdown " +
            "body, and an optional gray tint or background image.",
        props = listOf(
            optional("id", string),
            required("title", union(listOf(string, component("Tag")))),
            optional("body", string),
            optional("bgColor", enumeration(listOf("gray"))),
            optional("bgImageSrc", string),
            optional("bgImageAlt", string),
        ),
    )

    public val contextCardBlock: ComponentSchema = ComponentSchema(
        "ContextCardBlock",
        description = "A grid or carousel of compact tinted context cards (title or tag plus a short bold body); an " +
            "optional action makes every card clickable.",
        props = listOf(
            required("items", array(component("ContextCardItem"), minItems = 2)),
            defaulted("layout", enumeration(listOf("grid", "carousel")), "grid"),
            defaulted("responsive", boolean, true),
            optional("action", action),
            optional("gap", union(listOf(number, string))),
        ),
    )

    public val compositeCardItem: ComponentSchema = ComponentSchema(
        "CompositeCardItem",
        description = "A single card inside a CompositeCardBlock: an optional header (icon/image/text), a stack of " +
            "body elements (text, metrics, charts, lists, tags), and an optional price/button footer.",
        props = listOf(
            optional("id", string),
            optional("header", components(listOf("IconText", "ImageText", "ImageTextLarge", "Text", "Image"))),
            defaulted(
                "body",
                array(
                    components(
                        listOf(
                            "Text", "BoldText", "MetricIndicatorInline", "IconText", "Image", "AreaChart", "BarChart",
                            "LineChart", "ListBlock", "TagBlock", "EntityList",
                        ),
                    ),
                ),
                emptyList<Any>(),
            ),
            optional(
                "footer",
                obj(
                    listOf(
                        optional("price", components(listOf("BoldText", "MetricIndicatorWithStrikethrough"))),
                        optional("button", component("Button")),
                    ),
                ),
            ),
        ),
    )

    public val compositeCardBlock: ComponentSchema = ComponentSchema(
        "CompositeCardBlock",
        description = "A two-per-row grid or carousel of rich cards, each with an optional header, stacked body " +
            "content (text, metrics, charts, lists, tags) and a price/button footer; an optional action makes every " +
            "card clickable.",
        props = listOf(
            required("items", array(component("CompositeCardItem"), minItems = 2)),
            defaulted("layout", enumeration(listOf("grid", "carousel")), "grid"),
            defaulted("responsive", boolean, true),
            optional("action", action),
            optional("gap", union(listOf(number, string))),
        ),
    )

    public val visualCardItem: ComponentSchema = ComponentSchema(
        "VisualCardItem",
        description = "A single photo-first card inside a VisualCardBlock: a BoldText body panel, an optional Tag, " +
            "and a background image (bgImageSrc must be a real URL; bgImageAlt is its alt text).",
        props = listOf(
            required("body", component("BoldText")),
            optional("id", string),
            optional("bgImageSrc", string),
            optional("tag", component("Tag")),
            optional("bgImageAlt", string),
        ),
    )

    public val visualCardBlock: ComponentSchema = ComponentSchema(
        "VisualCardBlock",
        description = "A grid or carousel of photo-first cards: a full-bleed background image with a tag on top and " +
            "a bold text panel at the bottom; an optional action makes every card clickable.",
        props = listOf(
            required("items", array(component("VisualCardItem"), minItems = 2)),
            defaulted("layout", enumeration(listOf("grid", "carousel")), "grid"),
            defaulted("responsive", boolean, true),
            optional("action", action),
            optional("gap", union(listOf(number, string))),
        ),
    )
}
