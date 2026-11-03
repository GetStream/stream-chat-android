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

internal object PromptSections {
    const val builtinFunctions: String =
        "## Built-in Functions\n\nData functions prefixed with `@` to distinguish from components. These are the " +
            "ONLY functions available — do NOT invent new ones.\nUse @-prefixed built-in functions (@Count, @Sum, " +
            "@Avg, @Min, @Max, @Round) on {{target}}. Do NOT hardcode computed values.\n\n{{lines}}\n\nBuiltins " +
            "compose — output of one is input to the next:\n`@Count(@Filter(data.rows, \"field\", \"==\", \"val\"))` " +
            "for KPIs/chart values, `@Round(@Avg(data.rows.score), 1)`, `@Each(data.rows, \"item\", " +
            "Comp(item.field))` for per-item rendering.\nArray pluck: `data.rows.field` extracts a field from every " +
            "row → use with @Sum, @Avg, charts, tables.\n\nIMPORTANT @Each rule: The loop variable (e.g. \"item\") " +
            "is ONLY available inside the @Each template expression. Always inline the template — do NOT extract it " +
            "to a separate statement.\nCORRECT: `list = @Each(rows, \"t\", Comp(t.name))`\nWRONG: `item = " +
            "Comp(t.name)` then `list = @Each(rows, \"t\", item)`, because t is undefined in item."

    const val query: String =
        "## Query — Live Data Fetching\n\nFetch data from available tools. Returns defaults instantly, swaps in real " +
            "data when it arrives.\n\n```\nmetrics = Query(\"tool_name\", {arg1: value, arg2: \$binding}, " +
            "{defaultField: 0, defaultData: []}, refreshInterval?)\n```\n\n- First arg: tool name (string)\n- Second " +
            "arg: arguments object (may reference \$bindings — re-fetches automatically on change)\n- Third arg: " +
            "default data (rendered immediately before fetch resolves)\n- Fourth arg (optional): refresh interval in " +
            "seconds (e.g. 30 for auto-refresh every 30s)\n- Use dot access on results: metrics.totalEvents, " +
            "metrics.data.day (array pluck)\n- Query results must use regular identifiers: `metrics = Query(...)`, " +
            "NOT `\$metrics = Query(...)`\n- Manual refresh: `Button(\"Refresh\", Action([@Run(query1), " +
            "@Run(query2)]), \"secondary\")` — re-fetches the listed queries\n- Refresh all queries: create Action " +
            "with @Run for each query"

    const val mutation: String =
        "## Mutation — Write Operations\n\nExecute state-changing tool calls (create, update, delete). Unlike Query " +
            "(auto-fetches on render), Mutation fires only on button click via Action.\n\n```\nresult = " +
            "Mutation(\"tool_name\", {arg1: \$binding, arg2: \"value\"})\n```\n\n- First arg: tool name (string)\n- " +
            "Second arg: arguments object (evaluated with current \$binding values at click time)\n- result.status: " +
            "\"idle\" | \"loading\" | \"success\" | \"error\"\n- result.data: tool response on success\n- " +
            "result.error: error message on failure\n- Mutation results use regular identifiers: `result = " +
            "Mutation(...)`, NOT `\$result`\n- Show loading state: `result.status == \"loading\" ? " +
            "TextContent(\"Saving...\") : null`"

    const val actionMutationExample: String =
        "Example — mutation + refresh + reset (PREFERRED pattern):\n```\n\$binding = \"default\"\nresult = " +
            "Mutation(\"tool_name\", {field: \$binding})\ndata = Query(\"tool_name\", {}, {rows: []})\nonSubmit = " +
            "Action([@Run(result), @Run(data), @Reset(\$binding)])\n```"

    const val actionNavExample: String =
        "Example — simple nav:\n```\nviewBtn = Button(\"View\", @OpenUrl(\"https://example.com\"))\n```"

    const val action: String =
        "## Action — Button Behavior\n\nAn action wires a button click to operations. Pass one @-prefixed step bare, " +
            "e.g. Button(\"Ask\", @ToAssistant(\"Tell me more\")). Put several steps in Action([...]), e.g. " +
            "Button(\"Read\", Action([@OpenUrl(\"https://example.com\"), @ToAssistant(\"Summarize this page\")])). " +
            "Steps execute in order.\nButtons without an action automatically send their label to the assistant " +
            "(equivalent to @ToAssistant(label)).\n\nAvailable steps:\n{{steps}}\n\n{{examples}}\n\n{{rules}}"

