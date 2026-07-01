# OpenAPI Generator Issues

Issues hit while migrating `stream-chat-android-network` to generated models
from `Sviluppo/chat` (regen driven by `generate-kotlin-chat-client.sh`).

Each entry: symptom, root cause, fix status. **Local fix** = patched in the
local chat-repo checkout only (branch `chat-openapi-android`); lost on next
upstream pull. **Upstream** = merged into chat master.

---

## 1. Events generated with two abstract supertypes

**Symptom:** Compile errors `Only one class can appear in a supertype list`
plus `getEventType()` not overridden, on ~10 user/presence event classes.

**Root cause:** Two bugs.
1. Chat spec emits two identical discriminator schemas (`WSClientEvent`,
   `WSEvent`) with the same 75 `oneOf` entries. The split exists for video
   (where the distinction is real) but not for chat. `spec.go:1369-1392`
   only renames/suppresses these under `isVideoOnly()`.
2. `kotlin.go:372-378` names the child override `get<ParentName><Field>` while
   the parent's abstract method is `getEvent<Field>` (`:451-464`). The two
   disagree for non-feeds SDKs.

**Fix status:** Local fix.
- `spec.go`: added `isChatOnly()`, mirrors `isVideoOnly()`; chat-only renames
  `WSEvent` -> `ChatEvent` and suppresses `WSClientEvent`.
- `kotlin.go:372-378`: child override naming aligned with parent
  (`getEvent<Field>` by default).

Output now matches the existing `ChatEventDto` shape (single sealed parent,
`getEventType()`) and is symmetric with video's `VideoEvent`.

---

## 2. Kotlin reserved words not escaped in property names

**Symptom:** `EnrichedActivity.kt: Syntax error: Parameter name expected` on
`val object: ...`.

**Root cause:** The param-name converter in
`lib/combined/openapi/generator/languages/kotlin/kotlin.go` has no list of
Kotlin hard keywords to escape.

**Fix status:** Local fix. `kotlin.go` now has a `kotlinHardKeywords` table
and an `escapeIfKeyword` helper applied by `ParamName`. Only backticks names
that are dex-safe identifiers (`[A-Za-z_$][A-Za-z0-9_$]*`).

**Important Android constraint:** D8/R8 rejects identifiers with characters
outside `[A-Za-z0-9_$]`, even backticked. Rule for the converter:
1. Hard keyword + dex-safe -> backtick, keep wire name via `@Json(name = ...)`.
2. Non-dex-safe chars -> sanitize to a valid identifier; rely on `@Json(name)`.
3. Never emit a backticked identifier with non-dex-safe chars.

---

## 3. `Time` wrapper from feeds SDK leaks into chat spec, breaks codegen

**Symptom:** `EnrichedReaction.kt: Unresolved reference 'Time'`. No `Time.kt`
emitted.

**Root cause:** `feedsSDK.Time` embeds `time.Time` (`type Time struct {
time.Time }`). `spec.go:825,849,905` only special-cases
`reflect.TypeOf(time.Time{})` exactly, so wrappers that embed it become empty
`object` schemas. `kotlin.go:425-428` then silently skips empty-class emission
but doesn't rewrite `$ref` users.

`EnrichedActivity`/`EnrichedReaction` reach chat through
`ReviewQueueItem.feeds_v2_activity` (chat spec built with
`-products chat,common,moderation`).

**Fix status:** Indirectly fixed by issue #5's `moderation` product drop -
`EnrichedActivity`, `EnrichedReaction`, `Time` no longer in chat output.

**Suggested upstream fix:** Generalize time-recognition in `spec.go` to also
match structs that anonymously embed `time.Time`.

---

## 4. Empty-string enum value emits invalid `''` literal

**Symptom:** `MessageRequest.kt: Empty character literal` on
`object '' : Type("''")`.

**Root cause:** The spec lists `""` as an allowed value for
`MessageRequest.type`. The Kotlin enum template emits `''` for both the
companion `when` arm and the `object` declaration.

