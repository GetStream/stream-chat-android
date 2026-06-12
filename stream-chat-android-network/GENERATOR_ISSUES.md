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

**Fix status:** Not fixed. Likely affects any other reserved word that appears as a field name in
the future (`class`, `fun`, `interface`, `package`, `val`, `var`, `is`, `in`, `out`, `when`,
`object`, `typeof`, ...).

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

**Fix status:** Not fixed.

**Suggested fix:** Two angles:

- Spec-side: drop the empty-string value from the enum if it isn't a real allowed wire value.
- Generator-side: when an enum value is the empty string (or otherwise isn't a valid Kotlin
  identifier), generate a sanitized object name (e.g. `Empty`) and a properly-quoted string
  literal (`""`) for the value. Sanitization should also handle non-alphanumeric leading
  characters and Kotlin keywords (related to issue #2).

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

**Fix status:** Not fixed. Currently working around it by accepting the cross-product types in
the generated module.

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

Either (1) or a curated (3) is the right long-term answer. (2) is a stopgap if upstream changes
are slow.

**Audit needed:** Before shipping, sweep `stream-chat-android-network/src/main/kotlin/.../models/`
and identify which generated classes have no chat-domain meaning. Decide per-class whether to:

- Push for an upstream spec/generator fix (best),
- Mark `internal` and never expose,
- Keep as public if the symbol genuinely needs to round-trip (e.g. moderation reviewers do see
  feeds activities in cross-product orgs).

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

**Fix status:** Not fixed. Currently writing per-field conversions in mappers.

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
