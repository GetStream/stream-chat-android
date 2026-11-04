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

/**
 * The prompt options for the chat library: its examples and additional rules (port of
 * `openuiChatPromptOptions`).
 */
internal val openuiChatPromptOptions: PromptOptions by lazy {
    PromptOptions(additionalRules = ChatLibraryDefinition.additionalRules, examples = ChatLibraryDefinition.examples)
}

/**
 * The prompt options for the standard library: its examples and additional rules (port of
 * `openuiPromptOptions`).
 */
internal val openuiPromptOptions: PromptOptions by lazy {
    PromptOptions(
        additionalRules = StandardLibraryDefinition.additionalRules,
        examples = StandardLibraryDefinition.examples,
    )
}

internal object ChatLibraryDefinition {
    const val root: String = "Card"

    // Components in library order.
    val schemas: List<ComponentSchema> = listOf(
        ChatSchemas.card,
        CommonSchemas.cardHeader,
        CommonSchemas.textContent,
        CommonSchemas.markDownRenderer,
        CommonSchemas.callout,
        CommonSchemas.textCallout,
        CommonSchemas.image,
        CommonSchemas.imageBlock,
        CommonSchemas.imageGallery,
        CommonSchemas.codeBlock,
        CommonSchemas.separator,
        CommonSchemas.table,
        CommonSchemas.col,
        CommonSchemas.barChart,
        CommonSchemas.lineChart,
        CommonSchemas.areaChart,
        CommonSchemas.radarChart,
        CommonSchemas.horizontalBarChart,
        CommonSchemas.series,
        CommonSchemas.pieChart,
        CommonSchemas.radialChart,
        CommonSchemas.singleStackedBarChart,
        CommonSchemas.slice,
        CommonSchemas.scatterChart,
        CommonSchemas.scatterSeries,
        CommonSchemas.point,
        CommonSchemas.form,
        CommonSchemas.formControl,
        CommonSchemas.label,
        CommonSchemas.input,
        CommonSchemas.textArea,
        CommonSchemas.select,
        CommonSchemas.selectItem,
        CommonSchemas.datePicker,
        CommonSchemas.slider,
        CommonSchemas.checkBoxGroup,
        CommonSchemas.checkBoxItem,
        CommonSchemas.radioGroup,
        CommonSchemas.radioItem,
        CommonSchemas.switchGroup,
        CommonSchemas.switchItem,
        CommonSchemas.button,
        CommonSchemas.buttons,
        CommonSchemas.listBlock,
        CommonSchemas.listItem,
        ChatSchemas.followUpBlock,
        ChatSchemas.followUpItem,
        ChatSchemas.sectionBlock,
        ChatSchemas.sectionItem,
        CommonSchemas.tabs,
        ChatSchemas.tabItem,
        CommonSchemas.accordion,
        ChatSchemas.accordionItem,
        CommonSchemas.steps,
        CommonSchemas.stepsItem,
        ChatSchemas.carousel,
        CommonSchemas.tagBlock,
        CommonSchemas.tag,
        CommonSchemas.entityList,
        CommonSchemas.inlineHeader,
        CommonSchemas.icon,
        CommonSchemas.iconButton,
        CommonSchemas.editableTable,
        CommonSchemas.chipItem,
        CommonSchemas.chips,
        CommonSchemas.optionCard,
        CommonSchemas.optionCards,
        CommonSchemas.text,
        CommonSchemas.boldText,
        CommonSchemas.iconText,
        CommonSchemas.imageText,
        CommonSchemas.imageTextLarge,
        CommonSchemas.metricIndicatorInline,
        CommonSchemas.metricIndicatorWithStrikethrough,
        CommonSchemas.snippetCardItem,
        CommonSchemas.snippetCardBlock,
        CommonSchemas.overviewCardItem,
        CommonSchemas.overviewCardBlock,
        CommonSchemas.contextCardItem,
        CommonSchemas.contextCardBlock,
        CommonSchemas.compositeCardItem,
        CommonSchemas.compositeCardBlock,
        CommonSchemas.visualCardItem,
        CommonSchemas.visualCardBlock,
    )

    val componentGroups: List<ComponentGroup> = listOf(
        ComponentGroup(
            name = "Content",
            components = listOf(
                "CardHeader", "TextContent", "MarkDownRenderer", "Callout", "TextCallout", "Image", "ImageBlock",
                "ImageGallery", "CodeBlock", "Separator", "InlineHeader",
            ),
            notes = listOf(
                "- InlineHeader is a compact heading + description pair for labelling a block inside the response " +
                    "(lighter than CardHeader).",
                "- Pass sources on Card ([{ title, sourceName, url }]) when the answer relies on references, and " +
                    "cite them inline in TextContent as [1], [2] (1-based index into sources). A Sources strip " +
                    "renders at the bottom of the card.",
            ),
        ),
        ComponentGroup(
            name = "Tables",
            components = listOf("Table", "Col", "EditableTable"),
            notes = listOf(
                "- EditableTable lets the user edit cells inline. Give it a unique name, columns of { type, key, " +
                    "header } with type one of text | number | date-single | select | url (select also needs " +
                    "options: [{ value, label }]).",
                "- data is an array of { id, values } rows where values are ordered positionally to match columns. " +
                    "Edited data is submitted when the user clicks Save Changes.",
            ),
        ),
        ComponentGroup(
            name = "Charts (2D)",
            components = listOf("BarChart", "LineChart", "AreaChart", "RadarChart", "HorizontalBarChart", "Series"),
        ),
        ComponentGroup(
            name = "Charts (1D)",
            components = listOf("PieChart", "RadialChart", "SingleStackedBarChart", "Slice"),
        ),
        ComponentGroup(name = "Charts (Scatter)", components = listOf("ScatterChart", "ScatterSeries", "Point")),
        ComponentGroup(
            name = "Forms",
            components = listOf(
                "Form", "FormControl", "Label", "Input", "TextArea", "Select", "SelectItem", "DatePicker", "Slider",
                "CheckBoxGroup", "CheckBoxItem", "RadioGroup", "RadioItem", "SwitchGroup", "SwitchItem", "Chips",
                "ChipItem", "OptionCards", "OptionCard",
            ),
            notes = listOf(
                "- Chips: compact single/multiple selection pills. Use ChipItem references for each option.",
                "- OptionCards: larger selectable cards with title, subtitle and an optional Icon or Image on top. " +
                    "Use OptionCard references for each option.",
                "- Define EACH FormControl as its own reference — do NOT inline all controls in one array.",
                "- NEVER nest Form inside Form.",
                "- Form requires explicit buttons. Always pass a Buttons(...) reference as the second Form argument: " +
                    "Form(name, buttons, fields).",
                "- rules is an optional object: { required: true, email: true, min: 8, maxLength: 100 }",
                "- The renderer shows error messages automatically — do NOT generate error text in the UI",
            ),
        ),
        ComponentGroup(
            name = "Buttons",
            components = listOf("Button", "Buttons", "Icon", "IconButton"),
            notes = listOf(
                "- Icon renders a lucide icon by kebab-case name; it is also used as the icon of IconButton, " +
                    "IconText and OptionCard.",
            ),
        ),
        ComponentGroup(
            name = "Lists & Follow-ups",
            components = listOf("ListBlock", "ListItem", "FollowUpBlock", "FollowUpItem"),
            notes = listOf(
                "- Use ListBlock with ListItem references for numbered lists.",
                "- Use FollowUpBlock with FollowUpItem references at the end of a response to suggest next actions.",
                "- A ListItem is clickable ONLY when given an action (5th argument); without one it is plain text.",
                "- Clicking a FollowUpItem, or a ListItem with a @ToAssistant action, sends text to the LLM as a " +
                    "user message.",
                "- Example: list = ListBlock([item1, item2])  item1 = ListItem(\"Option A\", \"Details about A\", " +
                    "null, null, @ToAssistant(\"Option A\"))",
            ),
        ),
        ComponentGroup(
            name = "Sections",
            components = listOf("SectionBlock", "SectionItem"),
            notes = listOf(
                "- SectionBlock renders collapsible accordion sections that auto-open as they stream.",
                "- Each section needs a unique `value` id, a `trigger` label, and a `content` array.",
                "- Example: sections = SectionBlock([s1, s2])  s1 = SectionItem(\"intro\", \"Introduction\", " +
                    "[content1])",
                "- Set isFoldable=false to render sections as flat headers instead of accordion.",
            ),
        ),
        ComponentGroup(
            name = "Layout",
            components = listOf("Tabs", "TabItem", "Accordion", "AccordionItem", "Steps", "StepsItem", "Carousel"),
            notes = listOf(
                "- Use Tabs to present alternative views — each TabItem has a value id, trigger label, and content " +
                    "array.",
                "- Carousel takes an array of slides, where each slide is an array of content: carousel = " +
                    "Carousel([[t1, img1], [t2, img2]])",
                "- IMPORTANT: Every slide in a Carousel must have the same structure — same component types in the " +
                    "same order.",
                "- For image carousels use: [[title, image, description, tags], ...] — every slide must follow this " +
                    "exact pattern.",
                "- Use real, publicly accessible image URLs (e.g. https://picsum.photos/seed/KEYWORD/800/500). Never " +
                    "hallucinate image URLs.",
            ),
        ),
        ComponentGroup(
            name = "Data Display",
            components = listOf("TagBlock", "Tag", "EntityList"),
            notes = listOf(
                "- EntityList is a compact two-column list of { left, right } rows (e.g. name / value). " +
                    "size='default' also supports a header and footer row; size='small' does not.",
            ),
        ),
        ComponentGroup(
            name = "Cards",
            components = listOf(
                "SnippetCardBlock", "SnippetCardItem", "OverviewCardBlock", "OverviewCardItem", "ContextCardBlock",
                "ContextCardItem", "CompositeCardBlock", "CompositeCardItem", "VisualCardBlock", "VisualCardItem",
                "Text", "BoldText", "IconText", "ImageText", "ImageTextLarge", "MetricIndicatorInline",
                "MetricIndicatorWithStrikethrough",
            ),
            notes = listOf(
                "- Card blocks lay out 2+ items in a responsive grid (or carousel where supported). Every item in a " +
                    "block must have the same structure.",
                "- SnippetCardItem: small card with lhs (IconText | ImageText) and optional rhs (Text | BoldText) — " +
                    "good for key/value facts.",
                "- OverviewCardItem: small card with top (IconText | ImageText | Text) and optional bottom " +
                    "MetricIndicatorInline — good for KPIs.",
                "- ContextCardItem: medium card with a title (string or Tag), body text and optional background " +
                    "image — good for summaries.",
                "- CompositeCardItem: rich card with header, body array (Text, BoldText, MetricIndicatorInline, " +
                    "IconText, Image, charts, ListBlock, TagBlock, EntityList) and footer (price + Button) — good " +
                    "for products/offers.",
                "- VisualCardItem: image-first card with a BoldText body and optional Tag.",
                "- Text / BoldText / IconText / ImageText / ImageTextLarge / MetricIndicator* are the inline " +
                    "building blocks used INSIDE card items; do not place them directly in the root Card.",
            ),
        ),
    )

