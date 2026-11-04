# A2UI

`stream-chat-android-ai-a2ui` shows the generative UI agents write in [A2UI](https://a2ui.org) 1.0, the protocol in which an agent sends JSON messages that build surfaces of components. The module plugs into the generative UI layer of `stream-chat-android-ai-compose`.

## Protocol support

The renderer reads the final A2UI 1.0 wire format:

- every message from `createSurface` to `callRendererFunction`, with `"version": "1.0"`;
- the data model and its bindings: `{"@path": ...}` JSON Pointers, absolute or relative to a list template's item, written back by inputs;
- function calls with `{"@call": ...}`, including the `@index` system function and `@@` escapes for literal `@` keys;
- `formatString` templates (`${/path}`, `${relative}`, `${fn(arg: value)}`, `\${` for a literal);
- function calls from the agent: the agent can call the renderer's functions with `callRendererFunction`;
- the basic catalog: every component and function;
- errors with the spec's codes and JSON Pointer paths.

The message processor also produces the renderer's other messages to the agent: `callAgentFunction` for a function the catalogs lack, and `error`. A chat sends the agent only the actions a person takes; it logs the function calls and errors, so a value that needs a function of the agent's stays empty.

Parsing and processing are internal: the public API is the renderer, its catalog and the actions it sends.

## Conformance

The tests run the A2UI conformance suite (`conformance/core` of the A2UI repository) case by case, and every example published with the basic catalog. Cases for earlier protocol versions, and for what a renderer in a chat doesn't do, are skipped with the reason in `ConformanceSkips`.

## License

A2UI is Apache 2.0 licensed. This module bundles its basic catalog and common types, and its tests use its conformance suite; see [THIRD_PARTY_NOTICES](THIRD_PARTY_NOTICES).
