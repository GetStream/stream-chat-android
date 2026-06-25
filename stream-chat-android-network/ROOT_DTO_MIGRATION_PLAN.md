# Root-DTO migration plan

Followup to the leaf migrations in `stream-chat-android-client`. The remaining manual DTOs
(`UpstreamUserDto`, `DownstreamUserDto`, `UpstreamMessageDto`, `DownstreamMessageDto`,
`UpstreamMemberDto`, `DownstreamMemberDto`, `UpstreamReactionDto`, `DownstreamReactionDto`,
`DownstreamChannelDto`, `DownstreamMuteDto`, `DownstreamChannelMuteDto`) form a cyclic
dependency graph: every one of them contains a nested DTO that itself contains another
nested DTO, eventually circling back. We cannot migrate any single root DTO without
migrating its neighbors at the same time.

## The graph

```
User ─┐
      ├── Mute (user, target_user)
      ├── ChannelMute (user, channel)
      └── Member (user)
            └── Channel (members, created_by, config)
                  └── Message (user, attachments, mentioned_users, reactions, latest_reactions,
                               own_reactions, thread_participants)
                        └── Reaction (user)
```

User is at the bottom. Member, Channel, Message, Reaction all transitively contain User.

## Generated counterparts

| Manual | Generated | Notes |
|---|---|---|
| `UpstreamUserDto` | `UserRequest` | Lean. Loses `banned`, `role`, `teams`, `teams_role`, `devices`, `privacy_settings`. Need to route via `custom` overflow on encode, or accept the loss if those fields are never set by the client. |
| `DownstreamUserDto` | `FullUserResponse` | Closest match. Pulls in `UserMuteResponse`, `ChannelMute`, `DeviceResponse` (migrated), `PrivacySettingsResponse` (migrated). |
| `DownstreamMuteDto` | `UserMuteResponse` | Has typed `user`, `target` of type `UserResponse`. |
| `DownstreamChannelMuteDto` | `ChannelMute` | Has typed `user: UserResponse`, `channel: ChannelResponse`. |
| `UpstreamMemberDto` | `ChannelMemberRequest` (used today for `UpstreamMemberDataDto`) — but the manual `UpstreamMemberDto` is a full member (with embedded user), not just user-input fields. Probably needs `ChannelMember` (the response shape) on serialize too, or a server-side acceptance of the lean shape. |
| `DownstreamMemberDto` | `ChannelMemberResponse` (check existence) |
| `DownstreamReactionDto` | `ReactionResponse` |
| `DownstreamMessageDto` | `MessageResponse` |
| `UpstreamMessageDto` | `MessageRequest` |
| `DownstreamChannelDto` | `ChannelResponse` |

## Order of operations

The cycles resolve incrementally if we walk **upward**, swapping one DTO at a time. The
key insight: when we replace `DownstreamUserDto` with `FullUserResponse`, the **field
type** of every place User is nested in another (still-manual) DTO also flips to
`FullUserResponse`. Moshi binds the JSON object to whatever type the field declares — the
outer manual DTO doesn't care that its `user: FullUserResponse?` is now generated. The
outer mapper just calls `user.toDomain()` against the new type.

So each step touches one DTO + its mapper + the field-type references in the
still-manual outer DTOs. Each step is self-contained and live-testable.

### Inbound (Downstream) sequence

1. **`DownstreamUserDto` → `FullUserResponse`.** Update `DomainMapping.DownstreamUserDto.toDomain()`. Flip the `user`/`target`/`created_by` field types in `DownstreamMuteDto`, `DownstreamChannelMuteDto`, `DownstreamMemberDto`, `DownstreamReactionDto`, `DownstreamMessageDto`, `DownstreamChannelDto`. **Test:** login + channel list (user avatars / names render).
2. **`DownstreamMuteDto` → `UserMuteResponse`** (user already generated from step 1). **Test:** mute a user (long-press DM channel → Mute), check own-user's `mutes` populates after a connect.
3. **`DownstreamChannelMuteDto` → `ChannelMute`**. **Test:** mute a channel, check `channel_mutes` in user object.
4. **`DownstreamMemberDto` → `ChannelMember(Response)`** (whichever the spec gives us). **Test:** channel info / members list.
5. **`DownstreamReactionDto` → `ReactionResponse`**. **Test:** add/remove reactions on a message.
6. **`DownstreamChannelDto` → `ChannelResponse`** (members already generated from step 4). **Test:** channel list, channel info.
7. **`DownstreamMessageDto` → `MessageResponse`** (user, reactions, members already generated). **Test:** message list, threads, search, reminders.