**Fix status:** Local fix. `kotlin.go` (~line 336) now uses a
`buildEnumClassName` helper that runs `templates.PascalCase` (strips
non-alphanumeric) and falls back to `Empty`.

Collision case (multiple values sanitizing to the same identifier) is not
handled - silently overwrites, matching other language generators.

---

## 5. Cross-product types and admin endpoints leak into chat SDK

**Symptom:** Chat models include feeds/moderation types (`EnrichedActivity`,
`EnrichedReaction`, `ReviewQueueItem`, `Time`) and 21 standalone
`/api/v2/moderation/*` admin routes that the chat SDK should never expose.

**Root cause:** Chat spec built with `-products chat,common,moderation`.
Moderation responses reference cross-product shapes (e.g.
`ReviewQueueItem.feeds_v2_activity: EnrichedActivity`); when the spec walker
resolves every reachable `$ref`, those shapes get pulled in.

**Fix status:** Local fix. `generate-kotlin-chat-client.sh` now builds with
`-products chat,common` only (chat-scoped moderation routes
`/api/v2/chat/moderation/*` are tagged `chat` and survive). Model count
472 -> 324.

**Suggested upstream fix:** Either (a) split moderation responses per-product
context in the spec, (b) add `omit_for_chat` openapi tag (analogous to
`omit_for_video` at `spec.go:611-613`), or (c) adopt the TypeScript generator's
per-product API split.

**Audit needed before shipping:** sweep the generated models for cross-product
shapes that snuck through via `chat`/`common`. Decide per class: push upstream
fix, mark `internal`, or keep public.

---

## 6. Datetime fields generated as `OffsetDateTime` instead of `Date`

**Symptom:** Every generated DTO with a datetime field would use
`org.threeten.bp.OffsetDateTime`. The chat domain layer and existing mappers
all use `java.util.Date`, forcing per-field conversion
(`Date(dto.createdAt.toInstant().toEpochMilli())`) in every mapper.

**Root cause:** `kotlin.go:640-651` hardcodes `OffsetDateTime` for all
android SDKs except feeds.

**Fix status:** Local fix. Inverted the default at `kotlin.go:644-654`: video
keeps `OffsetDateTime`, everyone else gets `Date`. Network module no longer
depends on threetenbp.

**Suggested upstream fix:** Same as local fix. Long-term: a `--datetime-type`
flag on `chat-manager openapi generate-client`.

---

## 7. Generated model names collide with chat domain model names

**Symptom:** 12+ generated wire models share simple names with chat domain
models (`Command`, `Message`, `User`, `Channel`, `Member`, `Reaction`,
`Attachment`, `Device`, `Mute`, `Thread`, `Poll`, `Location`). Mappers need
per-import `as XxxDto` aliases.

**Root cause:** Kotlin generator emits a class per OpenAPI schema name
verbatim. No opt-in suffix mechanism.

**Fix status:** Worked around with per-file `as XxxDto` aliases.

**Suggested upstream fix:** Add a `classNameSuffix` config (defaults to `""`,
set to `"Dto"` for chat) applied at the single point in `kotlin.go` where the
class identifier is computed from the schema name. References, discriminator
interfaces, and enum nested adapters flow from that one source.

A project-side typealias file was considered and rejected: collision is
permanent, so a generator-side convention is cleaner than ~50 typealiases we
maintain forever.

---

## 8. Generated DTOs incompatible with `CustomObjectDtoAdapter`

**Symptom:** Can't typealias root DTOs (`AttachmentDto`, `MessageDto`,
`ChannelDto`, `UserDto`, `MemberDto`, `ReactionDto`, `PollDto`, `PollOptionDto`,
`ThreadDto`, `ThreadInfoDto`, `UpstreamMemberDataDto`) to generated counterparts.
The adapter routes unknown root keys into `extraData`; substituting the
generated DTO breaks both directions silently.