    const val interactiveFilters: String =
        "## Interactive Filters\n\nTo let the user filter data with a dropdown:\n1. Declare a \$variable with a " +
            "default: `\$dateRange = \"14\"`\n2. Create a Select with name, items, and binding: " +
            "`Select(\"dateRange\", [SelectItem(\"7\", \"Last 7 days\"), ...], null, null, \$dateRange)`\n3. Wrap in " +
            "FormControl for a label: `FormControl(\"Date Range\", Select(...))`\n4. Pass \$dateRange in Query args: " +
            "`Query(\"tool\", {dateRange: \$dateRange}, {defaults})`\n5. When the user changes the Select, " +
            "\$dateRange updates and the Query automatically re-fetches\n\nFILTER WIRING RULE: If a \$binding filter " +
            "is visible in the UI, EVERY relevant Query MUST reference that \$binding in its args. Never show a " +
            "filter dropdown while hardcoding the query args.\n\nRules for \$variables:\n- \$variables hold simple " +
            "values (strings or numbers), NOT arrays or objects\n- \$variables must be bound to a Select/Input " +
            "component via the value argument (last positional arg) to be interactive\n- Queries must use regular " +
            "identifiers (NOT \$variables): `metrics = Query(...)` not `\$metrics = Query(...)`\n- **Auto-declare**: " +
            "You do NOT need to explicitly declare \$variables. If you use `\$foo` without declaring it, the parser " +
            "auto-creates `\$foo = null`. You can still declare explicitly to set a default: `\$days = \"14\"`\n\n## " +
            "Forms\n\nSimple form — no \$bindings needed. Field values are managed internally by the Form via the " +
            "name prop:\n```\ncontactForm = Form(\"contact\", submitBtn, [nameField, emailField])\nnameField = " +
            "FormControl(\"Name\", Input(\"name\", \"Your name\", \"text\", {required: true}))\nemailField = " +
            "FormControl(\"Email\", Input(\"email\", \"your@email.com\", \"email\", {required: true, email: true}))\n" +
            "submitBtn = Button(\"Submit\")\n```\n\nUse \$bindings when you need to read field values elsewhere (in " +
            "Action context, Query args, or conditionals). They are auto-declared:\n```\n\$role = \"engineer\"\n" +
            "contactForm = Form(\"contact\", submitBtn, [nameField, emailField, roleField])\nnameField = " +
            "FormControl(\"Name\", Input(\"name\", \"Enter your name\", \"text\", {required: true}, \$name))\n" +
            "emailField = FormControl(\"Email\", Input(\"email\", \"Enter your email\", \"email\", {required: true, " +
            "email: true}, \$email))\nroleField = FormControl(\"Role\", Select(\"role\", [SelectItem(\"engineer\", " +
            "\"Engineer\"), SelectItem(\"designer\", \"Designer\"), SelectItem(\"pm\", \"PM\")], null, {required: " +
            "true}, \$role))\nsubmitBtn = Button(\"Submit\")\n```\n\nFor form + mutation patterns (create, refresh, " +
            "reset), see the Action section example above.\n\nIMPORTANT: Always add validation rules to form fields " +
            "used with Mutations. Use OBJECT syntax: {required: true, email: true, minLength: 8}. The renderer shows " +
            "error messages automatically and blocks submit when validation fails."

    const val editMode: String =
        "## Edit Mode\n\nThe runtime merges by statement name: same name = replace, new name = append.\nOutput ONLY " +
            "statements that changed or are new. Everything else is kept automatically.\n\n### Delete\nTo remove a " +
            "component, re-declare its parent without that component in the parent's children array. The removed " +
            "component and any statements only it referenced are automatically garbage-collected.\n\n### Patch size " +
            "guide\n- Changing a title or label: 1 statement\n- Adding a component: 2-3 statements (the new " +
            "component + parent update)\n- Removing a component: 1 statement (re-declare parent without the removed " +
            "child)\n- Adding a filter + wiring to query: 3-5 statements\n- Restructuring into tabs: 5-10 " +
            "statements\n\n### Rules\n- Reuse existing statement names exactly — do not rename\n- Do NOT re-emit " +
            "unchanged statements — the runtime keeps them\n- A typical edit patch is 1-10 statements, not 20+\n- If " +
            "the existing code already satisfies the request, output only the root statement\n- NEVER output the " +
            "entire program as a patch. Only output what actually changes\n- If you are about to output more than 10 " +
            "statements, reconsider — most edits need fewer"

