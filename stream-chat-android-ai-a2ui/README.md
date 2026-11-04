# A2UI

`stream-chat-android-ai-a2ui` shows the generative UI agents write in [A2UI](https://a2ui.org) 1.0, the protocol in which an agent sends JSON messages that build surfaces of components. The module plugs into the generative UI layer of `stream-chat-android-ai-compose`.

## Show A2UI replies

Give the conversation's `AIUIHost` an `A2UIRenderer`. The app's components join the basic catalog, so the agent can place them like the catalog's own:

```kotlin
val ui = AIUIHost(renderers = listOf(A2UIRenderer()), components = listOf(placeCard)) { action ->
    viewModelScope.launch { channel.sendMessage(Message(text = action.label, extraData = action.customData)) }
}

AIConversation(messages = messages, ui = ui)
```

A reply's `ai_ui` document has the format `a2ui`, and its content holds the reply's A2UI messages: JSON objects one after another, as the agent writes them, or JSON. Each reply has a message processor of its own, kept while the host is, so what the person enters survives scrolling, and its surfaces draw as the messages arrive.

- **Actions.** A button's event reaches the host as an `AIUIAction` named after the event. Its label is the text of the button the person tapped, or else the event's name as words (`submit_form` is "Submit form"), never the agent's words. Its payload holds the `surfaceId`, the `sourceComponentId`, the event's resolved `context`, the event's `userMessage` when it has one, and the surface's `dataModel` when the agent created the surface with `sendDataModel`. What a person types into an `obscured` text field is in none of them. An app component's events carry the component's name as `component`.
- **App components.** A component joins the catalog under its name in `PascalCase` (`place_card` is `PlaceCard`), with its parameters as its properties. The catalog with them goes by `getstream.io:app-catalog` (`A2UIRenderer.catalogId`); a surface that names the basic catalog's id shows them as well. A component named like one of the basic catalog's is left out.
- **Links.** `openUrl` runs only from a person's tap, and opens through the host, which decides the kinds of link that open.
- **Errors.** The problems A2UI reports to the agent, such as a message that doesn't read, reach `A2UIRenderer(onError = { error, messageId -> ... })` as an `A2UIError` with its spec `code`, `message`, `surfaceId` and `path`. A chat has no channel for them, so forward them to your backend if the agent should hear about them. Without `onError` they are logged.
- **Media.** `A2UIRenderer(mediaUrl = { url -> ... })` returns the URL an image, video or audio clip loads from, given the one the agent wrote, or `null` to not load it. Without it, only web links load. Images load only from web links whatever it returns.
- **Capabilities.** `capabilities` gives the agent's backend the fence the agent writes in (`fence`, `ui:a2ui@1.0`), the id of the catalog with the app's components (`catalog_id`), and the instructions its model needs to write UI with that catalog (`instructions`): how a reply carries A2UI, the protocol, the basic catalog's guidelines, and the catalog as an A2UI 1.0 catalog document, in Markdown.

## Protocol support

The renderer reads the final A2UI 1.0 wire format:

- every message from `createSurface` to `callRendererFunction`, with `"version": "1.0"`;
- the data model and its bindings: `{"@path": ...}` JSON Pointers, absolute or relative to a list template's item, written back by inputs;
- function calls with `{"@call": ...}`, including the `@index` system function and `@@` escapes for literal `@` keys;
- `formatString` templates (`${/path}`, `${relative}`, `${fn(arg: value)}`, `\${` for a literal);
- function calls from the agent: the agent can call the renderer's functions with `callRendererFunction`;
- the basic catalog: every component and function;
- errors with the spec's codes and JSON Pointer paths.

The message processor also produces the renderer's other messages to the agent: `callAgentFunction` for a function the catalogs lack, and `error`. A chat sends the agent only the actions a person takes. A `callAgentFunction` runs the host's tool of that name on the device instead, and the tool's output is the function's value: a read-only tool runs while a surface renders, any other only from a person's tap, and a call no tool answers fails. Errors go to `onError`, or to the log without it.

Parsing and processing are internal: the public API is the renderer, its catalog and the actions it sends.

## The basic catalog in Compose

`A2UICatalog.Basic` draws every component of the basic catalog with Material 3: text with simple Markdown, images, icons, video and audio, rows, columns, lists, cards, tabs, modals and dividers, and buttons, text fields, checkboxes, choice pickers, sliders and date and time inputs. Inputs write to the surface's data model as the person changes them, and a button's checks disable it until they pass.

To draw a type your own way, give the catalog a renderer for it. A renderer reads its component through `A2UIComponentScope`, with bindings resolved, and draws its children with the same catalog:

```kotlin
val catalog = A2UICatalog.Basic.with("Card") { scope ->
    BrandCard(modifier = scope.modifier) {
        scope.child()?.let { scope.Child(it) }
    }
}
```

To keep the agent from using a component, remove it. It leaves the catalog the agent is told about, and a reply that still uses it shows nothing for it, as for a type no catalog has:

```kotlin
val renderer = A2UIRenderer(catalog = A2UICatalog.Basic.removing("Video", "AudioPlayer"))
```

Agent-written content is untrusted: links in text don't open, images, video and audio load only over the web unless the app's `mediaUrl` says otherwise, and nothing on the device is read for them.

## Conformance

The tests run the A2UI conformance suite (`conformance/core` of the A2UI repository) case by case, and every example published with the basic catalog. Cases for earlier protocol versions, and for what a renderer in a chat doesn't do, are skipped with the reason in `ConformanceSkips`.

## License

A2UI is Apache 2.0 licensed. This module bundles its basic catalog and common types, and its tests use its conformance suite. The icons the basic catalog names that `material-icons-core` lacks are copied from `material-icons-extended`, also Apache 2.0. See [THIRD_PARTY_NOTICES](THIRD_PARTY_NOTICES).