**Root cause:** `CustomObjectDtoAdapter.memberNames` uses
`kClass.members.map { it.name }`. Two assumptions hardcoded:
1. Property name == JSON wire name. Manual DTOs are snake_case; generated DTOs
   are camelCase + `@Json(name = "snake_case")` - membership check fails and
   every declared field gets bucketed as extra data.
2. The DTO has an `extraData: Map<String, Any>` property. Generated DTOs don't
   - the spec doesn't model `additionalProperties` here.

**Fix status:** Local fix. `CustomObjectDtoAdapter.memberNames` now reads
`@Json(name = ...)` via reflection, falling back to property name. The
`extraData` field is supplied by aliasing the adapter's
`extraDataPropertyName` to `"custom"` (the wire name the generator already
emits), so the wrapper merges root keys into the generated `custom` map.

**Suggested upstream fix:** Generator emits an `extraData: Map<String, Any>
= emptyMap()` field when a schema is marked open (e.g. via
`additionalProperties: {}` or a chat-specific extension tag). Combine with
issue #9.

---

## 9. Spec emits `custom: object` for `jsonextra.ExtraFields` but wire flattens

**Symptom:** Generated DTOs corresponding to backend structs with
`Custom jsonextra.ExtraFields` get a typed `@Json(name = "custom") val custom:
Map<String, Any?>`. The wire doesn't contain a literal `"custom"` key -
`jsonextra.ExtraFields` flattens to root on encode and sweeps unknown root
keys on decode. Stock Moshi: `custom` always parses empty; real keys at root
get dropped.

**Root cause:** `spec.go`'s struct walker reflects through fields generically.
`jsonextra.ExtraFields` is `map[string]interface{}` under the hood and the
`json:"custom"` tag is read literally. Affects every chat schema with an
`ExtraFields` field (User, Channel, Message, Attachment, Member, Reaction,
Thread, Poll, Flag, ...).

**Fix status:** Worked around client-side via issue #8's adapter fix.

**Suggested upstream fix:**
1. Spec generator recognizes `jsonextra.ExtraFields`: emit
   `additionalProperties: { type: object }` on the parent schema instead of a
   named property. Each language generator then uses its overflow-field
   convention. Correct.
2. OpenAPI extension marker (`x-stream-extra-fields: true`) per property.
   Smaller spec-shape impact. Stopgap.

---

## 10. `float64` -> OpenAPI `format: float` (downgrades precision)

**Symptom:** `SharedLocation.latitude`/`longitude` come out as `kotlin.Float`,
round-tripping a `Double(37.7749, -122.4194)` loses precision
(`37.774898529052734`).

**Root cause:** `spec.go:836` `SchemaFormatFromReflectType` maps both
`float32` and `float64` to `"float"`. Backend uses `float64`
(`shared_location.go:15`), spec advertises 32-bit.

**Fix status:** Worked around in the SDK by widening Float to Double at the
mapper boundary; domain uses Double, wire DTO uses Float.

**Suggested upstream fix:** Split the case in `spec.go`:
```go
case "float32", "complex64":   return "float"
case "float64", "complex128":  return "double"
```
And default `type: number` (no explicit format) to `kotlin.Double` rather
than `kotlin.Float` in the Kotlin generator.

**Caveat:** Source/binary breaking change for every SDK; coordinate
multi-SDK release.

---

## 11. Spec emits `Custom` (capital C) `@Json` name for poll request DTOs

**Symptom:** Generated `CreatePollRequest`, `UpdatePollRequest`,
`CreatePollOptionRequest`, `UpdatePollOptionRequest` declare
`@Json(name = "Custom") val custom: Map<...>`.

**Root cause:** Backend poll-request structs declare `Custom
jsonextra.ExtraFields` without an explicit `json:"custom"` tag. Spec generator
falls back to Go field name verbatim.