### Outbound (Upstream) sequence

8. **`UpstreamUserDto` → `UserRequest`** (with `custom` overflow for missing fields). **Test:** updateUser via FAB or any path that re-uploads the user.
9. **`UpstreamMemberDto`** — already mostly migrated as `UpstreamMemberDataDto` → `ChannelMemberRequest`. Sweep remaining references.
10. **`UpstreamReactionDto` → inner `ReactionRequest`** (in `SendReactionRequest`). **Test:** add reaction.
11. **`UpstreamMessageDto` → `MessageRequest`** (typed `Type` enum). **Test:** send / edit message.

### Cleanup

12. Delete `DownstreamUserDtoAdapter`, `UpstreamUserDtoAdapter`, `DownstreamMessageDtoAdapter`, etc.
13. Delete the manual DTO files.

## Shim strategy

No shim is needed with the upward-walking approach: at each step, the manual DTO's
field type changes to the generated type, and the existing mapper just calls
`generatedType.toDomain()` instead of `manualType.toDomain()`. The wire stays identical
(it's the same JSON either way).

If a step's mapper diverges in field semantics (e.g. typed `giphy` instead of overflow,
same pattern as the Attachment migration), follow the precedent set by `AttachmentDto`:
re-emit the typed field into `extraData[key]` at the mapping boundary, mark as
`TODO: promote typed property on the domain model`, and continue. This is a known cost
for any typed field that the domain currently reads via `extraData`.

## Field-by-field gotchas

- `UserRequest` (upstream) is missing `banned`, `role`, `teams`, `teams_role`, `devices`,
  `privacy_settings`. The spec author's intent is "the client sends only user-input fields;
  the server fills in the rest." Confirm where the manual code sets these fields and decide
  per-field whether to (a) drop on encode, (b) push through `custom` overflow with the
  acknowledgement that the server will then sweep them back to root via `jsonextra`.
- `MessageRequest` (upstream) has a `type: Type?` sealed-class enum. The manual sends a
  raw string. Need `Type.fromString(...)` calls, same pattern as the push-preferences
  migration earlier in this branch.