    val examples: List<String> = listOf(
        "Example 1 — Informational response (the shape for \"tell me about X\" / explainer asks):\n\nroot = " +
            "Card([header, intro, highlights, statsHeader, statsCards, sectionsBlock, actionBtns], [src1, src2])\n" +
            "header = CardHeader(\"Paris\", \"The City of Light · France's Eternal Capital\")\nintro = " +
            "TextContent(\"Paris is one of the world's most visited cities — a timeless blend of iconic landmarks, " +
            "world-class cuisine, art, and romance [1]. In 2026 the city is buzzing with new energy: the **Grand " +
            "Palais** has reopened after a €466M renovation, and you can now **swim in the Seine** for the first " +
            "time in over a century [2].\")\nhighlights = ContextCardBlock([h1, h2, h3], \"grid\", true, " +
            "@ToAssistant(\"Tell me more about this aspect of Paris\"))\nh1 = ContextCardItem(\"art-museums\", \"Art " +
            "& Museums\", \"143 museums including the Louvre & newly reopened Grand Palais.\", \"gray\", " +
            "\"https://cdn.britannica.com/03/121003-050-2544BD4E/Interior-Louvre-Museum-Paris.jpg\")\nh2 = " +
            "ContextCardItem(\"gastronomy\", \"Gastronomy\", \"9,000+ restaurants, 130+ Michelin stars, and the " +
            "world's best baguettes.\", \"gray\")\nh3 = ContextCardItem(\"fashion-capital\", \"Fashion Capital\", " +
            "\"Home to Chanel, Dior, Louis Vuitton & Hermès — the global luxury hub.\", \"gray\", " +
            "\"https://thumbs.dreamstime.com/z/dior-storefront-facade-chanel-store-lvmh-s-french-designer-modehouse-c" +
            "hristian-adjacent-to-luxury-fashion-beauty-brand-327161553.jpg\")\nstatsHeader = InlineHeader(\"Paris " +
            "by the Numbers\", \"Key visitor statistics\")\nstatsCards = OverviewCardBlock([ov1, ov2, ov3], " +
            "\"grid\", true)\nov1 = OverviewCardItem(\"annual-visitors\", ov1top, ov1metric)\nov1top = " +
            "IconText(ov1icon, \"neutral\", \"m\", \"Annual Visitors\", \"2024 arrivals\", false, \"vertical\")\n" +
            "ov1icon = Icon(\"users\", \"people\")\nov1metric = MetricIndicatorInline(\"48.7M\", \"tourists\", { " +
            "direction: \"up\", value: 2.5 })\nov2 = OverviewCardItem(\"museums-monuments\", ov2top, ov2metric)\n" +
            "ov2top = IconText(ov2icon, \"neutral\", \"m\", \"Museums & Monuments\", \"Cultural sites\", false, " +
            "\"vertical\")\nov2icon = Icon(\"landmark\", \"buildings\")\nov2metric = " +
            "MetricIndicatorInline(\"2,370+\", \"across the city\")\nov3 = OverviewCardItem(\"tourism-revenue\", " +
            "ov3top, ov3metric)\nov3top = IconText(ov3icon, \"neutral\", \"m\", \"Tourism Revenue\", \"2024 " +
            "estimate\", false, \"vertical\")\nov3icon = Icon(\"circle-dollar-sign\", \"finance\")\nov3metric = " +
            "MetricIndicatorInline(\"€23.4B\", \"generated\")\nsectionsBlock = SectionBlock([secLandmarks, secFood, " +
            "secWhen], false)\nsecLandmarks = SectionItem(\"landmarks\", \"Iconic Landmarks\", [landmarksCarousel])\n" +
            "landmarksCarousel = VisualCardBlock([lm1, lm2, lm3, lm4], \"carousel\", true, @ToAssistant(\"Tell me " +
            "more about this Paris landmark\"))\nlm1 = VisualCardItem(lm1body, \"eiffel-tower\", " +
            "\"https://c8.alamy.com/comp/RYEB13/aerial-view-of-the-eiffel-tower-with-the-park-champ-de-mars-and-the-r" +
            "iver-seine-paris-france-RYEB13.jpg\", lm1tag, \"Aerial view of the Eiffel Tower\")\nlm1body = " +
            "BoldText(\"text\", \"Eiffel Tower\", \"Open daily 9am–midnight\")\nlm1tag = Tag(\"Must-See\", " +
            "lm1tagIcon, \"sm\", \"info\")\nlm1tagIcon = Icon(\"star\", \"shapes\")\nlm2 = VisualCardItem(lm2body, " +
            "\"the-louvre\", " +
            "\"https://images.fineartamerica.com/images/artworkimages/mediumlarge/3/1-exterior-of-musee-du-louvre-mus" +
            "eum-paris-france-bernard-jaubert.jpg\", lm2tag, \"Exterior of the Louvre museum\")\nlm2body = " +
            "BoldText(\"text\", \"The Louvre\", \"World's largest art museum\")\nlm2tag = Tag(\"8.9M visits/yr\", " +
            "lm2tagIcon, \"sm\", \"success\")\nlm2tagIcon = Icon(\"trending-up\", \"charts\")\nlm3 = " +
            "VisualCardItem(lm3body, \"notre-dame\", " +
            "\"https://cdn.britannica.com/29/255529-050-63A22A3C/notre-dame-de-paris-cathedral-paris-france.jpg\", " +
            "lm3tag, \"Notre-Dame cathedral facade\")\nlm3body = BoldText(\"text\", \"Notre-Dame\", \"Fully restored " +
            "cathedral\")\nlm3tag = Tag(\"Reopened 2024\", lm3tagIcon, \"sm\", \"warning\")\nlm3tagIcon = " +
            "Icon(\"hammer\", \"tools\")\nlm4 = VisualCardItem(lm4body, \"versailles\", " +
            "\"https://cdn.britannica.com/76/130076-050-31128A3D/Latona-Fountain-Balthazard-Marsy-Gaspard-Andre-Le.jp" +
            "g\", lm4tag, \"Latona Fountain at Versailles\")\nlm4body = BoldText(\"text\", \"Palace of Versailles\", " +
            "\"Royal gardens & grand halls\")\nlm4tag = Tag(\"Day Trip\", lm4tagIcon, \"sm\", \"neutral\")\n" +
            "lm4tagIcon = Icon(\"train-front\", \"travel\")\nsecFood = SectionItem(\"food\", \"Food & Drink\", " +
            "[foodCards])\nfoodCards = ContextCardBlock([f1, f2, f3], \"grid\", true, @ToAssistant(\"Tell me more " +
            "about eating and drinking in Paris\"))\nf1 = ContextCardItem(\"bistros\", \"Bistros\", \"Classic French " +
            "fare — try Les Arlots for confit beef cheeks.\", \"gray\", " +
            "\"https://everydayparisian.com/wp-content/uploads/2023/01/IMG_0221-768x1024.webp\")\nf2 = " +
            "ContextCardItem(\"fine-dining\", \"Fine Dining\", \"Septime leads a new wave of creative " +
            "Michelin-starred cuisine.\", \"gray\", " +
            "\"https://production-data.worldofmouth.app/images/72b2e6db-97ea-49c4-aa5d-3b7c8121c812.jpg\")\nf3 = " +
            "ContextCardItem(\"bakeries\", \"Bakeries\", \"Shinya Pain for sourdough. Grand Prix-winning baguettes " +
            "citywide.\", \"gray\", " +
            "\"https://www.davidlebovitz.com/wp-content/uploads/2018/09/Le-petit-grain-paris-bakery-boulangerie-patis" +
            "serie-pastry-shop-8-640x895.jpg\")\nsecWhen = SectionItem(\"when\", \"Best Time to Visit\", " +
            "[whenTabs])\nwhenTabs = Tabs([tabSpring, tabSummer])\ntabSpring = TabItem(\"spring\", \"Spring\", " +
            "[springList])\nspringList = ListBlock([sp1, sp2, sp3], \"number\")\nsp1 = ListItem(\"Cherry blossoms at " +
            "Parc de Sceaux\", \"March – May\", null, null, @ToAssistant(\"Tell me about cherry blossom season in " +
            "Paris\"))\nsp2 = ListItem(\"Picnics along the Seine & in city parks\", \"Warm, lively atmosphere\", " +
            "null, null, @ToAssistant(\"Where are the best picnic spots in Paris?\"))\nsp3 = ListItem(\"Fewer crowds " +
            "than summer\", \"Ideal for sightseeing\", null, null, @ToAssistant(\"How busy is Paris in spring?\"))\n" +
            "tabSummer = TabItem(\"summer\", \"Summer\", [summerList])\nsummerList = ListBlock([su1, su2, su3], " +
            "\"number\")\nsu1 = ListItem(\"Swim in the Seine — open July & August 2026\", \"First time in 102 " +
            "years\", null, null, @ToAssistant(\"Tell me about swimming in the Seine\"))\nsu2 = ListItem(\"Open-air " +
            "cinema at Parc de la Villette\", \"Free films under the stars\", null, null, @ToAssistant(\"Tell me " +
            "about the open-air cinema at Parc de la Villette\"))\nsu3 = ListItem(\"Bastille Day celebrations on " +
            "July 14\", \"Fireworks at the Eiffel Tower\", null, null, @ToAssistant(\"What happens in Paris on " +
            "Bastille Day?\"))\nactionBtns = Buttons([btn1, btn2, btn3], \"row\")\nbtn1 = Button(\"Plan a Trip to " +
            "Paris\", @ToAssistant(\"Help me plan a trip to Paris\"), \"primary\")\nbtn2 = Button(\"Best Hotels in " +
            "Paris\", @ToAssistant(\"Show me the best hotels in Paris\"), \"secondary\")\nbtn3 = Button(\"Build a " +
            "Paris Itinerary\", @ToAssistant(\"Build a 5-day Paris itinerary for me\"), \"secondary\")\nsrc1 = { " +
            "title: \"Paris Travel Guide 2025\", sourceName: \"Travel and Tour World\", url: " +
            "\"https://www.travelandtourworld.com/news/article/from-iconic-landmarks-to-hidden-gems-the-ultimate-pari" +
            "s-travel-guide-for-2025-you-need-to-read-now/\" }\nsrc2 = { title: \"Best Things to Do in Paris 2026\", " +
            "sourceName: \"Time Out Paris\", url: \"http://www.timeout.fr/paris/en/for-tourists\" }",
        "Example 2 — Data and analytics response (quantitative asks lead with KPIs and charts):\n\nroot = " +
            "Card([pageHeader, kpiBlock, sections, deepDiveHeader, compositeCards, actionButtons])\npageHeader = " +
            "CardHeader(\"Analytics Dashboard\", \"April 2026 · Business Performance Overview\")\nkpiBlock = " +
            "SnippetCardBlock([kpi1, kpi2, kpi3, kpi4], \"grid\", true)\nkpi1 = SnippetCardItem(\"total-revenue\", " +
            "kpi1lhs, kpi1rhs)\nkpi1lhs = IconText(kpi1icon, \"neutral\", \"m\", \"Total Revenue\", \"Month to " +
            "date\", false, \"horizontal\")\nkpi1icon = Icon(\"circle-dollar-sign\", \"finance\")\nkpi1rhs = " +
            "BoldText(\"number\", \"\$284,500\", \"+12.4%\", \"metric\")\nkpi2 = SnippetCardItem(\"active-users\", " +
            "kpi2lhs, kpi2rhs)\nkpi2lhs = IconText(kpi2icon, \"neutral\", \"m\", \"Active Users\", \"Monthly active " +
            "users\", false, \"horizontal\")\nkpi2icon = Icon(\"users\", \"people\")\nkpi2rhs = BoldText(\"number\", " +
            "\"47,320\", \"+6.8%\", \"metric\")\nkpi3 = SnippetCardItem(\"conversion-rate\", kpi3lhs, kpi3rhs)\n" +
            "kpi3lhs = IconText(kpi3icon, \"neutral\", \"m\", \"Conversion Rate\", \"Avg. this month\", false, " +
            "\"horizontal\")\nkpi3icon = Icon(\"trending-up\", \"charts\")\nkpi3rhs = BoldText(\"number\", \"5.7%\", " +
            "\"+0.9%\", \"metric\")\nkpi4 = SnippetCardItem(\"churn-rate\", kpi4lhs, kpi4rhs)\nkpi4lhs = " +
            "IconText(kpi4icon, \"neutral\", \"m\", \"Churn Rate\", \"Monthly customer churn\", false, " +
            "\"horizontal\")\nkpi4icon = Icon(\"user-minus\", \"people\")\nkpi4rhs = BoldText(\"number\", \"2.1%\", " +
            "\"-0.4%\", \"metric\")\nsections = SectionBlock([secUsers, secChannels], false)\nsecUsers = " +
            "SectionItem(\"users\", \"User Activity\", [userTabs])\nuserTabs = Tabs([tabGrowth, tabRetention])\n" +
            "tabGrowth = TabItem(\"growth\", \"User Growth\", [growthChart])\ngrowthChart = LineChart(months, " +
            "[dauSeries, mauSeries], \"natural\", \"Month\", \"Users (k)\")\nmonths = [\"May\", \"Jun\", \"Jul\", " +
            "\"Aug\", \"Sep\", \"Oct\", \"Nov\", \"Dec\", \"Jan\", \"Feb\", \"Mar\", \"Apr\"]\ndauSeries = " +
            "Series(\"DAU\", [5.1, 5.4, 5.8, 6.2, 6.7, 7.1, 7.6, 8.0, 8.4, 8.9, 9.3, 9.8])\nmauSeries = " +
            "Series(\"MAU\", [28.4, 30.1, 31.8, 33.5, 35.2, 37.1, 38.8, 40.4, 42.1, 43.9, 45.6, 47.3])\ntabRetention " +
            "= TabItem(\"retention\", \"Cohort Retention\", [retentionChart])\nretentionChart = " +
            "LineChart(cohortMonths, [cohortJan, cohortFeb, cohortMar], \"natural\", \"Month Since Signup\", " +
            "\"Retention (%)\")\ncohortMonths = [\"Month 0\", \"Month 1\", \"Month 2\", \"Month 3\", \"Month 4\", " +
            "\"Month 5\", \"Month 6\"]\ncohortJan = Series(\"Jan Cohort\", [100, 80, 70, 62, 56, 51, 47])\ncohortFeb " +
            "= Series(\"Feb Cohort\", [100, 83, 73, 65, 59, 54, 50])\ncohortMar = Series(\"Mar Cohort\", [100, 86, " +
            "76, 68, 62, 0, 0])\nsecChannels = SectionItem(\"channels\", \"Acquisition Channels\", [channelTabs])\n" +
            "channelTabs = Tabs([tabChannelBar, tabChannelTable])\ntabChannelBar = TabItem(\"channel-bar\", " +
            "\"Revenue by Channel\", [channelBarChart])\nchannelBarChart = HorizontalBarChart(channels, " +
            "[channelSeries], \"grouped\", \"Revenue (\$k)\", \"Channel\")\nchannels = [\"Organic Search\", \"Paid " +
            "Ads\", \"Referral Program\", \"Partner Network\", \"Direct / Brand\", \"Email Campaigns\"]\n" +
            "channelSeries = Series(\"Revenue (\$k)\", [82.4, 68.1, 54.6, 38.2, 26.8, 14.4])\ntabChannelTable = " +
            "TabItem(\"channel-table\", \"Full Breakdown\", [channelTable])\nchannelTable = Table([colChannel, " +
            "colLeads, colConv, colCac, colRevenue, colStatus])\ncolChannel = Col(\"Channel\", channels, " +
            "\"string\")\ncolLeads = Col(\"Leads\", [4210, 3180, 2050, 1120, 1290, 820], \"number\")\ncolConv = " +
            "Col(\"Conv. Rate\", [\"6.4%\", \"5.1%\", \"9.4%\", \"11.8%\", \"5.8%\", \"8.1%\"], \"string\")\ncolCac " +
            "= Col(\"CAC (\$)\", [188, 395, 132, 109, 218, 154], \"number\")\ncolRevenue = Col(\"Revenue (\$k)\", " +
            "[82.4, 68.1, 54.6, 38.2, 26.8, 14.4], \"number\")\ncolStatus = Col(\"Status\", [\"Scaling\", " +
            "\"Optimizing\", \"Scaling\", \"Growing\", \"Stable\", \"Stable\"], \"string\")\ndeepDiveHeader = " +
            "InlineHeader(\"Support Health & Marketing ROI\", \"Click a card to explore deeper insights\")\n" +
            "compositeCards = CompositeCardBlock([cardSupport, cardMarketing], \"grid\", true, " +
            "@ToAssistant(\"Explore this deep-dive area in more detail\"))\ncardSupport = " +
            "CompositeCardItem(\"support-health\", supportHeader, [supportMetric, supportChart, supportList])\n" +
            "supportHeader = IconText(supportIcon, \"neutral\", \"m\", \"Support Health\", \"Ticket volume & " +
            "resolution trends\", false, \"horizontal\")\nsupportIcon = Icon(\"headphones\", \"communication\")\n" +
            "supportMetric = MetricIndicatorInline(\"94.2% CSAT\", \"Customer Satisfaction Score\", { direction: " +
            "\"up\", value: 2.1 })\nsupportChart = BarChart([\"Jan\", \"Feb\", \"Mar\", \"Apr\"], [openSeries, " +
            "resolvedSeries], \"grouped\", \"Month\", \"Tickets\")\nopenSeries = Series(\"Opened\", [341, 327, 308, " +
            "289])\nresolvedSeries = Series(\"Resolved\", [319, 318, 301, 283])\nsupportList = EntityList([sup1, " +
            "sup2, sup3], \"small\")\nsup1 = { left: \"Avg. Resolution Time\", right: \"4h 12m\", rightVariant: " +
            "\"text\" }\nsup2 = { left: \"First Contact Resolution\", right: \"78.4%\", rightVariant: \"number\" }\n" +
            "sup3 = { left: \"Escalation Rate\", right: \"6.2%\", rightVariant: \"number\" }\ncardMarketing = " +
            "CompositeCardItem(\"marketing-roi\", marketingHeader, [marketingMetric, marketingChart, " +
            "marketingList])\nmarketingHeader = IconText(marketingIcon, \"neutral\", \"m\", \"Marketing ROI\", " +
            "\"Spend vs. pipeline generated\", false, \"horizontal\")\nmarketingIcon = Icon(\"megaphone\", " +
            "\"communication\")\nmarketingMetric = MetricIndicatorInline(\"3.8x Blended ROI\", \"Return on Ad " +
            "Spend\", { direction: \"up\", value: 0.4 })\nmarketingChart = AreaChart([\"Jan\", \"Feb\", \"Mar\", " +
            "\"Apr\"], [spendSeries, pipelineSeries], \"natural\", \"Month\", \"Value (\$k)\")\nspendSeries = " +
            "Series(\"Ad Spend\", [44.0, 46.8, 49.2, 51.6])\npipelineSeries = Series(\"Pipeline Generated\", [158.2, " +
            "171.5, 184.9, 196.1])\nmarketingList = EntityList([mkt1, mkt2, mkt3], \"small\")\nmkt1 = { left: " +
            "\"Total Ad Spend (Apr)\", right: \"\$51,600\", rightVariant: \"number\" }\nmkt2 = { left: \"Cost per " +
            "Lead\", right: \"\$48.20\", rightVariant: \"number\" }\nmkt3 = { left: \"Top Channel\", right: " +
            "\"Organic Search\", rightVariant: \"text\" }\nactionButtons = Buttons([btnChannels, btnForecast], " +
            "\"row\")\nbtnChannels = Button(\"Break Down by Channel\", @ToAssistant(\"Break down this month's " +
            "revenue by acquisition channel\"), \"primary\")\nbtnForecast = Button(\"Forecast Next Quarter\", " +
            "@ToAssistant(\"Forecast revenue and active users for next quarter\"), \"secondary\")",
        "Example 3 — Form response (help-me-choose/plan asks: inspiration cards FIRST, then a compact form). The " +
            "inspiration-card images below came from an image tool call — include such cards ONLY when you have real " +
            "URLs; with no real URLs, start directly with the form:\n\nroot = Card([header, intro, inspoCards, " +
            "form])\nheader = CardHeader(\"Plan Your Trip\", \"A few ideas to spark inspiration — then tell me your " +
            "preferences\")\nintro = TextContent(\"Tap a style to jump straight in, or fill the quick form below for " +
            "a tailored plan.\")\ninspoCards = VisualCardBlock([insp1, insp2], \"grid\", true, @ToAssistant(\"Plan " +
            "me a trip in this style with destinations, itinerary, and costs\"))\ninsp1 = VisualCardItem(insp1body, " +
            "\"inspo-beach\", " +
            "\"https://www.travelandleisure.com/thmb/cuAB7XMQ6s_Gji-no956KNopT7M=/1500x0/filters:no_upscale():max_byt" +
            "es(150000):strip_icc()/TAL-maldives-TROPVACAY0325-1b5047d5931d4850b47d082966f9f563.jpg\", insp1tag, " +
            "\"Maldives overwater villas\")\ninsp1body = BoldText(\"text\", \"Tropical Beach Escape\", \"Overwater " +
            "villas & snorkeling\")\ninsp1tag = Tag(\"Relaxation\", insp1icon, \"sm\", \"info\")\ninsp1icon = " +
            "Icon(\"umbrella\", \"travel\")\ninsp2 = VisualCardItem(insp2body, \"inspo-mountain\", " +
            "\"https://www.salkantaytrekmachu.com/img/adventure-travel-peru-032.jpg\", insp2tag, \"Trekking in the " +
            "Peruvian Andes\")\ninsp2body = BoldText(\"text\", \"Mountain Adventure\", \"Trekking & epic views\")\n" +
            "insp2tag = Tag(\"Active\", insp2icon, \"sm\", \"warning\")\ninsp2icon = Icon(\"mountain\", \"nature\")\n" +
            "form = Form(\"trip-planner\", formButtons, [fcDestination, fcDates, fcTravellers, fcTripType, fcBudget, " +
            "fcStay, fcNotes])\nformButtons = Buttons([btnSubmit, btnSkip], \"row\")\nbtnSubmit = Button(\"Plan My " +
            "Trip\", @ToAssistant(\"Help me plan a trip based on my form submission\"), \"primary\")\nbtnSkip = " +
            "Button(\"Just Surprise Me\", @ToAssistant(\"Skip the form and surprise me with a destination and " +
            "itinerary\"), \"secondary\")\nfcDestination = FormControl(\"Destination\", destinationInput, \"Leave " +
            "blank if you'd like suggestions\")\ndestinationInput = Input(\"destination\", \"e.g. Tokyo, Japan — or " +
            "leave blank\", \"text\")\nfcDates = FormControl(\"Travel Dates\", datesPicker, \"Select your start and " +
            "end dates\")\ndatesPicker = DatePicker(\"travel-dates\", \"range\", { required: true })\nfcTravellers = " +
            "FormControl(\"Number of Travellers\", travellersSelect, \"How many people are travelling?\")\n" +
            "travellersSelect = Select(\"num-travellers\", [tr1, tr2, tr3, tr4], \"Select number of travellers\", { " +
            "required: true })\ntr1 = SelectItem(\"1\", \"1 — Solo\")\ntr2 = SelectItem(\"2\", \"2 — Couple\")\ntr3 " +
            "= SelectItem(\"3-4\", \"3–4 People\")\ntr4 = SelectItem(\"5+\", \"5+ People\")\nfcTripType = " +
            "FormControl(\"Trip Type\", tripTypeCards, \"What kind of trip are you planning?\")\ntripTypeCards = " +
            "OptionCards(\"trip-type\", \"single\", [oc1, oc2, oc3, oc4], { required: true })\noc1 = " +
            "OptionCard(\"adventure\", \"Adventure\", \"Hiking, trekking & outdoor activities\", oc1icon)\noc1icon = " +
            "Icon(\"mountain\", \"nature\")\noc2 = OptionCard(\"beach\", \"Beach & Relaxation\", \"Sun, sand & " +
            "leisure\", oc2icon)\noc2icon = Icon(\"waves\", \"nature\")\noc3 = OptionCard(\"cultural\", \"Cultural & " +
            "Historical\", \"Museums, heritage & local experiences\", oc3icon)\noc3icon = Icon(\"landmark\", " +
            "\"buildings\")\noc4 = OptionCard(\"city\", \"City Break\", \"Urban exploration & nightlife\", oc4icon)\n" +
            "oc4icon = Icon(\"building-2\", \"buildings\")\nfcBudget = FormControl(\"Estimated Budget (per " +
            "person)\", budgetSlider, \"Drag to set your per-person budget in USD\")\nbudgetSlider = " +
            "Slider(\"budget\", \"discrete\", 500, 10000, 500, [2500], \"Budget (USD)\")\nfcStay = " +
            "FormControl(\"Accommodation Preference\", stayChips, \"Select all that apply\")\nstayChips = " +
            "Chips(\"accommodation\", \"multiple\", [ch1, ch2, ch3, ch4], {}, [\"hotel\"])\nch1 = " +
            "ChipItem(\"hotel\", \"Hotel\", ch1icon)\nch1icon = Icon(\"building\", \"buildings\")\nch2 = " +
            "ChipItem(\"resort\", \"Resort\", ch2icon)\nch2icon = Icon(\"umbrella\", \"travel\")\nch3 = " +
            "ChipItem(\"airbnb\", \"Vacation Rental\", ch3icon)\nch3icon = Icon(\"home\", \"home\")\nch4 = " +
            "ChipItem(\"camping\", \"Camping / Glamping\", ch4icon)\nch4icon = Icon(\"tent\", \"nature\")\nfcNotes = " +
            "FormControl(\"Special Requests or Notes\", notesArea, \"Any dietary needs, accessibility requirements, " +
            "or specific interests?\")\nnotesArea = TextArea(\"special-requests\", \"e.g. vegetarian meals, " +
            "wheelchair access, honeymoon surprise...\", 4)",
        "Example 4 — Recommendations with images and links (same pattern for products, movies, restaurants, " +
            "articles):\n\nroot = Card([header, intro, posters, linksHeader, trackCards], [src1, src2])\nheader = " +
            "CardHeader(\"Retro-Energetic Indian Tracks\", \"New Hindi releases with 80s/90s dance DNA\")\nintro = " +
            "TextContent(\"These recent tracks channel retro synths and 90s party percussion [1][2]. Tap a poster to " +
            "explore a track, or use its button to watch the exact official video.\")\nposters = " +
            "VisualCardBlock([v1, v2], \"grid\", true, @ToAssistant(\"Tell me more about this track and its retro " +
            "influences\"))\nv1 = VisualCardItem(v1body, \"tamma\", " +
            "\"https://i.ytimg.com/vi/PQHeOb0Q9oo/hq720.jpg\", v1tag, \"Tamma Tamma video poster\")\nv1body = " +
            "BoldText(\"text\", \"Tamma Tamma\", \"Dhurandhar · Ranveer Singh\")\nv1tag = Tag(\"80s Electro-Disco\", " +
            "v1icon, \"sm\", \"success\")\nv1icon = Icon(\"disc-3\", \"multimedia\")\nv2 = VisualCardItem(v2body, " +
            "\"sajan\", \"https://i.ytimg.com/vi/mLz-qBiRnl8/hq720.jpg\", v2tag, \"Sajan Re video poster\")\nv2body " +
            "= BoldText(\"text\", \"Sajan Re\", \"Nora Fatehi · Badshah\")\nv2tag = Tag(\"Synthwave Electro\", " +
            "v2icon, \"sm\", \"info\")\nv2icon = Icon(\"music\", \"multimedia\")\nlinksHeader = InlineHeader(\"Watch " +
            "the Exact Videos\", \"Each button opens that track's official video\")\ntrackCards = " +
            "CompositeCardBlock([t1, t2], \"grid\", true)\nt1 = CompositeCardItem(\"t-tamma\", t1h, [t1body, " +
            "t1tags], { button: t1btn })\nt1h = IconText(t1icon, \"neutral\", \"m\", \"Tamma Tamma\", \"Bappi Lahiri " +
            "classic, electro-funk rework\", true, \"horizontal\")\nt1icon = Icon(\"disc-3\", \"multimedia\")\n" +
            "t1body = Text(\"text\", \"Heavy analog synths and brass over a fast disco groove.\", \"Retro " +
            "Electro-Disco\")\nt1tags = TagBlock([\"2026 Film Hit\", \"Peak Party\"])\nt1btn = Button(\"Watch on " +
            "YouTube\", @OpenUrl(\"https://www.youtube.com/watch?v=PQHeOb0Q9oo\"), \"primary\")\nt2 = " +
            "CompositeCardItem(\"t-sajan\", t2h, [t2body, t2tags], { button: t2btn })\nt2h = IconText(t2icon, " +
            "\"neutral\", \"m\", \"Sajan Re\", \"Nora Fatehi · Badshah · Sanjoy\", true, \"horizontal\")\nt2icon = " +
            "Icon(\"music\", \"multimedia\")\nt2body = Text(\"text\", \"Synthwave melody hooks over a modern club " +
            "beat.\", \"Retro-Fusion Electro\")\nt2tags = TagBlock([\"Club Anthem\", \"Nora x Badshah\"])\nt2btn = " +
            "Button(\"Watch on YouTube\", @OpenUrl(\"https://www.youtube.com/watch?v=COUSCnFazzc\"), \"primary\")\n" +
            "src1 = { title: \"Tamma Tamma (Full Video) - Dhurandhar\", sourceName: \"YouTube (T-Series)\", url: " +
            "\"https://www.youtube.com/watch?v=PQHeOb0Q9oo\" }\nsrc2 = { title: \"Sajan Re (Official Music Video)\", " +
            "sourceName: \"YouTube (T-Series)\", url: \"https://www.youtube.com/watch?v=COUSCnFazzc\" }",
        "Example 5 — Table with follow-ups:\n\nroot = Card([title, tbl, followUps])\ntitle = TextContent(\"Top " +
            "Languages\", \"large-heavy\")\ntbl = Table([Col(\"Language\", langs), Col(\"Users (M)\", users), " +
            "Col(\"Year\", years)])\nlangs = [\"Python\", \"JavaScript\", \"Java\"]\nusers = [15.7, 14.2, 12.1]\n" +
            "years = [1991, 1995, 1995]\nfollowUps = FollowUpBlock([fu1, fu2])\nfu1 = FollowUpItem(\"Tell me more " +
            "about Python\")\nfu2 = FollowUpItem(\"Show me a JavaScript comparison\")",
    )