    const val streaming: String =
        "## Hoisting & Streaming (CRITICAL)\n\nopenui-lang supports hoisting: a reference can be used BEFORE it is " +
            "defined. The parser resolves all references after the full input is parsed.\n\nDuring streaming, the " +
            "output is re-parsed on every chunk. Undefined references are temporarily unresolved and appear once " +
            "their definitions stream in. This creates a progressive top-down reveal — structure first, then data " +
            "fills in.\n\n**Recommended statement order for optimal streaming:**\n{{steps}}\n\nAlways write the root " +
            "= {{rootName}}(...) statement first so the UI shell appears immediately, even before child data has " +
            "streamed in."

    const val inlineMode: String =
        "## Inline Mode\n\nYou are in inline mode. You can respond in two ways:\n\n### 1. Code response (when the " +
            "user wants to CREATE or CHANGE the UI)\nWrap openui-lang code in triple-backtick fences. You can " +
            "include explanatory text before/after:\n\nHere's your dashboard:\n\n```openui-lang\nroot = " +
            "RootComp([header, content])\nheader = SomeHeader(\"Title\")\ncontent = SomeContent(\"Hello world\")\n" +
            "```\n\nI created a simple layout with a header.\n\n### 2. Text-only response (when the user asks a " +
            "QUESTION)\nIf the user asks \"what is this?\", \"explain the chart\", \"how does this work\", etc. — " +
            "respond with plain text. Do NOT output any openui-lang code. The existing dashboard stays unchanged.\n\n" +
            "### Rules\n- When the user asks for changes, output ONLY the changed/new statements in a fenced block\n" +
            "- When the user asks a question, respond with text only — NO code. The dashboard stays unchanged.\n- " +
            "The parser extracts code from fences automatically. Text outside fences is shown as chat."

    const val toolWorkflow: String =
        "## Data Workflow\n\nWhen tools are available, follow this workflow:\n1. FIRST: Call the most relevant tool " +
            "to inspect the real data shape before generating code\n2. Use Query() for READ operations (data that " +
            "should stay live) — NEVER hardcode tool results as literal arrays or objects\n3. Use Mutation() for " +
            "WRITE operations (create, update, delete) — triggered by button clicks via @Run(mutationRef)\n4. Use " +
            "the real data from step 1 as condensed Query defaults (3-5 rows) so the UI renders immediately\n5. Use " +
            "@-prefixed builtins (@Count, @Filter, @Sort, @Sum) on Query results for KPIs and aggregations — the " +
            "runtime evaluates these live on every refresh\n6. Hardcoded arrays are ONLY for static display data " +
            "(labels, options) where no tool exists\n\nWRONG — you called a tool and got data back, but you inlined " +
            "the results:\n```\nopenCount = 2\nitem1 = SomeComp(\"first item title\")\nitem2 = SomeComp(\"second " +
            "item title\")\nlist = SomeList([item1, item2])\nchart = SomeChart([\"A\", \"B\"], [12, 8])\n```\nThis " +
            "is static — it shows stale data and won't update. Creating item1, item2, item3... manually is ALWAYS " +
            "wrong when a tool exists.\n\nRIGHT — use Query() for live data, Mutation() for writes, @builtins to " +
            "derive values:\n```\ndata = Query(\"tool_name\", {}, {rows: []})\nopenCount = @Count(@Filter(data.rows, " +
            "\"field\", \"==\", \"value\"))\nlist = @Each(data.rows, \"item\", SomeComp(item.title, item.field))\n" +
            "createResult = Mutation(\"create_tool\", {title: \$title})\nsubmitBtn = Button(\"Create\", " +
            "Action([@Run(createResult), @Run(data), @Reset(\$title)]))\n```\nEverything derives from the Query — " +
            "when data refreshes, the entire dashboard updates automatically."

    const val importantRules: String =
        "## Important Rules\n- Choose components that best represent the content (tables for comparisons, charts for " +
            "trends, forms for input, etc.)\n\n## Final Verification\nBefore finishing, walk your output and " +
            "verify:\n{{verifyLines}}"
}
