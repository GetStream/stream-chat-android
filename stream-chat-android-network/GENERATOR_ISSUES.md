# OpenAPI Generator Issues

Running log of problems hit while migrating `stream-chat-android-network` to generated models from
`Sviluppo/chat` (the `chat-manager openapi generate-client` flow driven by
`generate-kotlin-chat-client.sh`).

Each entry should record: symptom, where it came from, root cause, and fix status. "Local fix"
means edited in a local checkout of the generator repo only - changes are not upstream and will be
lost on the next `chat-manager` pull. "Upstream" means merged into the generator repo.

---

## 1. Events generated with two abstract-class supertypes

**Symptom:** Compile errors `Only one class can appear in a supertype list` plus
`'getWSClientEventType' / 'getWSEventType' overrides nothing` plus
`Class X is not abstract and does not implement getEventType()` on ~10 event types
(`UserBannedEvent`, `UserUnbannedEvent`, `UserUpdatedEvent`, `UserWatchingStartEvent`,
`UserWatchingStopEvent`, `UserReactivatedEvent`, `UserPresenceChangedEvent`, ...).

**Root cause:** Two related bugs.

1. The chat OpenAPI spec emits **two identical discriminator schemas** (`WSClientEvent` and
   `WSEvent`) with the same 75 `oneOf` entries. There is no semantic difference - every chat WS
   event is client-deliverable. The split was copied from the video spec where the distinction is
   real. Source: `lib/combined/openapi/spec/spec.go:1369-1392` only renames/suppresses these for
   `isVideoOnly()`, leaving chat to emit both. The Kotlin generator then appends both as parent
   classes to every event, producing illegal `: WSClientEvent(), WSEvent()`.
2. The Kotlin generator names the child's override method as `get<ParentName><FieldName>`
   (`getWSClientEventType`) but names the parent's abstract method as `getEvent<FieldName>`
   (`getEventType`) for non-feeds SDKs. The two sides disagree. Source:
   `lib/combined/openapi/generator/languages/kotlin/kotlin.go:372-378` vs `:451-464`.

**Fix status:** Local fix only. Both touches live in `/Users/gianmarcodavid/Sviluppo/chat`:

- `spec.go:615` - added `isChatOnly()` mirroring `isVideoOnly()` (chat without video/feeds).
- `spec.go:1369-1392` - chat-only specs rename `WSEvent` to `ChatEvent` and suppress
  `WSClientEvent`, matching how video-only is renamed to `VideoEvent`.
- `kotlin.go:372-378` - child override-method name now matches the parent's naming logic
  (`getEvent<Field>` by default, `get<ParentName><Field>` only for feeds).

**Suggested upstream fix:** Same as above. The chat output now matches the existing manual
`ChatEventDto` shape (single sealed parent, `getEventType()`) and is symmetric with video's
`VideoEvent`. No `WSClientEvent` / `WSEvent` artifact leaks into the public API.

---

## 2. Kotlin reserved word `object` not escaped in property names

**Symptom:** `EnrichedActivity.kt:64 Syntax error: Parameter name expected`.

```kotlin
@Json(name = "object")
val object: io.getstream.chat.android.network.models.Data? = null,
```

`object` is a hard keyword in Kotlin and must be backtick-escaped or renamed.

**Root cause:** The Kotlin param-name converter (Kotlin language's `ParamName` /
`getKotlinMethod` helpers in
`lib/combined/openapi/generator/languages/kotlin/kotlin.go`) lowercases / camel-cases the JSON
field name but has no list of Kotlin reserved words to escape.

**Fix status:** Locally fixed in the generator (not upstream). `kotlin.go` now has a
`kotlinHardKeywords` table and an `escapeIfKeyword` helper applied by `ParamName`. The helper
backticks the name only when it's already a dex-safe identifier (`[A-Za-z_$][A-Za-z0-9_$]*`),
preserving Android compatibility. Future regens auto-emit `` val `object`: ... `` without
manual patching.