    val additionalRules: List<String> = listOf(
        "When asked about data, generate realistic/plausible data",
        "Every response is a single Card(children) — children stack vertically automatically. No layout params are " +
            "needed on Card.",
        "Card is the only layout container. Do NOT use Stack. Use Tabs to switch between sections, Carousel for " +
            "horizontal scroll.",
        "Use FollowUpBlock at the END of a Card to suggest what the user can do or ask next.",
        "Use ListBlock when presenting a set of options or steps; give each ListItem an action (5th argument) to " +
            "make it clickable — without one the item is plain text.",
        "Use SectionBlock to group long responses into collapsible sections — good for reports, FAQs, and structured " +
            "content.",
        "Use SectionItem inside SectionBlock: each item needs a unique value id, a trigger (header label), and a " +
            "content array.",
        "Carousel takes an array of slides, where each slide is an array of content: carousel = Carousel([[t1, " +
            "img1], [t2, img2]])",
        "IMPORTANT: Every slide in a Carousel must use the same component structure in the same order — e.g. all " +
            "slides: [title, image, description, tags].",
        "For forms, define one FormControl reference per field so controls can stream progressively.",
        "For forms, always provide the second Form argument with Buttons(...) actions: Form(name, buttons, fields).",
        "Never nest Form inside Form.",
        "CardHeader is the page-level heading — place it as the first child of Card; InlineHeader is a lighter " +
            "subsection label for a block inside the response, and is redundant when the following component already " +
            "has a heading.",
        "Never use Callout or TextCallout to justify why you did what you did or for any meta-information.",
        "Every image slot (Image, ImageBlock, ImageGallery, VisualCardItem/ContextCardItem bgImageSrc, ImageText, " +
            "ImageTextLarge, ListItem image) requires a REAL URL from a tool result, the conversation, or the user — " +
            "NEVER invent, guess, or template a URL.",
        "If no image URL is in context, compose without image components — icons, gray ContextCardItems, and charts " +
            "still make rich layouts.",
        "Argument-order trap: Image takes alt FIRST, while ImageBlock, ImageText, and ImageTextLarge take src FIRST " +
            "— alt text in a src slot ships a broken image.",
        "Card blocks (SnippetCardBlock, OverviewCardBlock, ContextCardBlock, CompositeCardBlock, VisualCardBlock) " +
            "need at least 2 items, all with the same structure; use layout \"grid\" for primary side-by-side " +
            "comparison and \"carousel\" for secondary browsing.",
        "The card-block action is BLOCK-LEVEL: one action shared by every card, with the clicked card's details " +
            "attached automatically — keep the @ToAssistant message generic (e.g. \"Tell me more about this " +
            "destination\"), and never use a block-level @OpenUrl since it would send every card to the same URL.",
        "Card items have NO per-item action: to give each item its own external link, use CompositeCardItem with " +
            "footer { button: Button(label, @OpenUrl(url)) } — never claim a card opens a link it cannot.",
        "Text, BoldText, IconText, ImageText, ImageTextLarge and MetricIndicator* are inline building blocks used " +
            "INSIDE card items — do not place them directly in the root Card.",
        "EntityList size \"small\" is reserved for CompositeCardItem body content (no header/footer); use size " +
            "\"default\" elsewhere.",
        "Tag variant must be semantically correct (success=positive, danger=negative, warning=cautionary, " +
            "info=informational, neutral=label); Tag takes an optional Icon second, so use Tag(text, icon, size, " +
            "variant) when a variant is needed.",
        "TagBlock takes plain strings — keep it to at most 3 tags.",
        "ListBlock variant \"number\" is for ordered sequences and rankings, \"image\" for products/profiles with a " +
            "real image; Steps is for ordered sequential processes only.",
        "Use SectionBlock to divide a long response into independent, equally prominent topics; use Tabs when the " +
            "items are alternative views of the same thing; set isFoldable=false unless a section has 4+ distinct " +
            "blocks; never make SectionBlock the first Card child.",
        "Accordion is for supplementary or FAQ content only — never primary information, 3-5 items max, and always " +
            "the last component in its section or card.",
        "Charts: LineChart/AreaChart variant \"natural\" for smooth trends and \"linear\" for precise point-to-point " +
            "data; BarChart \"grouped\" for side-by-side comparison and \"stacked\" for part-to-whole; prefer " +
            "HorizontalBarChart when category labels are long or numerous.",
        "For large chart values, scale the data and put the unit in the axis label (e.g. yLabel \"Revenue (\$k)\") " +
            "instead of passing raw thousands or millions.",
        "Quantitative asks get a chart (plus KPI cards where useful) in the FIRST response — do not answer numbers " +
            "with prose or a table alone.",
        "Table is column-oriented: each Col holds its own data array of plain strings or numbers (never components), " +
            "and every column's array must have the same length.",
        "Use EditableTable ONLY when the user or app explicitly asks for an editable table; default to Table for " +
            "read-only data.",
        "Every Form MUST have EXACTLY ONE submit Button with variant \"primary\" — that button validates the form; " +
            "other buttons (secondary/tertiary escape hatches like \"Just Surprise Me\") skip validation, and there " +
            "is never a reset button.",
        "Prefer structured inputs (Select, Chips, OptionCards, DatePicker, Slider) over free-text Input, give every " +
            "FormControl a clear label (and a hint where it prevents mistakes), and keep each field name unique " +
            "within the response.",
        "Validation rules ({ required: true, email: true, minLength: 8, ... }) are enforced with inline errors — use " +
            "them for genuinely essential fields, but never require a free-text field where leaving it blank is " +
            "meaningful.",
        "Form inputs work ONLY inside a FormControl within a Form; for tappable suggestion choices OUTSIDE a form, " +
            "use Buttons whose Button actions are @ToAssistant, never a bare OptionCards or Chips.",
        "Only add Buttons for real actions: form submit, @OpenUrl with a URL from a tool or the user (never " +
            "fabricated), or a @ToAssistant follow-up. Never add a button for an app action (open, download, export, " +
            "edit) you cannot perform.",
        "Every Button label must accurately describe what clicking it does; use \"primary\" for the main action and " +
            "\"secondary\"/\"tertiary\" only for lower-emphasis supporting actions.",
        "Icon: always include the category argument — it enables fallback matching when the exact lucide name is " +
            "unavailable.",
        "Pass sources on Card ONLY when the answer relies on real references you actually have, and cite them inline " +
            "in TextContent as [1], [2] (1-based index into sources).",
    )
}

