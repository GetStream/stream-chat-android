# Optional Members and Messages in QueryChannels

## What This Is

A modification to the Stream Chat Android SDK's QueryChannels operation to make the `members` and `messages` size limits optional (nullable) with server-driven defaults. This gives integrators flexibility over payload size while simplifying the API.

## Core Value

Server-driven defaults for QueryChannels pagination — clients should not impose arbitrary limits when they don't need to.

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] Make `members` limit optional in QueryChannels request
- [ ] Make `messages` limit optional in QueryChannels request
- [ ] Change default values from hardcoded integers to `null`
- [ ] When `null`, omit parameters from network request entirely
- [ ] Update public API layer (QueryChannelsRequest, ChatClient)
- [ ] Update internal DTOs and network layer models
- [ ] Update State/Offline plugin to handle optional values

### Out of Scope

- Changing other QueryChannels parameters — members and messages only
- Adding new pagination options — this is about making existing ones optional
- Backward compatibility shims — intentional behavior change accepted

## Context

This is an existing, mature Android SDK for Stream Chat. The codebase has:
- Multi-module Gradle structure with clear separation (client, state, offline, UI components)
- Kotlin-first with comprehensive type safety
- Moshi for JSON serialization with custom adapters

The change affects customers using SDK defaults (they'll now get server-driven behavior) but not those who explicitly set values.

## Constraints

- **Serialization**: Null values must be omitted from JSON request, not sent as `null`
- **Type Safety**: Kotlin nullability must be properly reflected in public API
- **Layer Consistency**: Public API, DTOs, and state layer must all handle optionality consistently

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Null means "omit from request" | Server should decide defaults, not receive explicit nulls | — Pending |
| Intentional breaking change | Flexibility for integrators outweighs compatibility concern | — Pending |

---
*Last updated: 2026-02-08 after initialization*