**Suggested fix:** In the param-name converter, wrap identifiers that collide with Kotlin hard
keywords in backticks (`` `object` ``). The full hard-keyword list is documented at
[kotlinlang.org/docs/keyword-reference.html](https://kotlinlang.org/docs/keyword-reference.html).

**Important Android constraint:** Backticking only fixes JVM Kotlin compilation. **Android's
D8/R8 dex compiler rejects identifiers containing characters outside the JVM-identifier set**
(letters, digits, `_`, `$`). So `` `object` `` is dex-safe, but `` `name with spaces` ``,
`` `name-with-dashes` ``, or `` `name<with>angles` `` would compile to .class but fail to dex.

So the rule for the param-name converter must be: **if backticking the raw name would produce a
dex-safe identifier, backtick it; otherwise sanitize**. Concretely:

1. If the converted name is a Kotlin hard keyword and is otherwise a valid dex identifier
   (matches `[A-Za-z_$][A-Za-z0-9_$]*`), wrap in backticks. Use `@Json(name = ...)` to preserve
   the original wire name.
2. If the converted name contains characters outside `[A-Za-z0-9_$]`, sanitize to a valid
   identifier (e.g., replace non-alphanumerics with `_`, prepend `_` if leading digit) and rely
   on `@Json(name = ...)` for the wire mapping.
3. Never emit a backticked identifier containing characters outside the dex-safe set.

---

## 3. Unresolved `Time` reference (feedsSDK time-wrapper leaks into chat spec)

**Symptom:** `EnrichedReaction.kt:64 / :76 Unresolved reference 'Time'`.

```kotlin
@Json(name = "created_at")
val createdAt: io.getstream.chat.android.network.models.Time? = null,
```

No `Time.kt` is generated in the output.

**Root cause:** `EnrichedActivity` and `EnrichedReaction` come from
`github.com/GetStream/stream-go2/v8` (external feeds SDK) and reach the chat spec through
`ReviewQueueItem.feeds_v2_activity` in the moderation module (chat client-side spec is built with
`-products chat,common,moderation`). `feedsSDK.Time` is:

```go
type Time struct { time.Time }   // embeds Go's time.Time
```

The chat-manager spec generator only special-cases `reflect.TypeOf(time.Time{})` exactly
(`spec.go:825,849,905`), so wrappers that embed `time.Time` aren't recognized as date-time. They
get reflected as empty `type: object` schemas. The Kotlin generator then silently skips emitting
empty-class files (`kotlin.go:425-428`) but does not rewrite downstream `$ref` users, producing a
dangling type reference.

**Fix status:** Not fixed.

**Suggested fix:** In `spec.go`, generalize the time-recognition path to also match any struct
that anonymously embeds `time.Time`. Three touches:

- `SchemaTypeFromReflectType` (line 810): add `if embedsTimeTime(typ) { return timeFormat }`.
- `SchemaFormatFromReflectType` (line 846): same check returning `"date-time"`.
- `schemaFromType` (line 905): short-circuit for embedded-time structs before
  `schemaFromStruct` is called.

Helper:

```go
func embedsTimeTime(typ reflect.Type) bool {
    if typ.Kind() != reflect.Struct { return false }
    tt := reflect.TypeOf(time.Time{})
    for i := 0; i < typ.NumField(); i++ {
        f := typ.Field(i)
        if f.Anonymous && f.Type == tt { return true }
    }
    return false
}
```

Alternatively, drop `feeds_v2_activity` from the chat client-side spec entirely if the chat SDK
shouldn't surface feeds activities - this is a product decision.

---

## 4. Empty-string enum value generated as invalid `''` literal

**Symptom:** `MessageRequest.kt:115 Empty character literal`, `:121 Syntax error: Name expected`.

```kotlin
sealed class Type(val value: String) {
    companion object {
        fun fromString(s: String): Type = when (s) {
            "''" -> ''                          // invalid: empty char literal
            "regular" -> Regular
            ...
        }
    }
    object '' : Type("''")                      // invalid: bare '' is not a valid identifier
    object Regular : Type("regular")
    ...
}
```

**Root cause:** The OpenAPI spec lists an empty string `""` as one of the allowed enum values for
`MessageRequest.type`. The Kotlin generator's enum template emits the value as `''` for both the
companion-object match arm and the `object` declaration. `''` is interpreted as an empty `Char`
literal at the value site and as an invalid identifier at the declaration site.

**Fix status:** Locally fixed in the generator (not upstream). `kotlin.go` now has a small
`buildEnumClassName` helper used at the enum emission site (line ~336). It calls
`templates.PascalCase`, which already strips non-alphanumeric chars, and falls back to `Empty`
when the resulting identifier would be empty. Mirrors Swift's `ConstantName` shape.

The collision case (multiple values sanitizing to the same identifier, e.g. both `""` and `''`
in the same enum) is not handled - the Go map silently overwrites, matching what every other
language generator does. If a real spec hits this, we'll add disambiguation then.

**Suggested fix:** Two angles:

- Spec-side: drop the empty-string value from the enum if it isn't a real allowed wire value.
- Generator-side: implemented as above.

---

## 5. Cross-product types and admin-only endpoints leak into the chat SDK

**Symptom:** The chat client-side spec includes models that don't belong to chat - notably
feeds types (`EnrichedActivity`, `EnrichedReaction`, `Time`), and very likely other moderation
cross-product shapes (video, call-related types if moderation surfaces them). These end up as
generated Kotlin classes in `stream-chat-android-network`, bloating the public API surface and
adding binary size / method count overhead for chat consumers who will never use them.

Sample: `EnrichedReaction`, `EnrichedActivity`, `Time` (issue #3 above is a direct consequence).
Listing the rest requires a sweep of the generated `models/` directory against the chat product's
actual model set.

**Endpoint side of the same problem:** `ChatApi` includes 21 standalone
`/api/v2/moderation/...` endpoints (`review_queue`, `appeals`, `action_config`, `configs`,
`submit_action`, `ban`, `flag`, `mute`, `appeal/{id}`, `appeals/bulk_action`, ...) - admin /
moderator-console operations that a chat client app should never call directly. Chat-scoped
moderation that DOES belong (`/api/v2/chat/moderation/flags/message`,
`/api/v2/chat/moderation/mute/channel`, etc. - 5 endpoints) is correctly under the chat path
prefix. The 21 standalone moderation routes are the entry point through which the cross-product
types reach the chat models graph: `review_queue` returns `ReviewQueueItem` which carries
`feeds_v2_activity: EnrichedActivity`, etc.

Quick numbers from the current `ChatApi`:

| Path prefix | Count | Belongs in chat SDK? |
|---|---|---|
| `/api/v2/chat/...` | 83 | yes |
| `/api/v2/chat/moderation/...` | 5 | yes (chat-scoped moderation) |
| `/api/v2/moderation/...` | 21 | no (admin/moderator console) |
| other (`app`, `blocklists`, `devices`, `users`, ...) | 46 | case-by-case |
| **total** | 155 | |

**Root cause:** The chat client-side spec is built with three products
(`-products chat,common,moderation`, per `generate-kotlin-chat-client.sh`). Moderation responses
reference cross-product shapes - e.g. `ReviewQueueItem.feeds_v2_activity: EnrichedActivity` -
because a single moderation review-queue endpoint can return items from any product. When the
spec walker resolves every `$ref` reachable from a chat client-side endpoint, those cross-product
shapes are pulled in.

This is a product/spec concern more than a generator bug, but it surfaces as generator output and
becomes an SDK API problem the moment we ship.

**Fix status:** Fixed by dropping the `moderation` product from the chat spec build.
`generate-kotlin-chat-client.sh` now invokes `chat-manager openapi generate-spec -products
chat,common` directly instead of using the bundled spec (which is built with
`chat,common,moderation`). Chat-scoped moderation routes (`/api/v2/chat/moderation/*`) are
tagged with the `chat` product and survive; the 21 standalone `/api/v2/moderation/*` admin
routes and the cross-product types (`EnrichedActivity`, `EnrichedReaction`, `ReviewQueueItem`,
`Time`) are gone. Module shrank from 472 to 324 generated model files.

**Suggested fix:** Several angles, listed by ambition:

1. **Trim moderation responses for chat clients.** The `feeds_v2_activity` (and any other
   non-chat cross-product fields) shouldn't appear on chat-context moderation responses. Either
   (a) split moderation responses per product context in the spec, or (b) mark fields with an
   `omit_for_chat` openapi tag analogous to the existing `omit_for_video`
   (`lib/combined/openapi/spec/spec.go:611-613`).
2. **Filter at the generator.** A `--exclude-models` (or include-only-reachable-from-this-set)
   flag on `chat-manager openapi generate-client` would let the Kotlin generator drop models that
   shouldn't be in this product's SDK. Less precise (some legit shared shapes might be cut), but
   easy.
3. **Curate the chat product surface.** Build the chat client-side spec from chat-only routes
   plus a curated set of moderation routes that don't reference cross-product types. Most
   correct, biggest blast radius.
4. **Per-product API split, mirroring the TypeScript generator.** The TypeScript language plugin
   already does this. In `lib/combined/openapi/generator/languages/typescript/typescript.go` it
   iterates over the product set, filters operations via `spec.FilterByProduct(product)`, and
   emits a separate `{Product}Api.ts` file per product - `ChatApi.ts` contains only chat-tagged
   routes, `ModerationApi.ts` contains only the admin moderation routes, etc. The `Moderation`
   API is its own file, so consumers (e.g. the chat SDK) can choose not to expose it. There's
   also a `productToResourceClass` map (`chat` -> `channel`, `video` -> `call`, `feeds` -> `feed`)
   that emits a focused sub-API file (e.g. `ChannelApi.ts`) for routes under
   `/api/v2/{product}/{resource}/{type}/{id}`.

   The Kotlin language plugin has no equivalent: it emits a single `ChatApi.kt` containing every
   route in the spec. Adopting the TypeScript pattern would give us:

   - `apis/ChatApi.kt` - only chat-tagged routes (~88 methods, was 155)
   - `apis/ChannelApi.kt` - channel-scoped sub-API
   - `apis/ModerationApi.kt` - admin moderation, kept separate and likely excluded from the chat
     SDK's public surface
   - No code for video / feeds products in a chat-only generation pass

Either (1) or a curated (3) plus (4) is the right long-term answer. (2) is a stopgap if upstream
changes are slow.

## Cross-SDK state (as of 2026-06-15)

Surveyed how other Stream SDKs handle the same generator output:

| SDK | Uses OpenAPI generator? | Generated file count | Cross-product leak? | Notes |
|---|---|---|---|---|
| chat-android (this) | yes | 472 | yes | `EnrichedActivity`, `EnrichedReaction`, `Time`, 21 admin `/moderation/*` routes |
| chat-swift (`feature/open-api-llc`, last commit 2026-06-09) | yes | 471 | yes (but `EnrichedReaction` has the `Time`-typed fields stripped) | Same generator output shape; somehow avoided the Time wrapper bug, worth syncing with the iOS team on how |
| chat-js (`feat/integrate-open-api-generated-client`, last commit 2026-06-15) | yes - different shape | 7 aggregated files | partially avoided | Per-product `*Api.ts` files; admin moderation is `src/gen/moderation/ModerationApi.ts` and not pulled into the chat API surface by default |
| chat-swift `main` / chat-js `main` | no - hand-written | n/a | n/a | Existing released SDKs predate the generator |
| feeds-android, feeds-swift | yes | same leak | yes | Same generator, same leakage of cross-product types |

Conclusions:

- The cross-product leak is universal across teams that consume the Kotlin/Swift generator
  output. No SDK has fixed it upstream.
- The TypeScript generator already implements option (4) above - the per-product split. The
  Kotlin and Swift generators have not adopted the same pattern.
- The iOS team is roughly where we are. Coordinating on issues #3 and #5 with them avoids
  divergent local fixes and is the path to an upstream solution.

**Audit needed:** Before shipping, sweep `stream-chat-android-network/src/main/kotlin/.../models/`
and identify which generated classes have no chat-domain meaning. Decide per-class whether to:

- Push for an upstream spec/generator fix (best),
- Mark `internal` and never expose,
- Keep as public if the symbol genuinely needs to round-trip (e.g. moderation reviewers do see
  feeds activities in cross-product orgs).

### Product tags scope and the path to model trimming

Concrete observation from the upstream generator source
(`lib/combined/openapi/generator/types/types.go`):

- `Operation` has a `Product string` field (line 684). Endpoints are product-tagged.
- `Model` has no equivalent. Models are universal across products.
- `FilterByProduct` (line 1125) filters only operations.

This means option 4 (per-product **API** split) is straightforward to implement. **Model**
trimming - emitting only the models actually referenced by the kept operations - is a separate
problem because models have no product tag.

The generator already has a reachability traversal at `Context.ResponseModels()` (lines
1185-1212): seeds from each kept operation's `ResponseModel`, walks model fields, resolves
referenced model names via `GetModel`, expands until fixed point. The TypeScript plugin calls it
for date-decoder selection (`typescript.go:85`), but **no language plugin currently uses it to
trim the emitted model set** - every plugin still emits all of `spec.Models`.

A reachability-based model trim would need two changes:

1. Extend the seed set to also include request bodies and parameter types (currently only
   `ResponseModel`).
2. Apply it at model emission time in each language plugin: replace
   `for _, model := range spec.Models` with iteration over the reachable closure of the
   filtered operations.

Both changes live in shared (generic) generator code, not in any one language plugin. **Touching
that code should be coordinated with the iOS / TS / feeds-android maintainers** rather than
landed as a local fork.

---

## 6. Chat datetime fields generated as `OffsetDateTime` instead of `Date`

**Symptom:** Every generated DTO with a datetime field uses `org.threeten.bp.OffsetDateTime`
(159 files in the current chat output). The chat domain layer, the existing manual DTOs (via the
`ExactDate` Moshi adapter), and the entire mapping layer all use `java.util.Date`. Result: every
single mapper between a generated DTO and a domain class has to write a per-field conversion
like:

```kotlin
createdAt = Date(dto.createdAt.toInstant().toEpochMilli())
```

This is pure boilerplate, with no benefit - chat code has never preserved timezones and doesn't
need to. The cost compounds at scale: the migration will write thousands of these conversions
that will all be deleted later if/when the generator is fixed.

**Root cause:** Hard-coded in `lib/combined/openapi/generator/languages/kotlin/kotlin.go:640-651`:

```go
if strings.Contains(typeInfo.Name, "DatetimeType") {
    if androidsdk == "feeds" {
        return "java.util.Date"
    }
    return "org.threeten.bp.OffsetDateTime"
}
```

Only feeds opts into `Date`. Chat and video fall through to `OffsetDateTime`. There is no
explanation in the code or templates for why feeds was singled out - presumably the original
author of the feeds branch ran into the same conversion-boilerplate problem we are hitting now.

**Cross-SDK reality check:**

| Product | Datetime type | Mapper conversion |
|---|---|---|
| feeds | `java.util.Date` (107 files) | none |
| video | `org.threeten.bp.OffsetDateTime` (99 files) | yes |
| chat (today) | `org.threeten.bp.OffsetDateTime` (159 files) | yes |

Video pays the same tax as chat would. Feeds skips it.

**Fix status:** Locally fixed in the generator (not upstream). Inverted the default in
`kotlin.go:644-654`: video keeps `OffsetDateTime`, every other product gets `Date`. Chat
output regenerated, network module no longer depends on threetenbp, and the existing Role +
ReactionGroup mappers dropped their `Date(it.toInstant().toEpochMilli())` conversions.

**Suggested fix:** Invert the default - treat `Date` as the default, opt only video into
`OffsetDateTime`:

```go
if strings.Contains(typeInfo.Name, "DatetimeType") {
    if androidsdk == "video" {
        return "org.threeten.bp.OffsetDateTime"
    }
    return "java.util.Date"
}
```

Rationale: two of the three current products (feeds, chat) want `Date`, and any new product
added later is more likely to follow the same shape than to want timezone-preserving datetime.
Treat the majority as the default; let the outlier opt in.

After regeneration: generated `RoleDto.createdAt: Date`, mapper becomes `createdAt = createdAt`.
The network module also no longer needs `org.threeten:threetenbp` as a dependency (one less
transitive on the chat-client classpath), and the threetenbp date adapters in
`stream-chat-android-network/.../infrastructure/` become dead code.

Long-term, the right shape might be a `--datetime-type` flag on `chat-manager openapi
generate-client` so each consumer picks the type it wants - but the inverted-default per-SDK
switch is fine for now.

**Migration timing:** Worth applying this **before** we do more DTO swaps, because every mapper
we write between now and a fix will get rewritten when the fix lands. The swap done so far
(`DownstreamRoleDto -> Role`) is the first and only mapper that pays this cost; cheap to
re-simplify after a regen.

---

## 7. Generated chat model names collide with domain model names

**Symptom:** Generated wire models share their simple names with the chat domain models in
`stream-chat-android-core/src/main/java/io/getstream/chat/android/models/`. Confirmed collisions
today: `Command`, `Message`, `User`, `Channel`, `Member`, `Reaction`, `Attachment`, `Device`,
`Mute`, `Thread`, `Poll`, `Location` (12 already, more likely as the network module surface
grows).

Any mapper file needs both types in scope to convert between them, so the wire side has to be
aliased per-import:

```kotlin
import io.getstream.chat.android.network.models.Command as CommandDto
```

That alias accumulates across every mapper / parser file we touch during the migration.

**Root cause:** The Kotlin generator emits a class per OpenAPI schema name verbatim. Chat's
schema names (`Command`, `Message`, etc.) match the names long-established in the chat
domain model. There is no opt-in suffix mechanism to disambiguate.

**Fix status:** Not fixed. Worked around per-file with `as XxxDto` aliases in the migrated DTO
files so far (`UnreadDtos.kt`, etc.). The collision is permanent, not transitional - the chat
domain model uses these bare names by long-standing convention.

**Suggested fix:** Generator-side, behind a chat-only branch. Add a `classNameSuffix` value
that defaults to `""` and is set to `"Dto"` for the chat `androidsdk`. Apply at the single
point in `kotlin.go` where the Kotlin class identifier is computed from the OpenAPI schema
name; references / discriminator interfaces / service request-response types all flow from
that one source and pick up the suffix automatically.

Side effects to verify when implementing:

- Discriminator interface names (`WSEvent`, `WSClientEvent`) - whether they should also gain
  the suffix or stay as-is.
- Enum nested adapter / class names (e.g. `Command.SetAdapter`).
- The existing `Role`, `ReactionGroup`, `UnreadChannel`, `UnreadChannelByType`,
  `UnreadThread` aliased imports become redundant - clean them up at the same time.

Project-side alternative (typealias file in the client module) was considered but rejected:
the collision is permanent, so a permanent generator-side convention is the cleaner end state
than ~50+ typealiases we maintain forever.

**Migration timing:** Address before the migration volume grows. Every DTO we swap before the
fix lands will need its alias revisited.

---

## 8. Generated DTOs are incompatible with `CustomObjectDtoAdapter` extra-data routing

**Symptom:** Cannot migrate the chat root DTOs (`AttachmentDto`, `MessageDto`, `ChannelDto`,
`UserDto`, `MemberDto`, `ReactionDto`, `PollDto`, `PollOptionDto`, `ThreadDto`,
`ThreadInfoDto`, `UpstreamMemberDataDto`) to their generated counterparts. These DTOs go
through `stream-chat-android-client/.../parser2/adapters/CustomObjectDtoAdapter` which
round-trips an open-schema `extraData: Map<String, Any>` field — the wire allows arbitrary
unknown keys, the client persists them, and round-trips them on serialization. Substituting
the generated DTO breaks both directions silently.

**Root cause:** The adapter is built on Kotlin reflection over the DTO class:

```kotlin
private val memberNames: List<String> by lazy {
    kClass.members.map { member -> member.name }.minus(EXTRA_DATA)
}
```

It then walks the parsed JSON map and buckets any key not in `memberNames` into the
`extraData` sub-map. Two assumptions hard-coded into this:

1. **Kotlin property names equal JSON wire names.** Manual DTOs declare snake_case
   properties (`val message_text: String`) so the reflection-derived name matches the wire
   key. Generated DTOs declare camelCase + `@Json(name = "message_text")` -
   reflection returns `messageText`, the wire key is `message_text`, the membership check
   fails, and **every declared field gets bucketed as extra data**.
2. **The DTO has an `extraData: Map<String, Any>` property.** Manual DTOs declare one
   explicitly. Generated DTOs never have it - the OpenAPI spec doesn't currently model
   `additionalProperties` for these schemas, so the generator has nothing to emit. With no
   `extraData` field, the post-extraction map can't be deserialized.

`@StreamHandsOff` annotations on the affected manual DTOs encode exactly this hazard.

**Fix status:** Not fixed. Blocks ~10 leaf migrations and effectively gates the entire
root-DTO portion of the migration.

**Suggested fix:** Two changes that must land together.

- **Client-side**: rewrite `CustomObjectDtoAdapter.memberNames` to read `@Json(name = ...)`
  values via reflection (Kotlin property → wire name) instead of the Kotlin property names.
  After the change, both manual and generated DTOs work uniformly - members are identified
  by what they look like on the wire. Hand-validated DTOs without `@Json` annotations
  continue to work via property-name fallback.
- **Generator-side**: every chat DTO that goes through the open-schema adapter needs an
  `extraData: Map<String, Any>` field. Cleanest path is to mark the affected schemas in the
  OpenAPI spec as `additionalProperties: {}` (or a chat-specific extension tag) and have the
  Kotlin generator emit a `extraData: Map<String, Any> = emptyMap()` property whenever the
  schema is so marked. Alternative: a chat-only generator branch that adds the field to a
  hard-coded set of schema names. Spec-side is cleaner; generator-side is a faster stopgap.

Once both ship:

- `AttachmentDto`, `MessageDto`, `ChannelDto`, `UserDto`, `MemberDto`, `ReactionDto`,
  `PollDto`, `PollOptionDto`, `ThreadDto`, `ThreadInfoDto`, `UpstreamMemberDataDto` become
  mechanical alias-style migrations.
- The `@StreamHandsOff` annotations on the manual DTOs can come off (or stay as warning
  signal until the manual DTOs are fully retired).

**Migration timing:** Required before any of the root DTOs can move. All leaf migrations
that depend on these root types (mutes, channel reads, reminders, flags) are blocked until
then.

---

## 9. Spec generator emits `custom: object` for `jsonextra.ExtraFields` fields, but the wire flattens them to root

**Symptom:** Every generated Kotlin (and Swift/TS) DTO that corresponds to a backend struct
containing `Custom jsonextra.ExtraFields \`json:"custom"\`` gets a typed
`@Json(name = "custom") val custom: Map<String, Any?>` property. The wire produced by the
backend does not contain a literal `"custom"` key - `jsonextra.ExtraFields` flattens map
contents to the root of the enclosing object on encode and pulls unknown root-level keys
into the map on decode (see `kit/v3/jsonextra/JSONEXTRA_SPEC.md`, section 5). Plugging the
generated DTO into stock Moshi yields:

- `custom` always parses as `emptyMap()` because the wire has no `"custom"` key.
- Real custom keys at the root (e.g. `birthland`, `team_name`, `jcTest`) get silently
  dropped because Moshi defaults to ignoring unknown JSON fields.

**Root cause:** `lib/combined/openapi/spec/spec.go` walks struct fields generically and
reflects through them. There is no recognition of `jsonextra.ExtraFields` - the type is
`map[string]interface{}` under the hood, and the `json:"custom"` tag is read literally. The
spec emits `custom: type: object` as a normal nested property. The `jsonextra` runtime
flatten-on-encode / sweep-on-decode semantics live entirely outside the spec generator's
field walker.

This affects every chat schema that has an `ExtraFields` field - chat-android observed on
`UserResponseCommonFields`, and the same pattern lives on Channel, Message, Attachment,
Member, Reaction, Thread, Poll, Flag, etc. across the chat backend's payload types.

**Fix status:** Not fixed at the spec/generator level. Worked around client-side in chat-
android by extending `CustomObjectDtoAdapter` to bucket unknown root keys into the generated
`custom` property (see issue #8).

**Suggested fix:** Either:

1. **Spec generator recognizes `jsonextra.ExtraFields`**. In `spec.go`'s struct field walker,
   short-circuit fields whose Go type is `jsonextra.ExtraFields`: instead of emitting a
   named property, set `additionalProperties: { type: object }` on the parent schema. Each
   language generator then emits whatever overflow-field convention it uses (Kotlin: an
   `extraData`-style map field that the existing `CustomObjectDtoAdapter` populates; Swift /
   TS: similar conventions).
2. **OpenAPI extension marker**. Add `x-stream-extra-fields: true` on the property in the
   spec when the Go field is `jsonextra.ExtraFields`. The Kotlin generator emits the
   property as today plus a marker (or interface implementation) that the SDK's parser
   recognizes and wraps with the open-schema adapter. Less precise than option 1 but
   smaller spec-shape impact.

Option 1 is correct. Option 2 is a faster stopgap.

The collateral benefit: once spec + generator + SDK agree, every DTO that uses
`jsonextra.ExtraFields` becomes a clean leaf migration (combined with the fix for issue #8).

**Migration timing:** Combine with issue #8 - both must land together for root-DTO swaps
(`User`, `Channel`, `Message`, `Attachment`, etc.) to work end-to-end with the new wire
generator output.

---

## 10. Spec generator maps `float64` to OpenAPI `format: float`, downgrading precision

**Symptom:** Every `float64` field on the chat backend appears as `format: float` in the
OpenAPI spec (`chat.yaml`). The Kotlin generator turns `format: float` into `kotlin.Float`
(32-bit). Concrete impact in `stream-chat-android`: `SharedLocation.latitude` and
`SharedLocation.longitude` come out as `kotlin.Float`, and round-tripping a domain
`Location(latitude = 37.7749, longitude = -122.4194)` loses precision
(`37.774898529052734`, `-122.41940307617188`).

**Root cause:** `lib/combined/openapi/spec/spec.go:836` in `SchemaFormatFromReflectType`:

```go
case "float32", "float64", "complex64", "complex128":
    return "float"
```

Both `float32` (single precision) and `float64` (double precision) map to the same
OpenAPI format string. The backend Go uses `float64` (see e.g.
`lib/chat/controller/v1/payload/shared_location.go:15`), but the spec advertises 32-bit.

The Kotlin generator at `lib/combined/openapi/generator/languages/kotlin/kotlin.go:866`
correctly maps `format: float` -> `kotlin.Float`. The bug is upstream in the spec.

**Fix status:** Not fixed. Worked around in `stream-chat-android-client` by widening
`Float` to `Double` at the mapper boundary (`DtoMapping.Location.toDto()` calls
`.toFloat()`, `DomainMapping.DownstreamLocationDto.toDomain()` calls `.toDouble()`).
Domain models keep `Double`; only the wire DTO uses Float.

**Suggested fix:** Split the case in `spec.go`:

```go
case "float32", "complex64":
    return "float"
case "float64", "complex128":
    return "double"
```

And in the Kotlin generator, default `type: number` (no explicit format) to
`kotlin.Double` rather than `kotlin.Float` (currently both map to Float at
`kotlin.go:868-869`). `format: double` already maps to `kotlin.Double` correctly.

**Breaking-change caveat:** The spec fix changes the contract for every SDK consuming the
spec (chat-swift, chat-js, video, feeds, ...). Existing built artifacts with `Float`
fields would not be source/binary compatible with the new `Double` ones. Land only as a
coordinated multi-SDK release. Until then, the per-mapper `.toFloat()`/`.toDouble()`
workaround on the chat-android side keeps things working.

---

## 11. Spec emits `Custom` (capital C) `@Json` name for poll request DTOs

**Symptom:** Generated `CreatePollRequest`, `UpdatePollRequest`, `CreatePollOptionRequest`,
`UpdatePollOptionRequest` declare:

```kotlin
@Json(name = "Custom")
val custom: kotlin.collections.Map<kotlin.String, Any?>? = emptyMap()
```

**Root cause:** The chat backend's Go poll-request structs declare the extra-fields property
as `Custom jsonextra.ExtraFields` with no `json:"custom"` tag, unlike most other DTOs. The
spec generator falls back to using the Go field name (`Custom`) verbatim. The Kotlin
generator faithfully echoes that into `@Json(name = "Custom")`. Peer DTOs that have explicit
`json:"custom"` tags emit the correct lowercase name (see `PollOptionResponseData`,
`PollOptionInput`).

**Why this is cosmetic, not a wire-format bug:** The wire never contains `"Custom"` or
`"custom"` literally for these payloads. The server's `jsonextra.ExtraFields` flattens map
contents to root on encode and sweeps unknown root keys back into the map on decode (issue
#9). The client's `CustomObjectDtoAdapter` does the same: on encode, it serializes the DTO
through Moshi's default adapter (producing a Map keyed by the `@Json` name), then **looks up
that key, removes it from the Map, and merges the contents at root level** before writing the
wire. On decode it reverses the process. The literal annotation name only appears in the
intermediate Map between Moshi and the adapter - the wire shape is "flat root keys" on both
sides.

The only requirement is that the adapter be registered with
`extraDataPropertyName = "Custom"` (matching the `@Json` name) when wrapping these specific
DTOs. As long as that's consistent, the typo is invisible on the wire.

**Fix status:** Not fixed. Cosmetic; does not block migration of these DTOs as long as the
adapter wiring matches the annotation. Still worth fixing for spec consistency.

**Suggested fix:** Spec-side - add `json:"custom"` tags to the affected Go struct fields, or
recognize the convention in the spec generator's struct walker.

**Migration timing:** Not blocking. The real blocker for `CreatePollRequest` /
`UpdatePollRequest` migration is issue #8 (generator-side emission of the `custom` /
`extraData` field at all), not this naming quirk.

---

## Pattern observations

- Several issues collapse to the same root: the generator faithfully echoes whatever the spec
  produces and never validates that the output is legal Kotlin. Spec-side fixes (one-time, per
  artifact) cost less than generator-side fixes (one-time, but every spec quirk re-introduces the
  problem).
- The Kotlin generator has a per-`androidSdk` branching pattern (`feeds` / `video` / chat falling
  through) but new branches are easy to miss when changing logic - issues #1 surfaced because the
  child-method-name code at one site didn't replicate the per-SDK branching present at the parent
  site.
- Per-`androidSdk` branches in `spec.go` (`isVideoOnly`, now `isChatOnly`) are the right home for
  product-shape decisions and should be preferred over generator-side product detection.
- **Android dex identifier rules are stricter than JVM Kotlin.** Backticking lets the Kotlin
  compiler accept arbitrary characters in names, but D8/R8 rejects anything outside
  `[A-Za-z0-9_$]`. Issues #2 and #4 both rely on this: backtick only when the raw name is
  already a dex-safe identifier; otherwise rename to a sanitized identifier and preserve the
  original wire name via `@Json(name = ...)`.