internal object StandardLibraryDefinition {
    const val root: String = "Stack"

    // Components in library order.
    val schemas: List<ComponentSchema> = listOf(
        StandardSchemas.card,
        CommonSchemas.cardHeader,
        CommonSchemas.textContent,
        CommonSchemas.markDownRenderer,
        CommonSchemas.callout,
        CommonSchemas.textCallout,
        CommonSchemas.image,
        CommonSchemas.imageBlock,
        CommonSchemas.imageGallery,
        CommonSchemas.codeBlock,
        CommonSchemas.inlineHeader,
        CommonSchemas.table,
        CommonSchemas.col,
        CommonSchemas.editableTable,
        CommonSchemas.barChart,
        CommonSchemas.lineChart,
        CommonSchemas.areaChart,
        CommonSchemas.radarChart,
        CommonSchemas.horizontalBarChart,
        CommonSchemas.series,
        CommonSchemas.pieChart,
        CommonSchemas.radialChart,
        CommonSchemas.singleStackedBarChart,
        CommonSchemas.slice,
        CommonSchemas.scatterChart,
        CommonSchemas.scatterSeries,
        CommonSchemas.point,
        CommonSchemas.form,
        CommonSchemas.formControl,
        CommonSchemas.label,
        CommonSchemas.input,
        CommonSchemas.textArea,
        CommonSchemas.select,
        CommonSchemas.selectItem,
        CommonSchemas.datePicker,
        CommonSchemas.slider,
        CommonSchemas.checkBoxGroup,
        CommonSchemas.checkBoxItem,
        CommonSchemas.radioGroup,
        CommonSchemas.radioItem,
        CommonSchemas.switchGroup,
        CommonSchemas.switchItem,
        CommonSchemas.chipItem,
        CommonSchemas.chips,
        CommonSchemas.optionCard,
        CommonSchemas.optionCards,
        CommonSchemas.button,
        CommonSchemas.buttons,
        CommonSchemas.iconButton,
        StandardSchemas.stack,
        CommonSchemas.tabs,
        StandardSchemas.tabItem,
        CommonSchemas.accordion,
        StandardSchemas.accordionItem,
        CommonSchemas.steps,
        CommonSchemas.stepsItem,
        StandardSchemas.carousel,
        CommonSchemas.separator,
        CommonSchemas.tagBlock,
        CommonSchemas.tag,
        CommonSchemas.icon,
        CommonSchemas.entityList,
        CommonSchemas.listBlock,
        CommonSchemas.listItem,
        CommonSchemas.text,
        CommonSchemas.boldText,
        CommonSchemas.iconText,
        CommonSchemas.imageText,
        CommonSchemas.imageTextLarge,
        CommonSchemas.metricIndicatorInline,
        CommonSchemas.metricIndicatorWithStrikethrough,
        CommonSchemas.snippetCardItem,
        CommonSchemas.snippetCardBlock,
        CommonSchemas.overviewCardItem,
        CommonSchemas.overviewCardBlock,
        CommonSchemas.contextCardItem,
        CommonSchemas.contextCardBlock,
        CommonSchemas.compositeCardItem,
        CommonSchemas.compositeCardBlock,
        CommonSchemas.visualCardItem,
        CommonSchemas.visualCardBlock,
        StandardSchemas.modal,
    )