**Why cosmetic, not a wire bug:** The wire never contains `"Custom"` /
`"custom"` literally - `jsonextra.ExtraFields` flattens at root on encode
(see #9). `CustomObjectDtoAdapter` does the same on the client. The annotation
name only matters in the intermediate Map between Moshi and the adapter. As
long as the adapter is registered with `extraDataPropertyName = "Custom"`
matching the annotation, the typo is invisible on the wire.

**Fix status:** Not fixed. Cosmetic.

**Suggested upstream fix:** Add `json:"custom"` tags to the affected Go fields,
or recognize the convention in the spec generator's struct walker.

---

## 12. Distinct Go types with same simple name collapse into one OpenAPI schema

**Symptom:** Generated `ChannelMemberResponse.kt` has 2 fields; the real wire
shape for channel-member responses (QueryMembers, UpdateChannelPartial, etc.)
has ~20 fields. Typealiasing would silently drop user/dates/banned status.

**Root cause:** Two distinct Go structs are both named `ChannelMemberResponse`:
1. `lib/chat/controller/v1/payload/channel_response.go:389` - full type,
   used by real channel-member responses.
2. `lib/combined/controllers/common/commonpayloads/member.go:8` - lean type
   (`channel_role` + `notifications_muted`), used as `Message.member`.

`spec.go:569` `clarifyName` dedupes by string name, not `reflect.Type`. The
first registered type wins the bare name; the second silently aliases to it.

**Fix status:** Upstream fix CHA-3559 renamed the lean type to claim the bare
name back for the full type. (See #16 for the consequence.)

**Suggested upstream fix:** Patch `clarifyName` to key off `reflect.Type` (or
a `(package, name)` tuple). Eliminates the entire class of bug - other latent
collisions likely exist. Coordinate with other SDK teams.

---

## 13. `openapi:"-"` strips real wire fields from the spec

**Symptom:** Generated `ChannelConfigWithInfo.kt` missing `message_retention`,
even though the wire always carries it. Moshi drops the unknown key; domain
default `"infinite"` hides non-default backend values.

**Root cause:** `types/channel_config.go:66` tags the field
`json:"message_retention" ... openapi:"-"`. The `openapi:"-"` instructs the
spec generator to omit; the json tag still serializes it.

**Fix status:** Worked around in the SDK by defaulting
`Config.messageRetention = "infinite"` in `ChannelConfigWithInfo.toDomain()`.

**Suggested upstream fix:** Audit Go fields tagged `openapi:"-"` with a real
`json:` tag. Remove `openapi:"-"` so the spec describes the wire honestly.

---

## 14. `PollResponseData` collection fields lie about being required

**Symptom:** Casting a vote crashes with `JsonDataException: Non-null value
'voteCountsByOption' was null at $.poll`. Same for `latest_votes_by_option`,
`latest_answers`.

**Root cause:** Same class as #18. Go (`polls/poll.go:26-28`) types the
fields as `map[string]int` / `[]*PollVoteResponseData` with `json:"..."` and
no `omitempty`. `encoding/json` marshals nil maps/slices as JSON null.
`spec.go:90-95` `FieldRequiredResponse` marks any non-pointer field without
`omitempty` as required. Wire emits null for empty/new polls.

**Fix status:** Local fix. Commit
`[android-migration-only] Add omitempty to PollResponseData empty-able
collections` - adds `,omitempty` to the three Go tags; regen makes them
nullable; mapper uses `.orEmpty()`.

**Suggested upstream fix:** Land the same `omitempty` upstream, or apply the
generator-level fix described in #18.

---

## 15. `UserGroupResponse` schema collapses, drops `members` field

**Symptom:** Generated `UserGroupResponse.kt` has 7 fields; direct
user-group endpoints (`POST /user_groups`, `GET /user_groups/{id}`) carry an
additional `members` array.

**Root cause:** Same as #12. Two Go structs named `UserGroupResponse`:
1. `commonpayloads/user_group.go:13` - slim hydration payload used inside
   `message.mentioned_groups[]`.
2. `core/api/usergroups/controller/user_group_types.go:245` - full type that
   embeds the slim payload + `Members []*UserGroupMember`.

The slim one wins the bare name; the full one silently aliases. CHA-3559's
patch only fixed the `ChannelMemberResponse` instance.

**Fix status:** Local fix. Commit `[android-migration-only] Rename slim
UserGroupResponse to MentionedUserGroupResponse`. Renames the slim payload
+ constructors so the full controller type reclaims the bare schema name.

**Suggested upstream fix:** Land the same rename, or the long-term
`clarifyName` fix from #12.

---

## 16. CHA-3365 papered over a wire/spec mismatch for `Message.member`

**Symptom:** Decoding any response embedding a message with a `member` field
crashes with `JsonDataException: Required value 'banned' missing at
$.reminder.message.member`. The wire's `member` carries only `channel_role`
+ `notifications_muted`; generated DTO expects ~20 required fields.

**Root cause:** CHA-3365 (cherry-picked to restore the full
`ChannelMemberResponse`) made both Go types declare the same
`NameOverride: "ChannelMemberResponse"`. Picks the richer shape for the spec,
but silently lies about `Message.member` and equivalent moderation payloads
- both still use the slim Go type with 2 fields on the wire.

**Fix status:** Local fix. Commit `[android-migration-only] Rename slim
ChannelMemberResponse to MessageMemberResponse`. Renamed the slim type,
dropped `NameOverride`, updated the two callers (`message.go`,
`chat_v1_response.go`). Spec now emits two schemas;
`MessageResponse.member` references the slim one.

**Suggested upstream fix:** Land the same rename + drop the
`NameOverride: "ChannelMemberResponse"` on
`payload.ChannelMemberResponse.OpenAPIInfo()`. The long-term `clarifyName`
fix (#12) is the real answer.

---

## 17. Write-path send responses send `reaction_counts`/`reaction_scores` as JSON null

**Symptom:** `POST /messages` crashes parsing the response when the server-side
path builds the response from a freshly-constructed `types.Message`.
`JsonDataException: Non-null value 'reactionCounts' was null`. Wire emits
`"reaction_counts": null` / `"reaction_scores": null` and `"updated_at":
"0001-01-01T00:00:00Z"` (Go zero time, the giveaway). Read-path responses emit
`{}` and parse cleanly.

Two known triggers observed live in compose-sample:
- **Bounce path** (V1/V2 automod): blocklisted text hits `bounce` action,
  response uses the same write-path builder.
- **Pending message path**: channel type with `mark_messages_pending: true`,
  send a message. Response built at `send_message.go:562`.

Same root cause for both.

**Root cause:** `send_message.go` builds the response via
`payload.NewMessageResponse(*message)` (`defer` at line 305 for the normal
write path, line 562 for the pending branch) but never calls
`PrepareSerialization(user)` (`message.go:240`), which normalizes nil int-maps
to `{}`. Read paths (`get_pinned_messages`, `get_replies`, channel-state) do
call it; write paths don't.

This is wire-vs-spec drift, not a generator bug.

**Fix status:** Not fixed. Bounced-message parsing crashes until backend is
fixed. No SDK patch (would paper over a real backend inconsistency that
affects every SDK).

**Suggested upstream fix:** Call `response.Message.PrepareSerialization(user)`
after constructing the response in `send_message.go:305` and `:562`. Audit
other write controllers (`update_message`, `run_message_action`,
`translate_message`) that build a `MessageResponse` outside channel-state
serialization. Or move nil-map normalization into `NewMessageResponse` itself.

---

## 18. Non-pointer Map/Slice without `omitempty` are wire-nullable but spec-required (~91 models)

**Symptom:** Decoding `POST /threads` (and any other response nesting a
`ThreadParticipant`) crashes with `JsonDataException: Non-null value 'custom'
was null at $.thread_participants[0].custom`. Same class as #14 and #17.

**Scope.** 91 generated DTOs declare `val custom: Map<String, Any?> =
emptyMap()` (non-nullable, required). Examples: `ChannelMemberResponse`,
`OwnUserResponse`, `Attachment`, `ThreadParticipant`, `ThreadResponse`,
`DraftPayloadResponse`, `PollOptionResponseData`, ~60 event payloads.

Most don't crash today because:
- The parent DTO is wrapped by `CustomObjectDtoAdapter` with
  `extraDataPropertyName = "custom"` (User, Reaction, Channel, Message,
  Member, Attachment, Poll). The wrapper substitutes its own collected map
  before Moshi delegates.
- The model isn't typealiased yet (events, draft internals).
- The wire happens to always populate the field.

`ThreadParticipant` is the first crash because it's nested **without** a
wrapper AND the server sends `null`. Likely next: `PollOptionResponseData`,
`ThreadResponse`, event payloads.

**Root cause:** Same as #14. `spec.go:90-95` `FieldRequiredResponse` treats
only `reflect.Ptr` as wire-nullable:
```go
if strings.Contains(field.Tag.Get("json"), "omitempty") { return false }
return field.Type.Kind() != reflect.Ptr
```
Go maps and slices are reference types too - a nil map/slice marshals to
JSON `null` without `omitempty`, exactly like a nil pointer. Mismarks every
non-`omitempty` Map and Slice field as required.

**Fix status:** Targeted per-model workaround. Commit
`temp: Add omitempty to ThreadParticipant.Custom` adds `,omitempty` to the Go
tag; the spec drops it from `required:`; the regenerated DTO is
`Map<...>? = emptyMap()`. Same pattern as #14. Does not scale - each new
crash would need its own `temp:` commit.

**Suggested upstream fix:** Patch `FieldRequiredResponse` to treat
`reflect.Map` and `reflect.Slice` like `reflect.Ptr`:
```go
switch field.Type.Kind() {
case reflect.Ptr, reflect.Map, reflect.Slice:
    return false
}
```
Catches #14, #17, and this issue in one change. Mapper-side cost: `.orEmpty()`
at call sites. Coordinate with iOS / chat-js - same spec, same field-required
logic.

---

## 19. Flag endpoint response (`payload.FlagResponse`) not in chat-only spec output

**Symptom:** No generated Kotlin model maps the response of
`POST /moderation/flag` / `POST /moderation/unflag`. Closest generated type
is `MessageFlagResponse`, but that's the shape returned by `query_message_flags`
and lacks `target_user`, `target_message_id`. Blocks typealiasing
`DownstreamFlagDto`.

**Root cause:** `/api/v2/moderation/flag` and `/api/v2/moderation/unflag` are
admin moderation routes, not chat-scoped (`/api/v2/chat/moderation/*`). Issue
#5's local fix drops the `moderation` product from the chat-only spec build
(`generate-kotlin-chat-client.sh` uses `-products chat,common`), so these
routes and their response type `payload.FlagResponse` aren't reachable from
any kept operation - the generator never emits a Kotlin schema for them.

The SDK still calls these endpoints (`ModerationApi.flag` / `unflag` in the
Android client), so the hand-written `DownstreamFlagDto` stays in place. The
hand-written DTO has its own bugs (`created_at: String` instead of `Date`,
`reviewed_by: Date?` instead of `String`) that Moshi tolerates at runtime.

**Fix status:** Not fixed. `DownstreamFlagDto` stays hand-written; migration
skipped for now.

**Suggested upstream fix:** Either:

1. Add a chat-scoped flag endpoint on the backend (`/api/v2/chat/moderation/flag`
   tagged with `chat` product). The product filter would then surface its
   response type. Cleanest end state - aligns flag with the other chat-scoped
   moderation routes (`flags/message`, `mute/channel`, etc.).
2. Bring moderation back into the spec build with model-level filtering
   (issue #5's option 2 or 4). Larger lift; touches every other admin route too.
3. Add a chat-only tag on `payload.FlagResponse` itself so the spec walker
   surfaces it even when the moderation product is filtered. Targeted but
   ad-hoc.

**Migration timing:** Not blocking - the hand-written DTO works. Revisit if
the spec / product-filter architecture is reworked, or if the SDK's flag
endpoints are repointed at chat-scoped paths.

---

## 20. `UserResponseCommonFields.Language` non-pointer / no `omitempty`

**Symptom:** Receiving any WS event whose payload nests a user without a
`language` field (`member_added`, `member_removed`, and likely the
`notification.added_to_channel` / `notification.invited` family) crashes with
`JsonDataException: Required value 'language' missing at $ at $.user at $.member`.

**Root cause:** `commonpayloads/user.go:161` types `Language` as
`string` with `json:"language"` (no `omitempty`). Per `spec.go`'s
`FieldRequiredResponse`, a non-pointer string without `omitempty` becomes
spec-required, so generated `UserResponse.language`,
`UserResponseCommonFields.language`, and `UserResponsePrivacyFields.language`
are all non-null. The Go marshaler omits `language` on nested member.user
payloads on the wire, so codegen rejects the JSON.

This is a regression introduced on `openapi-generated` by commit
`0b1556c955 Migrate DownstreamUserDto to generated UserResponse / OwnUserResponse / UserResponsePrivacyFields`.
On `develop` the hand-written `DownstreamUserDto.language` was nullable, so
member events parsed fine. The typealias to generated `UserResponse` made the
field stricter than the wire.

**Fix status:** Local fix. Commit
`temp: Add omitempty to UserResponseCommonFields.Language` (chat repo
`chat-openapi-android` branch) adds `,omitempty` to the Go tag. Regenerated
`UserResponse`, `UserResponseCommonFields`, and `UserResponsePrivacyFields`
copied into `stream-chat-android-network`; downstream `.toDomain()` mappers
default missing values via `.orEmpty()`.

**Suggested upstream fix:** Either land the same `omitempty` upstream, or
type `Language` as `*string` to match the wire reality.

---

## 21. `HasChannel.Channel` is spec-required but wire strips it on some events

**Symptom:** Receiving certain WS events (observed live on `member.removed`
via the compose-sample "remove member" flow) crashes with
`JsonDataException: Required value 'channel' missing at $`. The wire omits
`channel` even though the spec declares it required.

**Root cause:** `lib/combined/events/event.go:102` declares
`Channel payload.ChannelResponse \`json:"channel"\`` on `HasChannel`. Per
`spec.go`'s `FieldRequiredResponse`, the non-pointer field without `omitempty`
becomes spec-required. `HasChannel` is embedded (via `ChannelSupportEvent`)
across ~22 event types — every `Reaction*`, `Member*`, `Channel*` variant,
several `Notification*` variants, and `MessageWithChannelResponse`.

The reaction events declare `CanHaveRestrictedVisibility() bool { return true }`
and `member.removed` behaves similarly: the recipient's copy of the event has
channel-scoped fields stripped when they no longer have visibility on the
channel. So the wire really does omit `channel` for a subset of these events,
per-recipient. `HasChannel` accurately describes the constructor argument,
not what lands on the wire.

Regression introduced on `openapi-generated` by migrating the affected event
DTOs (`ReactionNewEventDto`, `MemberAddedEventDto`, etc.) to the generated
types. Legacy hand-written DTOs never had a `channel` field, so parsing
tolerated its absence.

**Fix status:** Local fix. Commit `temp: Add omitempty to HasChannel.Channel`
(chat repo `chat-openapi-android` branch) adds `,omitempty` on the Go tag.
`,omitempty` on a non-pointer struct doesn't actually change what Go emits
on the wire; the value here is that `spec.go`'s `FieldRequiredResponse` sees
the tag and marks the field optional, so all 22 generated Kotlin event types
get `channel: ChannelResponse? = null`. Currently only the 6 migrated events
(`Member*`, `Reaction*`) are copied into `stream-chat-android-network`; the
other 16 will pick up the same nullability when they get migrated.

**Note:** This patch is semantically off — `HasChannel` still says "has
channel" but we're telling the spec it's optional. Needs a follow-up with
backend to decide the right shape (e.g. dedicated mixin for events whose
channel gets stripped by visibility filtering, or teach the filter to leave
`channel` in place).

**Suggested upstream fix:** Redesign so restricted-visibility events use a
proper optional-channel mixin instead of leaning on `omitempty` as a
spec-only hint.

