# Repository Guidelines

Guidance for AI coding agents (Copilot, Cursor, Aider, Claude, etc.) working in Stream’s Android Chat repo. Humans are welcome too; tone is optimised for tools.

### Repository purpose
This project delivers **Stream Chat Android**, a modular SDK spanning low-level client APIs, offline persistence, and both Compose and XML UI kits. Treat it as customer-facing product code: changes ripple to thousands of apps, so prioritise stability, binary compatibility, and polished UX.

### Tech & toolchain
- Language: Kotlin (JVM target 11 across modules) with limited Java interop
- Android min/target: 21 / 34 (verify in `gradle/libs.versions.toml` before raising)
- UI stacks: Jetpack Compose, classic Views, baseline profiles for performance
- Networking & storage: OkHttp, Retrofit + Moshi, Room, Coil, WorkManager
- Build: Gradle Kotlin DSL + `buildSrc/` conventions, version catalogs in `gradle/`
- Static analysis: Spotless (ktlint, XML headers), Detekt, API validator, Jacoco + Sonar
- Testing: JUnit4/5, Mockito-Kotlin, Turbine, Robolectric, Espresso, Paparazzi, Shot

## Project structure
- `stream-chat-android-client/` – core API client, REST/WebSocket, plugin hooks, state container
- `stream-chat-android-offline/` – persistence, sync, caching, retry workers
- `stream-chat-android-ui-common/` – theming, assets, shared UI helpers
- `stream-chat-android-compose/` & `stream-chat-android-ui-components/` – Compose and XML UI kits
- `*-sample/`, `stream-chat-android-ui-uitests/`, `stream-chat-android-test/` – samples, integration, and shared test harnesses
- `buildSrc/`, `config/`, `scripts/`, `fastlane/`, `metrics/` – build logic, lint configs, automation, release metrics, CI helpers

> Modules are published; avoid leaking internal types across boundaries without coordinating version policy and changelog updates.

## Build, test, and validation
- Format/licence: `./gradlew spotlessApply`
- Static analysis: `./gradlew detekt` or module-scoped `:module:detekt`
- Unit tests: `./gradlew testDebugUnitTest` (or `:module:test` for non-Android modules)
- UI snapshots: `./gradlew verifyPaparazziDebug` (Compose) / `./gradlew shotVerify` (Views)
- Instrumented suites: `./gradlew connectedAndroidTest` or targeted `:stream-chat-android-ui-uitests:connectedCheck`
- Full gate: `./gradlew check`

Prefer module-scoped tasks while iterating; PRs should pass `spotlessCheck`, `detekt`, and relevant unit/UI suites before review.

## Coding principles
- **API stability**: Public APIs are validated; favour additive changes and mark deprecations with clear migration paths (`DEPRECATIONS.md`).
- **Offline-first**: Respect sync contracts in offline/state modules—guard race conditions, idempotency, and background workers.
- **UI parity**: Keep Compose and XML kits behaviourally aligned; update shared fixtures/tests when touching one side.
- **Performance**: Maintain lazy flows, paging, and baseline profiles; avoid extra recompositions or heavy main-thread work.
- **Customisability**: Expose configuration via theming, factories, or style slots rather than hard-coded branching.
- **Logging & analytics**: Use provided logging abstractions; never leak user PII or tokens.

## Style & conventions
- Spotless-enforced Kotlin style (4 spaces, no wildcard imports, licence headers). Run `./gradlew spotlessCheck` in CI parity.
- Compose components follow noun-based naming (`MessageList`, `ChannelListHeader`).
- Use `@OptIn` annotations explicitly; avoid suppressions unless documented.
- Backtick test names (for example: ``fun `message list filters muted channels`()``) for readability; keep helper extensions private/internal.
- Document public APIs with KDoc, including thread expectations and state notes.

## Testing guidance
- Place JVM tests in `src/test/java|kotlin`, Android/UI tests in `src/androidTest` or dedicated UI test modules.
- Compose UI regressions: add Paparazzi snapshots and run `verifyPaparazziDebug`; record Shot baselines when behaviour changes in XML kit.
- Exercise chat flows with provided fakes (`stream-chat-android-test`) and ensure offline ↔ client boundaries are covered.
- For concurrency-sensitive logic (uploads, sync, message state), add deterministic tests using `runTest` + virtual time.
- Update baseline profiles (`baseline-prof.txt`) when start-up or scroll performance changes significantly.

## Documentation & comments
- Update module README, `docs/`, or API docs when altering setup, themes, or sample flows.
- Log deprecations or behavioural shifts in `CHANGELOG.md` and `DEPRECATIONS.md`.
- Keep inline comments focused on intent (why), not mechanics; prefer KDoc for public APIs.

## Security & configuration
- Secrets live in `local.properties`/env vars—never commit keys, tokens, or customer data.
- Verify `config/quality/` templates before adding new env flags; integrate with Sonar only after approval.
- Sanitise logs and analytics payloads; follow `SECURITY.md` for vulnerability handling.

## PR & release hygiene
- Target `develop`; main mirrors released artifacts. Sync with release owners before touching versioning, publishing, or changelog scripts.
- Keep commits imperative (`compose: Prevent duplicate typing indicators`) and scoped.
- Include test evidence in PR descriptions: Gradle task output, screenshots/screencasts for UI changes.
- Run `assemble`, relevant tests, and lint tasks locally before pushing. Update documentation alongside feature toggles.

### Quick checklist for agents
- [ ] Confirm the module(s) touched and run their focused Gradle tasks.
- [ ] Maintain binary/API compatibility; document any intentional breakage.
- [ ] Honour offline/state invariants—cover edge cases like retries, reconnections, and message dedupe.
- [ ] Keep Compose/XML parity when modifying shared UI behaviour.
- [ ] Run Spotless and Detekt before finishing.
- [ ] Add/refresh unit, UI, or snapshot tests for new behaviour.
- [ ] Update changelog/deprecation docs for user-visible changes.
- [ ] Scrub logs/configs for secrets before committing.