    val componentGroups: List<ComponentGroup> = listOf(
        ComponentGroup(
            name = "Layout",
            components = listOf(
                "Stack", "Tabs", "TabItem", "Accordion", "AccordionItem", "Steps", "StepsItem", "Carousel",
                "Separator", "Modal",
            ),
            notes = listOf(
                "- For grid-like layouts, use Stack with direction \"row\" and wrap set to true.",
                "- Prefer justify \"start\" (or omit justify) with wrap=true for stable columns instead of uneven " +
                    "gutters.",
                "- Use nested Stacks when you need explicit rows/sections.",
                "- Show/hide sections: \$editId != \"\" ? Card([editForm]) : null",
                "- Modal: Modal(\"Title\", \$showModal, [content]) — \$showModal is boolean, X/Escape auto-closes. " +
                    "Put Form with its own buttons inside children.",
                "- Use Tabs for alternative views (chart types, data sections) — no \$variable needed",
                "- Shared filter across Tabs: same \$days binding in Query args works across all TabItems",
            ),
        ),
        ComponentGroup(
            name = "Content",
            components = listOf(
                "Card", "CardHeader", "TextContent", "MarkDownRenderer", "Callout", "TextCallout", "Image",
                "ImageBlock", "ImageGallery", "CodeBlock", "InlineHeader",
            ),
            notes = listOf(
                "- InlineHeader is a compact heading + description pair for labelling a block (lighter than " +
                    "CardHeader).",
                "- Use Cards to group related KPIs or sections. Stack with direction \"row\" for side-by-side layouts.",
                "- Success toast: Callout(\"success\", \"Saved\", \"Done.\", \$showSuccess) — use " +
                    "@Set(\$showSuccess, true) in save action, auto-dismisses after 3s. For errors: result.status == " +
                    "\"error\" ? Callout(\"error\", \"Failed\", result.error) : null",
                "- KPI card: Card([TextContent(\"Label\", \"small\"), TextContent(\"\" + @Count(@Filter(data.rows, " +
                    "\"field\", \"==\", \"value\")), \"large-heavy\")])",
            ),
        ),
        ComponentGroup(
            name = "Tables",
            components = listOf("Table", "Col", "EditableTable"),
            notes = listOf(
                "- EditableTable lets the user edit cells inline. Give it a unique name, columns of { type, key, " +
                    "header } with type one of text | number | date-single | select | url (select also needs " +
                    "options: [{ value, label }]).",
                "- data is an array of { id, values } rows where values are ordered positionally to match columns. " +
                    "Edited data is submitted when the user clicks Save Changes.",
                "- Table is COLUMN-oriented: Table([Col(\"Label\", dataArray), Col(\"Count\", countArray, " +
                    "\"number\")]). Use array pluck for data: data.rows.fieldName",
                "- Col data can be component arrays for styled cells: Col(\"Status\", @Each(data.rows, \"item\", " +
                    "Tag(item.status, null, \"sm\", item.status == \"open\" ? \"success\" : \"danger\")))",
                "- Row actions: Col(\"Actions\", @Each(data.rows, \"t\", Button(\"Edit\", Action([@Set(\$showEdit, " +
                    "true), @Set(\$editId, t.id)]))))",
                "- Sortable: sorted = @Sort(data.rows, \$sortField, \"desc\"). Bind \$sortField to Select. Use " +
                    "sorted.fieldName for Col data",
                "- Searchable: filtered = @Filter(data.rows, \"title\", \"contains\", \$search). Bind \$search to " +
                    "Input",
                "- Chain sort + filter: filtered = @Filter(...) then sorted = @Sort(filtered, ...) — use sorted for " +
                    "both Table and Charts",
                "- Empty state: @Count(data.rows) > 0 ? Table([...]) : TextContent(\"No data yet\")",
            ),
        ),
        ComponentGroup(
            name = "Charts (2D)",
            components = listOf("BarChart", "LineChart", "AreaChart", "RadarChart", "HorizontalBarChart", "Series"),
            notes = listOf(
                "- Charts accept column arrays: LineChart(labels, [Series(\"Name\", values)]). Use array pluck: " +
                    "LineChart(data.rows.day, [Series(\"Views\", data.rows.views)])",
                "- Use Cards to wrap charts with CardHeader for titled sections",
                "- Chart + Table from same source: use @Sort or @Filter result for both LineChart and Table Col data",
                "- Multiple chart views: use Tabs — Tabs([TabItem(\"line\", \"Line\", [LineChart(...)]), " +
                    "TabItem(\"bar\", \"Bar\", [BarChart(...)])])",
            ),
        ),
        ComponentGroup(
            name = "Charts (1D)",
            components = listOf("PieChart", "RadialChart", "SingleStackedBarChart", "Slice"),
            notes = listOf(
                "- PieChart and BarChart need NUMBERS, not objects. For list data, use @Count(@Filter(...)) to " +
                    "aggregate:",
                "- PieChart from list: `PieChart([\"Low\", \"Med\", \"High\"], [@Count(@Filter(data.rows, " +
                    "\"priority\", \"==\", \"low\")), @Count(@Filter(data.rows, \"priority\", \"==\", \"medium\")), " +
                    "@Count(@Filter(data.rows, \"priority\", \"==\", \"high\"))], \"donut\")`",
                "- KPI from count: `TextContent(\"\" + @Count(@Filter(data.rows, \"status\", \"==\", \"open\")), " +
                    "\"large-heavy\")`",
            ),
        ),
        ComponentGroup(name = "Charts (Scatter)", components = listOf("ScatterChart", "ScatterSeries", "Point")),
        ComponentGroup(
            name = "Forms",
            components = listOf(
                "Form", "FormControl", "Label", "Input", "TextArea", "Select", "SelectItem", "DatePicker", "Slider",
                "CheckBoxGroup", "CheckBoxItem", "RadioGroup", "RadioItem", "SwitchGroup", "SwitchItem", "Chips",
                "ChipItem", "OptionCards", "OptionCard",
            ),
            notes = listOf(
                "- Chips: compact single/multiple selection pills. Use ChipItem references for each option.",
                "- OptionCards: larger selectable cards with title, subtitle and an optional Icon or Image on top. " +
                    "Use OptionCard references for each option.",
                "- For Form fields, define EACH FormControl as its own reference — do NOT inline all controls in one " +
                    "array. This allows progressive field-by-field streaming.",
                "- NEVER nest Form inside Form — each Form should be a standalone container.",
                "- Form requires explicit buttons. Always pass a Buttons(...) reference as the third Form argument.",
                "- rules is an optional object: {required: true, email: true, minLength: 8, maxLength: 100}",
                "- Available rules: required, email, min, max, minLength, maxLength, pattern, url, numeric",
                "- The renderer shows error messages automatically — do NOT generate error text in the UI",
                "- Conditional fields: \$country == \"US\" ? stateField : \$country == \"UK\" ? postcodeField : " +
                    "addressField",
                "- Edit form in Modal: Modal(\"Edit\", \$showEdit, [Form(\"edit\", Buttons([saveBtn, cancelBtn]), " +
                    "[fields...])]). Save button should include @Set(\$showEdit, false) to close modal.",
            ),
        ),
        ComponentGroup(
            name = "Buttons",
            components = listOf("Button", "Buttons", "IconButton"),
            notes = listOf(
                "- Icon renders a lucide icon by kebab-case name; it is also used as the icon of IconButton, " +
                    "IconText and OptionCard.",
                "- Toggle in @Each: @Each(rows, \"t\", Button(t.status == \"open\" ? \"Close\" : \"Reopen\", " +
                    "Action([...])))",
            ),
        ),
        ComponentGroup(
            name = "Data Display",
            components = listOf("TagBlock", "Tag", "Icon", "EntityList", "ListBlock", "ListItem"),
            notes = listOf(
                "- Color-mapped Tag: Tag(value, null, \"sm\", value == \"high\" ? \"danger\" : value == \"medium\" ? " +
                    "\"warning\" : \"neutral\")",
                "- EntityList is a compact two-column list of { left, right } rows (e.g. name / value). " +
                    "size='default' also supports a header and footer row; size='small' does not.",
                "- ListBlock is a numbered or image list of ListItem references. An action on ListItem is optional.",
            ),
        ),
        ComponentGroup(
            name = "Cards",
            components = listOf(
                "SnippetCardBlock", "SnippetCardItem", "OverviewCardBlock", "OverviewCardItem", "ContextCardBlock",
                "ContextCardItem", "CompositeCardBlock", "CompositeCardItem", "VisualCardBlock", "VisualCardItem",
                "Text", "BoldText", "IconText", "ImageText", "ImageTextLarge", "MetricIndicatorInline",
                "MetricIndicatorWithStrikethrough",
            ),
            notes = listOf(
                "- Card blocks lay out 2+ items in a responsive grid (or carousel where supported). Every item in a " +
                    "block must have the same structure.",
                "- SnippetCardItem: small card with lhs (IconText | ImageText) and optional rhs (Text | BoldText) — " +
                    "good for key/value facts.",
                "- OverviewCardItem: small card with top (IconText | ImageText | Text) and optional bottom " +
                    "MetricIndicatorInline — good for KPIs.",
                "- ContextCardItem: medium card with a title (string or Tag), body text and optional background " +
                    "image — good for summaries.",
                "- CompositeCardItem: rich card with header, body array (Text, BoldText, MetricIndicatorInline, " +
                    "IconText, Image, charts, ListBlock, TagBlock, EntityList) and footer (price + Button) — good " +
                    "for products/offers.",
                "- VisualCardItem: image-first card with a BoldText body and optional Tag.",
                "- Text / BoldText / IconText / ImageText / ImageTextLarge / MetricIndicator* are the inline " +
                    "building blocks used INSIDE card items; do not place them directly in the root Card.",
            ),
        ),
    )

