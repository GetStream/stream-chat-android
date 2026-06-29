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

## Order of operations

The cycles resolve incrementally if we walk **upward**, swapping one DTO at a time. The
key insight: when we replace `DownstreamUserDto` with `FullUserResponse`, the **field
type** of every place User is nested in another (still-manual) DTO also flips to
`FullUserResponse`. Moshi binds the JSON object to whatever type the field declares — the
outer manual DTO doesn't care that its `user: FullUserResponse?` is now generated. The
outer mapper just calls `user.toDomain()` against the new type.

So each step touches one DTO + its mapper + the field-type references in the
still-manual outer DTOs. Each step is self-contained and live-testable.

### Inbound (Downstream) sequence — DONE

1. ✅ **`DownstreamUserDto` → `UserResponse` / `OwnUserResponse` / `UserResponsePrivacyFields`.**
2. ✅ **`DownstreamMuteDto` → `UserMuteResponse`**.
3. ✅ **`DownstreamChannelMuteDto` → `ChannelMute`**.
4. ✅ **`DownstreamMemberDto` → `ChannelMemberResponse`**.
5. ✅ **`DownstreamReactionDto` → `ReactionResponse`**.
6. ✅ **`DownstreamChannelDto` → `ChannelResponse`**.
7. ✅ **`DownstreamMessageDto` → `MessageResponse`**.

Adjacent leaf migrations completed alongside the inbound sweep: `DownstreamPollDto` →
`PollResponseData`, `DownstreamVoteDto` → `PollVoteResponseData`, `DownstreamUserGroupDto`
→ `MentionedUserGroupResponse` (slim, members-less; see GENERATOR_ISSUES #15),
`DownstreamReminderDto` → `ReminderResponseData`. `ChannelInfoDto` and
`DownstreamModerationDetailsDto` were deleted entirely — the wire never put `channel`
inside a message and V1 moderation is now reconstructed from `custom["moderation_details"]`.

### Outbound (Upstream) sequence — pending

8. **`UpstreamUserDto` → `UserRequest`** (with `custom` overflow for missing fields). **Test:** updateUser via FAB or any path that re-uploads the user.
9. **`UpstreamMemberDto`** — already mostly migrated as `UpstreamMemberDataDto` → `ChannelMemberRequest`. Sweep remaining references.
10. **`UpstreamReactionDto` → inner `ReactionRequest`** (in `SendReactionRequest`). **Test:** add reaction.
11. **`UpstreamMessageDto` → `MessageRequest`** (typed `Type` enum). **Test:** send / edit message.

### Cleanup — pending

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

## Field-by-field gotchas (still relevant for outbound)

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