- `Custom` capital-C issue (generator issue #11) appears on `CreatePollRequest`,
  `UpdatePollRequest`, `CreatePollOptionRequest`, `UpdatePollOptionRequest`. Cosmetic
  (see updated #11) but the adapter wiring must pass `extraDataPropertyName = "Custom"`
  for those four DTOs only.
- `giphy` field on `Attachment` regression (`d7f530dbea` follow-up): typed properties that
  the legacy domain reads via `extraData[key]` need a re-emit step in the mapper. Anticipate
  similar regressions on User (`devices`, `mutes`, `channel_mutes`) and Message
  (`mentioned_users`, etc.) — write the mapper to copy typed lists back into the domain's
  expected shape, mark as TODO for proper domain refactor.

## Open questions to confirm before starting

- Are `banned`, `role`, `teams`, `teams_role` ever set by the client (on `updateUsers` or
  embedded in member payloads)? If yes, decide on the `custom` overflow route.
- Does `FullUserResponse` exactly cover what `DownstreamUserDto` decodes today? Compare
  fields side-by-side; flag anything missing for an upstream spec patch.
- Server's tolerance for receiving the lean `UserRequest` shape on endpoints that today
  receive the full `UpstreamUserDto` (i.e. `updateUsers`).

## Findings after checking the backend Go types

The wire shape is **not** overloaded — the backend sends two distinct response types
depending on context. Confirmed by reading
`/Users/gianmarcodavid/Sviluppo/chat/lib/combined/controllers/common/commonpayloads/user.go`
and `/lib/chat/controller/v1/payload/update_users.go`:

- **`UserResponse` (Go)** embeds `UserResponseCommonFields` and adds `shadow_banned`,
  `bypass_moderation`, `ban_expires`, `push_notifications`, `privacy_settings`,
  `devices`, `invisible`. **Every extra field is tagged `ignore_if_client_side`**, so
  the client-side spec strips them. What the mobile client receives is exactly
  `UserResponseCommonFields`: `id, name, image, custom, language, role, teams,
  teams_role, created_at, updated_at, deleted_at, banned, online, last_active,
  deactivated_at, blocked_user_ids, avg_response_time, revoke_tokens_issued_before`.
- **`OwnUserResponse` (Go)** embeds `UserResponseCommonFields` and adds
  `push_preferences`, `privacy_settings`, `devices`, `invisible`, `mutes`,
  `channel_mutes`, `unread_count`, `total_unread_count`, `unread_channels`,
  `unread_threads`, `latest_hidden_channels`, `blocked_user_ids`. No
  `ignore_if_client_side` on the extra fields here. Sent for own-user contexts only
  (connect, `me`, update self).
- **`FullUserResponse` (Go)** is admin-only (server-side / `update_users`). The Kotlin
  `FullUserResponse.kt` mirrors it but is **not** the right type for the mobile client.

### Kotlin counterparts

| Wire context | Kotlin type | Notes |
|---|---|---|
| Nested user (`mute.user`, `message.user`, `member.user`, `reaction.user`, `channel.created_by`, search results, etc.) | `UserResponse` | Matches the wire 1:1. |
| Own-user (`ConnectedEvent.me`, `MuteUserResponse.own_user`, notification `me` events) | `OwnUserResponse` | Resolved: `generate-kotlin-chat-client.sh` was passing `--classes-to-skip PrivacySettingsResponse,PrivacySettings,...` which stripped every field of those types from generated DTOs. Removed `PrivacySettingsResponse` (and dead `PrivacySettings` entry) from the skip list; regen now emits `privacy_settings` on `OwnUserResponse` / `UserResponsePrivacyFields` / `FullUserResponse` / `UserRequest` / `ConnectUserDetailsRequest`. |
| `FullUserResponse` | unused on mobile | Admin-only. |

### Implications for the plan

The original split (1: User, 2: Mute, ...) still works, just with two Kotlin user types
instead of one. `DownstreamUserDto` is replaced by:
- `UserResponse` everywhere it's nested in another DTO (Mute, Member, Message, Reaction,
  Channel, ChannelUserRead, Event payloads' user field, etc.)
- `OwnUserResponse` in the few places we deserialize own-user (`TokenResponse`,
  `ConnectedEvent`, possibly others). Cross-reference call sites.

Mappers:
- `UserResponse.toDomain()` populates the domain `User` from the common fields and
  leaves own-user-only fields (devices, mutes, channel_mutes, unread counts,
  privacy_settings, push_preferences) at their defaults.
- `OwnUserResponse.toDomain()` populates the full domain `User`, pulling
  `privacy_settings` out of the `custom` overflow via a Moshi sub-adapter until the
  generator regen lands.
- Domain `User` keeps its current shape; both mappers feed into it.

### Generator follow-up

Resolved during step 1: `generate-kotlin-chat-client.sh` had `PrivacySettingsResponse`
and a dead `PrivacySettings` entry in `--classes-to-skip`. The generator's skip logic
strips every field whose type matches a skipped class
(`kotlin.go:147 getLastWord(field.Type, ".") == classToSkip`), which silently removed
`privacy_settings` from `OwnUserResponse`, `UserResponsePrivacyFields`,
`FullUserResponse`, `UserRequest`, and `ConnectUserDetailsRequest`. Removed both
entries; the only entry left is `StopRTMPBroadcastsRequest` (video-only).

The script change is in the chat-manager repo at `generate-kotlin-chat-client.sh`;
needs to be committed there separately from this branch.