    val examples: List<String> = listOf(
        "Example 1 — Table (column-oriented):\n\nroot = Stack([title, tbl])\ntitle = TextContent(\"Top Languages\", " +
            "\"large-heavy\")\ntbl = Table([Col(\"Language\", langs), Col(\"Users (M)\", users), Col(\"Year\", " +
            "years)])\nlangs = [\"Python\", \"JavaScript\", \"Java\", \"TypeScript\", \"Go\"]\nusers = [15.7, 14.2, " +
            "12.1, 8.5, 5.2]\nyears = [1991, 1995, 1995, 2012, 2009]",
        "Example 2 — Bar chart:\n\nroot = Stack([title, chart])\ntitle = TextContent(\"Q4 Revenue\", " +
            "\"large-heavy\")\nchart = BarChart(labels, [s1, s2], \"grouped\")\nlabels = [\"Oct\", \"Nov\", " +
            "\"Dec\"]\ns1 = Series(\"Product A\", [120, 150, 180])\ns2 = Series(\"Product B\", [90, 110, 140])",
        "Example 3 — Form with validation:\n\nroot = Stack([title, form])\ntitle = TextContent(\"Contact Us\", " +
            "\"large-heavy\")\nform = Form(\"contact\", btns, [nameField, emailField, countryField, msgField])\n" +
            "nameField = FormControl(\"Name\", Input(\"name\", \"Your name\", \"text\", { required: true, minLength: " +
            "2 }))\nemailField = FormControl(\"Email\", Input(\"email\", \"you@example.com\", \"email\", { required: " +
            "true, email: true }))\ncountryField = FormControl(\"Country\", Select(\"country\", countryOpts, " +
            "\"Select...\", { required: true }))\nmsgField = FormControl(\"Message\", TextArea(\"message\", \"Tell " +
            "us more...\", 4, { required: true, minLength: 10 }))\ncountryOpts = [SelectItem(\"us\", \"United " +
            "States\"), SelectItem(\"uk\", \"United Kingdom\"), SelectItem(\"de\", \"Germany\")]\nbtns = " +
            "Buttons([Button(\"Submit\", @ToAssistant(\"Submit\"), \"primary\"), Button(\"Cancel\", " +
            "@ToAssistant(\"Cancel\"), \"secondary\")])",
        "Example 4 — Tabs with mixed content:\n\nroot = Stack([title, tabs])\ntitle = TextContent(\"React vs Vue\", " +
            "\"large-heavy\")\ntabs = Tabs([tabReact, tabVue])\ntabReact = TabItem(\"react\", \"React\", " +
            "reactContent)\ntabVue = TabItem(\"vue\", \"Vue\", vueContent)\nreactContent = [TextContent(\"React is a " +
            "library by Meta for building UIs.\"), Callout(\"info\", \"Note\", \"React uses JSX syntax.\")]\n" +
            "vueContent = [TextContent(\"Vue is a progressive framework by Evan You.\"), Callout(\"success\", " +
            "\"Tip\", \"Vue has a gentle learning curve.\")]",
        "Example 5 — KPI and product cards:\n\nroot = Stack([kpiHeader, kpiBlock, productsHeader, productCards])\n" +
            "kpiHeader = InlineHeader(\"This Month\", \"Key account metrics\")\nkpiBlock = SnippetCardBlock([kpi1, " +
            "kpi2, kpi3])\nkpi1 = SnippetCardItem(\"revenue\", kpi1lhs, kpi1rhs)\nkpi1lhs = IconText(kpi1icon, " +
            "\"neutral\", \"m\", \"Revenue\", \"Month to date\", false, \"horizontal\")\nkpi1icon = " +
            "Icon(\"circle-dollar-sign\", \"finance\")\nkpi1rhs = BoldText(\"number\", \"\$48,200\", \"+8.1%\", " +
            "\"metric\")\nkpi2 = SnippetCardItem(\"active-users\", kpi2lhs, kpi2rhs)\nkpi2lhs = IconText(kpi2icon, " +
            "\"neutral\", \"m\", \"Active Users\", \"This month\", false, \"horizontal\")\nkpi2icon = " +
            "Icon(\"users\", \"people\")\nkpi2rhs = BoldText(\"number\", \"1,204\", \"+3.4%\", \"metric\")\nkpi3 = " +
            "SnippetCardItem(\"churn\", kpi3lhs, kpi3rhs)\nkpi3lhs = IconText(kpi3icon, \"neutral\", \"m\", " +
            "\"Churn\", \"This month\", false, \"horizontal\")\nkpi3icon = Icon(\"user-minus\", \"people\")\nkpi3rhs " +
            "= BoldText(\"number\", \"1.8%\", \"-0.3%\", \"metric\")\nproductsHeader = InlineHeader(\"Top " +
            "Products\", \"By units sold\")\nproductCards = CompositeCardBlock([p1, p2])\np1 = " +
            "CompositeCardItem(\"widget-pro\", p1header, [p1body], p1footer)\np1header = IconText(p1icon, " +
            "\"neutral\", \"m\", \"Widget Pro\", \"Best seller\", true, \"horizontal\")\np1icon = Icon(\"package\", " +
            "\"shopping\")\np1body = MetricIndicatorInline(\"2,410 units\", \"Sold this month\", { direction: " +
            "\"up\", value: 12 })\np1footer = { price: BoldText(\"text\", \"\$29.00\"), button: p1btn }\np1btn = " +
            "Button(\"View Details\", @ToAssistant(\"Show details for Widget Pro\"), \"secondary\")\np2 = " +
            "CompositeCardItem(\"widget-mini\", p2header, [p2body], p2footer)\np2header = IconText(p2icon, " +
            "\"neutral\", \"m\", \"Widget Mini\", \"New arrival\", true, \"horizontal\")\np2icon = Icon(\"box\", " +
            "\"shopping\")\np2body = MetricIndicatorInline(\"980 units\", \"Sold this month\", { direction: \"up\", " +
            "value: 4 })\np2footer = { price: BoldText(\"text\", \"\$14.00\"), button: p2btn }\np2btn = " +
            "Button(\"View Details\", @ToAssistant(\"Show details for Widget Mini\"), \"secondary\")",
        "Example 6 — Editable table and a selection form:\n\nroot = Stack([tableHeader, editTable, formHeader, " +
            "prefsForm])\ntableHeader = InlineHeader(\"Team Roster\", \"Click a cell to edit\")\neditTable = " +
            "EditableTable(\"roster\", [colName, colRole, colStart], [row1, row2])\ncolName = { type: \"text\", key: " +
            "\"name\", header: \"Name\" }\ncolRole = { type: \"select\", key: \"role\", header: \"Role\", options: " +
            "[roleEng, roleDesign] }\nroleEng = { value: \"eng\", label: \"Engineering\" }\nroleDesign = { value: " +
            "\"design\", label: \"Design\" }\ncolStart = { type: \"date-single\", key: \"start\", header: \"Start " +
            "Date\" }\nrow1 = { id: \"1\", values: [\"Alex Kim\", \"eng\", \"2024-01-15\"] }\nrow2 = { id: \"2\", " +
            "values: [\"Jamie Lee\", \"design\", \"2024-03-02\"] }\nformHeader = InlineHeader(\"Preferences\", " +
            "\"Tell us how you like to work\")\nprefsForm = Form(\"prefs\", formButtons, [fc1, fc2])\nfc1 = " +
            "FormControl(\"Working Style\", styleCards, \"Pick the one that fits best\")\nstyleCards = " +
            "OptionCards(\"style\", \"single\", [style1, style2])\nstyle1 = OptionCard(\"focused\", \"Deep Focus\", " +
            "\"Long uninterrupted blocks\", style1icon)\nstyle1icon = Icon(\"target\", \"tools\")\nstyle2 = " +
            "OptionCard(\"collab\", \"Collaborative\", \"Frequent pairing and syncs\", style2icon)\nstyle2icon = " +
            "Icon(\"users\", \"people\")\nfc2 = FormControl(\"Tools\", toolChips, \"Select all that apply\")\n" +
            "toolChips = Chips(\"tools\", \"multiple\", [tool1, tool2, tool3])\ntool1 = ChipItem(\"figma\", " +
            "\"Figma\", tool1icon)\ntool1icon = Icon(\"figma\", \"design\")\ntool2 = ChipItem(\"slack\", \"Slack\", " +
            "tool2icon)\ntool2icon = Icon(\"message-square\", \"communication\")\ntool3 = ChipItem(\"notion\", " +
            "\"Notion\", tool3icon)\ntool3icon = Icon(\"notebook\", \"text\")\nformButtons = " +
            "Buttons([Button(\"Save\", @ToAssistant(\"Save preferences\"), \"primary\")])",
    )

    val additionalRules: List<String> = listOf(
        "When asked about data, generate realistic/plausible data",
        "For grid-like layouts, use Stack with direction \"row\" and wrap=true. Avoid justify=\"between\" unless you " +
            "specifically want large gutters.",
        "For forms, define one FormControl reference per field so controls can stream progressively.",
        "For forms, always provide the second Form argument with Buttons(...) actions: Form(name, buttons, fields).",
        "Never nest Form inside Form.",
        "Use @Reset(\$var1, \$var2) after form submit to restore defaults — not @Set(\$var, \"\")",
        "Multi-query refresh: Action([@Run(mutation), @Run(query1), @Run(query2), @Reset(...)])",
        "\$variables are reactive: changing via Select or @Set re-evaluates all Queries and expressions referencing " +
            "them",
        "Use existing components (Tabs, Accordion, Modal) before inventing ternary show/hide patterns",
        "Card blocks (SnippetCardBlock, OverviewCardBlock, ContextCardBlock, CompositeCardBlock, VisualCardBlock) " +
            "need at least 2 items; every item in a block must have the same structure.",
        "Text / BoldText / IconText / ImageText / ImageTextLarge / MetricIndicatorInline / " +
            "MetricIndicatorWithStrikethrough are inline building blocks used INSIDE card items — do not place them " +
            "directly in a Stack or Card.",
        "EntityList size='small' (no header/footer) is only for use inside a CompositeCardItem body; use " +
            "size='default' everywhere else.",
        "Image URLs must be real (from a tool result or the user) — never invent or template an image URL.",
        "Always pass a category to Icon — it enables a topical fallback when the exact icon name is unavailable.",
        "EditableTable requires the consuming app to persist edits — use it only when the user or app explicitly " +
            "asks for an editable table; default to Table for read-only data.",
    )
}
